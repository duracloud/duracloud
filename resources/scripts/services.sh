#!/bin/sh

killOsgiContainer() {
    kill $1
    pkill -xf ".*felix.*"
}


echo "========================="
echo "Starting services tests...."
echo "========================="
echo ""

export PATH=$PATH:/opt/pax/pax-construct-1.4/bin
export BUNDLE_HOME=$HOME/duracloud-home/osgi-bundles

echo "=========================="
echo "Starting Services Admin..."
echo "=========================="
echo ""

SERVICESADMIN_DIR=$BUILD_HOME/services/servicesadmin

cd $SERVICESADMIN_DIR
$MVN clean -f pom-run.xml pax:provision >& $SERVICESADMIN_DIR/provision.log

cd $SERVICESADMIN_DIR/runner
chmod +x run.sh
./run.sh >> $SERVICESADMIN_DIR/provision.log &

# Give a moment for osgi-container to come up
sleep 20

PAX_PID=$!

echo ""
echo "==============================================================="
echo "Compiling & running unit & integration tests for DuraService..."
echo "==============================================================="
cd $BUILD_HOME/duraservice
$MVN clean install -P profile-servicetest -Dtomcat.port.default=9090 -Dlog.level.default=DEBUG -Dmaven.test.failure.ignore=true

if [ $? -ne 0 ]; then
  echo ""
  echo "ERROR: DuraService Integration test(s) failed; see above"
  killOsgiContainer $PAX_PID
  return 1
fi

echo ""
echo "=================================================================="
echo "Compiling & running unit & integration tests for Service Client..."
echo "=================================================================="
cd $BUILD_HOME/serviceclient
$MVN clean install -P profile-servicetest -Dtomcat.port.default=9090 -Dlog.level.default=DEBUG -Dmaven.test.failure.ignore=true

if [ $? -ne 0 ]; then
  echo ""
  echo "ERROR: ServiceClient Integration test(s) failed; see above"
  killOsgiContainer $PAX_PID
  return 1
fi

echo ""
echo "========================================================================="
echo "Compiling & running unit & integration tests for Services Admin Client..."
echo "========================================================================="
cd $BUILD_HOME/servicesadminclient
$MVN clean install -P profile-servicetest -Dtomcat.port.default=9090 -Dlog.level.default=DEBUG -Dmaven.test.failure.ignore=true

if [ $? -ne 0 ]; then
  echo ""
  echo "ERROR: ServicesAdminClient Integration test(s) failed; see above"
  killOsgiContainer $PAX_PID
  return 1
fi

echo "======================="
echo "Shutting Down Services Admin..."
echo "======================="
echo ""
killOsgiContainer $PAX_PID

echo ""
echo "===================================="
echo "Completed services tests successfully!"
echo "===================================="
cd $BUILD_HOME

return 0
