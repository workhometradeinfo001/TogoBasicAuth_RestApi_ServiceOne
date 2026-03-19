package com.togo.restapi.DTO.RegisterDTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class UserDto {
    @NonNull
    private String firstName;
    @NonNull
    private String lastName;
    @NonNull
    private String email;
    @NonNull
    private String password;
    @NonNull
    private String phoneNumber;
    @NonNull
    private String numCountryCode;
}
