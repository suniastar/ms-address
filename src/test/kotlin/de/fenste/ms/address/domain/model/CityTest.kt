package de.fenste.ms.address.domain.model

import de.fenste.ms.address.test.SampleData
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@DataJpaTest
class CityTest(
    @Autowired private val testEntityManager: TestEntityManager,
) {
    private lateinit var copy : City
    private lateinit var notSaved: City

    @BeforeEach
    fun `set up`() {
        SampleData.reset(testEntityManager)

        copy = testEntityManager.find(City::class.java, SampleData.cities[0].id)

        notSaved = with(SampleData.cities[1]) {
            City(
                country = country,
                state = state,
                name = name,
            )
        }
    }

    @Test
    fun `test equals`() {
        assertEquals(SampleData.cities[0], SampleData.cities[0])
        assertEquals(copy, copy)
        assertEquals(SampleData.cities[0], copy)
        assertEquals(copy, SampleData.cities[0])

        assertNotEquals(SampleData.cities[0], SampleData.cities[1])
        assertNotEquals(copy, SampleData.cities[1])
        assertNotEquals(SampleData.cities[1], SampleData.cities[0])
        assertNotEquals(SampleData.cities[1], copy)

        assertNotEquals<City?>(copy, null)
        assertNotEquals<City?>(null, SampleData.cities[0])

        assertNotEquals(SampleData.cities[1], notSaved)
        assertNotEquals(notSaved, SampleData.cities[1])
    }

    @Test
    fun `test hashCode`() {
        assertEquals(SampleData.cities[0].hashCode(), SampleData.cities[0].hashCode())
        assertEquals(copy.hashCode(), copy.hashCode())
        assertEquals(SampleData.cities[0].hashCode(), copy.hashCode())
        assertEquals(copy.hashCode(), SampleData.cities[0].hashCode())

        assertNotEquals(SampleData.cities[0].hashCode(), SampleData.cities[1].hashCode())
        assertNotEquals(copy.hashCode(), SampleData.cities[1].hashCode())
        assertNotEquals(SampleData.cities[1].hashCode(), SampleData.cities[0].hashCode())
        assertNotEquals(SampleData.cities[1].hashCode(), copy.hashCode())

        assertNotEquals(copy.hashCode(), null.hashCode())
        assertNotEquals(null.hashCode(), SampleData.cities[0].hashCode())

        assertNotEquals(SampleData.cities[1].hashCode(), notSaved.hashCode())
        assertNotEquals(notSaved.hashCode(), SampleData.cities[1].hashCode())
    }

    @Test
    fun `test toString`() {
        val cId = copy.id
        val pIdC = copy.country.id
        val pIdS = copy.state?.id
        val cExpected = "City(id='$cId', country='$pIdC', state='$pIdS', name='Berlin')"
        val cActual = SampleData.cities[0].toString()
        assertEquals(cExpected, cActual)

        val nIdC = notSaved.country.id
        val nIdS = notSaved.state?.id
        val nExpected = "City(id='null', country='$nIdC', state='$nIdS', name='Berlin-Spandau')"
        val nActual = notSaved.toString()
        assertEquals(nExpected, nActual)
    }
}
