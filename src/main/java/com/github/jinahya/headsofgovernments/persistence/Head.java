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
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * An entity for the {@code head} table: one row per term of office of a head of government. The {@code number} column
 * is the country's own ordinal and is not unique, which is why the table carries a surrogate {@code id}.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 * @see Office
 */
@NamedQuery(name = "Head.selectAll_WhereOfficeEqual_OrderByTermStartAsc",
            query = """
                    SELECT e
                    FROM Head AS e
                    WHERE e.office = :office
                    ORDER BY e.termStart ASC""")
@NamedQuery(name = "Head.selectCount",
            query = """
                    SELECT COUNT(e)
                    FROM Head AS e""")
@NamedQuery(name = "Head.selectOfficeAndCount_GroupByOffice",
            query = """
                    SELECT e.office, COUNT(e)
                    FROM Head AS e
                    GROUP BY e.office""")
@NamedQuery(name = "Head.selectOfficeAndMinMaxTermStart_GroupByOffice",
            query = """
                    SELECT e.office, MIN(e.termStart), MAX(e.termStart)
                    FROM Head AS e
                    GROUP BY e.office""")
@Entity
@Table(name = "head")
@Getter
@Slf4j
public class Head {

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

    // ---------------------------------------------------------------------------------------------
    @Id
    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    // ---------------------------------------------------------------------------------------------
    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(name = "country", nullable = false, insertable = false, updatable = false)
    @Getter(AccessLevel.PACKAGE)
    private String country;

    @jakarta.persistence.ManyToOne(fetch = FetchType.EAGER, optional = false)
    @jakarta.persistence.JoinColumn(name = "country", nullable = false, insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Office office;

    // ---------------------------------------------------------------------------------------------
    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(name = "number", nullable = false, insertable = false, updatable = false)
    private Integer number;

    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(name = "term_start", nullable = false, insertable = false, updatable = false)
    private String termStart;

    @Basic(fetch = FetchType.EAGER, optional = true)
    @Column(name = "term_end", nullable = true, insertable = false, updatable = false)
    private String termEnd;

    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(name = "name", nullable = false, insertable = false, updatable = false)
    private String name;

    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(name = "party", nullable = false, insertable = false, updatable = false)
    private String party;

    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(name = "born", nullable = false, insertable = false, updatable = false)
    private Integer born;

    @Basic(fetch = FetchType.EAGER, optional = true)
    @Column(name = "died", nullable = true, insertable = false, updatable = false)
    private Integer died;
}
