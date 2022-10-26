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

import de.fenste.ms.address.application.dtos.StateDto
import de.fenste.ms.address.application.dtos.StateInputDto
import de.fenste.ms.address.application.services.StateService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller
import java.util.UUID

@Controller
@Suppress("unused")
class StateController(
    @Autowired private val stateService: StateService,
) {
    @SchemaMapping(field = "states", typeName = "Query")
    fun states(
        @Argument limit: Int? = null,
        @Argument offset: Int? = null,
    ): List<StateDto>? = stateService.list(
        limit = limit,
        offset = offset?.toLong(),
    )

    @SchemaMapping(field = "state", typeName = "Query")
    fun state(
        @Argument id: UUID,
    ): StateDto? = stateService.find(
        id = id,
    )

    @SchemaMapping(field = "createState", typeName = "Mutation")
    fun createState(
        @Argument state: StateInputDto,
    ): StateDto = stateService.create(
        state = state,
    )

    @SchemaMapping(field = "updateState", typeName = "Mutation")
    fun updateState(
        @Argument id: UUID,
        @Argument state: StateInputDto,
    ): StateDto = stateService.update(
        id = id,
        state = state,
    )

    @SchemaMapping(field = "deleteState", typeName = "Mutation")
    fun deleteState(
        @Argument id: UUID,
    ): Boolean = stateService.delete(
        id = id,
    )
}
