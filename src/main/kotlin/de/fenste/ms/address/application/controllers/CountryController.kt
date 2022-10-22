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

package de.fenste.ms.address.application.controllers

import de.fenste.ms.address.application.dtos.CountryDto
import de.fenste.ms.address.application.dtos.CountryInputDto
import de.fenste.ms.address.application.services.CountryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller
import java.util.UUID

@Controller
@Suppress("unused")
class CountryController(
    @Autowired private val countryService: CountryService,
) {
    @SchemaMapping(field = "countries", typeName = "Query")
    fun countries(
        @Argument limit: Int? = null,
        @Argument offset: Int? = null,
    ): List<CountryDto>? = countryService.list(
        limit = limit,
        offset = offset?.toLong(),
    )

    @SchemaMapping(field = "country", typeName = "Query")
    fun country(
        @Argument id: UUID? = null,
        @Argument alpha2: String? = null,
        @Argument alpha3: String? = null,
    ): CountryDto? = countryService.find(
        id = id,
        alpha2 = alpha2,
        alpha3 = alpha3,
    )

    @SchemaMapping(field = "createCountry", typeName = "Mutation")
    fun createCountry(
        @Argument country: CountryInputDto,
    ): CountryDto = countryService.create(
        country = country,
    )

    @SchemaMapping(field = "updateCountry", typeName = "Mutation")
    fun updateCountry(
        @Argument id: UUID,
        @Argument country: CountryInputDto,
    ): CountryDto = countryService.update(
        id = id,
        country = country,
    )

    @SchemaMapping(field = "deleteCountry", typeName = "Mutation")
    fun deleteCountry(
        id: UUID,
    ): Boolean = countryService.delete(
        id = id,
    )
}
