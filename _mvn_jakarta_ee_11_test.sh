#!/bin/sh
# Runs the unit tests under Jakarta EE 11.
#
# Naming any profile on the command line deactivates every activeByDefault profile, so the
# generation is named here even though it is the default one.
cd "$(dirname "$0")" || exit 1

exec ./mvnw -Pjakarta-ee-11 clean test "$@"
