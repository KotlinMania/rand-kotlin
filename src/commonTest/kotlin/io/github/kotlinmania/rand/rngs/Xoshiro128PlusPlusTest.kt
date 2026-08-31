// port-lint: tests rand/src/rngs/xoshiro128plusplus.rs
package io.github.kotlinmania.rand.rngs

import kotlin.test.Test
import kotlin.test.assertEquals

class Xoshiro128PlusPlusTest {
    @Test
    fun testReference() {
        val seed = byteArrayOf(1, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 4, 0, 0, 0)
        val rng = Xoshiro128PlusPlus.fromSeed(seed)
        val expected =
            uintArrayOf(
                641u, 1573767u, 3222811527u, 3517856514u, 836907274u,
                4247214768u, 3867114732u, 1355841295u, 495546011u, 621204420u,
            )
        for (e in expected) {
            assertEquals(e, rng.nextU32())
        }
    }

    @Test
    fun testStableSeedFromU64AndFromSeed() {
        val rng = Xoshiro128PlusPlus.seedFromU64(0uL)
        val rngFromSeed0 = Xoshiro128PlusPlus.fromSeed(ByteArray(16))
        val expected =
            uintArrayOf(
                1179900579u, 1938959192u, 3089844957u, 3657088315u, 1015453891u,
                479942911u, 3433842246u, 669252886u, 3985671746u, 2737205563u,
            )
        for (e in expected) {
            val v = rng.nextU32()
            assertEquals(e, v)
            assertEquals(v, rngFromSeed0.nextU32())
        }
    }
}
