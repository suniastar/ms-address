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

package de.fenste.ms.address.infrastructure.repositories

import de.fenste.ms.address.domain.model.Address
import de.fenste.ms.address.domain.model.Street
import de.fenste.ms.address.infrastructure.tables.AddressTable
import de.fenste.ms.address.infrastructure.tables.StreetTable
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class AddressRepository {

    private companion object {

        private fun checkDuplicate(
            original: Address? = null,
            houseNumber: String,
            extra: String?,
            street: Street,
        ) {
            val address = AddressTable
                .slice(AddressTable.columns)
                .select { (AddressTable.houseNumber eq houseNumber) and (AddressTable.streetId eq street.id) }
                .apply { extra?.let { andWhere { AddressTable.extra eq extra } } }
                .apply { original?.let { andWhere { AddressTable.id neq original.id } } }
                .limit(1)
                .notForUpdate()
                .map { r -> Address.wrapRow(r) }
                .firstOrNull()

            require(address == null) { "This address does already exist: $address" }
        }
    }

    fun count(): Int = Address
        .all()
        .count()
        .toInt()

    fun list(
        page: Int? = null,
        size: Int? = null,
        vararg order: Pair<Expression<*>, SortOrder> = arrayOf(),
    ): SizedIterable<Address> = when (size) {
        null ->
            Address
                .all()
                .orderBy(*order, AddressTable.id to SortOrder.ASC)
                .notForUpdate()

        else ->
            Address
                .all()
                .orderBy(*order, AddressTable.id to SortOrder.ASC)
                .limit(size, (page ?: 0).toLong() * size)
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
        extra: String?,
        streetId: UUID,
    ): Address {
        val street = Street
            .find { StreetTable.id eq streetId }
            .limit(1)
            .notForUpdate()
            .firstOrNull()

        requireNotNull(street) { "The street ($streetId) does not exist." }

        checkDuplicate(
            houseNumber = houseNumber,
            extra = extra,
            street = street,
        )

        return Address.new {
            this.houseNumber = houseNumber
            this.extra = extra
            this.street = street
        }
    }

    fun update(
        id: UUID,
        houseNumber: String,
        extra: String?,
        streetId: UUID,
    ): Address {
        val address = Address
            .find { AddressTable.id eq id }
            .limit(1)
            .forUpdate()
            .firstOrNull()

        requireNotNull(address) { "The Address ($id) does not exist." }

        val street = Street
            .find { StreetTable.id eq streetId }
            .limit(1)
            .notForUpdate()
            .firstOrNull()

        requireNotNull(street) { "The street ($streetId) does not exist." }

        checkDuplicate(
            original = address,
            houseNumber = houseNumber,
            extra = extra,
            street = street,
        )

        address.houseNumber = houseNumber
        address.extra = extra
        address.street = street
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
