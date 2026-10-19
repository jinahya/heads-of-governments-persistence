package com.github.jinahya.headsofgovernments.persistence.converter;

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
