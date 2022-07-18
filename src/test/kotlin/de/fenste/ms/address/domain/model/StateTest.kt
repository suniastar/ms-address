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
class StateTest(
    @Autowired private val testEntityManager: TestEntityManager,
) {
    private lateinit var copy: State
    private lateinit var notSaved: State

    @BeforeEach
    fun `set up`() {
        SampleData.reset(testEntityManager)

        copy = testEntityManager.find(State::class.java, SampleData.states[0].id)

        notSaved = with(SampleData.states[1]) {
            State(
                country = country,
                name = name,
            )
        }
    }

    @Test
    fun `test equals`() {
        assertEquals(SampleData.states[0], SampleData.states[0])
        assertEquals(copy, copy)
        assertEquals(SampleData.states[0], copy)
        assertEquals(copy, SampleData.states[0])

        assertNotEquals(SampleData.states[0], SampleData.states[1])
        assertNotEquals(copy, SampleData.states[1])
        assertNotEquals(SampleData.states[1], SampleData.states[0])
        assertNotEquals(SampleData.states[1], copy)

        assertNotEquals<State?>(copy, null)
        assertNotEquals<State?>(null, SampleData.states[0])

        assertNotEquals(SampleData.states[1], notSaved)
        assertNotEquals(notSaved, SampleData.states[1])
    }

    @Test
    fun `test hashCode`() {
        assertEquals(SampleData.states[0].hashCode(), SampleData.states[0].hashCode())
        assertEquals(copy.hashCode(), copy.hashCode())
        assertEquals(SampleData.states[0].hashCode(), copy.hashCode())
        assertEquals(copy.hashCode(), SampleData.states[0].hashCode())

        assertNotEquals(SampleData.states[0].hashCode(), SampleData.states[1].hashCode())
        assertNotEquals(copy.hashCode(), SampleData.states[1].hashCode())
        assertNotEquals(SampleData.states[1].hashCode(), SampleData.states[0].hashCode())
        assertNotEquals(SampleData.states[1].hashCode(), copy.hashCode())

        assertNotEquals(copy.hashCode(), null.hashCode())
        assertNotEquals(null.hashCode(), SampleData.states[0].hashCode())

        assertNotEquals(SampleData.states[1].hashCode(), notSaved.hashCode())
        assertNotEquals(notSaved.hashCode(), SampleData.states[1].hashCode())
    }

    @Test
    fun `test toString`() {
        val cId = copy.id
        val pId = copy.country.id
        val cExpected = "State(id='$cId', country='$pId', name='Berlin')"
        val cActual = SampleData.states[0].toString()
        assertEquals(cExpected, cActual)

        val nId = notSaved.country.id
        val nExpected = "State(id='null', country='$nId', name='Baden-Württemberg')"
        val nActual = notSaved.toString()
        assertEquals(nExpected, nActual)
    }
}
