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

import lombok.Getter;

import java.time.LocalDate;
import java.util.Optional;

/**
 * A class for the {@code contemporary} view: two terms in different countries that overlapped, one row per pair.
 * <p>
 * No column, and no combination of columns, identifies a row of this view, so this is not an
 * {@link jakarta.persistence.Entity @Entity}: it holds a row which a native query, or a row mapper, reads. It has
 * no {@code equals}/{@code hashCode} of its own and keeps identity equality.
 * <p>
 * It has two constructors: a {@code protected} no-arg one, and a {@code public} one taking every column in the
 * order the view declares them, which a name-based row mapper finds as the class's single public constructor.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 * @see Head
 */
@Getter
public class Contemporary {

    /**
     * Creates a new empty instance.
     */
    protected Contemporary() {
        super();
    }

    /**
     * Creates a new instance of a row.
     *
     * @param countryLeft  the value of the {@code country_left} column.
     * @param nameLeft     the value of the {@code name_left} column.
     * @param countryRight the value of the {@code country_right} column.
     * @param nameRight    the value of the {@code name_right} column.
     * @param overlapStart the value of the {@code overlap_start} column.
     * @param overlapEnd   the value of the {@code overlap_end} column.
     */
    public Contemporary(final String countryLeft, final String nameLeft,
                        final String countryRight, final String nameRight,
                        final String overlapStart, final String overlapEnd) {
        this();
        this.countryLeft = countryLeft;
        this.nameLeft = nameLeft;
        this.countryRight = countryRight;
        this.nameRight = nameRight;
        this.overlapStart = overlapStart;
        this.overlapEnd = overlapEnd;
    }

    // ---------------------------------------------------------------------------- java.lang.Object
    @Override
    public String toString() {
        return super.toString() + '{' +
               "countryLeft=" + countryLeft +
               ",nameLeft=" + nameLeft +
               ",countryRight=" + countryRight +
               ",nameRight=" + nameRight +
               ",overlapStart=" + overlapStart +
               ",overlapEnd=" + overlapEnd +
               '}';
    }

    // --------------------------------------------------------------------------------- countryLeft

    // ------------------------------------------------------------------------------------ nameLeft

    // -------------------------------------------------------------------------------- countryRight

    // ----------------------------------------------------------------------------------- nameRight

    // -------------------------------------------------------------------------------- overlapStart

    public LocalDate getOverlapStartAsLocalDate() {
        return Optional.ofNullable(getOverlapStart()).map(LocalDate::parse).orElse(null);
    }

    // ---------------------------------------------------------------------------------- overlapEnd

    public LocalDate getOverlapEndAsLocalDate() {
        return Optional.ofNullable(getOverlapEnd()).map(LocalDate::parse).orElse(null);
    }

    // ---------------------------------------------------------------------------------------------
    private String countryLeft;

    private String nameLeft;

    private String countryRight;

    private String nameRight;

    // ---------------------------------------------------------------------------------------------
    private String overlapStart;

    private String overlapEnd;
}

