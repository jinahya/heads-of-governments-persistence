package com.github.jinahya.headsofgovernments.persistence;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Locale;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
class GovernmentTest {

    @DisplayName("EntityManager#find(entityClass, primaryKey)")
    @Nested
    class FindTest {

        @ValueSource(strings = {
                "FR",
                "GB",
                "JP",
                "KR",
                "US"
        })
        @ParameterizedTest
        void __1(final String country) {
            final var found = PersistenceTestUtils.applyEntityManager(em -> {
                return em.find(Government.class, country);
            });
            assertThat(found)
                    .isNotNull()
                    .extracting(Government::getCountry)
                    .isEqualTo(country);
        }

        private static Stream<String> countries() {
            return Locale.availableLocales()
                    .map(Locale::getCountry)
                    .distinct();
        }

        @MethodSource({"countries"})
        @ParameterizedTest
        void __2(final String country) {
            final var found = PersistenceTestUtils.applyEntityManager(em -> {
                return em.find(Government.class, country);
            });
            if (found == null) {
                log.debug("no government found for {}", country);
                return;
            }
            assertThat(found)
                    .extracting(Government::getCountry)
                    .isEqualTo(country);
        }
    }
}
