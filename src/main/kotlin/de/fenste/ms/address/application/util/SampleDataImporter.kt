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

package de.fenste.ms.address.application.util

import de.fenste.ms.address.domain.model.Address
import de.fenste.ms.address.domain.model.City
import de.fenste.ms.address.domain.model.Country
import de.fenste.ms.address.domain.model.PostCode
import de.fenste.ms.address.domain.model.State
import de.fenste.ms.address.domain.model.Street
import de.fenste.ms.address.infrastructure.repositories.AddressRepository
import de.fenste.ms.address.infrastructure.repositories.CityRepository
import de.fenste.ms.address.infrastructure.repositories.CountryRepository
import de.fenste.ms.address.infrastructure.repositories.PostCodeRepository
import de.fenste.ms.address.infrastructure.repositories.StateRepository
import de.fenste.ms.address.infrastructure.repositories.StreetRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class SampleDataImporter(
    @Autowired private val addressRepository: AddressRepository,
    @Autowired private val cityRepository: CityRepository,
    @Autowired private val countryRepository: CountryRepository,
    @Autowired private val postCodeRepository: PostCodeRepository,
    @Autowired private val stateRepository: StateRepository,
    @Autowired private val streetRepository: StreetRepository,
) {

    @Transactional
    fun resetToSample() {
        addressRepository.deleteAll()
        streetRepository.deleteAll()
        postCodeRepository.deleteAll()
        cityRepository.deleteAll()
        stateRepository.deleteAll()
        countryRepository.deleteAll()

        val cGermany = countryRepository.saveAndFlush(
            Country(
                alpha2 = "DE",
                alpha3 = "DEU",
                name = "Germany",
                localizedName = "Deutschland",
            )
        )
        val cFrance = countryRepository.saveAndFlush(
            Country(
                alpha2 = "FR",
                alpha3 = "FRA",
                name = "France",
                localizedName = "Frankreich",
            )
        )
        val cGB = countryRepository.saveAndFlush(
            Country(
                alpha2 = "GB",
                alpha3 = "GBR",
                name = "United Kingdom of Great Britain and Northern Ireland",
                localizedName = "Großbritannien und Nordirland",
            )
        )

        val sBerlin = stateRepository.saveAndFlush(
            State(
                name = "Berlin",
                country = cGermany,
            )
        )
        val sBadenWuerttemberg = stateRepository.saveAndFlush(
            State(
                name = "Baden-Württemberg",
                country = cGermany,
            )
        )
        val sIleDeFrance = stateRepository.saveAndFlush(
            State(
                name = "Ile-de-France",
                country = cFrance,
            )
        )

        val cBerlin = cityRepository.saveAndFlush(
            City(
                name = "Berlin",
                country = cGermany,
                state = sBerlin,
            )
        )
        val cSpandau = cityRepository.saveAndFlush(
            City(
                name = "Berlin-Spandau",
                country = cGermany,
                state = sBerlin,
            )
        )
        val cKarlsruhe = cityRepository.saveAndFlush(
            City(
                name = "Karlsruhe",
                country = cGermany,
                state = sBadenWuerttemberg,
            )
        )
        val cParis = cityRepository.saveAndFlush(
            City(
                name = "Paris",
                country = cFrance,
                state = sIleDeFrance,
            )
        )
        val cBirmingham = cityRepository.saveAndFlush(
            City(
                name = "Birmingham",
                country = cGB,
                state = null,
            )
        )

        val p10557 = postCodeRepository.saveAndFlush(
            PostCode(
                code = "10557",
                city = cBerlin,
            )
        )
        val p10117 = postCodeRepository.saveAndFlush(
            PostCode(
                code = "10117",
                city = cBerlin,
            )
        )
        val p13597 = postCodeRepository.saveAndFlush(
            PostCode(
                code = "13597",
                city = cSpandau,
            )
        )
        val p76131 = postCodeRepository.saveAndFlush(
            PostCode(
                code = "76131",
                city = cKarlsruhe,
            )
        )
        val p75007 = postCodeRepository.saveAndFlush(
            PostCode(
                code = "75007",
                city = cParis,
            )
        )
        val pB100RJ = postCodeRepository.saveAndFlush(
            PostCode(
                code = "B10 0RJ",
                city = cBirmingham,
            )
        )

        val sPlatzDerRepublik = streetRepository.saveAndFlush(
            Street(
                name = "Platz der Republik",
                postCode = p10557,
            )
        )
        val sWillyBrandtStrasse = streetRepository.saveAndFlush(
            Street(
                name = "Willy-Brandt-Straße",
                postCode = p10557,
            )
        )
        val sFriedrichEbertPlatz = streetRepository.saveAndFlush(
            Street(
                name = "Friedrich-Ebert-Platz",
                postCode = p10117,
            )
        )
        val sBreiteStrasse = streetRepository.saveAndFlush(
            Street(
                name = "Breite Str.",
                postCode = p13597,
            )
        )
        val sAmFasanengarten = streetRepository.saveAndFlush(
            Street(
                name = "Am Fasanengarten",
                postCode = p76131,
            )
        )
        val sAnatoleFrance = streetRepository.saveAndFlush(
            Street(
                name = "Anatole France",
                postCode = p75007,
            )
        )
        val sCoventryRoad = streetRepository.saveAndFlush(
            Street(
                name = "Coventry Rd",
                postCode = pB100RJ,
            )
        )

        addressRepository.saveAndFlush(
            Address(
                houseNumber = "1",
                extra = null,
                street = sPlatzDerRepublik,
            )
        )
        addressRepository.saveAndFlush(
            Address(
                houseNumber = "2a",
                extra = null,
                street = sPlatzDerRepublik,
            )
        )
        addressRepository.saveAndFlush(
            Address(
                houseNumber = "1",
                extra = null,
                street = sWillyBrandtStrasse,
            )
        )
        addressRepository.saveAndFlush(
            Address(
                houseNumber = "2",
                extra = null,
                street = sFriedrichEbertPlatz,
            )
        )
        addressRepository.saveAndFlush(
            Address(
                houseNumber = "25",
                extra = null,
                street = sBreiteStrasse,
            )
        )
        addressRepository.saveAndFlush(
            Address(
                houseNumber = "5",
                extra = null,
                street = sAmFasanengarten,
            )
        )
        addressRepository.saveAndFlush(
            Address(
                houseNumber = "5",
                extra = "Av.",
                street = sAnatoleFrance,
            )
        )
        addressRepository.saveAndFlush(
            Address(
                houseNumber = "109",
                extra = null,
                street = sCoventryRoad,
            )
        )
    }
}
