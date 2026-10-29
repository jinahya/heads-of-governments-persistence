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

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.Year;
import java.util.Objects;
import java.util.Optional;

/**
 * An entity for the {@code head} table: one row per term of office of a head of government. The
 * {@code number} column is the country's own ordinal and is not unique, which is why the table
 * carries a surrogate {@code id}.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 * @see Office
 */
@NamedQuery(name = "Head.selectAllByOfficeAndDiedNullOrLessThan",
            query = """
                    SELECT e
                    FROM Head AS e
                    WHERE e.office = :office
                      AND (e.died IS NULL OR e.died < :diedMaxExclusive)
                    ORDER BY e.died DESC NULLS FIRST""")
@NamedQuery(name = "Head.selectAllByOfficeAndBornGreaterThanOrEqualTo",
            query = """
                    SELECT e
                    FROM Head AS e
                    WHERE e.office = :office
                      AND e.born >= :bornMinInclusive
                    ORDER BY e.born ASC""")
@NamedQuery(name = "Head.selectAllByOfficeAndTermEndNullOrLessThan",
            query = """
                    SELECT e
                    FROM Head AS e
                    WHERE e.office = :office
                      AND (e.termEnd IS NULL OR e.termEnd < :termEndMaxExclusive)
                    ORDER BY e.termEnd DESC NULLS FIRST""")
@NamedQuery(name = "Head.selectAllByOfficeAndTermStartGreaterThanOrEqualTo",
            query = """
                    SELECT e
                    FROM Head AS e
                    WHERE e.office = :office
                      AND e.termStart >= :termStartMinInclusive
                    ORDER BY e.termStart ASC""")
@NamedQuery(name = "Head.selectAllByOfficeOrderByTermStartDesc",
            query = """
                    SELECT e
                    FROM Head AS e
                    WHERE e.office = :office
                    ORDER BY e.termStart DESC""")
@NamedQuery(name = "Head.selectAllByOfficeOrderByNumberAsc",
            query = """
                    SELECT e
                    FROM Head AS e
                    WHERE e.office = :office
                    ORDER BY e.number ASC""")
@Entity(name = "Head")
@Table(name = "head")
@Getter
@Slf4j
public class Head {

    /**
     * An attribute converter between {@link LocalDate} attributes and their ISO-8601 text.
     */
    @Converter(autoApply = false)
    public static class LocalDateConverter
            implements AttributeConverter<LocalDate, String> {

        /**
         * Creates a new instance.
         */
        public LocalDateConverter() {
            super();
        }

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

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new instance.
     */
    protected Head() {
        super();
    }

    // ---------------------------------------------------------------------------- java.lang.Object
    @Override
    public String toString() {
        return super.toString() + '{' +
               "id=" + id +
               ",country=" + country +
               ",number=" + number +
               ",termStart=" + termStart +
               ",termEnd=" + termEnd +
               ",name=" + name +
               ",party=" + party +
               ",born=" + born +
               ",died=" + died +
               '}';
    }

    @Override
    public final boolean equals(final Object obj) {
        if (!(obj instanceof Head that)) {
            return false;
        }
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(getId());
    }

    // ------------------------------------------------------------------------------------------ id

    // ------------------------------------------------------------------------------------- country

    // -------------------------------------------------------------------------------------- office

    // -------------------------------------------------------------------------------------- number

    // ----------------------------------------------------------------------------------- termStart
    /**
     * Returns the value of {@code termStart} attribute, parsed as a {@link LocalDate}. The
     * attribute is mapped as the ISO-8601 text the {@code term_start} column holds, with no
     * converter, so that a query can compare it as text; this method is the typed view of it, and
     * is not mapped.
     *
     * @return the value of {@code termStart} attribute as a {@link LocalDate}; {@code null} when
     * the attribute is {@code null}, as on a new instance.
     * @see LocalDate#parse(CharSequence)
     */
    @Transient
    public LocalDate getTermStartAsLocalDate() {
        return Optional.ofNullable(getTermStart()).map(LocalDate::parse).orElse(null);
    }

    // ------------------------------------------------------------------------------------- termEnd

    // ---------------------------------------------------------------------------------------- name

    // --------------------------------------------------------------------------------------- party

    // ---------------------------------------------------------------------------------------- born
    /**
     * Returns the value of {@code born} attribute as a {@link Year}. The attribute is mapped as the
     * integer the {@code born} column holds, so that a query can do arithmetic on it; this method
     * is the typed view of it, and is not mapped.
     *
     * @return the value of {@code born} attribute as a {@link Year}; {@code null} when the
     * attribute is {@code null}, as on a new instance.
     * @see Year#of(int)
     */
    @Transient
    public Year getBornAsYear() {
        return Optional.ofNullable(getBorn()).map(Year::of).orElse(null);
    }

    // ---------------------------------------------------------------------------------------- died

    // ---------------------------------------------------------------------------------------------
    @Id
    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(name = "id", nullable = false, insertable = false, updatable = false)
    private Long id;

    // ---------------------------------------------------------------------------------------------
    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(name = "country", nullable = false, insertable = false, updatable = false)
    @Getter(AccessLevel.PACKAGE)
    private String country;

    @jakarta.persistence.ManyToOne(fetch = FetchType.EAGER, optional = false)
    @jakarta.persistence.JoinColumn(name = "country", nullable = false, insertable = false,
                                    updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Office office;

    // ---------------------------------------------------------------------------------------------
    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(name = "number", nullable = false, insertable = false, updatable = false)
    private Integer number;

    // ---------------------------------------------------------------------------------------------
    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(name = "term_start", nullable = false, insertable = false, updatable = false)
    private String termStart;

    @Convert(converter = LocalDateConverter.class)
    @Basic(fetch = FetchType.EAGER, optional = true)
    @Column(name = "term_end", nullable = true, insertable = false, updatable = false)
    private LocalDate termEnd;

    // ---------------------------------------------------------------------------------------------
    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(name = "name", nullable = false, insertable = false, updatable = false)
    private String name;

    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(name = "party", nullable = false, insertable = false, updatable = false)
    private String party;

    // ---------------------------------------------------------------------------------------------
    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(name = "born", nullable = false, insertable = false, updatable = false)
    private Integer born;

    @Basic(fetch = FetchType.EAGER, optional = true)
    @Column(name = "died", nullable = true, insertable = false, updatable = false)
    private Year died;
}
