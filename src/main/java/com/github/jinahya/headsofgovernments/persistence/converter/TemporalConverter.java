package com.github.jinahya.headsofgovernments.persistence.converter;

import jakarta.persistence.AttributeConverter;

import java.time.temporal.Temporal;

public abstract class TemporalConverter<X extends Temporal, Y>
        implements AttributeConverter<X, Y> {

}
