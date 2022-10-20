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

import de.fenste.ms.address.application.dtos.requests.CreateStreetDto
import de.fenste.ms.address.application.dtos.requests.UpdateStreetDto
import de.fenste.ms.address.application.dtos.responses.StreetDto
import de.fenste.ms.address.application.services.StreetService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller
import java.util.UUID

@Controller
@Suppress("unused")
class StreetController(
    @Autowired private val streetService: StreetService,
) {
    @SchemaMapping(field = "streets", typeName = "Query")
    fun streets(
        @Argument limit: Int? = null,
        @Argument offset: Int? = null,
    ): List<StreetDto>? = streetService.list(
        limit = limit,
        offset = offset?.toLong(),
    )

    @SchemaMapping(field = "street", typeName = "Query")
    fun street(
        @Argument id: String,
    ): StreetDto? = streetService.find(
        id = UUID.fromString(id),
    )

    @SchemaMapping(field = "createStreet", typeName = "Mutation")
    fun createStreet(
        @Argument create: CreateStreetDto,
    ): StreetDto = streetService.create(
        create = create,
    )

    @SchemaMapping(field = "updateStreet", typeName = "Mutation")
    fun updateStreet(
        @Argument update: UpdateStreetDto,
    ): StreetDto = streetService.update(
        update = update,
    )

    @SchemaMapping(field = "deleteStreet", typeName = "Mutation")
    fun deleteStreet(
        @Argument id: String,
    ): Boolean = streetService.delete(
        id = UUID.fromString(id),
    )
}
