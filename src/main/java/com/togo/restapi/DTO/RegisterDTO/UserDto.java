package com.togo.restapi.DTO.RegisterDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Map;

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
    private Map<String, Object> group;
    private Map<String, Object> fndList;
    private Map<String, Object> pageList;
    private Map<String, Object> chatList;
    private Map<String, Object> notificationList;
}
