#!/bin/bash
SCRIPT_DIR=$(dirname $0)
BASE_DIR=$(dirname "$SCRIPT_DIR")
LOG_DIR=${BASE_DIR}/logs
ETC_DIR=${BASE_DIR}/etc

CP_OPTS="${ETC_DIR}:${BASE_DIR}/lib/*:${BASE_DIR}/lib:${BASE_DIR}"

APP_OPTS="-Dlog.dir=${LOG_DIR}"

java ${APP_OPTS} -cp ${CP_OPTS} info.siddhuw.OddsCalculator "$@"