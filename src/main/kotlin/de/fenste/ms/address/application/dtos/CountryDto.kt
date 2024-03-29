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

package de.fenste.ms.address.application.dtos

import com.fasterxml.jackson.annotation.JsonIgnore
import de.fenste.ms.address.application.controllers.api.CountryApi.LINKER.generateEntityLinks
import de.fenste.ms.address.domain.model.Country
import de.fenste.ms.address.infrastructure.tables.CityTable
import de.fenste.ms.address.infrastructure.tables.StateTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.hateoas.RepresentationModel
import java.util.UUID

data class CountryInputDto(
    val alpha2: String,
    val alpha3: String,
    val name: String,
    val localizedName: String,
)

data class CountryDto(
    private val country: Country,
) : RepresentationModel<CountryDto>(generateEntityLinks(country.id.value)) {

    val id: UUID
        get() = country.id.value

    val alpha2: String
        get() = country.alpha2

    val alpha3: String
        get() = country.alpha3

    val name: String
        get() = country.name

    val localizedName: String
        get() = country.localizedName

    @get:JsonIgnore
    val states: List<StateDto>
        get() = transaction {
            country.states
                .orderBy(StateTable.id to SortOrder.ASC)
                .notForUpdate()
                .map { s -> StateDto(s) }
        }

    @get:JsonIgnore
    val cities: List<CityDto>
        get() = transaction {
            country.cities
                .orderBy(CityTable.id to SortOrder.ASC)
                .notForUpdate()
                .map { c -> CityDto(c) }
        }
}
