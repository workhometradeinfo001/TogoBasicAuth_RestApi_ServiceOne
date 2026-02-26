package com.togo.restapi.Controller.CreateAccount;
import com.togo.restapi.DTO.RegisterDTO.*;
import com.togo.restapi.Entity.UserEntity.User;
import com.togo.restapi.Services.RedisService.RedisService;
import com.togo.restapi.Services.UserCreateService.UserCreateServiceSys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegisterAccountController {

    private final UserCreateServiceSys userCreateServiceSys;

    @PostMapping("create-account")
    public ResponseEntity<UserDto> createAccountWithDetails(@RequestBody UserDto userDto){
        try{
            User newUserOnDatabase = userCreateServiceSys.createNewUserOnDatabase(userDto);
            if (newUserOnDatabase != null){
                return new ResponseEntity<>(HttpStatus.CREATED);
            }else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
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
    public ResponseEntity<HttpStatus> verficationMethod(@RequestBody VerifyEmailAddress emailCode){
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
    public ResponseEntity<HttpStatus> loginMethod(@RequestBody LoginDTO loginDTO){
        try {
            boolean loginResponse = userCreateServiceSys.checkLoginCredential(loginDTO.getEmail(), loginDTO.getPassword());
            if (loginResponse){
                return new ResponseEntity<>(HttpStatus.FOUND);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }catch (Exception e){
            log.error("Something wrong?{}", String.valueOf(e));
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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

    @GetMapping
    public String CheckSystem(){
        return "Ok";
    }

}
