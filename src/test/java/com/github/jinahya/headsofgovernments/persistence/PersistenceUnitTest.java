package com.github.jinahya.headsofgovernments.persistence;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
class PersistenceUnitTest {

    @Test
    void __() {
        PersistenceTestUtils.applyEntityManagerFactory(emf -> {
            final var metamodel = emf.getMetamodel();
            final var entities = metamodel.getEntities();
            for (final var entity : entities) {
                log.debug("entity: {}", entity);
                log.debug("\tname: {}", entity.getName());
                log.debug("\tidType: {}", entity.getIdType());
                final var attributes = entity.getAttributes();
                int attributeIndex = 0;
                for (final var attribute : attributes) {
                    log.debug("\tattribute[{}]: {}", attributeIndex++, attribute);
                }
            }
            return null;
        });
    }
}
