package com.ldif.delivery.menu.service;

import com.ldif.delivery.menu.dto.MenuRequest;
import com.ldif.delivery.menu.dto.MenuResponse;
import com.ldif.delivery.menu.entity.Menu;
import com.ldif.delivery.menu.repository.MenuRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;

    public MenuResponse getMenu(Long id) {
        Menu menu = findMenuById(id);
        return new MenuResponse(menu);
    }

    @Transactional
    public MenuResponse updateMenu(Long id, @Valid MenuRequest request) {
        Menu menu = findMenuById(id);
        menu.update(request);
        return new MenuResponse(menu);
    }

    @Transactional
    public void deleteMenu(Long id) {
        Menu menu = findMenuById(id);
        menuRepository.delete(menu);
    }

    @Transactional
    public MenuResponse hideMenu(Long id) {
        Menu menu = findMenuById(id);
        menu.hide();
        return new MenuResponse(menu);
    }

    private Menu findMenuById(Long id) {
        return menuRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("메뉴 없음." + id));
    }


}
