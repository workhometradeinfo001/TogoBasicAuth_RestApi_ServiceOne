package com.togo.restapi.Services.UserCreateService;

import com.togo.restapi.Entity.UserEntity.User;
import com.togo.restapi.Services.RedisService.RedisService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.mail.MailMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ServiceForgetPass {

    Random random = new Random();
    String sixDigitCode = "";
    private final JavaMailSender javaMailSender;
    private final RedisService<String> redisService;
    private final MongoTemplate mongoTemplate;
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public boolean sentCodeForPass(String email){
        for (int i = 0; i<6; i++){
            sixDigitCode += random.nextInt(10);
        }
        if (!sixDigitCode.isEmpty()){
            SimpleMailMessage ms = new SimpleMailMessage();
            ms.setTo(email);
            ms.setSubject("Togo-Forget password: Code");
            ms.setText("Your verification code is: "+ sixDigitCode);
            javaMailSender.send(ms);
            redisService.set(email, sixDigitCode);
            sixDigitCode = "";
            return true;
        }else {
            return false;
        }
    }

    public boolean verifyForgetPassCode(String email, String code) {
        int redisCode;
        int frontCode;
        redisCode = Integer.parseInt(redisService.get(email));
        frontCode = Integer.parseInt(code);
        return redisCode == frontCode;
    }
    public void deleteCodeFromRedis(String email){
        redisService.delete(email);
    }

    public boolean updatePassword(String email, String password){
        Query query = new Query();
        query.addCriteria(Criteria.where("email").is(email));
        List<User> user = mongoTemplate.find(query, User.class);
        if (user.isEmpty()){
            return false;
        }else {
            User particularUser = user.get(0);
            particularUser.setPassword(passwordEncoder.encode(password));
            return true;
        }
    }

}
