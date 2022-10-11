#!/usr/bin/env bash

# This script is used to run SDN6's integration test against a Neo4j cluster via `runClusterTests.java`. The idea behind it is:
# 1. Build a local deployment of SDN 6 including a test jar containing all tests
# 2. Extract the version, direct dependencies and the repo path from the Maven build descriptor and replace the placeholders
#    in the template.
# 3. Execute the Java script file via JBang.
#
# The result code of this script will be 0 in a successful run, a non-zero value otherwise. The Java program will try
# upto 100 times to get a completely successful test run, retrying on error cases that might happen in a cluster.
#
# Run this script with a pair of environmental values to point it to a cluster:
# SDN_NEO4J_URL=neo4j+s://your.neo4j.cluster.io SDN_NEO4J_PASSWORD=yourPassword ./runClusterTests.sh

set -euo pipefail

SRC_DIR=$(realpath $(dirname "$0"))
WORK_DIR=$(realpath $SRC_DIR/../work)

SDN_VERSION=${SDN_BRANCH:-'6.2.0'}
SDN_DIR=$WORK_DIR/spring-data-neo4j

mkdir -p $WORK_DIR

if [[ -d $SDN_DIR ]]
then
  echo "Removing existing clone at $SDN_DIR"
  rm -rf $SDN_DIR
fi

git clone --depth 1 --branch $SDN_VERSION https://github.com/spring-projects/spring-data-neo4j.git $SDN_DIR

(
  cd $SDN_DIR
  SDN_VERSION=$(./mvnw --no-transfer-progress help:evaluate -Dexpression=project.version -q -DforceStdout)

  echo "Will build and test Spring Data Neo4j $SDN_VERSION"
  # Create the distribution and deploy it into the target folder itself
  ./mvnw -q --no-transfer-progress -Pgenerate-test-jar -DskipTests clean deploy -DaltDeploymentRepository=snapshot-repo::default::file:///$WORK_DIR/snapshot-repo

  # Massage the directory name into something sed is happy with
  SNAPSHOT_REPO=$(printf '%s\n' "$WORK_DIR/snapshot-repo" | sed -e 's/[\/&]/\\&/g')

  # Create a plain list of dependencies
  ./mvnw --no-transfer-progress dependency:list -DexcludeTransitive  | sed -n -e 's/^\[INFO\]    //p' > $WORK_DIR/dependencies.txt

  # Update repository path, version and dependencies in template
  sed -e s/\$SDN_VERSION/$SDN_VERSION/ -e s/\$SNAPSHOT_REPO/$SNAPSHOT_REPO/ $SRC_DIR/runClusterTests.template.java |\
    awk -F: -v deps=$WORK_DIR/dependencies.txt -v target=$WORK_DIR/runClusterTests.java '
      /\/\/\$ADDITIONAL_DEPENDENCIES/ {
        while((getline < deps) > 0) {
          print "//DEPS "  $1 ":" $2 ":" $4 > target
        }
        next
      }
      {print > target}'
)

echo "Starting the cluster tests"
cd $SRC_DIR
chmod +x $WORK_DIR/runClusterTests.java && cp logback.xml $WORK_DIR/logback.xml
`java -jar ./.jbang/jbang.jar $WORK_DIR/runClusterTests.java`
