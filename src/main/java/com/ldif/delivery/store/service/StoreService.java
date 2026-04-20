package com.ldif.delivery.store.service;

import com.ldif.delivery.store.dto.StoreRequest;
import com.ldif.delivery.store.entity.StoreEntity;
import com.ldif.delivery.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    public Long createStore(StoreRequest request, String username){
        StoreEntity store = new StoreEntity(
                request.getName(),
                request.getAddress(),
                request.getPhone(),
                username
        );

        return storeRepository.save(store).getStoreId();
    }
}
