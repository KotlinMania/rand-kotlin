# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 5/31 (16.1%)
- **Function parity:** 21/319 matched (target 54) — 6.6%
- **Class/type parity:** 4/80 matched (target 16) — 5.0%
- **Combined symbol parity:** 25/399 matched (target 70) — 6.3%
- **Average inline-code cosine:** 0.24 (function body across 5 matched files)
- **Average documentation cosine:** 0.39 (doc text across 5 matched files)
- **Cheat-zeroed Files:** 1
- **Critical Issues:** 5 files with <0.60 function similarity

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

### 3. lib

- **Target:** `rand.Lib`
- **Similarity:** 0.27
- **Dependents:** 0
- **Priority Score:** 121607.3
- **Functions:** 4/15 matched (target 20)
- **Missing functions:** `thread_rng`, `random`, `random_iter`, `rng`, `const_rng`, `step_rng`, `next_u32`, `next_u64`, `fill_bytes`, `test_random`, `test_range`
- **Types:** 0/1 matched (target 2)
- **Missing types:** `StepRng`
- **Tests:** 0/8 matched

### 4. distr.bernoulli

- **Target:** `distr.Bernoulli`
- **Similarity:** 0.34
- **Dependents:** 0
- **Priority Score:** 71206.6
- **Functions:** 4/10 matched (target 7)
- **Missing functions:** `fmt`, `new`, `test_serializing_deserializing_bernoulli`, `test_average`, `value_stability`, `bernoulli_distributions_can_be_compared`
- **Types:** 1/2 matched
- **Missing types:** `BernoulliError`
- **Tests:** 1/5 matched

### 5. prelude

- **Target:** `rand.Prelude [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 1)
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

### Missing

| Source | Expected target | Deps | Source path | Expected path |
|--------|-----------------|------|-------------|---------------|
| `distr.mod` | `distr.Mod` | 0 | `distr/mod.rs` | `distr/Mod.kt` |
| `weighted.mod` | `distr.weighted.Mod` | 0 | `distr/weighted/mod.rs` | `distr/weighted/Mod.kt` |
| `rngs.mod` | `rngs.Mod` | 0 | `rngs/mod.rs` | `rngs/Mod.kt` |
| `seq.mod` | `seq.Mod` | 0 | `seq/mod.rs` | `seq/Mod.kt` |

