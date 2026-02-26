package com.togo.restapi.DTO.RegisterDTO;

import lombok.Data;
import lombok.NonNull;

@Data
public class CheckEmailForRegister {
    @NonNull
    private String email;
}
