package com.github.jinahya.headsofgovernments.persistence;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

/**
 * An entity for the {@code office} table: one row per country, saying what that country's head of government is called
 * and how the country numbers them.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 * @see Head
 */
@NamedQuery(name = "Office.selectAll_WhereHeadKindEqual_OrderByCountryAsc",
            query = """
                    SELECT e
                    FROM Office AS e
                    WHERE e.headKind = :headKind
                    ORDER BY e.country ASC""")
@NamedQuery(name = "Office.selectAllAndCountOfHeads_GroupByAll",
            query = """
                    SELECT e, COUNT(h)
                    FROM Office AS e JOIN e.heads AS h
                    GROUP BY e""")
@NamedQuery(name = "Office.selectAllAndMinMaxTermStartOfHeads_GroupByAll",
            query = """
                    SELECT e, MIN(h.termStart), MAX(h.termStart)
                    FROM Office AS e JOIN e.heads AS h
                    GROUP BY e""")
@NamedQuery(name = "Office.selectAllAndAvgLifespanOfHeads_WhereDiedIsNotNull_GroupByAll",
            query = """
                    SELECT e, AVG(h.died) - AVG(h.born)
                    FROM Office AS e JOIN e.heads AS h
                    WHERE h.died IS NOT NULL
                    GROUP BY e""")
@Entity
@Table(name = "office")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class Office {

    /**
     * Constants for the {@code head_kind} column: what kind of office the head of a government holds. The column is
     * mapped as a {@link String}; this enum lists the values the database allows.
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

    // ------------------------------------------------------------------------------------------------ java.lang.Object
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

    // -----------------------------------------------------------------------------------------------------------------
    @jakarta.persistence.Id
    @jakarta.persistence.Basic(fetch = FetchType.EAGER, optional = false)
    @jakarta.persistence.Column(name = "country", nullable = false, updatable = false)
    private String country;

    // -----------------------------------------------------------------------------------------------------------------
    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(name = "head_kind", nullable = false, insertable = false, updatable = false)
    private String headKind;

    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(name = "head_title", nullable = false, insertable = false, updatable = false)
    private String headTitle;

    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(name = "numbering", nullable = false, insertable = false, updatable = false)
    private String numbering;

    // -----------------------------------------------------------------------------------------------------------------
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "office")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<Head> heads;
}
