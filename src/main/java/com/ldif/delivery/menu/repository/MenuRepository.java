package com.ldif.delivery.menu.repository;

import com.ldif.delivery.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long> {
}
