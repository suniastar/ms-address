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

import de.fenste.ms.address.domain.model.City
import de.fenste.ms.address.domain.model.Country
import de.fenste.ms.address.domain.model.State
import de.fenste.ms.address.infrastructure.tables.CityTable
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
class CityRepository {

    private companion object {

        private fun checkDuplicate(
            original: City? = null,
            name: String,
            country: Country,
            state: State?,
        ) {
            val city = CityTable
                .slice(CityTable.columns)
                .select { (CityTable.name eq name) and (CityTable.country eq country.id) }
                .apply { state?.let { andWhere { CityTable.state eq state.id } } }
                .apply { original?.let { andWhere { CityTable.id neq original.id } } }
                .limit(1)
                .notForUpdate()
                .map { r -> City.wrapRow(r) }
                .firstOrNull()

            require(city == null) { "The city does already exist: $city" }
        }
    }

    fun count(): Long = City
        .all()
        .count()

    fun list(
        page: Int? = null,
        size: Int? = null,
        vararg order: Pair<Expression<*>, SortOrder> = emptyArray(),
    ): SizedIterable<City> = when (size) {
        null ->
            City
                .all()
                .orderBy(*order, CityTable.id to SortOrder.ASC)
                .notForUpdate()

        else ->
            City
                .all()
                .orderBy(*order, CityTable.id to SortOrder.ASC)
                .limit(size, (page ?: 0).toLong() * size)
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

        checkDuplicate(
            name = name,
            country = country,
            state = state,
        )

        return City.new {
            this.name = name
            this.country = country
            this.state = state
        }
    }

    fun update(
        id: UUID,
        name: String,
        countryId: UUID,
        stateId: UUID?,
    ): City {
        val city = City
            .find { CityTable.id eq id }
            .limit(1)
            .forUpdate()
            .firstOrNull()

        requireNotNull(city) { "The city ($id) does not exist." }

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

        checkDuplicate(
            original = city,
            name = name,
            country = country,
            state = state,
        )

        city.name = name
        city.country = country
        city.state = state
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
