# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 21/31 (67.7%)
- **Function parity:** 84/290 matched (target 237) — 29.0%
- **Class/type parity:** 17/83 matched (target 51) — 20.5%
- **Combined symbol parity:** 101/373 matched (target 288) — 27.1%
- **Average inline-code cosine:** 0.28 (function body across 18 matched files)
- **Average documentation cosine:** 0.43 (doc text across 18 matched files)
- **Cheat-zeroed Files:** 3
- **Critical Issues:** 20 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

### 1. rng
- **Similarity:** 0.17 (needs 68% improvement)
- **Dependencies:** 17
- **Priority Score:** 17212808.0
- **Functions:** 6/26 matched (target 18)
- **Missing functions:** `random`, `random_iter`, `r#gen`, `gen_range`, `gen_bool`, `gen_ratio`, `__unsafe`, `test_fill_bytes_default`, `test_fill`, `test_fill_empty`, `test_random_range_int`, `test_random_range_float`, `test_random_range_panic_int`, `test_random_range_panic_usize`, `test_random_bool`, `test_rng_mut_ref`, `use_rng`, `test_rng_trait_object`, `test_rng_boxed_trait`, `test_gen_ratio_average`
- **Types:** 1/2 matched (target 7)
- **Missing types:** `Fill`
- **Symbol Deficit:** 21 (functions: 20, types: 1)
- **Missing Tests:** 13 of 13 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. rng

- **Target:** `rand.Rng [PROVENANCE-FALLBACK]`
- **Similarity:** 0.17
- **Dependents:** 17
- **Priority Score:** 17212808.0
- **Functions:** 6/26 matched (target 18)
- **Missing functions:** `random`, `random_iter`, `r#gen`, `gen_range`, `gen_bool`, `gen_ratio`, `__unsafe`, `test_fill_bytes_default`, `test_fill`, `test_fill_empty`, `test_random_range_int`, `test_random_range_float`, `test_random_range_panic_int`, `test_random_range_panic_usize`, `test_random_bool`, `test_rng_mut_ref`, `use_rng`, `test_rng_trait_object`, `test_rng_boxed_trait`, `test_gen_ratio_average`
- **Types:** 1/2 matched (target 7)
- **Missing types:** `Fill`
- **Tests:** 0/13 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rand/src/rng.rs` vs expected `rng.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:rand/src/rng.rs` vs expected `rng.rs`
- **Proposed provenance header:** `// port-lint: source rng.rs` (current: `// port-lint: source rand/src/rng.rs`)
- **Proposed provenance header:** `// port-lint: tests rng.rs` (current: `// port-lint: tests rand/src/rng.rs`)
- **Lint issues:** 2

### 2. distr.distribution

- **Target:** `distr.Distribution [PROVENANCE-FALLBACK]`
- **Similarity:** 0.41
- **Dependents:** 6
- **Priority Score:** 6071606.0
- **Functions:** 7/11 matched (target 8)
- **Missing functions:** `size_hint`, `test_make_an_iter`, `ten_dice_rolls_other_than_five`, `test_dist_string`
- **Types:** 2/5 matched (target 4)
- **Missing types:** `Distribution`, `Item`, `Map`
- **Tests:** 2/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rand/src/distr/distribution.rs` vs expected `distr/distribution.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:rand/src/distr/distribution.rs` vs expected `distr/distribution.rs`
- **Proposed provenance header:** `// port-lint: source distr/distribution.rs` (current: `// port-lint: source rand/src/distr/distribution.rs`)
- **Proposed provenance header:** `// port-lint: tests distr/distribution.rs` (current: `// port-lint: tests rand/src/distr/distribution.rs`)
- **Lint issues:** 2

### 3. distr.uniform

