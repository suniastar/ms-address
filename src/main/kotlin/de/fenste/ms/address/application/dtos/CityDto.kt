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

package de.fenste.ms.address.application.dtos

import de.fenste.ms.address.domain.model.City
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

data class CityInputDto(
    val name: String,
    val country: UUID,
    val state: UUID? = null,
)

data class CityDto(private val city: City) {

    val id: UUID
        get() = city.id.value

    val name: String
        get() = city.name

    val country: CountryDto
        get() = transaction { CountryDto(city.country) }

    val state: StateDto?
        get() = transaction { city.state?.let { s -> StateDto(s) } }

    val postCodes: List<PostCodeDto>?
        get() = transaction { city.postCodes.map { p -> PostCodeDto(p) }.ifEmpty { null } }
}
