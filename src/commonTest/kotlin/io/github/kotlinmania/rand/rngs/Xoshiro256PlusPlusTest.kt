// port-lint: tests rngs/xoshiro256plusplus.rs
package io.github.kotlinmania.rand.rngs

import kotlin.test.Test
import kotlin.test.assertEquals

class Xoshiro256PlusPlusTest {
    @Test
    fun testReference() {
        val seed = ByteArray(32)
        seed[0] = 1
        seed[8] = 2
        seed[16] = 3
        seed[24] = 4

        val rng = Xoshiro256PlusPlus.fromSeed(seed)
        val expected =
            ulongArrayOf(
                41943041uL,
                58720359uL,
                3588806011781223uL,
                3591011842654386uL,
                9228616714210784205uL,
                9973669472204895162uL,
                14011001112246962877uL,
                12406186145184390807uL,
                15849039046786891736uL,
                10450023813501588000uL,
            )

        for (e in expected) {
            assertEquals(e, rng.nextU64())
        }
    }

    @Test
    fun testStableSeedFromU64AndFromSeed() {
        val rng = Xoshiro256PlusPlus.seedFromU64(0uL)
        val rngFromSeed0 = Xoshiro256PlusPlus.fromSeed(ByteArray(32))

        val expected =
            ulongArrayOf(
                5987356902031041503uL,
                7051070477665621255uL,
                6633766593972829180uL,
                211316841551650330uL,
                9136120204379184874uL,
                379361710973160858uL,
                15813423377499357806uL,
                15596884590815070553uL,
                5439680534584881407uL,
                1369371744833522710uL,
            )

        for (e in expected) {
            assertEquals(e, rng.nextU64())
            assertEquals(e, rngFromSeed0.nextU64())
        }
    }
}