- **Target:** `distr.Uniform [PROVENANCE-FALLBACK]`
- **Similarity:** 0.04
- **Dependents:** 1
- **Priority Score:** 1222409.6
- **Functions:** 1/14 matched (target 23)
- **Missing functions:** `fmt`, `new`, `new_inclusive`, `sample_single`, `sample_single_inclusive`, `try_from`, `borrow`, `is_empty`, `test_uniform_serialization`, `test_custom_uniform`, `value_stability`, `test_samples`, `uniform_distributions_can_be_compared`
- **Types:** 1/10 matched (target 2)
- **Missing types:** `Error`, `SampleUniform`, `UniformSampler`, `SampleBorrow`, `SampleRange`, `MyF32`, `UniformMyF32`, `X`, `Sampler`
- **Tests:** 0/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rand/src/distr/uniform.rs` vs expected `distr/uniform.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:rand/src/distr/uniform.rs` vs expected `distr/uniform.rs`
- **Proposed provenance header:** `// port-lint: source distr/uniform.rs` (current: `// port-lint: source rand/src/distr/uniform.rs`)
- **Proposed provenance header:** `// port-lint: tests distr/uniform.rs` (current: `// port-lint: tests rand/src/distr/uniform.rs`)
- **Lint issues:** 2

### 4. seq.slice

- **Target:** `seq.Slice [PROVENANCE-FALLBACK]`
- **Similarity:** 0.14
- **Dependents:** 1
- **Priority Score:** 1212708.6
- **Functions:** 6/22 matched (target 21)
- **Missing functions:** `is_empty`, `choose_multiple_array`, `choose_mut`, `choose_weighted_mut`, `len`, `partial_shuffle`, `next`, `size_hint`, `test_slice_choose`, `value_stability_slice`, `move_last`, `test_partial_shuffle`, `test_weighted`, `get_weight`, `test_multiple_weighted_edge_cases`, `test_multiple_weighted_distributions`
- **Types:** 0/5 matched (target 1)
- **Missing types:** `IndexedRandom`, `IndexedMutRandom`, `SliceRandom`, `SliceChooseIter`, `Item`
- **Tests:** 1/9 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rand/src/seq/slice.rs` vs expected `seq/slice.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:rand/src/seq/slice.rs` vs expected `seq/slice.rs`
- **Proposed provenance header:** `// port-lint: source seq/slice.rs` (current: `// port-lint: source rand/src/seq/slice.rs`)
- **Proposed provenance header:** `// port-lint: tests seq/slice.rs` (current: `// port-lint: tests rand/src/seq/slice.rs`)
- **Lint issues:** 2

### 5. seq.index

- **Target:** `seq.Index [PROVENANCE-FALLBACK]`
- **Similarity:** 0.28
- **Dependents:** 1
- **Priority Score:** 1183307.2
- **Functions:** 14/26 matched (target 30)
- **Missing functions:** `iter`, `into_iter`, `eq`, `next`, `size_hint`, `partial_cmp`, `cmp`, `zero`, `one`, `as_usize`, `test_serialization_index_vec`, `value_stability_sample`
- **Types:** 1/7 matched (target 4)
- **Missing types:** `IntoIter`, `Item`, `IndexVecIter`, `IndexVecIntoIter`, `Element`, `UInt`
- **Tests:** 3/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rand/src/seq/index.rs` vs expected `seq/index.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:rand/src/seq/index.rs` vs expected `seq/index.rs`
- **Proposed provenance header:** `// port-lint: source seq/index.rs` (current: `// port-lint: source rand/src/seq/index.rs`)
- **Proposed provenance header:** `// port-lint: tests seq/index.rs` (current: `// port-lint: tests rand/src/seq/index.rs`)
- **Lint issues:** 2

### 6. weighted.weighted_index

- **Target:** `weighted.WeightedIndex [PROVENANCE-FALLBACK]`
- **Similarity:** 0.16
- **Dependents:** 1
- **Priority Score:** 1162308.4
- **Functions:** 6/20 matched (target 14)
- **Missing functions:** `new`, `fmt`, `clone`, `next`, `test_weightedindex_serde`, `test_accepting_nan`, `test_weightedindex`, `test_update_weights_errors`, `test_weight_at`, `test_weights`, `value_stability`, `test_samples`, `weighted_index_distributions_can_be_compared`, `overflow`
- **Types:** 1/3 matched
- **Missing types:** `WeightedIndexIter`, `Item`
- **Tests:** 1/11 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rand/src/distr/weighted/weighted_index.rs` vs expected `distr/weighted/weighted_index.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:rand/src/distr/weighted/weighted_index.rs` vs expected `distr/weighted/weighted_index.rs`
- **Proposed provenance header:** `// port-lint: source distr/weighted/weighted_index.rs` (current: `// port-lint: source rand/src/distr/weighted/weighted_index.rs`)
- **Proposed provenance header:** `// port-lint: tests distr/weighted/weighted_index.rs` (current: `// port-lint: tests rand/src/distr/weighted/weighted_index.rs`)
- **Lint issues:** 2

