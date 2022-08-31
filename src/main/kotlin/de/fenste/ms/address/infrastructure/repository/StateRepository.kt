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

import de.fenste.ms.address.domain.model.Country
import de.fenste.ms.address.domain.model.State
import de.fenste.ms.address.infrastructure.tables.CountryTable
import de.fenste.ms.address.infrastructure.tables.StateTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.SortOrder
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class StateRepository {

    private companion object {

        private fun idOf(
            name: String,
        ): EntityID<UUID>? = State
            .find { StateTable.name eq name }
            .limit(1)
            .notForUpdate()
            .firstOrNull()
            ?.id
    }

    fun list(
        limit: Int? = null,
        offset: Long = 0L,
        vararg order: Pair<Expression<*>, SortOrder> = arrayOf(StateTable.id to SortOrder.ASC),
    ): SizedIterable<State> = when (limit) {
        null ->
            State
                .all()
                .orderBy(*order)
                .notForUpdate()

        else ->
            State
                .all()
                .orderBy(*order)
                .limit(limit, offset)
                .notForUpdate()
    }

    fun find(
        id: UUID,
    ): State? = State
        .find { StateTable.id eq id }
        .limit(1)
        .notForUpdate()
        .firstOrNull()

    fun create(
        name: String,
        countryId: String,
    ): State {
        require(idOf(name) == null) { "A state with this name does already exist." }

        val country = Country
            .find { CountryTable.id eq UUID.fromString(countryId) }
            .limit(1)
            .forUpdate()
            .firstOrNull()

        requireNotNull(country) { "The country ($countryId) does not exist." }

        return State.new {
            this.name = name
            this.country = country
        }
    }
}
