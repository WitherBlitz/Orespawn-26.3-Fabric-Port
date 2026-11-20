#!/bin/sh
export JAVA_HOME="$(ls -d /c/Users/Administrator/Tools/jdk-25* | head -1)"
export PATH="$JAVA_HOME/bin:$PATH"
# Test runs are muted (the user asked): OpenAL Soft opens no output device.
export ALSOFT_DRIVERS=null
cd "$(dirname "$0")" && ./gradlew "$@"
