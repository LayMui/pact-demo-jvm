package com.demo.app;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;


    public UserController(UserService userService) {
        this.userService=userService;
    }

    @PostMapping(value="/users",produces=MediaType.APPLICATION_JSON_UTF8_VALUE)
    public User addUser(@RequestBody User user){
        return userService.addUser(user);
    }

}
