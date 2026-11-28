#!/usr/bin/env python3
"""Verify the signature of `chand-live-config.json` offline (same rules as the Android app).

Usage:
    python3 scripts/verify-live-config.py \
        --config chand-live-config.json \
        --keys "prod-2026-11:MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE..."

`--keys` accepts the same `keyId:base64,keyId2:base64` format as the
`LIVE_CONFIG_PUBLIC_KEYS` build property. Exit code 0 means "valid", 1 means "invalid/unsigned".
"""

from __future__ import annotations

import argparse
import base64
import os
import sys
import tempfile

from live_config_canonical import canonical_payload, read_signature, verify_with_files


def parse_keys(raw: str) -> dict[str, str]:
    keys: dict[str, str] = {}
    for chunk in raw.split(","):
        chunk = chunk.strip()
        if not chunk:
            continue
        key_id, separator, value = chunk.partition(":")
        if not separator or not key_id.strip() or not value.strip():
            raise ValueError(f"malformed key entry: {chunk!r}")
        keys[key_id.strip()] = value.strip()
    return keys


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--config", required=True)
    parser.add_argument("--keys", required=True, help="keyId:base64SPKI[,keyId2:base64SPKI]")
    args = parser.parse_args(argv)

    raw = open(args.config, encoding="utf-8").read()
    keys = parse_keys(args.keys)
    key_id, signature_b64 = read_signature(raw)
    if key_id is None or signature_b64 is None:
        print("FAIL: config is unsigned", file=sys.stderr)
        return 1
    public_key_b64 = keys.get(key_id)
    if public_key_b64 is None:
        print(f"FAIL: unknown keyId '{key_id}' (trusted: {', '.join(sorted(keys))})", file=sys.stderr)
        return 1

    try:
        payload = canonical_payload(raw)
        signature = base64.b64decode(signature_b64, validate=True)
        public_der = base64.b64decode(public_key_b64, validate=True)
    except Exception as exc:  # noqa: BLE001 - report and fail closed
        print(f"FAIL: cannot decode config material ({exc})", file=sys.stderr)
        return 1

    with tempfile.TemporaryDirectory() as tmp:
        signature_path = os.path.join(tmp, "signature.der")
        key_path = os.path.join(tmp, "public.der")
        with open(signature_path, "wb") as handle:
            handle.write(signature)
        with open(key_path, "wb") as handle:
            handle.write(public_der)
        ok = verify_with_files(payload, signature_path, key_path)

    if not ok:
        print(f"FAIL: signature does not match key '{key_id}'", file=sys.stderr)
        return 1
    print(f"OK: valid signature for key '{key_id}' ({len(payload)} payload bytes)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
