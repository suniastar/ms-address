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

import de.fenste.ms.address.application.controllers.api.CountryApi.LINKER.generateEntityLinks
import de.fenste.ms.address.domain.model.Country
import org.springframework.hateoas.RepresentationModel
import java.util.UUID

data class CountryInputDto(
    val alpha2: String,
    val alpha3: String,
    val name: String,
    val localizedName: String,
)

data class CountryDto(
    val id: UUID,
    val alpha2: String,
    val alpha3: String,
    val name: String,
    val localizedName: String,
) : RepresentationModel<CountryDto>(generateEntityLinks(id)) {

    constructor(country: Country) : this(
        id = country.id.value,
        alpha2 = country.alpha2,
        alpha3 = country.alpha3,
        name = country.name,
        localizedName = country.localizedName,
    )
}
