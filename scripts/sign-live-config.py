#!/usr/bin/env python3
"""Sign `chand-live-config.json` (ECDSA P-256 / SHA-256) and embed `signature` + `keyId`.

Usage:
    python3 scripts/sign-live-config.py \
        --key .live-config-signing-key.pem \
        --key-id prod-2026-11 \
        --in chand-live-config.json \
        --out chand-live-config.json

The private key is never read from the repository: pass a path (CI: write the
`LIVE_CONFIG_SIGNING_KEY` secret to a temp file first).
"""

from __future__ import annotations

import argparse
import base64
import sys

from live_config_canonical import (
    KEY_ID_FIELD,
    RESERVED_FIELDS,
    SIGNATURE_FIELD,
    canonical_payload,
    dump_config,
    load_config,
    sign_payload,
)


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--key", required=True, help="ECDSA P-256 private key (PEM)")
    parser.add_argument("--key-id", required=True, help="Short key identifier, e.g. prod-2026-11")
    parser.add_argument("--in", dest="input", required=True, help="Unsigned config JSON")
    parser.add_argument("--out", dest="output", required=True, help="Where to write the signed JSON")
    args = parser.parse_args(argv)

    raw = open(args.input, encoding="utf-8").read()
    config = load_config(raw)
    for field in RESERVED_FIELDS:
        config.pop(field, None)

    # Two-phase dump: sign the exact bytes the verifier will canonicalize after the signature
    # fields are embedded again.
    payload = canonical_payload(dump_config(config))
    signature = sign_payload(payload, args.key)

    config[SIGNATURE_FIELD] = base64.b64encode(signature).decode("ascii")
    config[KEY_ID_FIELD] = args.key_id
    signed_text = dump_config(config)

    # Guard: the embedded fields must not influence the payload.
    if canonical_payload(signed_text) != payload:
        print("error: canonical payload changed while embedding the signature", file=sys.stderr)
        return 2

    with open(args.output, "w", encoding="utf-8") as handle:
        handle.write(signed_text)
    print(
        f"signed {args.output} with key '{args.key_id}' "
        f"({len(payload)} payload bytes, {len(signature)} signature bytes)"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
