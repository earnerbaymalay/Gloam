#!/usr/bin/env bash
# release.sh — Tag a version to trigger a GitHub release with APK + deb
# Usage: ./release.sh 26.04

set -e

VERSION="${1:?Usage: ./release.sh <version>}"

echo "Creating release tag v$VERSION..."

git tag -a "v$VERSION" -m "Release v$VERSION"
git push origin "v$VERSION"

echo ""
echo "Tag v$VERSION pushed."
echo "GitHub Actions will now build APK + deb and create a release."
echo "Check: https://github.com/earnerbaymalay/Gloam/actions"
echo "Release will appear at: https://github.com/earnerbaymalay/Gloam/releases"
