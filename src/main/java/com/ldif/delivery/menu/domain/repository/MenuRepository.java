package com.ldif.delivery.menu.domain.repository;

import com.ldif.delivery.menu.domain.entity.MenuEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<MenuEntity, Long> {
    Page<MenuEntity> findAllByStoreEntity_StoreIdAndNameContainingIgnoreCaseAndIsDeletedFalse(Long storeId, String keyword, Pageable pageable);
}
