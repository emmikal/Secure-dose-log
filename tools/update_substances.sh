#!/usr/bin/env bash

set -euo pipefail

REPO="$HOME/Documents/Turboautism-dose-log-"
TOOLS="$REPO/tools"
CONTAINER="bifrost"

STARTED_CONTAINER=false

echo "Checking Bifrost..."

if ! curl -fs http://localhost:3000/health | grep -q '"ready":true'; then
    echo "Starting Bifrost..."

    docker run -d \
        --rm \
        --name "$CONTAINER" \
        -p 3000:3000 \
        bifrost >/dev/null

    STARTED_CONTAINER=true

    echo "Waiting for Bifrost..."

    until curl -fs http://localhost:3000/health | grep -q '"ready":true'; do
        sleep 1
    done
else
    echo "✓ Bifrost already running."
fi

echo
echo "Updating substances..."

cd "$TOOLS"

python3 update_substances.py

echo

if $STARTED_CONTAINER; then
    echo "Stopping Bifrost..."
    docker stop "$CONTAINER" >/dev/null
    echo
fi

echo "Checking for changes..."

cd "$REPO"

git add \
    app/src/main/assets/substances.json \
    app/src/main/assets/substances_ATTRIBUTION.txt

if git diff --cached --quiet; then
    echo "✓ No changes detected."
else
    TODAY=$(date +%F)

    COUNT=$(python3 - <<'PY'
import json

with open("app/src/main/assets/substances.json", encoding="utf-8") as f:
    data = json.load(f)

if isinstance(data, list):
    print(len(data))
elif isinstance(data, dict) and "substances" in data:
    print(len(data["substances"]))
else:
    raise RuntimeError("Unexpected JSON format")
PY
)

    git commit -m "Update PsychonautWiki database (${COUNT} substances)

Generated ${TODAY}"

    echo "✓ Commit created."
    echo
    echo "Review the commit, then push when you're ready:"
    echo "    git push"
fi

echo
echo "Done!"
