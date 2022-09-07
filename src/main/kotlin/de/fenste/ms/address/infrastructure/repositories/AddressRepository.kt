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

package de.fenste.ms.address.infrastructure.repositories

import de.fenste.ms.address.domain.model.Address
import de.fenste.ms.address.infrastructure.tables.AddressTable
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.SortOrder
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class AddressRepository {

    fun list(
        limit: Int? = null,
        offset: Long = 0L,
        vararg order: Pair<Expression<*>, SortOrder> = arrayOf(AddressTable.id to SortOrder.ASC),
    ): SizedIterable<Address> = when {
        limit != null ->
            Address
                .all()
                .orderBy(*order)
                .limit(limit, offset)
                .notForUpdate()
        else ->
            Address
                .all()
                .orderBy(*order)
                .notForUpdate()
    }

    fun find(
        id: UUID,
    ): Address? = Address
        .find { AddressTable.id eq id }
        .limit(1)
        .notForUpdate()
        .firstOrNull()
}
