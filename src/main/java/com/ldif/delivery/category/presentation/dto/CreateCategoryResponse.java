package com.ldif.delivery.category.presentation.dto;

import com.ldif.delivery.category.domain.entity.CategoryEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CreateCategoryResponse {

    private final UUID categroyId;
    private final String name;

    //public CreateCategoryResponse(CategoryEntity categoryEntity)
    //{
    //    this.categroyId = categoryEntity.getCategoryId();
    //    this.name = categoryEntity.getName();
    //}

    // @Builder + 정적팩토리메서드
    public static CreateCategoryResponse from(CategoryEntity categoryEntity)
    {
        return CreateCategoryResponse.builder()
                .categroyId(categoryEntity.getCategoryId())
                .name(categoryEntity.getName())
                .build();
    }
}
