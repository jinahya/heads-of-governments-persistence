package com.github.jinahya.headsofgovernments.persistence;

import jakarta.persistence.Persistence;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
class PersistenceContextTest {

    @Test
    void find_sameInstance_sameIdentifier() {
        try (var factory = Persistence.createEntityManagerFactory("heads-of-governments");
             var manager = factory.createEntityManager()) {
            final var office1 = manager.find(Office.class, "KR");
            final var office2 = manager.find(Office.class, "KR");
            assertThat(office2).isSameAs(office1);
        }
    }
}
