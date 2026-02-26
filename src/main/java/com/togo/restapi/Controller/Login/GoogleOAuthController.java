package com.togo.restapi.Controller.Login;

import com.togo.restapi.DTO.LoginDTO.GoogleAuthToken;
import com.togo.restapi.Services.LoginService.Google.GoogleAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth/google")
@Slf4j
@RequiredArgsConstructor
public class GoogleOAuthController {

    private final GoogleAuthService googleAuthService;

    @PostMapping
    public ResponseEntity<Object> googleAuth(@RequestBody GoogleAuthToken googleAuthToken){
        try {
            Map<String, Object> userBody = googleAuthService.authenticateUser(googleAuthToken.getCode());
            if (userBody.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Google Code");
            }
            boolean isNewUser = googleAuthService.checkUserOnDB(userBody);
            Map<String, String> response = new HashMap<>();
            response.put("access_token", (String) userBody.get("access_token"));
            response.put("email", (String) userBody.get("email"));
            response.put("status", isNewUser ? "Register Successful." : "Login Successful.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Authentication Error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
