package com.ldif.delivery.menu.presentation.dto;

import com.ldif.delivery.menu.domain.entity.MenuEntity;
import com.ldif.delivery.store.entity.StoreEntity;
import lombok.Getter;

@Getter
public class MenuResponse {
    private final Long menuId;
    private final String name;
    private final Integer price;
    private final String description;
    private final Boolean isHidden;
    private final StoreEntity storeEntity;

    public MenuResponse(MenuEntity menuEntity) {
        this.menuId = menuEntity.getMenuId();
        this.name = menuEntity.getName();
        this.price = menuEntity.getPrice();
        this.description = menuEntity.getDescription();
        this.isHidden = menuEntity.getIsHidden();
        this.storeEntity = menuEntity.getStoreEntity();
    }
}
