package com.togo.restapi.Controller.CreateAccount;
import com.togo.restapi.DTO.RegisterDTO.*;
import com.togo.restapi.Entity.UserEntity.User;
import com.togo.restapi.Services.UserCreateService.UserCreateServiceSys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegisterAccountController {

    private final UserCreateServiceSys userCreateServiceSys;

    @PostMapping("create-account")
    public ResponseEntity<?> createAccountWithDetails(@RequestBody UserDto userDto){
        return userCreateServiceSys.createNewUserOnDatabase(userDto)
                .map(token -> ResponseEntity.status(HttpStatus.CREATED).body(token)) // Extract the String
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    @PostMapping("/verifyEmail")
    public ResponseEntity<CheckEmailForRegister> emailVerification(@RequestBody CheckEmailForRegister emailCheck){
        try {
            boolean result = userCreateServiceSys.emailVerifyService(emailCheck);
            if (result){
               return new ResponseEntity<>(HttpStatus.FOUND);
            }else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @PostMapping("/confirm-verification-code")
    public ResponseEntity<HttpStatus> verificationMethod(@RequestBody VerifyEmailAddress emailCode){
        boolean codeStatus = userCreateServiceSys.confirmationCode(emailCode.getEmail());
        if (codeStatus){
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/delete-code")
    public ResponseEntity<HttpStatus> deleteCode(@RequestBody VerifyEmailAddress emailAddress){
        boolean deleteResponse = userCreateServiceSys.deleteVerificaionCode(emailAddress.getEmail());
        if (deleteResponse){
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/submit-code")
    public ResponseEntity<HttpStatus> verifySubmitCode(@RequestBody VerificationDetails verificationDetails){
        try {
            boolean response = userCreateServiceSys.verifySubCode(verificationDetails.getEmail(), verificationDetails.getCode());
            if (response){
                return new ResponseEntity<>(HttpStatus.FOUND);
            }
        }catch (Exception e){
            log.error("Code can't match now!", e);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/login")
    public Map<String, String> loginMethod(@RequestBody LoginDTO loginDTO){
        try {
            Map<String, String> loginResponse = userCreateServiceSys.checkLoginCredential(loginDTO.getEmail(), loginDTO.getPassword());
            if (!loginResponse.isEmpty()){
                loginResponse.put("Http", "302");
                log.info(loginResponse.toString());
                return loginResponse;
            }
            loginResponse.put("Http", "404");
            return loginResponse;
        }catch (Exception e){
            log.error("Something wrong?{}", String.valueOf(e));
            Map<String, String> map = new HashMap<>();
            map.put("Http", "500");
            return map;
        }
    }

    @PostMapping("/checkNumber")
    public ResponseEntity<Object> checkNumberIntoDatabase(@RequestBody CheckCountryPhNumbr checkCountryPhNumbr){
        try {
            List<User> users = userCreateServiceSys.checkPhnNumber(checkCountryPhNumbr);
            if (!users.isEmpty()){
                return ResponseEntity.status(HttpStatus.FOUND).body(users);
            }else {
                Map<String, String> numMap = new HashMap<>();
                numMap.put("nationalCode", checkCountryPhNumbr.getNumCountryCode());
                numMap.put("nationalNumber", checkCountryPhNumbr.getPhnNumber());
                return ResponseEntity.ok(numMap);
            }
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
        }
    }


}
