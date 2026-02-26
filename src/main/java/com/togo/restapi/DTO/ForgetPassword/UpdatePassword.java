package com.togo.restapi.DTO.ForgetPassword;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class UpdatePassword {

    @NonNull
    private String email;
    @NonNull
    private String password;
}
