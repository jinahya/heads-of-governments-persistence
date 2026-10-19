package com.github.jinahya.headsofgovernments.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Year;

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
