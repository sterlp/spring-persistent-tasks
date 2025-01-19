#!/bin/bash

# Fetch the current version from the POM
MVN_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
echo "Current Maven version: $MVN_VERSION"

# Release version
RELEASE_VERSION=${MVN_VERSION%-SNAPSHOT}
echo "Releasing version: $RELEASE_VERSION"

# Set the new release version and tag it in Git
mvn versions:set -DnewVersion="$RELEASE_VERSION" -DgenerateBackupPoms=false
# Deploy the project
mvn clean source:jar javadoc:jar deploy -DskipTests -Prelease
# update git
git add '**/pom.xml'
git commit -am "$RELEASE_VERSION release"
git tag -a "v$RELEASE_VERSION" -m "v$RELEASE_VERSION release"

# Extract the current version number components
IFS='.' read -r -a VERSION_PARTS <<< "$MVN_VERSION"
MAJOR="${VERSION_PARTS[0]}"
MINOR="${VERSION_PARTS[1]}"
PATCH="${VERSION_PARTS[2]}"
# Increment the patch version for the next snapshot
PATCH=$((PATCH + 1))
NEXT_VERSION="$MAJOR.$MINOR.$PATCH-SNAPSHOT"
echo "Next development version: $NEXT_VERSION"

# Set the next snapshot version and commit it in Git
mvn versions:set -DnewVersion="$NEXT_VERSION" -DgenerateBackupPoms=false
git add '**/pom.xml'
git commit -am "Next development version $NEXT_VERSION"

git push
git push --tags