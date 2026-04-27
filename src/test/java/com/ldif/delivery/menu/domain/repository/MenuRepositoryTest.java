package com.ldif.delivery.menu.domain.repository;

import com.ldif.delivery.global.infrastructure.config.QueryDslConfig;
import com.ldif.delivery.menu.domain.entity.MenuEntity;
import com.ldif.delivery.menu.presentation.dto.MenuRequest;
import com.ldif.delivery.store.domain.entity.StoreEntity;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(QueryDslConfig.class)
class MenuRepositoryTest {

    @Autowired
    private MenuRepository menuRepository;

    @Test
    void setAndGetMenu() {
        //given
        MenuRequest newMenuRequest = new MenuRequest();
        ReflectionTestUtils.setField(newMenuRequest, "name", "qwer");
        ReflectionTestUtils.setField(newMenuRequest, "price", 1234);
        EasyRandom easyRandom = new EasyRandom();
        StoreEntity store = easyRandom.nextObject(StoreEntity.class);
        MenuEntity menuEntity = new MenuEntity(newMenuRequest, store);
        menuRepository.save(menuEntity);

        //when
        Optional<MenuEntity> foundMenu = menuRepository.findById(menuEntity.getMenuId());

        //then
        assertTrue(foundMenu.isPresent());
        assertEquals("qwer", foundMenu.get().getName());
    }
}