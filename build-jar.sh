#!/usr/bin/env bash
set -euo pipefail

mkdir -p bin
echo "Compiling sources..."
javac -d bin -cp src src/*.java

rm -rf dist
mkdir -p dist

echo "Main-Class: App" > dist/MANIFEST.MF

(cd bin && jar cfm ../dist/SnakeGame.jar ../dist/MANIFEST.MF .)

# Add resources (sounds) inside JAR at sounds/
if [ -d src/sounds ]; then
  (cd src && jar uf ../dist/SnakeGame.jar sounds)
fi

# Copy for GitHub Pages (docs/)
mkdir -p docs
cp dist/SnakeGame.jar docs/SnakeGame.jar

echo "Built dist/SnakeGame.jar and copied to docs/SnakeGame.jar"

