#!/bin/bash

dir=$(cd `dirname $0`; pwd)
cd $dir

if [[ -f gateway_config.sh ]]; then
    source ./gateway_config.sh
fi

echo "==== start watch project info log ===="

tail -f /web/$WEB_DIR/logs/$APP_ID/info.log
