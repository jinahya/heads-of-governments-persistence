package com.github.jinahya.headsofgovernments.persistence.converter;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.time.temporal.Temporal;
import java.util.Objects;

public abstract class ParsableTemporalStringConverter<X extends Temporal>
        extends TemporalStringConverter<X> {

    protected ParsableTemporalStringConverter(final Class<X> attributeClass) {
        super();
        this.attributeClass = Objects.requireNonNull(attributeClass, "attributeClass is null");
        try {
            parse = MethodHandles.publicLookup().findStatic(
                    this.attributeClass,
                    "parse",
                    MethodType.methodType(this.attributeClass, CharSequence.class)
            );
        } catch (final ReflectiveOperationException roe) {
            throw new IllegalArgumentException(
                    "no public static " + this.attributeClass.getSimpleName() + " parse(CharSequence) in "
                    + this.attributeClass,
                    roe
            );
        }
    }

    @Override
    public String convertToDatabaseColumn(final X attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.toString();
    }

    @Override
    public X convertToEntityAttribute(final String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return attributeClass.cast(parse.invoke(dbData));
        } catch (final RuntimeException re) {
            throw re;
        } catch (final Throwable t) {
            throw new IllegalStateException("failed to parse '" + dbData + "' as " + attributeClass, t);
        }
    }

    private final Class<X> attributeClass;

    private final MethodHandle parse;
}
