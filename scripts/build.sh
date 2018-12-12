#!/usr/bin/env bash

cd "${0%/*}"

cd ..

rm -rf ~/.m2/repository/net/dloud/

sh scripts/deploy.sh

sh scripts/plugin_deploy.sh


echo "==== starting to deploy main ===="

mvn deploy -N

echo "==== upload main success ===="

echo "==== starting to build platform ===="

mvn clean package -DskipTests -pl platform-center -am

echo "==== building platform success ===="

echo "==== starting to build gateway ===="

mvn clean package -DskipTests -pl gateway-center -am

echo "==== building gateway success ===="
