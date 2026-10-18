package com.github.jinahya.headsofgovernments.persistence;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
class PersistenceUnitTest {

    /**
     * Re-encodes every console appender on the root logger to print the message only.
     */
    @BeforeAll
    static void messageOnly() {
        LogbackUtils.messageOnly();
//        final var context = (LoggerContext) LoggerFactory.getILoggerFactory();
//        final var root = context.getLogger(Logger.ROOT_LOGGER_NAME);
//        final var encoder = new PatternLayoutEncoder();
//        encoder.setContext(context);
//        encoder.setPattern("%msg%n");
//        encoder.start();
//        for (final var i = root.iteratorForAppenders(); i.hasNext(); ) {
//            if (i.next() instanceof OutputStreamAppender<ILoggingEvent> appender) {
//                appender.setEncoder(encoder);
//            }
//        }
    }

    @Test
    void __() {
        PersistenceTestUtils.applyEntityManagerFactory(emf -> {
            log.debug("properties");
            emf.getProperties().forEach((k, v) -> {
                log.debug("\tproperty: {}: {}", k, v);
            });
            final var metamodel = emf.getMetamodel();
            log.debug("entities");
            final var entities = metamodel.getEntities();
            for (final var entity : entities) {
                log.debug("entity: {}", entity);
                log.debug("\tname: {}", entity.getName());
                log.debug("\tidType: {}", entity.getIdType());
                log.debug("\tbindableJavaType: {}", entity.getBindableJavaType());
                log.debug("\tjavaType: {}", entity.getJavaType());
                log.debug("\tattributes");
                final var attributes = entity.getAttributes();
                for (final var attribute : attributes) {
                    log.debug("\t\tattribute: {}", attribute);
                }
            }
            return null;
        });
    }
}
