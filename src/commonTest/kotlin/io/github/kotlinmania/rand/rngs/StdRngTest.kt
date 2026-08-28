// port-lint: tests rngs/std.rs
package io.github.kotlinmania.rand.rngs

import kotlin.test.Test
import kotlin.test.assertEquals

class StdRngTest {
    @Test
    fun testStdRngConstruction() {
        val seed = ByteArray(32)
        seed[0] = 1
        seed[4] = 23
        seed[8] = 200.toByte()
        seed[9] = 1
        seed[12] = 210.toByte()
        seed[13] = 30

        val rng0 = StdRng.fromSeed(seed)
        val x0 = rng0.nextU64()

        val rng1 = StdRng.fromRng(rng0)
        val x1 = rng1.nextU64()

        assertEquals(10719222850664546238uL, x0)
        assertEquals(14064965282130556830uL, x1)
    }
}
