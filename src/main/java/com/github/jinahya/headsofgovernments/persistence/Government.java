package com.github.jinahya.headsofgovernments.persistence;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.List;

@Entity(name = "Government")
@Table(
        name = "government",
        catalog = "",
        schema = ""
)
public class Government {

    public enum HeadKind {
        PRESIDENT,
        PRIME_MINISTER
    }

    // -----------------------------------------------------------------------------------------------------------------
    protected Government() {
        super();
    }

    // -----------------------------------------------------------------------------------------------------------------

    // -----------------------------------------------------------------------------------------------------------------
    public String getCountry() {
        return country;
    }

    public void setCountry(final String country) {
        this.country = country;
    }

    // -----------------------------------------------------------------------------------------------------------------
    public String getHeadKind() {
        return headKind;
    }

    public void setHeadKind(final String headKind) {
        this.headKind = headKind;
    }

    // -----------------------------------------------------------------------------------------------------------------
    public String getHeadTitle() {
        return headTitle;
    }

    public void setHeadTitle(final String headTitle) {
        this.headTitle = headTitle;
    }

    // -----------------------------------------------------------------------------------------------------------------
    public String getNumbering() {
        return numbering;
    }

    public void setNumbering(final String numbering) {
        this.numbering = numbering;
    }

    // -----------------------------------------------------------------------------------------------------------------
    public List<Head> getHeads() {
        return heads;
    }

    public void setHeads(final List<Head> heads) {
        this.heads = heads;
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
    private List<Head> heads;
}
