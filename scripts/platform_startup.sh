#!/bin/bash

dir=$(cd `dirname $0`; pwd)
cd $dir

if [[ -f platform_config.sh ]]; then
    source ./platform_config.sh
fi

sh platform_shutdown.sh

if [[ -z "$JAVA_HOME" && -d /usr/java/latest/ ]]; then
    export JAVA_HOME=/usr/java/latest/
fi

if [[ ! -d /web/$WEB_DIR ]]; then
    mkdir -p /web/$WEB_DIR
fi

\cp -f ../$SERVICE_DIR/target/$APP_NAME-$APP_VERSION".jar" /web/$WEB_DIR

cd /web/$WEB_DIR

if [[ ! -f $APP_NAME-$APP_VERSION".jar" && -d current ]]; then
    cd current
fi

if [[ -f $APP_NAME-$APP_VERSION".jar" ]]; then
    nohup java -jar $APP_NAME-$APP_VERSION".jar" > /dev/null 2>&1 &
fi

cd $dir

sh platform_watch.sh
