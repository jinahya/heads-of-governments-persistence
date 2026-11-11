package com.github.jinahya.headsofgovernments.persistence;

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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sqlite.SQLiteDataSource;

import java.sql.DriverManager;

/**
 * Reads the database with plain JDBC, before any mapping; the book's "R for Relational" quotes this class.
 */
class Jdbc_Test {

    static final String URL = "jdbc:sqlite:src/main/resources/heads_of_governments.sqlite3";

    static final String DRIVER = "org.sqlite.JDBC";

    static {
        try {
            Class.forName(DRIVER);
        } catch (final ClassNotFoundException cnfe) {
            throw new ExceptionInInitializerError(cnfe);
        }
    }

    @DisplayName("The offices, through DriverManager")
    @Test
    void offices() throws Exception {
        final var sql = """
                SELECT country, head_kind, head_title
                FROM office
                ORDER BY country ASC""";
        try (var connection = DriverManager.getConnection(URL);
             var statement = connection.createStatement();
             var results = statement.executeQuery(sql)) {
            while (results.next()) {
                final var country = results.getString("country");
                final var headKind = results.getString("head_kind");
                final var headTitle = results.getString("head_title");
            }
        }
    }

    @DisplayName("One country's heads, through a DataSource")
    @ParameterizedTest(name = "[{index}] {0}")
    @ValueSource(strings = {"FR", "GB", "JP", "KR", "US"})
    void heads(final String country) throws Exception {
        final var sql = """
                SELECT *
                FROM head
                WHERE country = ?
                ORDER BY term_start ASC""";
        final var dataSource = new SQLiteDataSource();
        dataSource.setUrl(URL);
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, country);
            try (var results = statement.executeQuery()) {
                while (results.next()) {
                    final var number = results.getInt("number");
                    final var termStart = results.getString("term_start");
                    final var termEnd = results.getString("term_end");
                    final var name = results.getString("name");
                    final var party = results.getString("party");
                    final var born = results.getInt("born");
                    var died = (Integer) results.getInt("died");
                    if (results.wasNull()) {
                        died = null;
                    }
                }
            }
        }
    }
}
