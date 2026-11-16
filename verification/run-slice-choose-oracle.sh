#!/usr/bin/env bash
set -euo pipefail
repo_root=$(cd "$(dirname "$0")/.." && pwd)
cd "$repo_root"
oracle_dir=build/rust-kotlin-slice-choose
core_root=
while IFS= read -r manifest; do
    case "$manifest" in
        */rand_core-0.9.3/Cargo.toml) core_root=${manifest%/Cargo.toml} ;;
    esac
done < <(rg --files "$HOME/.cargo/registry/src" -g Cargo.toml)
if [[ -z "$core_root" ]]; then
    echo "A cached rand_core 0.9.3 source tree is required; no downloads are performed." >&2
    exit 1
fi
mkdir -p "$oracle_dir"
rustc --crate-name rand_core --crate-type rlib --edition 2021 --cfg 'feature="std"' "$core_root/src/lib.rs" -o "$oracle_dir/librand_core.rlib"
rustc --crate-name rand --crate-type rlib --edition 2021 --cfg 'feature="std"' --cfg 'feature="alloc"' --extern rand_core="$oracle_dir/librand_core.rlib" tmp/rand/src/lib.rs -o "$oracle_dir/librand_upstream.rlib"
rustc --test --edition 2021 -L dependency="$oracle_dir" --extern rand="$oracle_dir/librand_upstream.rlib" --extern rand_core="$oracle_dir/librand_core.rlib" verification/slice-choose-oracle.rs -o "$oracle_dir/slice-choose-oracle"
"$oracle_dir/slice-choose-oracle"
