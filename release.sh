#!/usr/bin/env bash
set -euo pipefail

# ---------------------------------------------------------------------------
# Release script: verifies the project and publishes to Maven Central via
# the central-publishing-maven-plugin (release profile).
#
# Usage: ./release.sh
# ---------------------------------------------------------------------------

MAIN_BRANCH="main"

# ---- helpers ---------------------------------------------------------------
die() { echo "ERROR: $*" >&2; exit 1; }
info() { echo "==> $*"; }

# ---- pre-flight checks -----------------------------------------------------
info "Checking git state..."

current_branch=$(git rev-parse --abbrev-ref HEAD)
[[ "$current_branch" == "$MAIN_BRANCH" ]] \
  || die "Must be on '$MAIN_BRANCH' branch (currently on '$current_branch')"

[[ -z "$(git status --porcelain)" ]] \
  || die "Working tree is not clean. Commit or stash changes before releasing."

git fetch --quiet origin
behind=$(git rev-list --count HEAD..origin/"$MAIN_BRANCH")
[[ "$behind" -eq 0 ]] \
  || die "Local branch is $behind commit(s) behind origin/$MAIN_BRANCH. Pull first."

# ---- read version from pom.xml ---------------------------------------------
version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
[[ "$version" != *-SNAPSHOT ]] \
  || die "Version '$version' is a SNAPSHOT. Set a release version in pom.xml before releasing."

info "Releasing version: $version"

# ---- full verify (tests + spotless + spotbugs + javadoc jar) ---------------
info "Running mvn verify..."
mvn verify --no-transfer-progress

# ---- publish to Maven Central ----------------------------------------------
info "Publishing to Maven Central..."
mvn deploy -Prelease --no-transfer-progress

info "Release $version published successfully."
