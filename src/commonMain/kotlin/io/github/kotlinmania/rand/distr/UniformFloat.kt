// port-lint: source distr/uniform_float.rs
package io.github.kotlinmania.rand.distr

import io.github.kotlinmania.rand.Rng

/**
 * The back-end implementing [UniformSampler] for floating-point types.
 *
 * Unless you are implementing [UniformSampler] for your own type, this type
 * should not be used directly, use [Uniform] instead.
 */
public data class UniformFloat<X>(
    public val low: X,
    public val scale: X,
    private val sampler: (Rng) -> X,
) : UniformSampler<X> {
    override fun sample(rng: Rng): X = sampler(rng)

    public companion object {
        private val FLOAT_EPSILON: Float = Float.fromBits(0x3400_0000)
        private val DOUBLE_EPSILON: Double = Double.fromBits(0x3CB0_0000_0000_0000L)

        internal fun newBounded(low: Float, high: Float, initialScale: Float): UniformFloat<Float> {
            val maxRand = 1.0f - FLOAT_EPSILON
            var scale = initialScale
            while (true) {
                val mask = (scale * maxRand + low) > high
                if (!mask) break
                scale = scale.decreaseMasked(mask)
            }
            return UniformFloat(low, scale) { rng ->
                val value1to2 = Float.fromBits(((rng.nextU32() shr 9) or 0x3F80_0000u).toInt())
                val value0to1 = value1to2 - 1.0f
                value0to1 * scale + low
            }
        }

        internal fun newBounded(low: Double, high: Double, initialScale: Double): UniformFloat<Double> {
            val maxRand = 1.0 - DOUBLE_EPSILON
            var scale = initialScale
            while (true) {
                val mask = (scale * maxRand + low) > high
                if (!mask) break
                scale = scale.decreaseMasked(mask)
            }
            return UniformFloat(low, scale) { rng ->
                val value1to2 = Double.fromBits(((rng.nextU64() shr 12) or 0x3FF0_0000_0000_0000uL).toLong())
                val value0to1 = value1to2 - 1.0
                value0to1 * scale + low
            }
        }

        public fun new(low: Float, high: Float): Result<UniformFloat<Float>, Error> {
            if (!low.isFinite() || !high.isFinite()) return Result.Err(Error.NonFinite)
            if (!(low < high)) return Result.Err(Error.EmptyRange)
            val scale = high - low
            if (!scale.isFinite()) return Result.Err(Error.NonFinite)
            return Result.Ok(newBounded(low, high, scale))
        }

        public fun newInclusive(low: Float, high: Float): Result<UniformFloat<Float>, Error> {
            if (!low.isFinite() || !high.isFinite()) return Result.Err(Error.NonFinite)
            if (!(low <= high)) return Result.Err(Error.EmptyRange)
            val maxRand = 1.0f - FLOAT_EPSILON
            val scale = (high - low) / maxRand
            if (!scale.isFinite()) return Result.Err(Error.NonFinite)
            return Result.Ok(newBounded(low, high, scale))
        }

        public fun sampleSingle(low: Float, high: Float, rng: Rng): Result<Float, Error> =
            sampleSingleInclusive(low, high, rng)

        public fun sampleSingleInclusive(low: Float, high: Float, rng: Rng): Result<Float, Error> {
            if (!low.isFinite() || !high.isFinite()) return Result.Err(Error.NonFinite)
            if (!(low <= high)) return Result.Err(Error.EmptyRange)
            val scale = high - low
            if (!scale.isFinite()) return Result.Err(Error.NonFinite)
            val value1to2 = Float.fromBits(((rng.nextU32() shr 9) or 0x3F80_0000u).toInt())
            val value0to1 = value1to2 - 1.0f
            return Result.Ok(value0to1 * scale + low)
        }

        public fun new(low: Double, high: Double): Result<UniformFloat<Double>, Error> {
            if (!low.isFinite() || !high.isFinite()) return Result.Err(Error.NonFinite)
            if (!(low < high)) return Result.Err(Error.EmptyRange)
            val scale = high - low
            if (!scale.isFinite()) return Result.Err(Error.NonFinite)
            return Result.Ok(newBounded(low, high, scale))
        }

        public fun newInclusive(low: Double, high: Double): Result<UniformFloat<Double>, Error> {
            if (!low.isFinite() || !high.isFinite()) return Result.Err(Error.NonFinite)
            if (!(low <= high)) return Result.Err(Error.EmptyRange)
            val maxRand = 1.0 - DOUBLE_EPSILON
            val scale = (high - low) / maxRand
            if (!scale.isFinite()) return Result.Err(Error.NonFinite)
            return Result.Ok(newBounded(low, high, scale))
        }

        public fun sampleSingle(low: Double, high: Double, rng: Rng): Result<Double, Error> =
            sampleSingleInclusive(low, high, rng)

        public fun sampleSingleInclusive(low: Double, high: Double, rng: Rng): Result<Double, Error> {
            if (!low.isFinite() || !high.isFinite()) return Result.Err(Error.NonFinite)
            if (!(low <= high)) return Result.Err(Error.EmptyRange)
            val scale = high - low
            if (!scale.isFinite()) return Result.Err(Error.NonFinite)
            val value1to2 = Double.fromBits(((rng.nextU64() shr 12) or 0x3FF0_0000_0000_0000uL).toLong())
            val value0to1 = value1to2 - 1.0
            return Result.Ok(value0to1 * scale + low)
        }
    }
}
