package com.ldif.delivery.user.presentation.controller;

import com.ldif.delivery.user.application.service.UserServiceV1;
import com.ldif.delivery.user.presentation.dto.response.ResUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserControllerV1 {

    private final UserServiceV1 userService;

    @GetMapping("/")
    public List<ResUserDto> getUsers () {
        return userService.getUsers();
    }


}
