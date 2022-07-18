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
class StreetTest(
    @Autowired private val testEntityManager: TestEntityManager,
) {
    private lateinit var copy: Street
    private lateinit var notSaved: Street

    @BeforeEach
    fun `set up`() {
        SampleData.reset(testEntityManager)

        copy = testEntityManager.find(Street::class.java, SampleData.streets[0].id)

        notSaved = with(SampleData.streets[1]) {
            Street(
                postCode = postCode,
                name = name,
            )
        }
    }

    @Test
    fun `test equals`() {
        assertEquals(SampleData.streets[0], SampleData.streets[0])
        assertEquals(copy, copy)
        assertEquals(SampleData.streets[0], copy)
        assertEquals(copy, SampleData.streets[0])

        assertNotEquals(SampleData.streets[0], SampleData.streets[1])
        assertNotEquals(copy, SampleData.streets[1])
        assertNotEquals(SampleData.streets[1], SampleData.streets[0])
        assertNotEquals(SampleData.streets[1], copy)

        assertNotEquals<Street?>(copy, null)
        assertNotEquals<Street?>(null, SampleData.streets[0])

        assertNotEquals(SampleData.streets[1], notSaved)
        assertNotEquals(notSaved, SampleData.streets[1])
    }

    @Test
    fun `test hashCode`() {
        assertEquals(SampleData.streets[0].hashCode(), SampleData.streets[0].hashCode())
        assertEquals(copy.hashCode(), copy.hashCode())
        assertEquals(SampleData.streets[0].hashCode(), copy.hashCode())
        assertEquals(copy.hashCode(), SampleData.streets[0].hashCode())

        assertNotEquals(SampleData.streets[0].hashCode(), SampleData.streets[1].hashCode())
        assertNotEquals(copy.hashCode(), SampleData.streets[1].hashCode())
        assertNotEquals(SampleData.streets[1].hashCode(), SampleData.streets[0].hashCode())
        assertNotEquals(SampleData.streets[1].hashCode(), copy.hashCode())

        assertNotEquals(copy.hashCode(), null.hashCode())
        assertNotEquals(null.hashCode(), SampleData.streets[0].hashCode())

        assertNotEquals(SampleData.streets[1].hashCode(), notSaved.hashCode())
        assertNotEquals(notSaved.hashCode(), SampleData.streets[1].hashCode())
    }

    @Test
    fun `test toString`() {
        val cId = copy.id
        val pId = copy.postCode.id
        val cExpected = "Street(id='$cId', postCode='$pId', name='Platz der Republik')"
        val cActual = SampleData.streets[0].toString()
        assertEquals(cExpected, cActual)

        val nId = notSaved.postCode.id
        val nExpected = "Street(id='null', postCode='$nId', name='Willy-Brandt-Straße')"
        val nActual = notSaved.toString()
        assertEquals(nExpected, nActual)
    }
}
