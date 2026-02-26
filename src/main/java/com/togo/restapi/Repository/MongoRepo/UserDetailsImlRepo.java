package com.togo.restapi.Repository.MongoRepo;

import com.togo.restapi.Entity.UserEntity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserDetailsImlRepo extends MongoRepository<User, String> {
    void findByEmail(String email);
}
