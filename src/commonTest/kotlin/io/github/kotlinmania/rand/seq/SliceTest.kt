// port-lint: tests seq/slice.rs
package io.github.kotlinmania.rand.seq

import io.github.kotlinmania.rand.rngs.SmallRng
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SliceTest {
    @Test
    fun testChoose() {
        val rng = SmallRng.seedFromU64(200uL)
        val list = listOf("a", "b", "c", "d")
        val chosen = list.choose(rng)
        assertTrue(chosen in list)

        val empty = emptyList<String>()
        assertNull(empty.choose(rng))
    }

    @Test
    fun testChooseMultiple() {
        val rng = SmallRng.seedFromU64(201uL)
        val list = (1..100).toList()
        val chosen = list.chooseMultiple(rng, 5)
        assertEquals(5, chosen.size)
        assertEquals(5, chosen.toSet().size)
        assertTrue(chosen.all { it in list })
    }

    @Test
    fun testChooseWeighted() {
        val rng = SmallRng.seedFromU64(202uL)
        val items = listOf("rare" to 1.0, "common" to 100.0)
        var commonCount = 0
        for (i in 0 until 1000) {
            val chosen = items.chooseWeighted(rng) { it.second }
            if (chosen?.first == "common") commonCount++
        }
        assertTrue(commonCount > 900, "Expected common to be selected most of the time, got $commonCount")
    }

    @Test
    fun testShuffle() {
        val rng = SmallRng.seedFromU64(203uL)
        val original = (1..50).toMutableList()
        val copy = original.toMutableList()
        copy.shuffle(rng)
        assertEquals(original.size, copy.size)
        assertEquals(original.toSet(), copy.toSet())
        assertNotEquals(original, copy)
    }
}
