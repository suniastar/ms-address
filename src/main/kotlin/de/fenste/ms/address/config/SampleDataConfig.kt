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

package de.fenste.ms.address.config

import de.fenste.ms.address.domain.model.Address
import de.fenste.ms.address.domain.model.City
import de.fenste.ms.address.domain.model.Country
import de.fenste.ms.address.domain.model.PostCode
import de.fenste.ms.address.domain.model.State
import de.fenste.ms.address.domain.model.Street
import de.fenste.ms.address.infrastructure.tables.AddressTable
import de.fenste.ms.address.infrastructure.tables.CityTable
import de.fenste.ms.address.infrastructure.tables.CountryTable
import de.fenste.ms.address.infrastructure.tables.PostCodeTable
import de.fenste.ms.address.infrastructure.tables.StateTable
import de.fenste.ms.address.infrastructure.tables.StreetTable
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.util.UUID

@Configuration
@Profile("sample")
class SampleDataConfig : InitializingBean {

    private var pCountries: List<Country>? = null
    private var pStates: List<State>? = null
    private var pCities: List<City>? = null
    private var pPostCodes: List<PostCode>? = null
    private var pStreets: List<Street>? = null
    private var pAddresses: List<Address>? = null

    val countries: List<Country>
        get() = pCountries!!
    val states: List<State>
        get() = pStates!!
    val cities: List<City>
        get() = pCities!!
    val postCodes: List<PostCode>
        get() = pPostCodes!!
    val streets: List<Street>
        get() = pStreets!!
    val addresses: List<Address>
        get() = pAddresses!!

    override fun afterPropertiesSet(): Unit = reset()

    fun clear(): Unit = transaction {
        AddressTable.deleteAll()
        StreetTable.deleteAll()
        PostCodeTable.deleteAll()
        CityTable.deleteAll()
        StateTable.deleteAll()
        CountryTable.deleteAll()
    }

