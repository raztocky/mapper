#!/usr/bin/env bash

ANDROID_LISTINGS=android/app/src/fdroid/play/listings
ANDROID_NOTES=$ANDROID_LISTINGS/en-US/release-notes.txt

find $ANDROID_LISTINGS -name release-notes.txt -exec rsync -a $ANDROID_NOTES {} \;
