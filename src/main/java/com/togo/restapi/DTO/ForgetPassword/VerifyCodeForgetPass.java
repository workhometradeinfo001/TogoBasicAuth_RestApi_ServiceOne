package com.togo.restapi.DTO.ForgetPassword;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VerifyCodeForgetPass {

    private String email;
    private String code;
}
