package com.togo.restapi.DTO.RegisterDTO;

import lombok.Data;

@Data
public class VerificationDetails {

    private String email;
    private String code;

}
