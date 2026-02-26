package com.togo.restapi.DTO.RegisterDTO;

import lombok.Data;
import lombok.NonNull;

@Data
public class VerifyEmailAddress {
    @NonNull
    private String email;

}
