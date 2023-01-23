/*
 * Copyright (c) 2023 Frederik Enste <frederik@fenste.de>.
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

package de.fenste.ms.address.infrastructure.repositories

import de.fenste.ms.address.domain.model.Country
import de.fenste.ms.address.domain.model.State
import de.fenste.ms.address.infrastructure.tables.CountryTable
import de.fenste.ms.address.infrastructure.tables.StateTable
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class StateRepository {

    private companion object {

        private fun checkDuplicate(
            original: State? = null,
            name: String,
            country: Country,
        ) {
            val state = StateTable
                .slice(StateTable.columns)
                .select { (StateTable.name eq name) and (StateTable.country eq country.id) }
                .apply { original?.let { andWhere { StateTable.id neq original.id } } }
                .limit(1)
                .notForUpdate()
                .firstOrNull()
                ?.let { r -> State.wrapRow(r) }

            require(state == null) { "This State does already exist: $state" }
        }
    }

    fun count(): Long = State
        .all()
        .count()

    fun list(
        page: Int? = null,
        size: Int? = null,
        vararg order: Pair<Expression<*>, SortOrder> = emptyArray(),
    ): SizedIterable<State> = when (size) {
        null ->
            State
                .all()
                .orderBy(*order, StateTable.id to SortOrder.ASC)
                .notForUpdate()

        else ->
            State
                .all()
                .orderBy(*order, StateTable.id to SortOrder.ASC)
                .limit(size, (page ?: 0).toLong() * size)
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
        countryId: UUID,
    ): State {
        val country = Country
            .find { CountryTable.id eq countryId }
            .limit(1)
            .notForUpdate()
            .firstOrNull()

        requireNotNull(country) { "The country ($countryId) does not exist." }

        checkDuplicate(
            name = name,
            country = country,
        )

        return State.new {
            this.name = name
            this.country = country
        }
    }

    fun update(
        id: UUID,
        name: String,
        countryId: UUID,
    ): State {
        val state = State
            .find { StateTable.id eq id }
            .limit(1)
            .forUpdate()
            .firstOrNull()

        requireNotNull(state) { "The state ($id) does not exist." }

        val country = Country
            .find { CountryTable.id eq countryId }
            .limit(1)
            .notForUpdate()
            .firstOrNull()

        requireNotNull(country) { "The country ($countryId) does not exist." }

        checkDuplicate(
            original = state,
            name = name,
            country = country,
        )

        state.name = name
        state.country = country
        return state
    }

    fun delete(
        id: UUID,
    ) {
        val state = State
            .find { StateTable.id eq id }
            .limit(1)
            .forUpdate()
            .firstOrNull()

        requireNotNull(state) { "The state ($id) does not exist." }

        state.delete()
    }
}
