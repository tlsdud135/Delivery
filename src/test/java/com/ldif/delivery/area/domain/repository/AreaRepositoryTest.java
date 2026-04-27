package com.ldif.delivery.area.domain.repository;

import com.ldif.delivery.area.domain.entity.AreaEntity;
import com.ldif.delivery.area.persentation.dto.AreaRequest;
import com.ldif.delivery.global.infrastructure.config.QueryDslConfig;
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
class AreaRepositoryTest {

    @Autowired
    private AreaRepository areaRepository;

    @Test
    void setAndGetArea() {
        //given
        AreaRequest areaRequest = new AreaRequest();
        ReflectionTestUtils.setField(areaRequest, "name", "n");
        ReflectionTestUtils.setField(areaRequest, "city", "c");
        ReflectionTestUtils.setField(areaRequest, "district", "d");

        AreaEntity areaEntity = new AreaEntity(areaRequest);
        areaRepository.save(areaEntity);

        //when
        Optional<AreaEntity> foundLog = areaRepository.findById(areaEntity.getAreaId());

        //then
        assertTrue(foundLog.isPresent());
        assertEquals("n", foundLog.get().getName());
        assertEquals("c", foundLog.get().getCity());
        assertEquals("d", foundLog.get().getDistrict());
    }
}