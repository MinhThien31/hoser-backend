package com.minhthien.hoser_backend.service.impl;


import com.minhthien.hoser_backend.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService {
    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendOtp(String email, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("Password Reset OTP");
        message.setText("Your OTP code is: " + otp);
        message.setSubject("CẢM ƠN VÌ BẠN ĐÃ TRỞ LẠI VỚI CHÚNG TÔI");

        mailSender.send(message);
    }
}
