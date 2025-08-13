#!/usr/bin/env bash
set -euo pipefail

mkdir -p bin
echo "Compiling sources..."
javac -d bin -cp src src/*.java

rm -rf dist
mkdir -p dist/sounds

if [ -d src/sounds ]; then
  cp -r src/sounds/* dist/sounds/
fi

echo "Main-Class: App" > dist/MANIFEST.MF

(cd bin && jar cfm ../dist/SnakeGame.jar ../dist/MANIFEST.MF .)

echo "Built dist/SnakeGame.jar"

