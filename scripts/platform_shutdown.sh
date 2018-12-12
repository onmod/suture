#!/bin/bash

dir=$(cd `dirname $0`; pwd)
cd $dir

if [[ -f platform_config.sh ]]; then
    source ./platform_config.sh
fi

if [[ ! -d /web/$WEB_DIR ]]; then
    mkdir -p /web/$WEB_DIR
fi

if [[ -z "$JAVA_HOME" && -d /usr/java/latest/ ]]; then
    export JAVA_HOME=/usr/java/latest/
fi

echo "==== shutdown now process ===="
kill -9 `ps aux | grep "$APP_NAME" | grep -v 'grep' | awk '{print \$2}'`
