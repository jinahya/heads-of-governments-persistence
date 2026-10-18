package com.github.jinahya.headsofgovernments.persistence;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import org.slf4j.LoggerFactory;

final class LogbackUtils {

    static void messageOnly() {
        final var context = (LoggerContext) LoggerFactory.getILoggerFactory();
        final var root = context.getLogger(Logger.ROOT_LOGGER_NAME);
        final var encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%msg%n");
        encoder.start();
        for (final var i = root.iteratorForAppenders(); i.hasNext(); ) {
            if (i.next() instanceof OutputStreamAppender<ILoggingEvent> appender) {
                appender.setEncoder(encoder);
            }
        }
    }

    private LogbackUtils() {
        throw new AssertionError("instantiation is not allowed");
    }
}
