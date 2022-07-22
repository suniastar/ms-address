/*
 * Copyright (c) 2022 Frederik Enste <frederik@fenste.de>.
 *
 * Licensed under the GNU General Public License, Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.fenste.ms.address.infrastructure.repository

import de.fenste.ms.address.domain.model.Street
import de.fenste.ms.address.infrastructure.tables.StreetTable
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.SortOrder
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class StreetRepository {

    fun list(
        limit: Int? = null,
        offset: Long = 0L,
        vararg order: Pair<Expression<*>, SortOrder> = arrayOf(StreetTable.id to SortOrder.ASC)
    ): SizedIterable<Street> = when {
        limit != null -> Street
            .all()
            .orderBy(*order)
            .limit(limit, offset)
            .notForUpdate()
        else -> Street
            .all()
            .orderBy(*order)
            .notForUpdate()
    }

    fun find(
        id: UUID,
    ): Street? = Street
        .find { StreetTable.id eq id }
        .limit(1)
        .notForUpdate()
        .firstOrNull()
}