    @Suppress("LongMethod")
    fun reset() {
        clear()

        transaction {
            val country1 = Country.new(UUID.fromString("12345678-1234-1234-1234-000000100000")) {
                alpha2 = "C1"
                alpha3 = "C01"
                name = "Country One"
                localizedName = "Sample Country"
            }
            val country2 = Country.new(UUID.fromString("12345678-1234-1234-1234-000000200000")) {
                alpha2 = "C2"
                alpha3 = "C02"
                name = "Country Two"
                localizedName = "Üppiges Stück Erde"
            }
            val country3 = Country.new(UUID.fromString("12345678-1234-1234-1234-000000300000")) {
                alpha2 = "C3"
                alpha3 = "C03"
                name = "Country Three"
                localizedName = "国"
            }
            val country4 = Country.new(UUID.fromString("12345678-1234-1234-1234-000000400000")) {
                alpha2 = "C4"
                alpha3 = "C04"
                name = "Country Four"
                localizedName = "국가"
            }

            val state11 = State.new(UUID.fromString("12345678-1234-1234-1234-000000110000")) {
                name = "State One"
                country = country1
            }
            val state12 = State.new(UUID.fromString("12345678-1234-1234-1234-000000120000")) {
                name = "State Two"
                country = country1
            }
            val state13 = State.new(UUID.fromString("12345678-1234-1234-1234-000000130000")) {
                name = "State Three"
                country = country1
            }
            val state21 = State.new(UUID.fromString("12345678-1234-1234-1234-000000210000")) {
                name = "State One"
                country = country2
            }
            val state22 = State.new(UUID.fromString("12345678-1234-1234-1234-000000220000")) {
                name = "State Two"
                country = country2
            }
            val state23 = State.new(UUID.fromString("12345678-1234-1234-1234-000000230000")) {
                name = "State Three"
                country = country2
            }

            val city111 = City.new(UUID.fromString("12345678-1234-1234-1234-000000111000")) {
                name = "City One"
                country = country1
                state = state11
            }
            val city112 = City.new(UUID.fromString("12345678-1234-1234-1234-000000112000")) {
                name = "City Two"
                country = country1
                state = state11
            }
            val city113 = City.new(UUID.fromString("12345678-1234-1234-1234-000000113000")) {
                name = "City Three"
                country = country1
                state = state11
            }
            val city121 = City.new(UUID.fromString("12345678-1234-1234-1234-000000121000")) {
                name = "City One"
                country = country1
                state = state12
            }
            val city122 = City.new(UUID.fromString("12345678-1234-1234-1234-000000122000")) {
                name = "City Two"
                country = country1
                state = state12
            }
            val city123 = City.new(UUID.fromString("12345678-1234-1234-1234-000000123000")) {
                name = "City Three"
                country = country1
                state = state12
            }
            val city131 = City.new(UUID.fromString("12345678-1234-1234-1234-000000131000")) {
                name = "City One"
                country = country1
                state = state13
            }
            val city132 = City.new(UUID.fromString("12345678-1234-1234-1234-000000132000")) {
                name = "City Two"
                country = country1
                state = state13
            }
            val city133 = City.new(UUID.fromString("12345678-1234-1234-1234-000000133000")) {
                name = "City Three"
                country = country1
                state = state13
            }
            val city211 = City.new(UUID.fromString("12345678-1234-1234-1234-000000211000")) {
                name = "City One"
                country = country2
                state = state21
            }
            val city212 = City.new(UUID.fromString("12345678-1234-1234-1234-000000212000")) {
                name = "City Two"
                country = country2
                state = state21
            }
            val city213 = City.new(UUID.fromString("12345678-1234-1234-1234-000000213000")) {
                name = "City Three"
                country = country2
                state = state21
            }
            val city301 = City.new(UUID.fromString("12345678-1234-1234-1234-000000301000")) {
                name = "City One"
                country = country3
                state = null
            }
            val city302 = City.new(UUID.fromString("12345678-1234-1234-1234-000000302000")) {
                name = "City Two"
                country = country3
                state = null
            }
            val city303 = City.new(UUID.fromString("12345678-1234-1234-1234-000000303000")) {
                name = "City Three"
                country = country3
                state = null
            }

            val postCode1111 = PostCode.new(UUID.fromString("12345678-1234-1234-1234-000000111100")) {
                code = "111"
                city = city111
            }
            val postCode1112 = PostCode.new(UUID.fromString("12345678-1234-1234-1234-000000111200")) {
                code = "222"
                city = city111
            }
            val postCode1113 = PostCode.new(UUID.fromString("12345678-1234-1234-1234-000000111300")) {
                code = "333"
                city = city111
            }
            val postCode1121 = PostCode.new(UUID.fromString("12345678-1234-1234-1234-000000112100")) {
                code = "111"
                city = city112
            }
            val postCode1122 = PostCode.new(UUID.fromString("12345678-1234-1234-1234-000000112200")) {
                code = "222"
                city = city112
            }
            val postCode1123 = PostCode.new(UUID.fromString("12345678-1234-1234-1234-000000112300")) {
                code = "333"
                city = city112
            }
            val postCode1131 = PostCode.new(UUID.fromString("12345678-1234-1234-1234-000000113100")) {
                code = "111"
                city = city113
            }
            val postCode1132 = PostCode.new(UUID.fromString("12345678-1234-1234-1234-000000113200")) {
                code = "222"
                city = city113
            }
            val postCode1133 = PostCode.new(UUID.fromString("12345678-1234-1234-1234-000000113300")) {
                code = "333"
                city = city113
            }

            val street11111 = Street.new(UUID.fromString("12345678-1234-1234-1234-000000111110")) {
                name = "Street One"
                postCode = postCode1111
            }
            val street11112 = Street.new(UUID.fromString("12345678-1234-1234-1234-000000111120")) {
                name = "Street Two"
                postCode = postCode1111
            }
            val street11113 = Street.new(UUID.fromString("12345678-1234-1234-1234-000000111130")) {
                name = "Street Three"
                postCode = postCode1111
            }
            val street11121 = Street.new(UUID.fromString("12345678-1234-1234-1234-000000111210")) {
                name = "Street One"
                postCode = postCode1112
            }
            val street11122 = Street.new(UUID.fromString("12345678-1234-1234-1234-000000111220")) {
                name = "Street Two"
                postCode = postCode1112
            }
            val street11123 = Street.new(UUID.fromString("12345678-1234-1234-1234-000000111230")) {
                name = "Street Three"
                postCode = postCode1112
            }
            val street11131 = Street.new(UUID.fromString("12345678-1234-1234-1234-000000111310")) {
                name = "Street One"
                postCode = postCode1113
            }
            val street11132 = Street.new(UUID.fromString("12345678-1234-1234-1234-000000111320")) {
                name = "Street Two"
                postCode = postCode1113
            }
            val street11133 = Street.new(UUID.fromString("12345678-1234-1234-1234-000000111330")) {
                name = "Street Three"
                postCode = postCode1113
            }

            val address111111 = Address.new(UUID.fromString("12345678-1234-1234-1234-000000111111")) {
                houseNumber = "1"
                extra = null
                street = street11111
            }
            val address111112 = Address.new(UUID.fromString("12345678-1234-1234-1234-000000111112")) {
                houseNumber = "2"
                extra = null
                street = street11111
            }
            val address111113 = Address.new(UUID.fromString("12345678-1234-1234-1234-000000111113")) {
                houseNumber = "3"
                extra = "a"
                street = street11111
            }
            val address111121 = Address.new(UUID.fromString("12345678-1234-1234-1234-000000111121")) {
                houseNumber = "1"
                extra = null
                street = street11112
            }
            val address111122 = Address.new(UUID.fromString("12345678-1234-1234-1234-000000111122")) {
                houseNumber = "2"
                extra = null
                street = street11112
            }
            val address111123 = Address.new(UUID.fromString("12345678-1234-1234-1234-000000111123")) {
                houseNumber = "3"
                extra = "a"
                street = street11112
            }
            val address111131 = Address.new(UUID.fromString("12345678-1234-1234-1234-000000111131")) {
                houseNumber = "1"
                extra = null
                street = street11113
            }
            val address111132 = Address.new(UUID.fromString("12345678-1234-1234-1234-000000111132")) {
                houseNumber = "2"
                extra = null
                street = street11113
            }
            val address111133 = Address.new(UUID.fromString("12345678-1234-1234-1234-000000111133")) {
                houseNumber = "3"
                extra = "a"
                street = street11113
            }

            pCountries = listOf(
                country1,
                country2,
                country3,
                country4,
            )

            pStates = listOf(
                state11,
                state12,
                state13,
                state21,
                state22,
                state23,
            )

            pCities = listOf(
                city111,
                city112,
                city113,
                city121,
                city122,
                city123,
                city131,
                city132,
                city133,
                city211,
                city212,
                city213,
                city301,
                city302,
                city303,
            )

            pPostCodes = listOf(
                postCode1111,
                postCode1112,
                postCode1113,
                postCode1121,
                postCode1122,
                postCode1123,
                postCode1131,
                postCode1132,
                postCode1133,
            )

            pStreets = listOf(
                street11111,
                street11112,
                street11113,
                street11121,
                street11122,
                street11123,
                street11131,
                street11132,
                street11133,
            )

            pAddresses = listOf(
                address111111,
                address111112,
                address111113,
                address111121,
                address111122,
                address111123,
                address111131,
                address111132,
                address111133,
            )
        }
    }
}
