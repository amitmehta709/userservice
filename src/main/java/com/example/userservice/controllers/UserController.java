package com.example.userservice.controllers;

import com.example.userservice.dtos.LoginRequestDto;
import com.example.userservice.dtos.SignUpRequestDto;
import com.example.userservice.dtos.UserDto;
import com.example.userservice.dtos.ValidateRequestDto;
import com.example.userservice.models.Token;
import com.example.userservice.models.User;
import com.example.userservice.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public UserDto signup(@RequestBody SignUpRequestDto requestDto) {
        User user =
                userService.createUser(requestDto.getEmail(),requestDto.getPassword(), requestDto.getName());
        return UserDto.from(user);
    }

    @PostMapping("/login")
    public Token login(@RequestBody LoginRequestDto requestDto) {
        return userService.login(requestDto.getEmail(),requestDto.getPassword());
    }

//    @PostMapping("/validate")
//    public UserDto validate(@RequestBody ValidateRequestDto requestDto) {
//        return UserDto.from(userService.validateToken(requestDto.getTokenValue()));
//    }
    @PostMapping("/validate/{token}")
    public ResponseEntity<UserDto> validate(@PathVariable String token) {
        return new ResponseEntity<>(UserDto.from(userService.validateToken(token)), HttpStatus.OK);
    }
}
