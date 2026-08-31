// port-lint: source distr/weighted/mod.rs
package io.github.kotlinmania.rand.distr.weighted

/**
 * Invalid weight errors from weighted index operations.
 */
public sealed class WeightError(message: String) : Exception(message) {
    public class InvalidInput(message: String = "Weights sequence is empty/too long/unordered") : WeightError(message)
    public class InvalidWeight(message: String = "A weight is negative, too large or not a valid number") : WeightError(message)
    public class InsufficientNonZero(message: String = "Not enough weights > zero") : WeightError(message)
    public class Overflow(message: String = "Overflow when summing weights") : WeightError(message)
}
