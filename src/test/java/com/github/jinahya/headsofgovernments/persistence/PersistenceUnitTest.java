package com.github.jinahya.headsofgovernments.persistence;

/*-
 * #%L
 * heads-of-governments-persistence
 * %%
 * Copyright (C) 2026 Jinahya, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
        try (var factory = Persistence.createEntityManagerFactory("test")) {
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
