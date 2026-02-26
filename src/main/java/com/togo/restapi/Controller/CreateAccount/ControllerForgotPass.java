package com.togo.restapi.Controller.CreateAccount;

import com.togo.restapi.DTO.ForgetPassword.UpdatePassword;
import com.togo.restapi.DTO.ForgetPassword.VerifyCodeForgetPass;
import com.togo.restapi.DTO.RegisterDTO.VerifyEmailAddress;
import com.togo.restapi.Services.UserCreateService.ServiceForgetPass;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/forgetpass")
@RequiredArgsConstructor
public class ControllerForgotPass {

    private final ServiceForgetPass serviceForgetPass;

    @PostMapping("/emailValidation")
    public ResponseEntity<HttpStatus> forgetPassCodeSent(@RequestBody VerifyEmailAddress emailAddress){
        try {
            boolean b = serviceForgetPass.sentCodeForPass(emailAddress.getEmail());
            if (b){
                return new ResponseEntity<>(HttpStatus.OK);
            }else {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
        } catch (Exception e) {
            log.error("Internal server: {}", String.valueOf(e));
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/code/verify-code")
    public ResponseEntity<HttpStatus> matchingCode(@RequestBody VerifyCodeForgetPass verifyCode){
        try{
            boolean serviceResponse = serviceForgetPass.verifyForgetPassCode(verifyCode.getEmail(), verifyCode.getCode());
            if (serviceResponse){
                serviceForgetPass.deleteCodeFromRedis(verifyCode.getEmail());
                return new ResponseEntity<>(HttpStatus.FOUND);
            }else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Something wrong! {}", String.valueOf(e));
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PutMapping("/update-pass")
    public ResponseEntity<HttpStatus> updatePassword(@RequestBody UpdatePassword udPass){
        try{
            boolean updateResponse = serviceForgetPass.updatePassword(udPass.getEmail(), udPass.getPassword());
            if (updateResponse){
                return new ResponseEntity<>(HttpStatus.OK);
            }else {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
        } catch (Exception e) {
            log.error("Update not successful! {}", String.valueOf(e));
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}
