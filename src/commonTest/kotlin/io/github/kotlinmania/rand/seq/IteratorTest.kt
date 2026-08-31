// port-lint: tests rand/src/seq/iterator.rs
package io.github.kotlinmania.rand.seq

import io.github.kotlinmania.rand.rngs.SmallRng
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class IteratorTest {
    @Test
    fun testSequenceChoose() {
        val rng = SmallRng.seedFromU64(300uL)
        val seq = generateSequence(1) { if (it < 100) it + 1 else null }
        val chosen = seq.choose(rng)
        assertTrue(chosen != null && chosen in 1..100)

        val empty = emptySequence<Int>()
        assertNull(empty.choose(rng))
    }

    @Test
    fun testSequenceChooseMultiple() {
        val rng = SmallRng.seedFromU64(301uL)
        val seq = generateSequence(1) { if (it < 50) it + 1 else null }
        val chosen = seq.chooseMultiple(rng, 10)
        assertEquals(10, chosen.size)
        assertEquals(10, chosen.toSet().size)
        assertTrue(chosen.all { it in 1..50 })
    }
}
