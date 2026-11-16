// port-lint: source distr/utils.rs
package io.github.kotlinmania.rand.distr

/**
 * Trait for widening multiplication.
 */
internal interface WideningMultiply<RHS, Output> {
    public fun wmul(x: RHS): Output
}

/**
 * 8-bit unsigned widening multiplication: returns (high, low).
 */
internal fun UByte.wmul(x: UByte): Pair<UByte, UByte> {
    val tmp = this.toUInt() * x.toUInt()
    return Pair((tmp shr 8).toUByte(), tmp.toUByte())
}

/**
 * 16-bit unsigned widening multiplication: returns (high, low).
 */
internal fun UShort.wmul(x: UShort): Pair<UShort, UShort> {
    val tmp = this.toUInt() * x.toUInt()
    return Pair((tmp shr 16).toUShort(), tmp.toUShort())
}

/**
 * 32-bit unsigned widening multiplication: returns (high, low).
 */
internal fun UInt.wmul(x: UInt): Pair<UInt, UInt> {
    val tmp = this.toULong() * x.toULong()
    return Pair((tmp shr 32).toUInt(), tmp.toUInt())
}

/**
 * 64-bit unsigned widening multiplication: returns (high, low).
 *
 * Implements widening multiplication `(a * b)` producing a 128-bit result represented
 * as a pair of 64-bit halves `(high, low)` without requiring 128-bit hardware arithmetic.
 */
internal fun ULong.wmul(b: ULong): Pair<ULong, ULong> {
    val lowerMask: ULong = 0xFFFF_FFFFuL
    val half = 32
    var low = (this and lowerMask) * (b and lowerMask)
    var t = low shr half
    low = low and lowerMask
    t += (this shr half) * (b and lowerMask)
    low += (t and lowerMask) shl half
    var high = t shr half
    t = low shr half
    low = low and lowerMask
    t += (b shr half) * (this and lowerMask)
    low += (t and lowerMask) shl half
    high += t shr half
    high += (this shr half) * (b shr half)
    return Pair(high, low)
}

/**
 * Helper trait when dealing with scalar and SIMD floating point types.
 */
internal interface FloatSIMDUtils<Mask, UIntType> {
    public fun allLt(other: FloatSIMDUtils<Mask, UIntType>): Boolean

    public fun allLe(other: FloatSIMDUtils<Mask, UIntType>): Boolean

    public fun allFinite(): Boolean

    public fun gtMask(other: FloatSIMDUtils<Mask, UIntType>): Mask

    public fun decreaseMasked(mask: Mask): FloatSIMDUtils<Mask, UIntType>

    public fun castFromInt(i: UIntType): FloatSIMDUtils<Mask, UIntType>
}

/**
 * Helper trait for scalar operations on float SIMD types.
 */
internal interface FloatSIMDScalarUtils<Scalar, Mask, UIntType> : FloatSIMDUtils<Mask, UIntType> {
    public fun replace(index: Int, newValue: Scalar): FloatSIMDUtils<Mask, UIntType>

    public fun extractLane(index: Int): Scalar
}

/**
 * Implement functions on Float/Double to give them APIs similar to SIMD types.
 */
internal interface FloatAsSIMD {
    public fun splat(scalar: Float): Float = scalar

    public fun splat(scalar: Double): Double = scalar

    public companion object {
        public const val LEN: Int = 1
    }
}

/**
 * Implement functions on UInt/ULong to give them APIs similar to SIMD types.
 */
internal interface IntAsSIMD {
    public fun splat(scalar: UInt): UInt = scalar

    public fun splat(scalar: ULong): ULong = scalar
}

/**
 * Implement functions on Boolean to give them APIs similar to SIMD types.
 */
internal interface BoolAsSIMD {
    public fun any(): Boolean
}

internal fun Boolean.any(): Boolean = this

internal fun Float.allLt(other: Float): Boolean = this < other

internal fun Float.allLe(other: Float): Boolean = this <= other

internal fun Float.allFinite(): Boolean = !this.isNaN() && !this.isInfinite()

internal fun Float.gtMask(other: Float): Boolean = this > other

internal fun Float.decreaseMasked(mask: Boolean): Float {
    check(mask) { "At least one lane must be set" }
    return Float.fromBits(this.toBits() - 1)
}

internal fun Float.Companion.castFromInt(i: UInt): Float = i.toFloat()

internal fun Double.allLt(other: Double): Boolean = this < other

internal fun Double.allLe(other: Double): Boolean = this <= other

internal fun Double.allFinite(): Boolean = !this.isNaN() && !this.isInfinite()

internal fun Double.gtMask(other: Double): Boolean = this > other

internal fun Double.decreaseMasked(mask: Boolean): Double {
    check(mask) { "At least one lane must be set" }
    return Double.fromBits(this.toBits() - 1L)
}

internal fun Double.Companion.castFromInt(i: ULong): Double = i.toDouble()
