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

import java.time.Year;

/**
 * An attribute converter between {@link Year} and its {@code String} form, such as {@code "1808"}.
 *
 * @deprecated Not needed for this database: {@code born} and {@code died} are {@code INTEGER} columns, and since
 * Jakarta Persistence 3.2 a provider maps {@link Year} to {@code INTEGER} without a converter. Kept for a year stored
 * as {@code TEXT}.
 */
@Deprecated
@Converter(autoApply = false)
public class YearConverter
        implements AttributeConverter<Year, String> {

    /**
     * Creates a new instance.
     */
    public YearConverter() {
        super();
    }

    @Override
    public String convertToDatabaseColumn(final Year attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.toString();
    }

    @Override
    public Year convertToEntityAttribute(final String dbData) {
        if (dbData == null) {
            return null;
        }
        return Year.parse(dbData);
    }
}
