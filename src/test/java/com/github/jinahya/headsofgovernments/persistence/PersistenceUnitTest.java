package com.github.jinahya.headsofgovernments.persistence;

import jakarta.persistence.Persistence;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
class PersistenceUnitTest {

    @Test
    void __() {
        try (var factory = Persistence.createEntityManagerFactory("heads-of-governments")) {
            log.debug("properties");
            factory.getProperties().forEach((k, v) -> {
                log.debug("\tproperty: {}: {}", k, v);
            });
            final var metamodel = factory.getMetamodel();
            log.debug("entities");
            final var entities = metamodel.getEntities();
            for (final var entity : entities) {
                log.debug("entity: {}", entity);
                log.debug("\tname: {}", entity.getName());
                log.debug("\tidType: {}", entity.getIdType());
                log.debug("\tbindableJavaType: {}", entity.getBindableJavaType());
                log.debug("\tjavaType: {}", entity.getJavaType());
                log.debug("\tattributes");
                final var attributes = entity.getAttributes();
                for (final var attribute : attributes) {
                    log.debug("\t\tattribute: {}", attribute);
                }
            }
        }
    }
}
