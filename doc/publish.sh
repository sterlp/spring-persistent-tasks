#!/bin/bash

# Configuration
HOST="w0125542.kasserver.com"
USER="f017969a"
PASSWORD=$(security find-generic-password -a "$USER" -s "$HOST" -w)
LOCAL_DIR=".vitepress/dist"
REMOTE_DIR="."

rm -rf $LOCAL_DIR
npm install
npm run docs:build

cp public/* .vitepress/dist/


# Upload with mirror
lftp -u "$USER","$PASSWORD" "$HOST" <<EOF
mirror --reverse \
       --delete \
       --verbose \
       --parallel=8 \
       "$LOCAL_DIR" "$REMOTE_DIR"
quit
EOF