// port-lint: source seq/coin_flipper.rs
package io.github.kotlinmania.rand.seq

import io.github.kotlinmania.rand.rngs.StepRng
import kotlin.test.Test
import kotlin.test.assertNotNull

class CoinFlipperTest {
    @Test
    fun testCoinFlipperBasic() {
        val rng = StepRng(12345uL, 999uL)
        val flipper = CoinFlipper(rng)

        val r1 = flipper.randomRatioOneOver(5)
        assertNotNull(r1)

        val r2 = flipper.randomRatio(3, 7)
        assertNotNull(r2)
    }
}
