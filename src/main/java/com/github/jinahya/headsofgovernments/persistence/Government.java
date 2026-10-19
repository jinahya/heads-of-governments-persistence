package com.github.jinahya.headsofgovernments.persistence;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * An entity for the {@code government} table: one row per country, saying what that country's head of government is
 * called and how the country numbers them.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 * @see Head
 */
@NamedQuery(name = "Government.selectAll_WhereHeadKindEqual_OrderByCountryAsc",
            query = """
                    SELECT e
                    FROM Government AS e
                    WHERE e.headKind = :headKind
                    ORDER BY e.country ASC""")
@Entity(name = "Government")
@Table(
        name = "government",
        catalog = "",
        schema = ""
)
@Getter
@Slf4j
public class Government {

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

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new instance.
     */
    protected Government() {
        super();
    }

    // -----------------------------------------------------------------------------------------------------------------
    @Id
    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(
            name = "country",
            unique = false,
            nullable = false,
            insertable = true,
            updatable = true,
            length = 255
    )
    private String country;

    // -----------------------------------------------------------------------------------------------------------------
    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(
            name = "head_kind",
            unique = false,
            nullable = false,
            insertable = false,
            updatable = false,
            length = 255
    )
    private String headKind;

    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(
            name = "head_title",
            unique = false,
            nullable = false,
            insertable = false,
            updatable = false,
            length = 255
    )
    private String headTitle;

    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(
            name = "numbering",
            unique = false,
            nullable = false,
            insertable = false,
            updatable = false,
            length = 255
    )
    private String numbering;

    // -----------------------------------------------------------------------------------------------------------------
    @OneToMany(
            targetEntity = Head.class,
            cascade = {},
            fetch = FetchType.LAZY,
            mappedBy = "government",
            orphanRemoval = false
    )
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<Head> heads;
}
