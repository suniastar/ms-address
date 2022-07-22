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

import de.fenste.ms.address.domain.model.Street
import org.jetbrains.exposed.sql.transactions.transaction

@Suppress("unused")
data class StreetDto(private val street: Street) {

    val id: String
        get() = street.id.value.toString()

    val name: String
        get() = street.name

    val postCode: PostCodeDto
        get() = transaction { PostCodeDto(street.postCode) }

    val addresses: List<AddressDto>?
        get() = transaction { street.addresses.map { a -> AddressDto(a) }.ifEmpty { null } }
}
