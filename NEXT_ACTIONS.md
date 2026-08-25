# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 3/31 (9.7%)
- **Function parity:** 8/320 matched (target 37) — 2.5%
- **Class/type parity:** 1/80 matched (target 10) — 1.2%
- **Combined symbol parity:** 9/400 matched (target 47) — 2.2%
- **Average inline-code cosine:** 0.12 (function body across 3 matched files)
- **Average documentation cosine:** 0.25 (doc text across 3 matched files)
- **Cheat-zeroed Files:** 1
- **Critical Issues:** 3 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

### 1. rng
- **Similarity:** 0.09 (needs 76% improvement)
- **Dependencies:** 17
- **Priority Score:** 17232810.0
- **Functions:** 4/26 matched (target 16)
- **Missing functions:** `random`, `random_iter`, `sample`, `sample_iter`, `r#gen`, `gen_range`, `gen_bool`, `gen_ratio`, `__unsafe`, `test_fill_bytes_default`, `test_fill`, `test_fill_empty`, `test_random_range_int`, `test_random_range_float`, `test_random_range_panic_int`, `test_random_range_panic_usize`, `test_random_bool`, `test_rng_mut_ref`, `use_rng`, `test_rng_trait_object`, `test_rng_boxed_trait`, `test_gen_ratio_average`
- **Types:** 1/2 matched (target 7)
- **Missing types:** `Fill`
- **Symbol Deficit:** 23 (functions: 22, types: 1)
- **Missing Tests:** 13 of 13 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. rng

- **Target:** `rand.Rng`
- **Similarity:** 0.09
- **Dependents:** 17
- **Priority Score:** 17232810.0
- **Functions:** 4/26 matched (target 16)
- **Missing functions:** `random`, `random_iter`, `sample`, `sample_iter`, `r#gen`, `gen_range`, `gen_bool`, `gen_ratio`, `__unsafe`, `test_fill_bytes_default`, `test_fill`, `test_fill_empty`, `test_random_range_int`, `test_random_range_float`, `test_random_range_panic_int`, `test_random_range_panic_usize`, `test_random_bool`, `test_rng_mut_ref`, `use_rng`, `test_rng_trait_object`, `test_rng_boxed_trait`, `test_gen_ratio_average`
- **Types:** 1/2 matched (target 7)
- **Missing types:** `Fill`
- **Tests:** 0/13 matched

### 2. lib

- **Target:** `rand.Lib`
- **Similarity:** 0.27
- **Dependents:** 0
- **Priority Score:** 121607.3
- **Functions:** 4/15 matched (target 20)
- **Missing functions:** `thread_rng`, `random`, `random_iter`, `rng`, `const_rng`, `step_rng`, `next_u32`, `next_u64`, `fill_bytes`, `test_random`, `test_range`
- **Types:** 0/1 matched (target 2)
- **Missing types:** `StepRng`
- **Tests:** 0/8 matched

### 3. prelude

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

