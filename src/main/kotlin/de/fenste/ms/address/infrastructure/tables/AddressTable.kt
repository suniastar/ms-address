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

package de.fenste.ms.address.infrastructure.tables

import org.jetbrains.exposed.dao.id.UUIDTable

object AddressTable : UUIDTable("addresses") {

    const val HOUSE_NUMBER_MAX_LENGTH = 255
    const val EXTRA_MAX_LENGTH = 255

    val street = reference("street_id", StreetTable)

    val houseNumber = varchar("house_number", HOUSE_NUMBER_MAX_LENGTH)

    val extra = varchar("extra", EXTRA_MAX_LENGTH).nullable()
}
