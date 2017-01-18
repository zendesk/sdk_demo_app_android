#!/bin/bash

ZD_CONFIG="./app/src/main/res/values/zd.xml"
REPLACE_KEY="replace_me_chat_account_id"

SUFFIX='.bak'

sed -i${SUFFIX} "s/$REPLACE_KEY/$ZOPIM_ACCOUNT_KEY/g" $ZD_CONFIG
rm ${ZD_CONFIG}${SUFFIX}

# Rebuild app
./scripts/buildApp.sh
