#!/bin/bash

if [[ $encrypted_218b70c0d15d_key ]]; then
    openssl aes-256-cbc -K $encrypted_218b70c0d15d_key -iv $encrypted_218b70c0d15d_iv -in scripts/rtd.jks.enc -out scripts/rtd.jks -d
fi
