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

import de.fenste.ms.address.application.dtos.StreetDto
import de.fenste.ms.address.application.dtos.StreetInputDto
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import java.util.UUID

interface StreetGraphql {

    @SchemaMapping(typeName = "Query", field = "streets")
    fun graphqlGetStreets(
        @Argument page: Int? = null,
        @Argument size: Int? = null,
        @Argument sort: String? = null,
    ): List<StreetDto>

    @SchemaMapping(typeName = "Query", field = "street")
    fun graphqlGetStreet(
        @Argument id: UUID,
    ): StreetDto?

    @SchemaMapping(typeName = "Mutation", field = "createStreet")
    fun graphqlCreateStreet(
        @Argument street: StreetInputDto,
    ): StreetDto

    @SchemaMapping(typeName = "Mutation", field = "updateStreet")
    fun graphqlUpdateStreet(
        @Argument id: UUID,
        @Argument street: StreetInputDto,
    ): StreetDto

    @SchemaMapping(typeName = "Mutation", field = "deleteStreet")
    fun graphqlDeleteStreet(
        @Argument id: UUID,
    ): Boolean
}
