package com.togo.restapi.Services.LoginService.Google;

import com.togo.restapi.Entity.UserEntity.User;
import com.togo.restapi.Repository.MongoRepo.UserDetailsImlRepo;
import com.togo.restapi.Services.UserCreateService.UserCreateServiceSys;
import com.togo.restapi.components.ParsePhone;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    @Value("${googleOAuth.tokenEndPoint}")
    private String tokenEndPoint;
    @Value("${googleOAuth.clientId}")
    private String clientID;
    @Value("${googleOAuth.clientSecret}")
    private String clientSecret;
    @Value("${googleOAuth.userInfoUrl}")
    private String userInfo;
    @Value("${googleOAuth.redirectUri}")
    private String redirectUri;
    private String randomPassword;
    private static final String REFRESH_TOKEN_TEXT = "refresh_token";
    private static final String ACCESS_TOKEN_TEXT = "access_token";

    private final RestTemplate restTemplate;
    private final MongoTemplate mongoTemplate;
    private final JavaMailSender javaMailSender;
    private final UserDetailsImlRepo userDetailsImlRepo;
    private final UserCreateServiceSys userService;
    private final ParsePhone parsePhone;
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Map<String, Object> authenticateUser(String code) {
        try {
            HttpHeaders headers = new HttpHeaders();
            // This header is CRITICAL for Google to accept the MultiValueMap
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
            param.add("code", code);
            param.add("client_id", clientID);
            param.add("client_secret", clientSecret);
            param.add("redirect_uri", "postmessage");
            param.add("grant_type", "authorization_code");
            // ✅ FIX: You MUST wrap the params AND headers into an HttpEntity
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(param, headers);
            // ✅ FIX: Pass the requestEntity instead of just 'param'
            try {
                var response = restTemplate.postForObject(tokenEndPoint, requestEntity, Map.class);
                if (response == null || response.get(ACCESS_TOKEN_TEXT) == null) {
                    log.error("Google response: {}", response); // Log this to see what Google actually sent
                    return Collections.emptyMap();
                }
                String accessToken = (String) response.get(ACCESS_TOKEN_TEXT);
                String refreshToken = (String) response.get(REFRESH_TOKEN_TEXT);
                // Now proceed to get User
                HttpHeaders authHeaders = new HttpHeaders();
                authHeaders.setBearerAuth(accessToken);
                HttpEntity<String> userInfoEntity = new HttpEntity<>(authHeaders);

                var userProfile = restTemplate.exchange(userInfo, HttpMethod.GET, userInfoEntity, Map.class).getBody();
                assert userProfile != null;
                if (!userProfile.isEmpty()){
                    userProfile.put(ACCESS_TOKEN_TEXT, accessToken);
                    Query query = new Query(Criteria.where("email").is(userProfile.get("email")));
                    Update update = new Update();
                    update.set(REFRESH_TOKEN_TEXT, refreshToken);
                    update.set("last_login", new Date());
                    mongoTemplate.upsert(query, update, "refresh_token");
                }
                return userProfile;
            }catch (Exception e){
                e.printStackTrace();
                return Collections.emptyMap();
            }

        } catch (Exception e) {
            log.error("Can't authenticate user!", e);
            return Collections.emptyMap();
        }
    }

    public String checkUserOnDB(Map<String, Object> userObj) {
        if (userObj == null || userObj.isEmpty()) {
            return null;
        }
        String email = (String) userObj.get("email");
        // 1. Look for the user by email
        Query query = new Query();
        query.addCriteria(Criteria.where("email").is(email));
        // findOne returns null if no user is found, preventing the IndexOutOfBoundsException
        User existingUser = mongoTemplate.findOne(query, User.class);
        // 2. If user does NOT exist, save them
        if (existingUser == null) {
            User saveUser = new User();
            if (userObj.get("given_name") == null){
                saveUser.setFirstName("Given Name");
            }else {
                saveUser.setFirstName((String) userObj.get("given_name"));
            }
            if (userObj.get("family_name") == null){
                saveUser.setLastName("Family Name");
            }else {
                saveUser.setLastName((String) userObj.get("family_name"));
            }
            if (userObj.get("phone_number") == null){
                saveUser.setPhoneNumber("No_Number");
                saveUser.setNumCountryCode("No_CC");
            }else {
                String fullNbr = (String) userObj.get("phone_number");
                Map<String, String> stringStringMap = parsePhone.parsePhone(fullNbr);
                saveUser.setPhoneNumber(stringStringMap.get("number"));
                saveUser.setNumCountryCode(stringStringMap.get("countryCode"));
            }
            saveUser.setUsername(String.valueOf(UUID.randomUUID()));
            saveUser.setEmail(email);
            saveUser.setRole(Collections.singletonList("user"));
            // Use a random UUID for password as a placeholder
            randomPassword = UUID.randomUUID().toString();
            saveUser.setPassword(passwordEncoder.encode(randomPassword));
            sendPasswordToMail(email, randomPassword);
            User save = userDetailsImlRepo.save(saveUser);
            return userService.createJwtNewUser(save.getId(), save.getEmail());
            // Successfully registered new user
        }
        // 3. User already exists in DB
        return null;
    }
    public void sendPasswordToMail(String email, String password){
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setSubject("This email for your knowledge about your account password (Togo).");
        mailMessage.setText("Password is: "+password);
        javaMailSender.send(mailMessage);
    }

}
