#!/bin/bash
# =============================================================================
# deploy-webapp.sh - push the built webapp into the running Tomcat container.
#
# WHY: `docker compose build tomcat` re-resolves the tomcat:11-jre17-temurin base
# image from Docker Hub on every run. When that lookup hangs or the registry is
# unreachable, the whole dev loop stops even though nothing about the base image
# changed. This copies the exploded webapp maven just produced straight into the
# container that is already running, which is all that is needed for a change to
# JSP / JS / CSS / class files.
#
# Still run `docker compose run --rm maven` first: this script copies what maven
# built, it does not build anything.
#
# Use `docker compose build tomcat` for a real image rebuild (new dependency,
# Dockerfile change, or before anything that leaves this machine).
# =============================================================================
set -eu
cd "$(dirname "$0")"

CONTAINER=cerberus-local-app
EXPLODED=target/cerberus-core-6.0-SNAPSHOT

if [ ! -d "$EXPLODED" ]; then
    echo "No $EXPLODED - run 'docker compose run --rm maven' first." >&2
    exit 2
fi
if ! docker ps --format '{{.Names}}' | grep -qx "$CONTAINER"; then
    echo "Container $CONTAINER is not running." >&2
    exit 2
fi

echo "Copying webapp into $CONTAINER ..."
docker cp "$EXPLODED/." "$CONTAINER:/usr/local/tomcat/webapps/ROOT/"

echo "Restarting Tomcat ..."
docker restart "$CONTAINER" >/dev/null

printf "Waiting for Login.jsp "
for _ in $(seq 1 90); do
    if [ "$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/Login.jsp)" = "200" ]; then
        echo " ready."
        exit 0
    fi
    printf "."
    sleep 2
done
echo " TIMEOUT - check 'docker logs $CONTAINER'." >&2
exit 1
