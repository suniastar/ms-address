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

package de.fenste.ms.address.config

import de.fenste.ms.address.application.util.SampleDataImporter
import de.fenste.ms.address.infrastructure.tables.AddressTable
import de.fenste.ms.address.infrastructure.tables.CityTable
import de.fenste.ms.address.infrastructure.tables.CountryTable
import de.fenste.ms.address.infrastructure.tables.PostCodeTable
import de.fenste.ms.address.infrastructure.tables.StateTable
import de.fenste.ms.address.infrastructure.tables.StreetTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class ExposedConfig(
    @Autowired private val environment: Environment,
    @Autowired private val sampleDataImporter: SampleDataImporter, // TODO remove
) : InitializingBean {

    override fun afterPropertiesSet() {
        val driver = environment.getProperty(
            "spring.datasource.driver-class-name",
            "org.h2.Driver",
        )
        val url = environment.getProperty(
            "spring.datasource.url",
            "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;",
        )
        val user = environment.getProperty(
            "spring.datasource.username",
            "",
        )
        val password = environment.getProperty(
            "spring.datasource.password",
            "",
        )
        TransactionManager.defaultDatabase = Database.connect(
            driver = driver,
            url = url,
            user = user,
            password = password,
        )
        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                AddressTable,
                CityTable,
                CountryTable,
                PostCodeTable,
                StateTable,
                StreetTable,
            )
        }
        sampleDataImporter.resetToSample()
    }
}
