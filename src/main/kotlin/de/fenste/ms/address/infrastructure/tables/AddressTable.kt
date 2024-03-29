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

package de.fenste.ms.address.infrastructure.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption

object AddressTable : UUIDTable("addresses") {

    private const val HOUSE_NUMBER_MAX_LENGTH = 255
    private const val EXTRA_MAX_LENGTH = 255

    val streetId = reference("street_id", StreetTable, onDelete = ReferenceOption.CASCADE)

    val houseNumber = varchar("house_number", HOUSE_NUMBER_MAX_LENGTH)

    val extra = varchar("extra", EXTRA_MAX_LENGTH).nullable()

    fun valueOf(value: String): Column<*> = when (value.lowercase()) {
        "id" -> id
        "street_id", "streetid" -> streetId
        "house_number", "housenumber" -> houseNumber
        "extra" -> extra
        else -> throw IllegalArgumentException("\"$value\" is not a valid column name.")
    }
}
