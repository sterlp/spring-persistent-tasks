#!/usr/bin/env bash

# Exit immediately on error
set -e -o pipefail

# Check if version argument is provided
if [ -z "$1" ]; then
  echo "Usage: $0 <version>"
  echo "Example: $0 1.2.3"
  exit 1
fi

VERSION=$1

# Set the version
npm version "$VERSION"

# Build the project
npm run build

# Publish the package publicly
npm publish --access public

echo "Published version $VERSION successfully!"