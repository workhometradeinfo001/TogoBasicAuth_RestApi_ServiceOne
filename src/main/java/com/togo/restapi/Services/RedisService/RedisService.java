package com.togo.restapi.Services.RedisService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisService<T> {

    private final RedisTemplate<String, T> redisTemplate;

    public void set(String key, T value){
        redisTemplate.opsForValue().set(key, value);
    }
    public T get(String key){
        return redisTemplate.opsForValue().get(key);
    }
    public boolean delete(String key){
        try{
            return redisTemplate.delete(key);
        }catch (Exception e){
            return false;
        }
    }


}
