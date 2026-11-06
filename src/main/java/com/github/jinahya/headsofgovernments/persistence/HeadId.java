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

import java.io.Serializable;

/**
 * An id class for the {@code head} table, whose primary key is {@code (country, term_start)}: a
 * country starts at most one term of office on a given day.
 * <p>
 * Jakarta Persistence 3.2 allows an id class to be a record, which needs no constructor with no
 * parameters, and whose generated {@code equals} and {@code hashCode} give the value equality an id
 * class must define. Its components match the {@code @Id} attributes of {@link Head} by name and
 * type. The specification does not require an id class to be serializable; the providers'
 * documentation does, and so this one is. The database is read-only, so an instance is only ever
 * built to look a head up.
 *
 * @param country   the value of the {@code country} column.
 * @param termStart the value of the {@code term_start} column.
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 * @see Head
 */
public record HeadId(String country, String termStart)
        implements Serializable {
}
