package com.github.jinahya.headsofgovernments.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class YearConverter
        implements AttributeConverter<Year, String> {

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
