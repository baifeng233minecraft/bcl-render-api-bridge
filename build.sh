#!/bin/sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
CACHE="$ROOT/.cache"
LOADER="$CACHE/fabric-loader-0.19.2.jar"
LOADER_SHA256=1b91025ac6b5253a49aabb22f658b5eed9fbba6dc88eb0e4a9364c7b2d1d1865
mkdir -p "$CACHE" "$ROOT/build/classes" "$ROOT/build/test-classes" "$ROOT/dist"
if [ ! -f "$LOADER" ]; then
  curl --fail --location --proto '=https' --tlsv1.2 \
    'https://maven.fabricmc.net/net/fabricmc/fabric-loader/0.19.2/fabric-loader-0.19.2.jar' \
    --output "$LOADER"
fi
ACTUAL=$(shasum -a 256 "$LOADER" | cut -d ' ' -f 1)
if [ "$ACTUAL" != "$LOADER_SHA256" ]; then echo 'Fabric Loader SHA-256 mismatch' >&2; exit 1; fi
javac --release 21 -cp "$LOADER" -d "$ROOT/build/classes" \
  "$ROOT/src/main/java/dev/baifeng/bcl/render/BackendOptions.java" \
  "$ROOT/src/main/java/dev/baifeng/bcl/render/RenderBridge.java"
javac --release 21 -cp "$ROOT/build/classes:$LOADER" -d "$ROOT/build/test-classes" \
  "$ROOT/src/test/java/dev/baifeng/bcl/render/BackendOptionsTest.java"
java -cp "$ROOT/build/classes:$ROOT/build/test-classes:$LOADER" dev.baifeng.bcl.render.BackendOptionsTest
dot_clean -m "$ROOT/build/classes" "$ROOT/src/main/resources" 2>/dev/null || true
jar --create --file "$ROOT/dist/bcl-render-api-bridge-0.1.0.jar" \
  -C "$ROOT/build/classes" . \
  -C "$ROOT/src/main/resources" . \
  -C "$ROOT" LICENSE
jar --list --file "$ROOT/dist/bcl-render-api-bridge-0.1.0.jar"
