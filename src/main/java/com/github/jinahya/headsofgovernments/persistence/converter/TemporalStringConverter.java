package com.github.jinahya.headsofgovernments.persistence.converter;

import java.time.temporal.Temporal;

public abstract class TemporalStringConverter<X extends Temporal>
        extends TemporalConverter<X, String> {

}
