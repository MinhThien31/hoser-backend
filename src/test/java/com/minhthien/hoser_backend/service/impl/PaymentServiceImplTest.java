package com.minhthien.hoser_backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhthien.hoser_backend.dto.request.CreateDepositOrderRequest;
import com.minhthien.hoser_backend.dto.response.PaymentOrderResponse;
import com.minhthien.hoser_backend.entity.PaymentOrder;
import com.minhthien.hoser_backend.entity.User;
import com.minhthien.hoser_backend.enums.PaymentOrderStatus;
import com.minhthien.hoser_backend.enums.PaymentProvider;
import com.minhthien.hoser_backend.enums.WalletTransactionType;
import com.minhthien.hoser_backend.exception.BadRequestException;
import com.minhthien.hoser_backend.repository.PaymentOrderRepository;
import com.minhthien.hoser_backend.repository.UserRepository;
import com.minhthien.hoser_backend.service.PayOsGateway;
import com.minhthien.hoser_backend.service.PaymentCallbackLogService;
import com.minhthien.hoser_backend.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import vn.payos.exception.WebhookException;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkStatus;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentOrderRepository paymentOrderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletService walletService;

    @Mock
    private PaymentCallbackLogService paymentCallbackLogService;

    @Mock
    private PayOsGateway payOsGateway;

    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(
                paymentOrderRepository,
                userRepository,
                walletService,
                paymentCallbackLogService,
                payOsGateway,
                new ObjectMapper()
        );
        ReflectionTestUtils.setField(paymentService, "callbackToken", "test-callback-token");
        ReflectionTestUtils.setField(paymentService, "payOsReturnUrl", "https://app.example/success");
        ReflectionTestUtils.setField(paymentService, "payOsCancelUrl", "https://app.example/cancel");
    }

    @Test
    void createPayOsDepositOrderReturnsCheckoutFields() {
        User user = user();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(paymentOrderRepository.save(any(PaymentOrder.class))).thenAnswer(invocation -> {
            PaymentOrder order = invocation.getArgument(0);
            if (order.getId() == null) {
                order.setId(10L);
            }
            return order;
        });
        when(payOsGateway.createPaymentLink(any(CreatePaymentLinkRequest.class))).thenReturn(
                CreatePaymentLinkResponse.builder()
                        .bin("970422")
                        .accountNumber("123456789")
                        .accountName("HOSER")
                        .orderCode(10L)
                        .amount(10000L)
                        .description("HS10")
                        .currency("VND")
                        .paymentLinkId("payos-link-10")
                        .status(PaymentLinkStatus.PENDING)
                        .checkoutUrl("https://pay.payos.vn/web/payos-link-10")
                        .qrCode("000201010212")
                        .expiredAt(1893456000L)
                        .build());

        PaymentOrderResponse response = paymentService.createDepositOrder(1L, depositRequest("10000"));

        assertThat(response.getProvider()).isEqualTo(PaymentProvider.PAYOS);
        assertThat(response.getOrderCode()).isEqualTo(10L);
        assertThat(response.getPaymentLinkId()).isEqualTo("payos-link-10");
        assertThat(response.getCheckoutUrl()).isEqualTo("https://pay.payos.vn/web/payos-link-10");
        assertThat(response.getQrCode()).isEqualTo("000201010212");
        assertThat(response.getTransferContent()).isEqualTo("HS10");

        ArgumentCaptor<CreatePaymentLinkRequest> captor = ArgumentCaptor.forClass(CreatePaymentLinkRequest.class);
        verify(payOsGateway).createPaymentLink(captor.capture());
        assertThat(captor.getValue().getOrderCode()).isEqualTo(10L);
        assertThat(captor.getValue().getAmount()).isEqualTo(10000L);
        assertThat(captor.getValue().getDescription()).isEqualTo("HS10");
    }

    @Test
    void payOsWebhookPaidCreditsWalletsAndDuplicateDoesNotDoubleCredit() {
        User user = user();
        PaymentOrder order = payOsOrder(user);
        WebhookData data = paidWebhookData();
        Webhook webhook = payOsWebhook(data);

        when(payOsGateway.verifyWebhook(webhook)).thenReturn(data);
        when(paymentOrderRepository.findByOrderCode(10L)).thenReturn(Optional.of(order));
        when(paymentOrderRepository.save(any(PaymentOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentOrderResponse paid = paymentService.handlePayOsWebhook(webhook);
        PaymentOrderResponse duplicate = paymentService.handlePayOsWebhook(webhook);

        assertThat(paid.getStatus()).isEqualTo(PaymentOrderStatus.PAID);
        assertThat(duplicate.getStatus()).isEqualTo(PaymentOrderStatus.PAID);
        verify(walletService, times(1)).credit(eq(1L), eq(new BigDecimal("10000")),
                eq(WalletTransactionType.DEPOSIT), eq("DEPOSIT_ORDER"), eq(order.getReferenceCode()),
                eq("deposit:user:" + order.getReferenceCode()), anyString(), eq("payOS deposit paid"));
        verify(walletService, times(1)).creditAdmin(eq(new BigDecimal("10000")),
                eq(WalletTransactionType.DEPOSIT), eq("DEPOSIT_ORDER"), eq(order.getReferenceCode()),
                eq("deposit:admin:" + order.getReferenceCode()), anyString(), eq("payOS deposit paid"));
    }

    @Test
    void invalidPayOsWebhookIsRejectedAndDoesNotCreditWallets() {
        Webhook webhook = payOsWebhook(paidWebhookData());
        when(payOsGateway.verifyWebhook(webhook)).thenThrow(new WebhookException("Invalid signature"));

        assertThatThrownBy(() -> paymentService.handlePayOsWebhook(webhook))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid payOS webhook");

        verify(walletService, never()).credit(anyLong(), any(), any(), any(), any(), any(), any(), any());
        verify(walletService, never()).creditAdmin(any(), any(), any(), any(), any(), any(), any());
        verify(paymentCallbackLogService).record(any(), eq(false), eq(false), eq("Invalid signature"));
    }

    @Test
    void createDepositOrderRejectsFractionalVndBeforeCallingPayOs() {
        CreateDepositOrderRequest request = depositRequest("10000.50");

        assertThatThrownBy(() -> paymentService.createDepositOrder(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Amount must be a whole VND amount");

        verifyNoInteractions(userRepository, paymentOrderRepository, payOsGateway);
    }

    private CreateDepositOrderRequest depositRequest(String amount) {
        CreateDepositOrderRequest request = new CreateDepositOrderRequest();
        request.setAmount(new BigDecimal(amount));
        return request;
    }

    private User user() {
        return User.builder()
                .id(1L)
                .username("payos-user")
                .email("payos-user@example.com")
                .build();
    }

    private PaymentOrder payOsOrder(User user) {
        return PaymentOrder.builder()
                .id(10L)
                .user(user)
                .amount(new BigDecimal("10000"))
                .currency(PaymentOrder.DEFAULT_CURRENCY)
                .provider(PaymentProvider.PAYOS)
                .status(PaymentOrderStatus.PENDING)
                .referenceCode("DEP-TESTPAYOS")
                .orderCode(10L)
                .paymentLinkId("payos-link-10")
                .build();
    }

    private WebhookData paidWebhookData() {
        return WebhookData.builder()
                .orderCode(10L)
                .amount(10000L)
                .description("HS10")
                .accountNumber("123456789")
                .paymentLinkId("payos-link-10")
                .reference("FT123456")
                .transactionDateTime("2026-05-18 14:00:00")
                .currency("VND")
                .code("00")
                .desc("Thanh cong")
                .counterAccountBankId("")
                .counterAccountBankName("")
                .counterAccountName("")
                .counterAccountNumber("")
                .virtualAccountName("")
                .virtualAccountNumber("")
                .build();
    }

    private Webhook payOsWebhook(WebhookData data) {
        return Webhook.builder()
                .code("00")
                .desc("success")
                .success(true)
                .data(data)
                .signature("valid-signature")
                .build();
    }
}
