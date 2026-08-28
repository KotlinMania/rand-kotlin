# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 13/31 (41.9%)
- **Function parity:** 50/297 matched (target 169) — 16.8%
- **Class/type parity:** 10/83 matched (target 31) — 12.0%
- **Combined symbol parity:** 60/380 matched (target 200) — 15.8%
- **Average inline-code cosine:** 0.28 (function body across 12 matched files)
- **Average documentation cosine:** 0.31 (doc text across 12 matched files)
- **Cheat-zeroed Files:** 1
- **Critical Issues:** 12 files with <0.60 function similarity

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

- **Target:** `rand.Rng`
- **Similarity:** 0.17
- **Dependents:** 17
- **Priority Score:** 17212808.0
- **Functions:** 6/26 matched (target 18)
- **Missing functions:** `random`, `random_iter`, `r#gen`, `gen_range`, `gen_bool`, `gen_ratio`, `__unsafe`, `test_fill_bytes_default`, `test_fill`, `test_fill_empty`, `test_random_range_int`, `test_random_range_float`, `test_random_range_panic_int`, `test_random_range_panic_usize`, `test_random_bool`, `test_rng_mut_ref`, `use_rng`, `test_rng_trait_object`, `test_rng_boxed_trait`, `test_gen_ratio_average`
- **Types:** 1/2 matched (target 7)
- **Missing types:** `Fill`
- **Tests:** 0/13 matched

### 2. distr.distribution

- **Target:** `distr.Distribution`
- **Similarity:** 0.41
- **Dependents:** 6
- **Priority Score:** 6071606.0
- **Functions:** 7/11 matched (target 8)
- **Missing functions:** `size_hint`, `test_make_an_iter`, `ten_dice_rolls_other_than_five`, `test_dist_string`
- **Types:** 2/5 matched (target 4)
- **Missing types:** `Distribution`, `Item`, `Map`
- **Tests:** 2/5 matched

### 3. distr.uniform

- **Target:** `distr.Uniform`
- **Similarity:** 0.04
- **Dependents:** 1
- **Priority Score:** 1222409.6
- **Functions:** 1/14 matched (target 23)
- **Missing functions:** `fmt`, `new`, `new_inclusive`, `sample_single`, `sample_single_inclusive`, `try_from`, `borrow`, `is_empty`, `test_uniform_serialization`, `test_custom_uniform`, `value_stability`, `test_samples`, `uniform_distributions_can_be_compared`
- **Types:** 1/10 matched (target 2)
- **Missing types:** `Error`, `SampleUniform`, `UniformSampler`, `SampleBorrow`, `SampleRange`, `MyF32`, `UniformMyF32`, `X`, `Sampler`
- **Tests:** 0/5 matched

### 4. seq.slice

- **Target:** `seq.Slice`
- **Similarity:** 0.14
- **Dependents:** 1
- **Priority Score:** 1212708.6
- **Functions:** 6/22 matched (target 21)
- **Missing functions:** `is_empty`, `choose_multiple_array`, `choose_mut`, `choose_weighted_mut`, `len`, `partial_shuffle`, `next`, `size_hint`, `test_slice_choose`, `value_stability_slice`, `move_last`, `test_partial_shuffle`, `test_weighted`, `get_weight`, `test_multiple_weighted_edge_cases`, `test_multiple_weighted_distributions`
- **Types:** 0/5 matched (target 1)
- **Missing types:** `IndexedRandom`, `IndexedMutRandom`, `SliceRandom`, `SliceChooseIter`, `Item`
- **Tests:** 1/9 matched

### 5. seq.iterator

- **Target:** `seq.Iterator`
- **Similarity:** 0.13
- **Dependents:** 0
- **Priority Score:** 182008.7
- **Functions:** 2/15 matched (target 8)
- **Missing functions:** `choose_stable`, `choose_multiple_fill`, `next`, `size_hint`, `test_iterator_choose`, `test_iter`, `test_iterator_choose_stable`, `test_iterator_choose_stable_stability`, `test_sample_iter`, `value_stability_choose`, `value_stability_choose_stable`, `value_stability_choose_multiple`, `do_test`
- **Types:** 0/5 matched (target 1)
- **Missing types:** `IteratorRandom`, `UnhintedIterator`, `Item`, `ChunkHintedIterator`, `WindowHintedIterator`
- **Tests:** 0/11 matched

### 6. lib

- **Target:** `rand.Lib`
- **Similarity:** 0.30
- **Dependents:** 0
- **Priority Score:** 111607.0
- **Functions:** 5/15 matched (target 25)
- **Missing functions:** `thread_rng`, `random_iter`, `rng`, `const_rng`, `step_rng`, `next_u32`, `next_u64`, `fill_bytes`, `test_random`, `test_range`
- **Types:** 0/1 matched (target 2)
- **Missing types:** `StepRng`
- **Tests:** 0/8 matched

### 7. distr.bernoulli

- **Target:** `distr.Bernoulli`
- **Similarity:** 0.34
- **Dependents:** 0
- **Priority Score:** 71206.6
- **Functions:** 4/10 matched (target 7)
- **Missing functions:** `fmt`, `new`, `test_serializing_deserializing_bernoulli`, `test_average`, `value_stability`, `bernoulli_distributions_can_be_compared`
- **Types:** 1/2 matched
- **Missing types:** `BernoulliError`
- **Tests:** 1/5 matched

### 8. distr.other

- **Target:** `distr.Other`
- **Similarity:** 0.39
- **Dependents:** 0
- **Priority Score:** 41006.1
- **Functions:** 4/8 matched (target 28)
- **Missing functions:** `test_misc`, `test_chars`, `value_stability`, `test_samples`
- **Types:** 2/2 matched (target 6)
- **Missing types:** _none_
- **Tests:** 2/6 matched

### 9. rngs.xoshiro256plusplus

- **Target:** `rngs.Xoshiro256PlusPlus`
- **Similarity:** 0.30
- **Dependents:** 0
- **Priority Score:** 30907.0
- **Functions:** 5/7 matched (target 9)
- **Missing functions:** `reference`, `stable_seed_from_u64_and_from_seed`
- **Types:** 1/2 matched
- **Missing types:** `Seed`
- **Tests:** 0/2 matched

### 10. rngs.small

- **Target:** `rngs.Small`
- **Similarity:** 0.78
- **Dependents:** 0
- **Priority Score:** 20802.2
- **Functions:** 5/5 matched (target 8)
- **Missing functions:** _none_
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Rng`, `Seed`

### 11. rngs.std

- **Target:** `rngs.Std`
- **Similarity:** 0.36
- **Dependents:** 0
- **Priority Score:** 10706.4
- **Functions:** 5/5 matched (target 11)
- **Missing functions:** _none_
- **Types:** 1/2 matched
- **Missing types:** `Seed`
- **Tests:** 1/1 matched

### 12. rngs.mod

- **Target:** `rngs.Os [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 3)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

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

