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
import de.fenste.ms.address.application.controllers.api.AddressApi.LINKER.generateEntityLinks
import de.fenste.ms.address.domain.model.Address
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.hateoas.RepresentationModel
import java.util.UUID

data class AddressInputDto(
    val houseNumber: String,
    val extra: String? = null,
    val street: UUID,
)

data class AddressDto(
    private val address: Address,
) : RepresentationModel<AddressDto>(generateEntityLinks(address.id.value)) {

    val id: UUID
        get() = address.id.value

    val houseNumber: String
        get() = address.houseNumber

    val extra: String?
        get() = address.extra

    @get:JsonIgnore
    val street: StreetDto
        get() = transaction { StreetDto(address.street) }
}
