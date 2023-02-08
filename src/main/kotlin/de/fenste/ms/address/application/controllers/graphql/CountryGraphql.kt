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

package de.fenste.ms.address.application.controllers.graphql

import de.fenste.ms.address.application.dtos.CityDto
import de.fenste.ms.address.application.dtos.CountryDto
import de.fenste.ms.address.application.dtos.CountryInputDto
import de.fenste.ms.address.application.dtos.StateDto
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import java.util.UUID

interface CountryGraphql {

    @SchemaMapping(typeName = "Query", field = "countries")
    fun graphqlGetCountries(
        @Argument page: Int? = null,
        @Argument size: Int? = null,
        @Argument sort: String? = null,
    ): List<CountryDto>

    @SchemaMapping(typeName = "Query", field = "country")
    fun graphqlGetCountry(
        @Argument id: UUID? = null,
        @Argument alpha2: String? = null,
        @Argument alpha3: String? = null,
    ): CountryDto?

    @SchemaMapping(typeName = "Mutation", field = "createCountry")
    fun graphqlCreateCountry(
        @Argument country: CountryInputDto,
    ): CountryDto

    @SchemaMapping(typeName = "Mutation", field = "updateCountry")
    fun graphqlUpdateCountry(
        @Argument id: UUID,
        @Argument country: CountryInputDto,
    ): CountryDto

    @SchemaMapping(typeName = "Mutation", field = "deleteCountry")
    fun graphqlDeleteCountry(
        @Argument id: UUID,
    ): Boolean

    @SchemaMapping(typeName = "Country", field = "states")
    fun graphqlGetCountryStates(
        country: CountryDto,
        @Argument page: Int? = null,
        @Argument size: Int? = null,
        @Argument sort: String? = null,
    ): List<StateDto>

    @SchemaMapping(typeName = "Country", field = "cities")
    fun graphqlGetCountryCities(
        country: CountryDto,
        @Argument page: Int? = null,
        @Argument size: Int? = null,
        @Argument sort: String? = null,
    ): List<CityDto>
}
