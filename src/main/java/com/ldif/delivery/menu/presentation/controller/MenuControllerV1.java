package com.ldif.delivery.menu.presentation.controller;

import com.ldif.delivery.global.infrastructure.config.security.UserDetailsImpl;
import com.ldif.delivery.menu.application.service.MenuServiceV1;
import com.ldif.delivery.menu.presentation.dto.MenuRequest;
import com.ldif.delivery.menu.presentation.dto.MenuResponse;
import com.ldif.delivery.user.domain.entity.UserRoleEnum;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuControllerV1 {

    private final MenuServiceV1 menuService;

    @GetMapping("/{id}")
    public ResponseEntity<MenuResponse> getMenu(@PathVariable UUID id) {
        return ResponseEntity.ok(menuService.getMenu(id));
    }

    @PutMapping("/{id}")
    @Secured({UserRoleEnum.Authority.MASTER, UserRoleEnum.Authority.MANAGER, UserRoleEnum.Authority.OWNER})
    public ResponseEntity<MenuResponse> updateMenu(@PathVariable UUID id, @Valid @RequestBody MenuRequest request, @AuthenticationPrincipal UserDetailsImpl loginUser) {
        return ResponseEntity.ok(menuService.updateMenu(id, request, loginUser));
    }

    @DeleteMapping("/{id}")
    @Secured({UserRoleEnum.Authority.MASTER, UserRoleEnum.Authority.OWNER})
    public void deleteMenu(@PathVariable UUID id, @AuthenticationPrincipal UserDetailsImpl loginUser) {
        menuService.deleteMenu(id, loginUser);
    }

    @PatchMapping("/{id}/hide")
    @Secured({UserRoleEnum.Authority.MASTER, UserRoleEnum.Authority.MANAGER, UserRoleEnum.Authority.OWNER})
    public ResponseEntity<MenuResponse> hideMenu(@PathVariable UUID id, @AuthenticationPrincipal UserDetailsImpl loginUser) {
        return ResponseEntity.ok(menuService.hideMenu(id, loginUser));
    }

}
