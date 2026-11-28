#!/usr/bin/env bash
# Self-test for the live-config signing pipeline: sign -> verify -> tamper -> reject.
# Runs without Gradle/Android SDK so CI can execute it in seconds.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

echo "== generating throwaway P-256 keypair =="
openssl ecparam -name prime256v1 -genkey -noout -out "$TMP/key.pem"
PUB_B64="$(openssl ec -in "$TMP/key.pem" -pubout -outform DER 2>/dev/null | base64 -w0)"

cat > "$TMP/config.json" <<'JSON'
{
  "configVersion": 9,
  "minAppVersionCode": 1,
  "summaryEn": "Self-test config",
  "summaryFa": "پیکربندی آزمون",
  "endpoints": { "tgjuAjaxUrl": "https://mirror.example.com/tgju", "frankfurterLatestUrls": [] },
  "validation": { "cryptoDisagreementThresholdPct": 4.0, "fiatDisagreementThresholdPct": 3.5 },
  "features": { "enableInAppApkUpdates": true, "minAutoRefreshSeconds": 10 },
  "disabledAssetIds": ["SHIB"]
}
JSON
cp "$TMP/config.json" "$TMP/unsigned.json"

echo "== signing =="
python3 scripts/sign-live-config.py \
  --key "$TMP/key.pem" --key-id selftest --in "$TMP/unsigned.json" --out "$TMP/signed.json"

echo "== verifying a valid signature =="
python3 scripts/verify-live-config.py --config "$TMP/signed.json" --keys "selftest:$PUB_B64"

echo "== reformatting must not break the signature =="
python3 - "$TMP/signed.json" "$TMP/reformatted.json" <<'PY'
import json, sys
config = json.load(open(sys.argv[1]))
with open(sys.argv[2], "w") as handle:
    # different key order + compact separators
    handle.write(json.dumps(dict(reversed(list(config.items()))), separators=(",", ":")))
PY
python3 scripts/verify-live-config.py --config "$TMP/reformatted.json" --keys "selftest:$PUB_B64"

echo "== tampering must be rejected =="
python3 - "$TMP/signed.json" "$TMP/tampered.json" <<'PY'
import json, sys
config = json.load(open(sys.argv[1]))
config["endpoints"]["tgjuAjaxUrl"] = "https://attacker.example.net/tgju"
with open(sys.argv[2], "w") as handle:
    json.dump(config, handle, indent=2)
PY
if python3 scripts/verify-live-config.py --config "$TMP/tampered.json" --keys "selftest:$PUB_B64"; then
  echo "FAIL: tampered config was accepted" >&2
  exit 1
fi
echo "   rejected as expected"

echo "== unknown key id must be rejected =="
if python3 scripts/verify-live-config.py --config "$TMP/signed.json" --keys "other:$PUB_B64"; then
  echo "FAIL: unknown keyId was accepted" >&2
  exit 1
fi
echo "   rejected as expected"

echo "== unsigned config must be rejected =="
if python3 scripts/verify-live-config.py --config "$TMP/unsigned.json" --keys "selftest:$PUB_B64"; then
  echo "FAIL: unsigned config was accepted" >&2
  exit 1
fi
echo "   rejected as expected"

echo "== committed config must verify against the embedded production key =="
PROD_KEYS_FILE="$ROOT/gradle/live-config-keys.properties"
if [[ -f "$PROD_KEYS_FILE" ]]; then
  PROD_KEYS="$(grep -E '^liveConfigPublicKeys=' "$PROD_KEYS_FILE" | cut -d= -f2-)"
  if [[ -n "$PROD_KEYS" ]]; then
    python3 scripts/verify-live-config.py --config chand-live-config.json --keys "$PROD_KEYS"
  else
    echo "   (no liveConfigPublicKeys configured; skipping)"
  fi
else
  echo "   (gradle/live-config-keys.properties missing; skipping)"
fi

echo "ALL LIVE-CONFIG SIGNATURE TESTS PASSED"
