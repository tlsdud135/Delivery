package com.ldif.delivery.menu.entity;

import com.ldif.delivery.menu.dto.MenuRequest;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_menu")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long menu_id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Column
    private String description;

    @Column
    private Boolean is_hidden = Boolean.FALSE;

//    @ManyToOne
//    @JoinColumn(name = store_id, nullable = false)
//    private Store store;

    public Menu(String name, Integer price, String description, Boolean is_hidden) {
        //this.store=store;
        this.name = name;
        this.price = price;
        this.description = description;
        this.is_hidden = is_hidden;
    }

    public void update(@Valid MenuRequest request) {
        this.name = request.getName();
        this.price = request.getPrice();
        this.description = request.getDescription();
    }

    public void hide() {
        is_hidden = !is_hidden;
    }
}
