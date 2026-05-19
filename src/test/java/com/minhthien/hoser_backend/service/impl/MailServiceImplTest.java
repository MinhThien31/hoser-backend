package com.minhthien.hoser_backend.service.impl;

import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Test
    void sendOtpSendsResponsiveHtmlEmailWithPlainTextFallback() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        MailServiceImpl mailService = new MailServiceImpl(mailSender);

        mailService.sendOtp("user@example.com", "123456");

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        MimeMessage sentMessage = messageCaptor.getValue();
        sentMessage.saveChanges();
        TextParts textParts = collectTextParts(sentMessage);

        assertThat(sentMessage.getRecipients(Message.RecipientType.TO))
                .extracting(Object::toString)
                .containsExactly("user@example.com");
        assertThat(sentMessage.getSubject()).isEqualTo("Mã OTP đặt lại mật khẩu HOSER");
        assertThat(textParts.plainText()).contains(
                "HOSER",
                "Mã OTP đặt lại mật khẩu của bạn là: 1 2 3 4 5 6",
                "Mã này sẽ hết hạn sau 10 phút",
                "Nếu bạn không yêu cầu đặt lại mật khẩu"
        );
        assertThat(textParts.html()).contains(
                "<html lang=\"vi\">",
                "HOSER",
                "Đặt lại mật khẩu",
                "1 2 3 4 5 6",
                "10 phút",
                "Nếu bạn không yêu cầu đặt lại mật khẩu"
        );
    }

    private TextParts collectTextParts(Part part) throws Exception {
        List<String> plainText = new ArrayList<>();
        List<String> html = new ArrayList<>();
        collectTextParts(part, plainText, html);
        return new TextParts(String.join("\n", plainText), String.join("\n", html));
    }

    private void collectTextParts(Part part, List<String> plainText, List<String> html) throws Exception {
        Object content = part.getContent();

        if (content instanceof Multipart multipart) {
            for (int index = 0; index < multipart.getCount(); index++) {
                collectTextParts(multipart.getBodyPart(index), plainText, html);
            }
            return;
        }

        if (part.isMimeType("text/plain")) {
            plainText.add((String) content);
            return;
        }

        if (part.isMimeType("text/html")) {
            html.add((String) content);
        }
    }

    private record TextParts(String plainText, String html) {
    }
}
