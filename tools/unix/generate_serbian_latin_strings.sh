#!/usr/bin/env sh

echo "Converting Serbian Cyrillic resource files to Serbian Latin"

REPO_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
ANDROID="$REPO_ROOT/android"

if ! command -v uconv >/dev/null 2>&1; then
  echo "Error: uconv not found. Check your package manager for a package providing uconv (often part of ICU)." >&2
  exit 1
fi

convert() {
  INPUT="$1"
  OUTPUT="$2"
  uconv -x 'Serbian-Latin/BGN' "$INPUT" > "$OUTPUT"
}

mkdir -p "android/app/src/fdroid/play/listings/sr-Latn"
convert "$ANDROID/app/src/fdroid/play/listings/sr/full-description.txt" \
        "$ANDROID/app/src/fdroid/play/listings/sr-Latn/full-description.txt"
convert "$ANDROID/app/src/fdroid/play/listings/sr/short-description.txt" \
        "$ANDROID/app/src/fdroid/play/listings/sr-Latn/short-description.txt"
convert "$ANDROID/app/src/fdroid/play/listings/sr/title.txt" \
        "$ANDROID/app/src/fdroid/play/listings/sr-Latn/title.txt"

mkdir -p "$ANDROID/app/src/main/res/values-b+sr+Latn"
convert "$ANDROID/app/src/main/res/values-sr/strings.xml" \
        "$ANDROID/app/src/main/res/values-b+sr+Latn/strings.xml"

mkdir -p "$ANDROID/sdk/src/main/res/values-b+sr+Latn"
convert "$ANDROID/sdk/src/main/res/values-sr/strings.xml" \
        "$ANDROID/sdk/src/main/res/values-b+sr+Latn/strings.xml"
convert "$ANDROID/sdk/src/main/res/values-sr/types_strings.xml" \
        "$ANDROID/sdk/src/main/res/values-b+sr+Latn/types_strings.xml"

mkdir -p "$REPO_ROOT/data/countries-strings/sr_Latn.json"
convert "$REPO_ROOT/data/countries-strings/sr.json/localize.json" \
        "$REPO_ROOT/data/countries-strings/sr_Latn.json/localize.json"
