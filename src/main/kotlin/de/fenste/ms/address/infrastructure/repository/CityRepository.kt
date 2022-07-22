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

import de.fenste.ms.address.domain.model.City
import de.fenste.ms.address.infrastructure.tables.CityTable
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.SortOrder
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class CityRepository {

    fun list(
        limit: Int? = null,
        offset: Long = 0L,
        vararg order: Pair<Expression<*>, SortOrder> = arrayOf(CityTable.id to SortOrder.ASC),
    ): SizedIterable<City> = when {
        limit != null ->
            City
                .all()
                .orderBy(*order)
                .limit(limit, offset)
                .notForUpdate()
        else ->
            City
                .all()
                .orderBy(*order)
                .notForUpdate()
    }

    fun find(
        id: UUID,
    ): City? = City
        .find { CityTable.id eq id }
        .limit(1)
        .notForUpdate()
        .firstOrNull()
}
