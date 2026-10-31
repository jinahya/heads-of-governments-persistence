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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
class Office_Test {

    @DisplayName("toString")
    @Nested
    class ToStringTest {

        @Test
        void _NotBlank_() {
            final var string = new Office().toString();
            log.debug("string: {}", string);
            assertThat(string).isNotBlank();
        }
    }

    @DisplayName("equals/hashCode")
    @Nested
    class EqualsTest {

        private static Head of(final Long id) throws ReflectiveOperationException {
            final var head = new Head();
            final var field = Head.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(head, id);
            return head;
        }

        @Test
        void __() throws ReflectiveOperationException {
            EqualsVerifier.forClass(Office.class)
                    .suppress(Warning.SURROGATE_KEY)
                    .withPrefabValues(Head.class, of(1L), of(2L))
                    .verify();
        }
    }
}
