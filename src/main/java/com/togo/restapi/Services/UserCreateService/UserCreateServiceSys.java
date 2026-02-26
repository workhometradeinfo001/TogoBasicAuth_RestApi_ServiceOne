package com.togo.restapi.Services.UserCreateService;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.togo.restapi.DTO.RegisterDTO.CheckCountryPhNumbr;
import com.togo.restapi.DTO.RegisterDTO.CheckEmailForRegister;
import com.togo.restapi.DTO.RegisterDTO.UserDto;
import com.togo.restapi.Entity.UserEntity.User;
import com.togo.restapi.Repository.MongoRepo.UserDetailsImlRepo;
import com.togo.restapi.Services.RedisService.RedisService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCreateServiceSys {

    private final UserDetailsImlRepo userDetailsImlRepo;
    private final JavaMailSender javaMailSender;
    private final RedisService<String> redisService;
    private final SecureRandom SECURECODE = new SecureRandom();
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
    private final MongoTemplate mongoTemplate;
    private String confirmCode;
    private final SecureRandom secureRandom = new SecureRandom();


    public User createNewUserOnDatabase(UserDto userDto){
        User user = new User();
        try{
            user.setFirstName(userDto.getFirstName());
            user.setLastName(userDto.getLastName());
            user.setUsername(String.valueOf(secureRandom.nextLong()));
            user.setNumCountryCode(userDto.getNumCountryCode());
            user.setPhoneNumber(userDto.getPhoneNumber());
            user.setEmail(userDto.getEmail());
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            user.setGroup(userDto.getGroup());
            user.setPageList(userDto.getPageList());
            user.setFndList(userDto.getFndList());
            user.setChatList(userDto.getChatList());
            user.setNotificationList(userDto.getNotificationList());
            user.setRole(Collections.singletonList("user"));
            @NonNull String collection = "user_"+userDto.getNumCountryCode();
            userDetailsImlRepo.save(user);
            return mongoTemplate.save(user, collection);
        } catch (Exception e) {
            log.error("Something wrong! Check your details.", e);
        }
        return null;
    }
    public void generateRandomCode(String emailLen){
        int num = emailLen.length();
        byte[] randombyte = new byte[num];
        SECURECODE.nextBytes(randombyte);
        confirmCode = Base64.getUrlEncoder().withoutPadding().encodeToString(randombyte);
    }
    public boolean confirmationCode(String email){
        try{
            generateRandomCode(email);
            simpleMailMessage.setTo(email);
            simpleMailMessage.setSubject("Verification code for Togo Account");
            simpleMailMessage.setText("Your code is: " + confirmCode);
            javaMailSender.send(simpleMailMessage);
            redisService.set(email, confirmCode);
            return true;
        }catch (Exception e){
            log.error("Code not send! Check your details.{}", String.valueOf(e));
            return false;
        }

    }
    public boolean deleteVerificaionCode(String email){
        try{
            return redisService.delete(email);
        }catch (Exception e){
            log.error("Email code not found!"+e);
            return false;
        }
    }
    public boolean emailVerifyService(CheckEmailForRegister emailReg){
        String email = emailReg.getEmail();
        Query query = new Query();
        query.addCriteria(Criteria.where("email").is(email));
        List<User> users = mongoTemplate.find(query, User.class);
        return !users.isEmpty();
    }
    public boolean verifySubCode(String email, String confirmCode){
        String code = redisService.get(email);
        return code.equals(confirmCode);
    }

    public boolean checkLoginCredential(String email, String password){
        if (email.isEmpty() || password.isEmpty()){
            return false;
        }else {
            Query query = new Query();
            query.addCriteria(Criteria.where("email").is(email));
            List<User> user = mongoTemplate.find(query, User.class);
            if (user.isEmpty()){
                return false;
            }else {
                String hashPass = user.get(0).getPassword();
                return passwordEncoder.matches(password, hashPass);
            }
        }
    }

    public List<User> checkPhnNumber(CheckCountryPhNumbr dto) throws NumberParseException {
        String number = dto.getPhnNumber();
        String code = dto.getNumCountryCode();
        Query query = new Query();
        if (number != null && number.startsWith("+")){
            Map<String, String> splitNum = separateCcNum(number);
            String cc = "+"+splitNum.get("nationalCode");
            String num = splitNum.get("nationalNumber");
            dto.setPhnNumber(num);
            dto.setNumCountryCode(cc);
            query.addCriteria(Criteria.where("phoneNumber").is(num));
            query.addCriteria(Criteria.where("numCountryCode").is(cc));
        }else {
            query.addCriteria(Criteria.where("phoneNumber").is(number));
            query.addCriteria(Criteria.where("numCountryCode").is(code));
        }
        return mongoTemplate.find(query, User.class);
    }
    public Map<String, String> separateCcNum(String fullNumber) throws NumberParseException {
        Map<String, String> parts = new HashMap<>();
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber numberProto = phoneNumberUtil.parse(fullNumber, "");
        parts.put("nationalCode", String.valueOf(numberProto.getCountryCode()));
        parts.put("nationalNumber", String.valueOf(numberProto.getNationalNumber()));
        return parts;
    }

}
