cd "${0%/*}"

cd ..

rm -rf ~/.m2/repository/net/dloud/

echo "==== starting to deploy maven-plugin ===="

mvn clean deploy -DskipTests -pl gateway-info,platform-maven-plugin

echo "==== deploying maven-plugin ===="