### 7. seq.increasing_uniform

- **Target:** `seq.IncreasingUniform [PROVENANCE-FALLBACK]`
- **Similarity:** 0.33
- **Dependents:** 1
- **Priority Score:** 1020506.8
- **Functions:** 2/4 matched
- **Missing functions:** `new`, `inner`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rand/src/seq/increasing_uniform.rs` vs expected `seq/increasing_uniform.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:rand/src/seq/increasing_uniform.rs` vs expected `seq/increasing_uniform.rs`
- **Proposed provenance header:** `// port-lint: source seq/increasing_uniform.rs` (current: `// port-lint: source rand/src/seq/increasing_uniform.rs`)
- **Proposed provenance header:** `// port-lint: tests seq/increasing_uniform.rs` (current: `// port-lint: tests rand/src/seq/increasing_uniform.rs`)
- **Lint issues:** 2

### 8. seq.coin_flipper

- **Target:** `seq.CoinFlipper [PROVENANCE-FALLBACK]`
- **Similarity:** 0.55
- **Dependents:** 1
- **Priority Score:** 1010504.5
- **Functions:** 3/4 matched
- **Missing functions:** `new`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rand/src/seq/coin_flipper.rs` vs expected `seq/coin_flipper.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:rand/src/seq/coin_flipper.rs` vs expected `seq/coin_flipper.rs`
- **Proposed provenance header:** `// port-lint: source seq/coin_flipper.rs` (current: `// port-lint: source rand/src/seq/coin_flipper.rs`)
- **Proposed provenance header:** `// port-lint: tests seq/coin_flipper.rs` (current: `// port-lint: tests rand/src/seq/coin_flipper.rs`)
- **Lint issues:** 2

### 9. seq.iterator

- **Target:** `seq.Iterator [PROVENANCE-FALLBACK]`
- **Similarity:** 0.13
- **Dependents:** 0
- **Priority Score:** 182008.7
- **Functions:** 2/15 matched (target 8)
- **Missing functions:** `choose_stable`, `choose_multiple_fill`, `next`, `size_hint`, `test_iterator_choose`, `test_iter`, `test_iterator_choose_stable`, `test_iterator_choose_stable_stability`, `test_sample_iter`, `value_stability_choose`, `value_stability_choose_stable`, `value_stability_choose_multiple`, `do_test`
- **Types:** 0/5 matched (target 1)
- **Missing types:** `IteratorRandom`, `UnhintedIterator`, `Item`, `ChunkHintedIterator`, `WindowHintedIterator`
- **Tests:** 0/11 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rand/src/seq/iterator.rs` vs expected `seq/iterator.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:rand/src/seq/iterator.rs` vs expected `seq/iterator.rs`
- **Proposed provenance header:** `// port-lint: source seq/iterator.rs` (current: `// port-lint: source rand/src/seq/iterator.rs`)
- **Proposed provenance header:** `// port-lint: tests seq/iterator.rs` (current: `// port-lint: tests rand/src/seq/iterator.rs`)
- **Lint issues:** 2

### 10. lib

- **Target:** `rand.Lib [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 111610.0
- **Functions:** 5/15 matched (target 25)
- **Missing functions:** `thread_rng`, `random_iter`, `rng`, `const_rng`, `step_rng`, `next_u32`, `next_u64`, `fill_bytes`, `test_random`, `test_range`
- **Types:** 0/1 matched (target 2)
- **Missing types:** `StepRng`
- **Tests:** 0/8 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rand/src/lib.rs` vs expected `lib.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:rand/src/lib.rs` vs expected `lib.rs`
- **Proposed provenance header:** `// port-lint: source lib.rs` (current: `// port-lint: source rand/src/lib.rs`)
- **Proposed provenance header:** `// port-lint: tests lib.rs` (current: `// port-lint: tests rand/src/lib.rs`)
- **Lint issues:** 2

