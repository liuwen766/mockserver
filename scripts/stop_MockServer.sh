#!/usr/bin/env bash

`ps -ef | grep mockserver | grep -v grep | grep -v mvn | grep -v maven | grep -v sonatype-oss-release | grep -v $0 | awk '{print $2}' | xargs -t -I '{}' kill '{}'`; echo done
