package com.minhthien.hoser_backend.service.impl;

import com.minhthien.hoser_backend.entity.User;
import com.minhthien.hoser_backend.enums.UserRole;
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

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailServiceImplTest {
    @Mock
    private JavaMailSender mailSender;

    @Test
    void sendRoleApplicationApprovedBuildsHtmlEmail() throws Exception {
        MailServiceImpl service = service();
        User user = user();

        service.sendRoleApplicationApproved(user, UserRole.OWNER);

        MimeMessage message = sentMessage();
        String html = htmlContent(message);
        assertThat(message.getSubject()).isEqualTo("HORSE - Hồ sơ đăng ký vai trò đã được duyệt");
        assertThat(html).contains("Alice Stable");
        assertThat(html).contains("HORSE");
        assertThat(html).contains("#0f766e");
    }

    @Test
    void sendRoleApplicationRejectedBuildsEscapedHtmlEmail() throws Exception {
        MailServiceImpl service = service();
        User user = user();

        service.sendRoleApplicationRejected(user, UserRole.OWNER, "A <b>bad</b> reason");

        MimeMessage message = sentMessage();
        String html = htmlContent(message);
        assertThat(message.getSubject()).isEqualTo("HORSE - Hồ sơ đăng ký vai trò cần bổ sung");
        assertThat(html).contains("A &lt;b&gt;bad&lt;/b&gt; reason");
        assertThat(html).doesNotContain("A <b>bad</b> reason");
    }

    private MailServiceImpl service() {
        when(mailSender.createMimeMessage())
                .thenAnswer(invocation -> new MimeMessage(Session.getInstance(new Properties())));
        return new MailServiceImpl(mailSender);
    }

    private MimeMessage sentMessage() {
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        MimeMessage message = captor.getValue();
        try {
            message.saveChanges();
        } catch (Exception ex) {
            throw new AssertionError("Could not finalize test email", ex);
        }
        return message;
    }

    private String htmlContent(Part part) throws Exception {
        if (part.isMimeType("text/html")) {
            return part.getContent().toString();
        }
        Object content = part.getContent();
        if (content instanceof String text) {
            return part.isMimeType("text/html") ? text : "";
        }
        if (content instanceof Multipart multipart) {
            for (int i = 0; i < multipart.getCount(); i++) {
                String html = htmlContent(multipart.getBodyPart(i));
                if (!html.isBlank()) {
                    return html;
                }
            }
        }
        return "";
    }

    private User user() {
        return User.builder()
                .id(1L)
                .username("alice")
                .fullName("Alice Stable")
                .email("alice@example.com")
                .role(UserRole.USER)
                .active(true)
                .build();
    }
}
