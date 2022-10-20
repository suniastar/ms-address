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
import de.fenste.ms.address.domain.model.Street
import de.fenste.ms.address.infrastructure.tables.AddressTable
import de.fenste.ms.address.infrastructure.tables.StreetTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class AddressRepository {

    private companion object {

        private fun idOf(
            houseNumber: String,
            extra: String?,
            streetId: UUID,
        ): EntityID<UUID>? = AddressTable
            .slice(AddressTable.id)
            .select {
                (AddressTable.houseNumber eq houseNumber) and
                    (AddressTable.extra eq extra) and
                    (AddressTable.street eq streetId)
            }
            .limit(1)
            .notForUpdate()
            .firstOrNull()
            ?.let { a -> a[AddressTable.id] }
    }

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

    fun create(
        houseNumber: String,
        extra: String? = null,
        streetId: UUID,
    ): Address {
        require(
            idOf(
                houseNumber = houseNumber,
                extra = extra,
                streetId = streetId,
            ) == null,
        ) { "An Address with this house number, extra and parent street does already exist." }

        val street = Street
            .find { StreetTable.id eq streetId }
            .limit(1)
            .notForUpdate()
            .firstOrNull()

        requireNotNull(street) { "The street ($streetId) does not exist." }

        return Address.new {
            this.houseNumber = houseNumber
            this.extra = extra
            this.street = street
        }
    }

    fun update(
        id: UUID,
        houseNumber: String? = null,
        extra: String? = null,
        streetId: UUID? = null,
        removeExtra: Boolean = false,
    ): Address {
        val address = Address
            .find { AddressTable.id eq id }
            .limit(1)
            .forUpdate()
            .firstOrNull()

        requireNotNull(address) { "The Address ($id) does not exist." }

        val uId = idOf(
            houseNumber = houseNumber ?: address.houseNumber,
            extra = if (!removeExtra) extra ?: address.extra else null,
            streetId = streetId ?: address.street.id.value,
        )
        require(
            uId == null || uId == address.id,
        ) { "An Address with this house number, extra and parent street does already exist." }

        streetId?.let {
            val street = Street
                .find { StreetTable.id eq streetId }
                .limit(1)
                .notForUpdate()
                .firstOrNull()

            requireNotNull(street) { "The street ($streetId) does not exist." }

            address.street = street
        }
        houseNumber?.let { address.houseNumber = houseNumber }
        extra?.let { address.extra = extra }
        if (removeExtra) address.extra = null

        return address
    }

    fun delete(
        id: UUID,
    ) {
        val address = Address
            .find { AddressTable.id eq id }
            .limit(1)
            .forUpdate()
            .firstOrNull()

        requireNotNull(address) { "The Address ($id) does not exist." }

        address.delete()
    }
}
