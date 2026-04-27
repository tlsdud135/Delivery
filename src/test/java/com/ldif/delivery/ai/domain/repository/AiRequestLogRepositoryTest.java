package com.ldif.delivery.ai.domain.repository;

import com.ldif.delivery.ai.domain.entity.AiRequestLogEntity;
import com.ldif.delivery.ai.presentation.dto.AiRequest;
import com.ldif.delivery.global.infrastructure.config.QueryDslConfig;
import com.ldif.delivery.global.infrastructure.config.security.UserDetailsImpl;
import com.ldif.delivery.user.domain.entity.UserEntity;
import com.ldif.delivery.user.domain.entity.UserRoleEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(QueryDslConfig.class)
class AiRequestLogRepositoryTest {

    @Autowired
    private AiRequestLogRepository aiRequestLogRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void setAndGetAiRepo() {
        //given
        AiRequest aiRequest = new AiRequest();
        ReflectionTestUtils.setField(aiRequest, "prompt", "qwer");
        UserEntity ownerUser = new UserEntity("User", "nick", "user@user.com", "pass", UserRoleEnum.OWNER);
        UserDetailsImpl loginUser = new UserDetailsImpl(ownerUser);
        AiRequestLogEntity aiRequestLogEntity = new AiRequestLogEntity(aiRequest, loginUser);
        entityManager.persist(ownerUser);
        aiRequestLogRepository.save(aiRequestLogEntity);

        //when
        Optional<AiRequestLogEntity> foundLog = aiRequestLogRepository.findById(aiRequestLogEntity.getAiLogId());

        //then
        assertTrue(foundLog.isPresent());
        assertEquals(ownerUser, foundLog.get().getUserEntity());
    }
}