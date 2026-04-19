package com.ldif.delivery.menu.dto;

import com.ldif.delivery.menu.entity.Menu;
import lombok.Getter;

@Getter
public class MenuResponse {
    private final Long menu_id;
    private final String name;
    private final Integer price;
    private final String description;
    private final Boolean is_hidden;
    //private final Store store;

    public MenuResponse(Menu menu) {
        this.menu_id = menu.getMenu_id();
        this.name = menu.getName();
        this.price = menu.getPrice();
        this.description = menu.getDescription();
        this.is_hidden = menu.getIs_hidden();
        //this.store=menu.getStore();
    }
}
