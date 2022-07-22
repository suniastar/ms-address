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

import de.fenste.ms.address.domain.model.Address
import org.springframework.graphql.data.method.annotation.SchemaMapping

@SchemaMapping(typeName = "Address")
data class AddressDto(

    @get:SchemaMapping(field = "id", typeName = "String")
    val id: String,

    @get:SchemaMapping(field = "houseNumber", typeName = "String")
    val houseNumber: String,

    @get:SchemaMapping(field = "extra", typeName = "String")
    val extra: String?,

    @get:SchemaMapping(field = "street", typeName = "Street")
    val street: StreetDto,
) {
    constructor(address: Address) : this(
        id = address.id.value.toString(),
        houseNumber = address.houseNumber,
        extra = address.extra,
        street = StreetDto(address.street),
    )
}
