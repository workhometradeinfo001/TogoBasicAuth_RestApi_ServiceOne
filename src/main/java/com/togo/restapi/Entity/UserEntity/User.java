package com.togo.restapi.Entity.UserEntity;

import lombok.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Document(collection = "togo_user_database")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityScan
public class User {
        @Id
        private String id;
        @NonNull
        private String firstName;
        @NonNull
        private String lastName;
        private String username;
        @NonNull
        private String email;
        @NonNull
        private String password;
        @NonNull
        private String phoneNumber;
        @NonNull
        private String numCountryCode;

        private List<String> role;

        public Collection<? extends GrantedAuthority> getAuthorities() {
                // Example: converting a string role like "ROLE_USER" into a GrantedAuthority
                return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }

}
