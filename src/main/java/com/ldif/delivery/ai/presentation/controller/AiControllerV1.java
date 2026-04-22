package com.ldif.delivery.ai.presentation.controller;

import com.ldif.delivery.ai.application.service.AiServiceV1;
import com.ldif.delivery.ai.presentation.dto.AiRequest;
import com.ldif.delivery.ai.presentation.dto.AiResponse;
import com.ldif.delivery.global.infrastructure.config.security.UserDetailsImpl;
import com.ldif.delivery.user.domain.entity.UserRoleEnum;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiControllerV1 {

    private final AiServiceV1 aiServiceV1;

    @PostMapping("/product-description")
    @Secured(UserRoleEnum.Authority.OWNER)
    public ResponseEntity<AiResponse> setDescription(@Valid @RequestBody AiRequest aiRequest, @AuthenticationPrincipal UserDetailsImpl loginUser) {
        return ResponseEntity.ok(aiServiceV1.setDescription(aiRequest, loginUser));
    }

}
