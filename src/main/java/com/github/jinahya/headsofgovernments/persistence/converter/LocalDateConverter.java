package com.github.jinahya.headsofgovernments.persistence.converter;

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

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Converter(autoApply = false)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class LocalDateConverter
        implements AttributeConverter<LocalDate, String> {

    @Override
    public String convertToDatabaseColumn(final LocalDate attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.toString();
    }

    @Override
    public LocalDate convertToEntityAttribute(final String dbData) {
        if (dbData == null) {
            return null;
        }
        return LocalDate.parse(dbData);
    }
}
