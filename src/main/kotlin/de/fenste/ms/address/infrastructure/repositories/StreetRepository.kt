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

import de.fenste.ms.address.domain.model.PostCode
import de.fenste.ms.address.domain.model.Street
import de.fenste.ms.address.infrastructure.tables.PostCodeTable
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
class StreetRepository {

    private companion object {

        private fun checkDuplicate(
            original: Street? = null,
            name: String,
            postCode: PostCode,
        ) {
            val street = StreetTable
                .slice(StreetTable.columns)
                .select { (StreetTable.name eq name) and (StreetTable.postCode eq postCode.id) }
                .apply { original?.let { andWhere { StreetTable.id neq original.id } } }
                .limit(1)
                .notForUpdate()
                .firstOrNull()
                ?.let { r -> Street.wrapRow(r) }

            require(street == null) { "This street does already exist: $street" }
        }
    }

    fun count(): Long = Street
        .all()
        .count()

    fun list(
        page: Int? = null,
        size: Int? = null,
        vararg order: Pair<Expression<*>, SortOrder> = emptyArray(),
    ): SizedIterable<Street> = when (size) {
        null ->
            Street
                .all()
                .orderBy(*order, StreetTable.id to SortOrder.ASC)
                .notForUpdate()

        else ->
            Street
                .all()
                .orderBy(*order, StreetTable.id to SortOrder.ASC)
                .limit(size, (page ?: 0).toLong() * size)
                .notForUpdate()
    }

    fun find(
        id: UUID,
    ): Street? = Street
        .find { StreetTable.id eq id }
        .limit(1)
        .notForUpdate()
        .firstOrNull()

    fun create(
        name: String,
        postCodeId: UUID,
    ): Street {
        val postCode = PostCode
            .find { PostCodeTable.id eq postCodeId }
            .limit(1)
            .notForUpdate()
            .firstOrNull()

        requireNotNull(postCode) { "The post code ($postCodeId) does not exist." }

        checkDuplicate(
            name = name,
            postCode = postCode,
        )

        return Street.new {
            this.name = name
            this.postCode = postCode
        }
    }

    fun update(
        id: UUID,
        name: String,
        postCodeId: UUID,
    ): Street {
        val street = Street
            .find { StreetTable.id eq id }
            .limit(1)
            .notForUpdate()
            .firstOrNull()

        requireNotNull(street) { "The street ($id) does not exit." }

        val postCode = PostCode
            .find { PostCodeTable.id eq postCodeId }
            .limit(1)
            .forUpdate()
            .firstOrNull()

        requireNotNull(postCode) { "The post code ($postCodeId) does not exist." }

        checkDuplicate(
            original = street,
            name = name,
            postCode = postCode,
        )

        street.name = name
        street.postCode = postCode
        return street
    }

    fun delete(
        id: UUID,
    ) {
        val street = Street
            .find { StreetTable.id eq id }
            .limit(1)
            .forUpdate()
            .firstOrNull()

        requireNotNull(street) { "The street ($id) does not exit." }

        street.delete()
    }
}
