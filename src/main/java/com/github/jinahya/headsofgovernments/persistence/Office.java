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
import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

/**
 * An entity for the {@code office} table: one row per country, saying what that country's head of
 * government is called and how the country numbers them.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 * @see Head
 */
@NamedQuery(name = "Office.selectAllByHeadKind",
            query = """
                    SELECT e
                    FROM Office AS e
                    WHERE e.headKind = :headKind
                    ORDER BY e.country ASC""")
@NamedQuery(name = "Office.selectAll",
            query = """
                    SELECT e
                    FROM Office AS e
                    ORDER BY e.country ASC""")
@Entity(name = "Office")
@Table(name = "office",
       check = @CheckConstraint(constraint = "head_kind IN ('PRESIDENT', 'PRIME_MINISTER')"))
@Getter
@Slf4j
public class Office {

    /**
     * Creates a new instance.
     */
    protected Office() {
        super();
    }

    /**
     * Constants for the {@code head_kind} column: what kind of office the head of a government
     * holds. The {@code headKind} attribute is mapped with this enum by name, and its constants
     * are the values the database allows.
     */
    public enum HeadKind {

        /**
         * The head of government is a president.
         */
        PRESIDENT,

        /**
         * The head of government is a prime minister.
         */
        PRIME_MINISTER
    }

    // ---------------------------------------------------------------------------- java.lang.Object
    @Override
    public String toString() {
        return super.toString() + '{' +
               "country=" + country +
               ",headKind=" + headKind +
               ",headTitle=" + headTitle +
               ",numbering=" + numbering +
               '}';
    }

    @Override
    public final boolean equals(final Object obj) {
        if (!(obj instanceof Office that)) {
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

    // ----------------------------------------------------------------------------------- numbering

    // --------------------------------------------------------------------------------------- heads

    // ---------------------------------------------------------------------------------------------
    @jakarta.persistence.Id
    @jakarta.persistence.Basic(fetch = FetchType.EAGER, optional = false)
    @jakarta.persistence.Column(name = "country", nullable = false, insertable = false,
                                updatable = false)
    private String country;

    // ---------------------------------------------------------------------------------------------
    @Enumerated(EnumType.STRING)
    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(name = "head_kind", nullable = false, insertable = false, updatable = false)
    private HeadKind headKind;

    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(name = "head_title", nullable = false, insertable = false, updatable = false)
    private String headTitle;

    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(name = "numbering", nullable = false, insertable = false, updatable = false)
    private String numbering;

    // ---------------------------------------------------------------------------------------------
    @OneToMany(targetEntity = Head.class, fetch = FetchType.LAZY, mappedBy = "office")
    @Setter(AccessLevel.PACKAGE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<Head> heads;
}
