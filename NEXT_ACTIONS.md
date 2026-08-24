# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 0/31 (0.0%)
- **Function parity:** 0/307 matched — 0.0%
- **Class/type parity:** 0/79 matched — 0.0%
- **Combined symbol parity:** 0/386 matched — 0.0%
- **Average inline-code cosine:** 0.00 (function body across 0 matched files)
- **Average documentation cosine:** 0.00 (doc text across 0 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 0 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

1. **rng** (17 deps)
   - Path: `src/rng.rs`
   - Essential for 17 other files

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

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
| `distr.mod` | `distr.Mod` | 0 | `src/distr/mod.rs` | `distr/Mod.kt` |
| `weighted.mod` | `distr.weighted.Mod` | 0 | `src/distr/weighted/mod.rs` | `distr/weighted/Mod.kt` |
| `lib` | `Lib` | 0 | `src/lib.rs` | `Lib.kt` |
| `rngs.mod` | `rngs.Mod` | 0 | `src/rngs/mod.rs` | `rngs/Mod.kt` |
| `seq.mod` | `seq.Mod` | 0 | `src/seq/mod.rs` | `seq/Mod.kt` |

