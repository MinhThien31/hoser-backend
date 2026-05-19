package com.minhthien.hoser_backend.service.impl;

import com.minhthien.hoser_backend.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;

@Service
public class MailServiceImpl implements MailService {
    private static final String SUBJECT = "Mã OTP đặt lại mật khẩu HOSER";
    private static final int OTP_EXPIRES_IN_MINUTES = 10;

    private final JavaMailSender mailSender;

    public MailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendOtp(String email, String otp) {
        MimeMessage message = mailSender.createMimeMessage();
        String formattedOtp = formatOtp(otp);

        try {
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED,
                    StandardCharsets.UTF_8.name()
            );
            helper.setTo(email);
            helper.setSubject(SUBJECT);
            helper.setText(buildPlainText(formattedOtp), buildHtml(formattedOtp));
        } catch (MessagingException ex) {
            throw new MailPreparationException("Could not prepare OTP email", ex);
        }

        mailSender.send(message);
    }

    private String formatOtp(String otp) {
        return otp == null ? "" : otp.replaceAll("(.)(?=.)", "$1 ");
    }

    private String buildPlainText(String formattedOtp) {
        return """
                HOSER

                Mã OTP đặt lại mật khẩu của bạn là: %s

                Mã này sẽ hết hạn sau %d phút. Vui lòng không chia sẻ mã này với bất kỳ ai.

                Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này.
                """.formatted(formattedOtp, OTP_EXPIRES_IN_MINUTES);
    }

    private String buildHtml(String formattedOtp) {
        String safeOtp = HtmlUtils.htmlEscape(formattedOtp);

        return """
                <!doctype html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s</title>
                </head>
                <body style="margin:0;padding:0;background:#f4f7fb;font-family:Arial,'Helvetica Neue',Helvetica,sans-serif;color:#172033;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:#f4f7fb;margin:0;padding:24px 12px;">
                        <tr>
                            <td align="center">
                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:560px;background:#ffffff;border-radius:8px;overflow:hidden;border:1px solid #e6edf5;">
                                    <tr>
                                        <td style="background:#0f766e;padding:24px 28px;text-align:center;">
                                            <div style="font-size:24px;line-height:32px;font-weight:700;color:#ffffff;letter-spacing:0;">HOSER</div>
                                            <div style="font-size:14px;line-height:20px;color:#d7f7f1;margin-top:4px;">Đặt lại mật khẩu</div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:32px 28px 28px;text-align:center;">
                                            <h1 style="margin:0 0 12px;font-size:22px;line-height:30px;color:#172033;font-weight:700;">Mã xác thực của bạn</h1>
                                            <p style="margin:0 auto 24px;max-width:420px;font-size:15px;line-height:24px;color:#526071;">Nhập mã OTP bên dưới để tiếp tục đặt lại mật khẩu tài khoản HOSER.</p>
                                            <div style="display:inline-block;background:#eefcf8;border:1px solid #99f6e4;border-radius:8px;padding:16px 24px;font-size:32px;line-height:40px;font-weight:700;color:#0f766e;letter-spacing:6px;">%s</div>
                                            <p style="margin:24px auto 0;max-width:420px;font-size:14px;line-height:22px;color:#526071;">Mã này sẽ hết hạn sau <strong style="color:#172033;">%d phút</strong>. Vui lòng không chia sẻ mã này với bất kỳ ai.</p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:0 28px 28px;">
                                            <div style="background:#fff7ed;border:1px solid #fed7aa;border-radius:8px;padding:14px 16px;font-size:13px;line-height:20px;color:#9a3412;">
                                                Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này. Tài khoản của bạn vẫn an toàn.
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="background:#f8fafc;padding:18px 28px;text-align:center;font-size:12px;line-height:18px;color:#7b8794;">
                                            Email này được gửi tự động từ HOSER.
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(SUBJECT, safeOtp, OTP_EXPIRES_IN_MINUTES);
    }
}
