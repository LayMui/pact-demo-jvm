package com.demo.app;

import org.springframework.stereotype.Service;

@Service
public class UserService {


    public User addUser(User user){

        /* for testing the provider end. This service will be mocked */
        return new User(1234, "mike", "mike", "tan");
    }

}
