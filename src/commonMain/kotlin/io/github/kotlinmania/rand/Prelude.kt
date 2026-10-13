// port-lint: source prelude.rs
package io.github.kotlinmania.rand

import io.github.kotlinmania.rand.rngs.ThreadRng

/**
 * Convenience re-exports of common Rand members.
 */
public object Prelude {
    public fun rng(): ThreadRng = Rand.rng()
}
