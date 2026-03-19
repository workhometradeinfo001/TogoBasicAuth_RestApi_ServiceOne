package com.togo.restapi.Repository.MongoRepo;

import com.togo.restapi.Entity.UserEntity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDetailsImlRepo extends MongoRepository<User, String> {
}
