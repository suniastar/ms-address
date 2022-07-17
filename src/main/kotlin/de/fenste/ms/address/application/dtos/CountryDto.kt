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

import de.fenste.ms.address.domain.model.Country
import org.springframework.graphql.data.method.annotation.SchemaMapping

@SchemaMapping(typeName = "Country")
data class CountryDto(

    @get:SchemaMapping(field = "id", typeName = "Int")
    val id: String,

    @get:SchemaMapping(field = "alpha2", typeName = "String")
    val alpha2: String,

    @get:SchemaMapping(field = "alpha3", typeName = "String")
    val alpha3: String,

    @get:SchemaMapping(field = "name", typeName = "String")
    val name: String,

    @get:SchemaMapping(field = "localizedName", typeName = "String")
    val localizedName: String,
) {

    constructor(country: Country) : this(
        id = country.id.toString(),
        alpha2 = country.alpha2,
        alpha3 = country.alpha3,
        name = country.name,
        localizedName = country.localizedName,
    )
}
