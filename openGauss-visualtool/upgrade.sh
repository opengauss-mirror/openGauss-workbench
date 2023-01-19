#!/usr/bin/env bash
echo "begin upgrade..."
mvn clean install -P prod -Dmaven.test.skip=true
/bin/cp -rf visualtool-api/target/visualtool-main.jar /ops/server/openGauss-visualtool/
chown -R og_ops:og_ops /ops
echo "end upgrade"