### 11. distr.bernoulli

- **Target:** `distr.Bernoulli [PROVENANCE-FALLBACK]`
- **Similarity:** 0.34
- **Dependents:** 0
- **Priority Score:** 71206.6
- **Functions:** 4/10 matched (target 7)
- **Missing functions:** `fmt`, `new`, `test_serializing_deserializing_bernoulli`, `test_average`, `value_stability`, `bernoulli_distributions_can_be_compared`
- **Types:** 1/2 matched
- **Missing types:** `BernoulliError`
- **Tests:** 1/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rand/src/distr/bernoulli.rs` vs expected `distr/bernoulli.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:rand/src/distr/bernoulli.rs` vs expected `distr/bernoulli.rs`
- **Proposed provenance header:** `// port-lint: source distr/bernoulli.rs` (current: `// port-lint: source rand/src/distr/bernoulli.rs`)
- **Proposed provenance header:** `// port-lint: tests distr/bernoulli.rs` (current: `// port-lint: tests rand/src/distr/bernoulli.rs`)
- **Lint issues:** 2

### 12. distr.slice

- **Target:** `distr.Slice [PROVENANCE-FALLBACK]`
- **Similarity:** 0.10
- **Dependents:** 0
- **Priority Score:** 60809.0
- **Functions:** 1/6 matched (target 3)
- **Missing functions:** `new`, `num_choices`, `fmt`, `append_string`, `value_stability`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Empty`
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rand/src/distr/slice.rs` vs expected `distr/slice.rs`
- **Proposed provenance header:** `// port-lint: source distr/slice.rs` (current: `// port-lint: source rand/src/distr/slice.rs`)
- **Lint issues:** 1

### 13. distr.other

- **Target:** `distr.Other [PROVENANCE-FALLBACK]`
- **Similarity:** 0.39
- **Dependents:** 0
- **Priority Score:** 41006.1
- **Functions:** 4/8 matched (target 28)
- **Missing functions:** `test_misc`, `test_chars`, `value_stability`, `test_samples`
- **Types:** 2/2 matched (target 6)
- **Missing types:** _none_
- **Tests:** 2/6 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rand/src/distr/other.rs` vs expected `distr/other.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:rand/src/distr/other.rs` vs expected `distr/other.rs`
- **Proposed provenance header:** `// port-lint: source distr/other.rs` (current: `// port-lint: source rand/src/distr/other.rs`)
- **Proposed provenance header:** `// port-lint: tests distr/other.rs` (current: `// port-lint: tests rand/src/distr/other.rs`)
- **Lint issues:** 2

### 14. rngs.xoshiro128plusplus

- **Target:** `rngs.Xoshiro128PlusPlus [PROVENANCE-FALLBACK]`
- **Similarity:** 0.27
- **Dependents:** 0
- **Priority Score:** 30907.3
- **Functions:** 5/7 matched (target 9)
- **Missing functions:** `reference`, `stable_seed_from_u64_and_from_seed`
- **Types:** 1/2 matched
- **Missing types:** `Seed`
- **Tests:** 0/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rand/src/rngs/xoshiro128plusplus.rs` vs expected `rngs/xoshiro128plusplus.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:rand/src/rngs/xoshiro128plusplus.rs` vs expected `rngs/xoshiro128plusplus.rs`
- **Proposed provenance header:** `// port-lint: source rngs/xoshiro128plusplus.rs` (current: `// port-lint: source rand/src/rngs/xoshiro128plusplus.rs`)
- **Proposed provenance header:** `// port-lint: tests rngs/xoshiro128plusplus.rs` (current: `// port-lint: tests rand/src/rngs/xoshiro128plusplus.rs`)
- **Lint issues:** 2

### 15. rngs.xoshiro256plusplus

