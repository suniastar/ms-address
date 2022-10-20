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

package de.fenste.ms.address.infrastructure.repositories

import de.fenste.ms.address.domain.model.City
import de.fenste.ms.address.domain.model.Country
import de.fenste.ms.address.domain.model.State
import de.fenste.ms.address.infrastructure.tables.CityTable
import de.fenste.ms.address.infrastructure.tables.CountryTable
import de.fenste.ms.address.infrastructure.tables.StateTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class CityRepository {

    private companion object {

        private fun idOf(
            name: String,
            countryId: UUID,
            stateId: UUID?,
        ): EntityID<UUID>? = when (stateId) {
            null ->
                CityTable
                    .slice(CityTable.id)
                    .select { (CityTable.name eq name) and (CityTable.country eq countryId) }
                    .limit(1)
                    .notForUpdate()
                    .firstOrNull()
                    ?.let { r -> r[CityTable.id] }

            else ->
                CityTable
                    .slice(CityTable.id)
                    .select {
                        (CityTable.name eq name) and (CityTable.country eq countryId) and (CityTable.state eq stateId)
                    }
                    .limit(1)
                    .notForUpdate()
                    .firstOrNull()
                    ?.let { r -> r[CityTable.id] }
        }

        private fun findUpdatedCountryAndState(
            city: City,
            countryId: UUID?,
            stateId: UUID?,
            removeState: Boolean,
        ): Pair<Country, State?> {
            val country = countryId?.let {
                val country = Country
                    .find { CountryTable.id eq countryId }
                    .limit(1)
                    .notForUpdate()
                    .firstOrNull()

                requireNotNull(country) { "The country ($countryId) does not exist." }
            } ?: city.country
            val state = stateId?.let {
                val s = State
                    .find { StateTable.id eq stateId }
                    .limit(1)
                    .notForUpdate()
                    .firstOrNull()

                requireNotNull(s) { "The state ($stateId) does not exist." }
            } ?: city.state
            if (!removeState) {
                require(
                    state?.let { country.states.contains(state) } != false,
                ) { "The state (${state?.id?.value}) does not belong to the country (${country.id.value})." }
            }
            return country to state
        }
    }

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

    fun create(
        name: String,
        countryId: UUID,
        stateId: UUID?,
    ): City {
        require(
            idOf(
                name = name,
                countryId = countryId,
                stateId = stateId,
            ) == null,
        ) { "A city with this name and parent country and state does already exist." }

        val country = Country
            .find { CountryTable.id eq countryId }
            .limit(1)
            .notForUpdate()
            .firstOrNull()

        requireNotNull(country) { "The country ($countryId) does not exist." }

        val state = stateId?.let {
            val s = State
                .find { StateTable.id eq stateId }
                .limit(1)
                .notForUpdate()
                .firstOrNull()

            requireNotNull(s) { "The state ($stateId) does not exist." }
            require(country.states.contains(s)) { "The state ($stateId) does not belong to the country ($countryId)." }

            s
        }

        return City.new {
            this.name = name
            this.country = country
            this.state = state
        }
    }

    fun update(
        id: UUID,
        name: String? = null,
        countryId: UUID? = null,
        stateId: UUID? = null,
        removeState: Boolean = false,
    ): City {
        val city = City
            .find { CityTable.id eq id }
            .limit(1)
            .forUpdate()
            .firstOrNull()

        requireNotNull(city) { "The city ($id) does not exist." }

        val uId = idOf(
            name = name ?: city.name,
            countryId = countryId ?: city.country.id.value,
            stateId = if (!removeState) stateId ?: city.state?.id?.value else null,
        )
        require(
            uId == null || uId == city.id,
        ) { "A city with this name and parent country and state does already exist." }

        val (country, state) = findUpdatedCountryAndState(
            city = city,
            countryId = countryId,
            stateId = stateId,
            removeState = removeState,
        )

        name?.let { city.name = name }
        countryId?.let { city.country = country }
        stateId?.let { city.state = state }
        if (removeState) city.state = null

        return city
    }

    fun delete(
        id: UUID,
    ) {
        val city = City
            .find { CityTable.id eq id }
            .limit(1)
            .forUpdate()
            .firstOrNull()

        requireNotNull(city) { "The city ($id) does not exist. " }

        city.delete()
    }
}
