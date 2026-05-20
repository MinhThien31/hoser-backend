package com.minhthien.hoser_backend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhthien.hoser_backend.dto.request.CreateDepositOrderRequest;
import com.minhthien.hoser_backend.dto.request.DepositCallbackRequest;
import com.minhthien.hoser_backend.dto.response.PaymentCallbackLogResponse;
import com.minhthien.hoser_backend.dto.response.PaymentOrderResponse;
import com.minhthien.hoser_backend.entity.PaymentOrder;
import com.minhthien.hoser_backend.entity.User;
import com.minhthien.hoser_backend.enums.PaymentOrderStatus;
import com.minhthien.hoser_backend.enums.PaymentProvider;
import com.minhthien.hoser_backend.enums.WalletTransactionType;
import com.minhthien.hoser_backend.exception.BadRequestException;
import com.minhthien.hoser_backend.exception.ResourceNotFoundException;
import com.minhthien.hoser_backend.repository.PaymentOrderRepository;
import com.minhthien.hoser_backend.repository.UserRepository;
import com.minhthien.hoser_backend.service.PayOsGateway;
import com.minhthien.hoser_backend.service.PaymentCallbackLogService;
import com.minhthien.hoser_backend.service.PaymentService;
import com.minhthien.hoser_backend.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.exception.PayOSException;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final String REFERENCE_TYPE = "DEPOSIT_ORDER";

    private final PaymentOrderRepository paymentOrderRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;
    private final PaymentCallbackLogService paymentCallbackLogService;
    private final PayOsGateway payOsGateway;
    private final ObjectMapper objectMapper;

    @Value("${app.payment.callback-token:dev-callback-token}")
    private String callbackToken;

    @Value("${payos.return-url}")
    private String payOsReturnUrl;

    @Value("${payos.cancel-url}")
    private String payOsCancelUrl;

    @Override
    @Transactional
    public PaymentOrderResponse createDepositOrder(Long userId, CreateDepositOrderRequest request) {
        validateAmount(request.getAmount());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        String referenceCode = "DEP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();

        PaymentOrder order = PaymentOrder.builder()
                .user(user)
                .amount(request.getAmount())
                .currency(PaymentOrder.DEFAULT_CURRENCY)
                .provider(PaymentProvider.PAYOS)
                .status(PaymentOrderStatus.PENDING)
                .referenceCode(referenceCode)
                .transferContent("HORSE " + referenceCode)
                .expiredAt(LocalDateTime.now().plusMinutes(30))
                .createdBy(user.getUsername())
                .updatedBy(user.getUsername())
                .build();
        PaymentOrder savedOrder = paymentOrderRepository.save(order);

        Long orderCode = savedOrder.getId();
        String description = buildPayOsDescription(orderCode);
        CreatePaymentLinkResponse payOsResponse = createPayOsPaymentLink(savedOrder, orderCode, description);

        savedOrder.setOrderCode(orderCode);
        savedOrder.setPaymentLinkId(payOsResponse.getPaymentLinkId());
        savedOrder.setCheckoutUrl(payOsResponse.getCheckoutUrl());
        savedOrder.setQrCode(payOsResponse.getQrCode());
        savedOrder.setTransferContent(description);
        savedOrder.setMetadata(toMetadata(payOsResponse));
        if (payOsResponse.getExpiredAt() != null) {
            savedOrder.setExpiredAt(LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochSecond(payOsResponse.getExpiredAt()), ZoneId.systemDefault()));
        }
        return mapToResponse(paymentOrderRepository.save(savedOrder));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentOrderResponse> getUserDepositOrders(Long userId) {
        return paymentOrderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentOrderResponse> getAdminPaymentOrders() {
        return paymentOrderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<PaymentCallbackLogResponse> getAdminPaymentCallbackLogs() {
        return paymentCallbackLogService.getAdminPaymentCallbackLogs();
    }

    @Override
    @Transactional
    public PaymentOrderResponse handleDepositCallback(DepositCallbackRequest request) {
        if (!isValidCallbackToken(request.getCallbackToken())) {
            paymentCallbackLogService.record(request, false, false, "Invalid payment callback token");
            throw new BadRequestException("Invalid payment callback token");
        }

        try {
            PaymentOrderResponse response = processDepositCallback(request);
            paymentCallbackLogService.record(request, true, true, null);
            return response;
        } catch (RuntimeException ex) {
            paymentCallbackLogService.record(request, true, false, ex.getMessage());
            throw ex;
        }
    }

    @Override
    @Transactional
    public PaymentOrderResponse handlePayOsWebhook(Webhook webhook) {
        DepositCallbackRequest logRequest = toCallbackRequest(webhook, PaymentOrderStatus.FAILED, null);
        try {
            WebhookData data = payOsGateway.verifyWebhook(webhook);
            PaymentOrderStatus status = isPayOsPaid(data) ? PaymentOrderStatus.PAID : PaymentOrderStatus.FAILED;
            logRequest = toCallbackRequest(data, status);
            PaymentOrderResponse response = processPayOsWebhook(data, status);
            paymentCallbackLogService.record(logRequest, true, true, null);
            return response;
        } catch (PayOSException | IllegalArgumentException ex) {
            paymentCallbackLogService.record(logRequest, false, false, ex.getMessage());
            throw new BadRequestException("Invalid payOS webhook");
        } catch (RuntimeException ex) {
            paymentCallbackLogService.record(logRequest, true, false, ex.getMessage());
            throw ex;
        }
    }

    private PaymentOrderResponse processDepositCallback(DepositCallbackRequest request) {
        PaymentOrder order = paymentOrderRepository.findByReferenceCode(request.getReferenceCode())
                .orElseThrow(() -> new ResourceNotFoundException("PaymentOrder", "referenceCode", request.getReferenceCode()));

        if (order.getStatus() == PaymentOrderStatus.PAID) {
            return mapToResponse(order);
        }
        if (order.getStatus() == PaymentOrderStatus.CANCELLED || order.getStatus() == PaymentOrderStatus.EXPIRED) {
            throw new BadRequestException("Payment order cannot be paid from status " + order.getStatus());
        }
        if (request.getStatus() == PaymentOrderStatus.FAILED || request.getStatus() == PaymentOrderStatus.CANCELLED) {
            order.setStatus(request.getStatus());
            order.setProviderTransactionId(request.getProviderTransactionId());
            order.setMetadata(request.getMetadata());
            return mapToResponse(paymentOrderRepository.save(order));
        }
        if (request.getStatus() != PaymentOrderStatus.PAID) {
            throw new BadRequestException("Unsupported callback status: " + request.getStatus());
        }

        String referenceId = order.getReferenceCode();
        walletService.credit(order.getUser().getId(), order.getAmount(), WalletTransactionType.DEPOSIT,
                REFERENCE_TYPE, referenceId, "deposit:user:" + referenceId, request.getMetadata(), "Deposit paid");
        walletService.creditAdmin(order.getAmount(), WalletTransactionType.DEPOSIT,
                REFERENCE_TYPE, referenceId, "deposit:admin:" + referenceId, request.getMetadata(), "Deposit paid");

        order.setStatus(PaymentOrderStatus.PAID);
        order.setProviderTransactionId(request.getProviderTransactionId());
        order.setMetadata(request.getMetadata());
        order.setPaidAt(LocalDateTime.now());
        return mapToResponse(paymentOrderRepository.save(order));
    }

    private PaymentOrderResponse processPayOsWebhook(WebhookData data, PaymentOrderStatus status) {
        PaymentOrder order = findPayOsOrder(data);

        if (order.getStatus() == PaymentOrderStatus.PAID) {
            return mapToResponse(order);
        }
        if (order.getStatus() == PaymentOrderStatus.CANCELLED || order.getStatus() == PaymentOrderStatus.EXPIRED) {
            throw new BadRequestException("Payment order cannot be paid from status " + order.getStatus());
        }

        String metadata = toMetadata(data);
        if (status != PaymentOrderStatus.PAID) {
            order.setStatus(status);
            order.setProviderTransactionId(resolvePayOsTransactionId(data));
            order.setMetadata(metadata);
            return mapToResponse(paymentOrderRepository.save(order));
        }

        validatePayOsPaidData(order, data);
        String referenceId = order.getReferenceCode();
        walletService.credit(order.getUser().getId(), order.getAmount(), WalletTransactionType.DEPOSIT,
                REFERENCE_TYPE, referenceId, "deposit:user:" + referenceId, metadata, "payOS deposit paid");
        walletService.creditAdmin(order.getAmount(), WalletTransactionType.DEPOSIT,
                REFERENCE_TYPE, referenceId, "deposit:admin:" + referenceId, metadata, "payOS deposit paid");

        order.setStatus(PaymentOrderStatus.PAID);
        order.setProviderTransactionId(resolvePayOsTransactionId(data));
        order.setMetadata(metadata);
        order.setPaidAt(LocalDateTime.now());
        return mapToResponse(paymentOrderRepository.save(order));
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }
        try {
            amount.longValueExact();
        } catch (ArithmeticException ex) {
            throw new BadRequestException("Amount must be a whole VND amount");
        }
    }

    private boolean isValidCallbackToken(String token) {
        return callbackToken != null && !callbackToken.isBlank() && callbackToken.equals(token);
    }

    private CreatePaymentLinkResponse createPayOsPaymentLink(PaymentOrder order, Long orderCode, String description) {
        CreatePaymentLinkRequest paymentLinkRequest = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(order.getAmount().longValueExact())
                .description(description)
                .returnUrl(payOsReturnUrl)
                .cancelUrl(payOsCancelUrl)
                .expiredAt(toEpochSecond(order.getExpiredAt()))
                .build();
        try {
            return payOsGateway.createPaymentLink(paymentLinkRequest);
        } catch (PayOSException ex) {
            throw new BadRequestException("Could not create payOS payment link: " + ex.getMessage());
        }
    }

    private PaymentOrder findPayOsOrder(WebhookData data) {
        if (data.getOrderCode() != null) {
            return paymentOrderRepository.findByOrderCode(data.getOrderCode())
                    .orElseThrow(() -> new ResourceNotFoundException("PaymentOrder", "orderCode", data.getOrderCode()));
        }
        if (data.getPaymentLinkId() != null && !data.getPaymentLinkId().isBlank()) {
            return paymentOrderRepository.findByPaymentLinkId(data.getPaymentLinkId())
                    .orElseThrow(() -> new ResourceNotFoundException("PaymentOrder", "paymentLinkId", data.getPaymentLinkId()));
        }
        throw new BadRequestException("payOS webhook missing order code and payment link id");
    }

    private void validatePayOsPaidData(PaymentOrder order, WebhookData data) {
        if (data.getAmount() == null || order.getAmount().compareTo(BigDecimal.valueOf(data.getAmount())) != 0) {
            throw new BadRequestException("payOS webhook amount does not match payment order");
        }
        if (data.getPaymentLinkId() != null && order.getPaymentLinkId() != null
                && !order.getPaymentLinkId().equals(data.getPaymentLinkId())) {
            throw new BadRequestException("payOS webhook payment link id does not match payment order");
        }
    }

    private boolean isPayOsPaid(WebhookData data) {
        return "00".equals(data.getCode());
    }

    private DepositCallbackRequest toCallbackRequest(Webhook webhook, PaymentOrderStatus status, String metadata) {
        DepositCallbackRequest request = new DepositCallbackRequest();
        WebhookData data = webhook == null ? null : webhook.getData();
        request.setReferenceCode(resolveLogReference(data));
        request.setStatus(status);
        request.setCallbackToken("PAYOS_SIGNATURE");
        request.setProviderTransactionId(data == null ? null : resolvePayOsTransactionId(data));
        request.setMetadata(metadata == null ? toMetadata(webhook) : metadata);
        return request;
    }

    private DepositCallbackRequest toCallbackRequest(WebhookData data, PaymentOrderStatus status) {
        DepositCallbackRequest request = new DepositCallbackRequest();
        request.setReferenceCode(resolveLogReference(data));
        request.setStatus(status);
        request.setCallbackToken("PAYOS_SIGNATURE");
        request.setProviderTransactionId(resolvePayOsTransactionId(data));
        request.setMetadata(toMetadata(data));
        return request;
    }

    private String resolveLogReference(WebhookData data) {
        if (data == null) {
            return "PAYOS_UNKNOWN";
        }
        if (data.getOrderCode() != null) {
            return String.valueOf(data.getOrderCode());
        }
        if (data.getPaymentLinkId() != null && !data.getPaymentLinkId().isBlank()) {
            return data.getPaymentLinkId();
        }
        return "PAYOS_UNKNOWN";
    }

    private String resolvePayOsTransactionId(WebhookData data) {
        if (data.getReference() != null && !data.getReference().isBlank()) {
            return data.getReference();
        }
        return data.getPaymentLinkId();
    }

    private Long toEpochSecond(LocalDateTime value) {
        return value == null ? null : value.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    private String buildPayOsDescription(Long orderCode) {
        String suffix = String.valueOf(orderCode);
        if (suffix.length() > 7) {
            suffix = suffix.substring(suffix.length() - 7);
        }
        return "HS" + suffix;
    }

    private String toMetadata(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return value.toString();
        }
    }

    private PaymentOrderResponse mapToResponse(PaymentOrder order) {
        return PaymentOrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .amount(order.getAmount())
                .currency(order.getCurrency())
                .provider(order.getProvider())
                .status(order.getStatus())
                .referenceCode(order.getReferenceCode())
                .providerTransactionId(order.getProviderTransactionId())
                .orderCode(order.getOrderCode())
                .paymentLinkId(order.getPaymentLinkId())
                .checkoutUrl(order.getCheckoutUrl())
                .qrCode(order.getQrCode())
                .transferContent(order.getTransferContent())
                .paidAt(order.getPaidAt())
                .expiredAt(order.getExpiredAt())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
