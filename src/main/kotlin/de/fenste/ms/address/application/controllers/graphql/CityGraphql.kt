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
import de.fenste.ms.address.application.dtos.CityInputDto
import de.fenste.ms.address.application.dtos.PostCodeDto
import de.fenste.ms.address.application.dtos.StateDto
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import java.util.UUID

interface CityGraphql {

    @SchemaMapping(typeName = "Query", field = "city")
    fun graphqlGetCity(
        @Argument id: UUID,
    ): CityDto?

    @SchemaMapping(typeName = "Query", field = "cities")
    fun graphqlGetCities(
        @Argument page: Int? = null,
        @Argument size: Int? = null,
        @Argument sort: String? = null,
    ): List<CityDto>

    @SchemaMapping(typeName = "City", field = "state")
    fun graphqlGetCityState(
        city: CityDto,
    ): StateDto

    @SchemaMapping(typeName = "City", field = "postCodes")
    fun graphqlGetCityPostCodes(
        city: CityDto,
        @Argument page: Int? = null,
        @Argument size: Int? = null,
        @Argument sort: String? = null,
    ): List<PostCodeDto>

    @SchemaMapping(typeName = "Mutation", field = "createCity")
    fun graphqlCreateCity(
        @Argument city: CityInputDto,
    ): CityDto

    @SchemaMapping(typeName = "Mutation", field = "updateCity")
    fun graphqlUpdateCity(
        @Argument id: UUID,
        @Argument city: CityInputDto,
    ): CityDto

    @SchemaMapping(typeName = "Mutation", field = "deleteCity")
    fun graphqlDeleteCity(
        @Argument id: UUID,
    ): Boolean
}
