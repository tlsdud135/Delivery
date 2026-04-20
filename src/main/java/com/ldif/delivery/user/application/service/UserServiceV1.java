package com.ldif.delivery.user.application.service;

import com.ldif.delivery.user.domain.entity.UserEntity;
import com.ldif.delivery.user.domain.repository.UserRepository;
import com.ldif.delivery.user.presentation.dto.response.ResUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceV1 {

    private final UserRepository userRepository;

    public List<ResUserDto> getUsers() {

        List<UserEntity> allUsers = userRepository.findAll();

        List<ResUserDto> userDtoList = allUsers.stream()
                .map(ResUserDto::new)
                .collect(Collectors.toList());

    }

}
