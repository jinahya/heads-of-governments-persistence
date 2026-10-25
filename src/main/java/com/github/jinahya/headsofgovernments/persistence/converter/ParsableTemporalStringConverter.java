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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.time.temporal.Temporal;
import java.util.Objects;

/**
 * An attribute converter between {@link Temporal} attributes and their text, read back through the attribute
 * class's {@code public static parse(CharSequence)} method.
 *
 * @param <X> the attribute type.
 */
public abstract class ParsableTemporalStringConverter<X extends Temporal>
        extends TemporalStringConverter<X> {

    /**
     * Creates a new instance for the specified attribute class.
     *
     * @param attributeClass the attribute class, which declares a {@code public static parse(CharSequence)}.
     */
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
