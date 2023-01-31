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

object StateTable : UUIDTable("states") {

    private const val NAME_MAX_LENGTH = 255

    val countryId = reference("country_id", CountryTable, onDelete = ReferenceOption.CASCADE)

    val name = varchar("name", NAME_MAX_LENGTH)

    fun valueOf(value: String): Column<*> = when (value.lowercase()) {
        "id" -> id
        "country_id", "countryid" -> countryId
        "name" -> name
        else -> throw IllegalArgumentException("\"$value\" is not a valid column name.")
    }
}
