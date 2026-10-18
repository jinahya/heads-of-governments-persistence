package com.github.jinahya.headsofgovernments.persistence;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@NamedQuery(name = "Head.selectAll_WhereGovenmentEqual_OrderByTermStartAsc",
            query = """
                    SELECT e
                    FROM Government AS e
                    WHERE e.headKind = :headKind""")
@Entity(name = "Head")
@Table(
        name = "head",
        catalog = "",
        schema = ""
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class Head {

    // -----------------------------------------------------------------------------------------------------------------
    @Id
    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(
            name = "id",
            unique = false,
            nullable = false,
            insertable = true,
            updatable = true,
            length = 255
    )
    private Integer id;

    // -----------------------------------------------------------------------------------------------------------------
    @ManyToOne(
            targetEntity = Government.class,
            cascade = {},
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "country",
            referencedColumnName = "country",
            unique = false,
            nullable = false,
            insertable = false,
            updatable = false
    )
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Government government;

    // -----------------------------------------------------------------------------------------------------------------
    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(
            name = "number",
            unique = false,
            nullable = false,
            insertable = false,
            updatable = false,
            length = 255
    )
    private Integer number;

    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(
            name = "term_start",
            unique = false,
            nullable = false,
            insertable = false,
            updatable = false,
            length = 255
    )
    private String termStart;

    @Basic(fetch = FetchType.EAGER, optional = true)
    @Column(
            name = "term_end",
            unique = false,
            nullable = true,
            insertable = false,
            updatable = false,
            length = 255
    )
    private String termEnd;

    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(
            name = "name",
            unique = false,
            nullable = false,
            insertable = false,
            updatable = false,
            length = 255
    )
    private String name;

    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(
            name = "party",
            unique = false,
            nullable = false,
            insertable = false,
            updatable = false,
            length = 255
    )
    private String party;

    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(
            name = "born",
            unique = false,
            nullable = false,
            insertable = false,
            updatable = false,
            length = 255
    )
    private Integer born;

    @Basic(fetch = FetchType.EAGER, optional = true)
    @Column(
            name = "died",
            unique = false,
            nullable = true,
            insertable = false,
            updatable = false,
            length = 255
    )
    private Integer died;
}
