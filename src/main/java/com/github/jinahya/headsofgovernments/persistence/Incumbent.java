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

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.Year;
import java.util.Objects;
import java.util.Optional;

/**
 * An entity for the {@code incumbent} view: the head of each office today, one row per country.
 * <p>
 * The view joins each {@code office} row to the {@code head} row whose {@code term_end} is {@code NULL}, so
 * {@code country} identifies a row, and is this entity's {@link Id @Id}. Every column is read-only.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 * @see Office
 * @see Head
 */
@Entity(name = "Incumbent")
@Table(name = "incumbent")
@Getter
@Slf4j
public class Incumbent {

    /**
     * Creates a new instance.
     */
    protected Incumbent() {
        super();
    }

    // ---------------------------------------------------------------------------- java.lang.Object
    @Override
    public String toString() {
        return super.toString() + '{' +
               "country=" + country +
               ",headKind=" + headKind +
               ",headTitle=" + headTitle +
               ",number=" + number +
               ",termStart=" + termStart +
               ",name=" + name +
               ",party=" + party +
               ",born=" + born +
               '}';
    }

    @Override
    public final boolean equals(final Object obj) {
        if (!(obj instanceof Incumbent that)) {
            return false;
        }
        return Objects.equals(getCountry(), that.getCountry());
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(getCountry());
    }

    // ------------------------------------------------------------------------------------- country

    // ------------------------------------------------------------------------------------ headKind

    // ----------------------------------------------------------------------------------- headTitle

    // -------------------------------------------------------------------------------------- number

    // ----------------------------------------------------------------------------------- termStart

    @Transient
    public LocalDate getTermStartAsLocalDate() {
        return Optional.ofNullable(getTermStart()).map(LocalDate::parse).orElse(null);
    }

    // ---------------------------------------------------------------------------------------- name

    // --------------------------------------------------------------------------------------- party

    // ---------------------------------------------------------------------------------------- born

    @Transient
    public Year getBornAsYear() {
        return Optional.ofNullable(getBorn()).map(Year::of).orElse(null);
    }

    // ---------------------------------------------------------------------------------------------
    @Id
    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(name = "country", insertable = false, updatable = false)
    private String country;

    // ---------------------------------------------------------------------------------------------
    @Enumerated(EnumType.STRING)
    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(name = "head_kind", insertable = false, updatable = false)
    private Office.HeadKind headKind;

    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(name = "head_title", insertable = false, updatable = false)
    private String headTitle;

    // ---------------------------------------------------------------------------------------------
    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(name = "number", insertable = false, updatable = false)
    private Integer number;

    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(name = "term_start", insertable = false, updatable = false)
    private String termStart;

    // ---------------------------------------------------------------------------------------------
    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(name = "name", insertable = false, updatable = false)
    private String name;

    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(name = "party", insertable = false, updatable = false)
    private String party;

    // ---------------------------------------------------------------------------------------------
    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(name = "born", insertable = false, updatable = false)
    private Integer born;
}

