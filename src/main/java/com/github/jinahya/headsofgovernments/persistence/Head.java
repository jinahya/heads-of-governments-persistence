package com.github.jinahya.headsofgovernments.persistence;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity(name = "Head")
@Table(
        name = "head",
        catalog = "",
        schema = ""
)
public class Head {

    // -----------------------------------------------------------------------------------------------------------------
    protected Head() {
        super();
    }

    // -----------------------------------------------------------------------------------------------------------------
    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public Government getGovernment() {
        return government;
    }

    public void setGovernment(final Government government) {
        this.government = government;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(final Integer number) {
        this.number = number;
    }

    public String getTermStart() {
        return termStart;
    }

    public void setTermStart(final String termStart) {
        this.termStart = termStart;
    }

    public String getTermEnd() {
        return termEnd;
    }

    public void setTermEnd(final String termEnd) {
        this.termEnd = termEnd;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getParty() {
        return party;
    }

    public void setParty(final String party) {
        this.party = party;
    }

    public Integer getBorn() {
        return born;
    }

    public void setBorn(final Integer born) {
        this.born = born;
    }

    public Integer getDied() {
        return died;
    }

    public void setDied(final Integer died) {
        this.died = died;
    }
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