- **Target:** `rngs.Xoshiro256PlusPlus [PROVENANCE-FALLBACK]`
- **Similarity:** 0.30
- **Dependents:** 0
- **Priority Score:** 30907.0
- **Functions:** 5/7 matched (target 9)
- **Missing functions:** `reference`, `stable_seed_from_u64_and_from_seed`
- **Types:** 1/2 matched
- **Missing types:** `Seed`
- **Tests:** 0/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rand/src/rngs/xoshiro256plusplus.rs` vs expected `rngs/xoshiro256plusplus.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:rand/src/rngs/xoshiro256plusplus.rs` vs expected `rngs/xoshiro256plusplus.rs`
- **Proposed provenance header:** `// port-lint: source rngs/xoshiro256plusplus.rs` (current: `// port-lint: source rand/src/rngs/xoshiro256plusplus.rs`)
- **Proposed provenance header:** `// port-lint: tests rngs/xoshiro256plusplus.rs` (current: `// port-lint: tests rand/src/rngs/xoshiro256plusplus.rs`)
- **Lint issues:** 2

### 16. weighted.mod

- **Target:** `weighted.Mod [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 30310.0
- **Functions:** 0/1 matched (target 0)
- **Missing functions:** `fmt`
- **Types:** 0/2 matched (target 5)
- **Missing types:** `Weight`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rand/src/distr/weighted/mod.rs` vs expected `distr/weighted/mod.rs`
- **Proposed provenance header:** `// port-lint: source distr/weighted/mod.rs` (current: `// port-lint: source rand/src/distr/weighted/mod.rs`)
- **Lint issues:** 1

### 17. rngs.small

- **Target:** `rngs.Small [PROVENANCE-FALLBACK]`
- **Similarity:** 0.78
- **Dependents:** 0
- **Priority Score:** 20802.2
- **Functions:** 5/5 matched (target 8)
- **Missing functions:** _none_
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Rng`, `Seed`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rand/src/rngs/small.rs` vs expected `rngs/small.rs`
- **Proposed provenance header:** `// port-lint: source rngs/small.rs` (current: `// port-lint: source rand/src/rngs/small.rs`)
- **Lint issues:** 1

### 18. rngs.std

- **Target:** `rngs.Std [PROVENANCE-FALLBACK]`
- **Similarity:** 0.36
- **Dependents:** 0
- **Priority Score:** 10706.4
- **Functions:** 5/5 matched (target 11)
- **Missing functions:** _none_
- **Types:** 1/2 matched
- **Missing types:** `Seed`
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rand/src/rngs/std.rs` vs expected `rngs/std.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:rand/src/rngs/std.rs` vs expected `rngs/std.rs`
- **Proposed provenance header:** `// port-lint: source rngs/std.rs` (current: `// port-lint: source rand/src/rngs/std.rs`)
- **Proposed provenance header:** `// port-lint: tests rngs/std.rs` (current: `// port-lint: tests rand/src/rngs/std.rs`)
- **Lint issues:** 2

### 19. rngs.mock

- **Target:** `rngs.Mock [PROVENANCE-FALLBACK]`
- **Similarity:** 0.28
- **Dependents:** 0
- **Priority Score:** 10507.2
- **Functions:** 3/4 matched
- **Missing functions:** `new`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rand/src/rngs/mock.rs` vs expected `rngs/mock.rs`
- **Proposed provenance header:** `// port-lint: source rngs/mock.rs` (current: `// port-lint: source rand/src/rngs/mock.rs`)
- **Lint issues:** 1

### 20. rngs.mod

- **Target:** `rngs.Os [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 3)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rand/src/rngs/mod.rs` vs expected `rngs/mod.rs`
- **Proposed provenance header:** `// port-lint: source rngs/mod.rs` (current: `// port-lint: source rand/src/rngs/mod.rs`)
- **Lint issues:** 1

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

## Reexport / Wiring Modules

These files match `reexport_modules` patterns in `.ast_distance_config.json`. They are filtered out of
normal priority and missing-file ladders because they are wiring
modules, not direct logic ports. Consult them for call-site routing;
do not treat them as the next implementation target by default.

### Matched

| Source | Target | Path |
|--------|--------|------|
| `prelude` | `rand.Prelude` | `prelude` |

