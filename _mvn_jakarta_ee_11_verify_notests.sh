#!/bin/sh
# Runs `verify` under Jakarta EE 11: the whole lifecycle -- compile, package, javadoc, enforcer --
# with no test executed. The tests are still COMPILED, which is half of what this is for;
# -Dmaven.test.skip would drop that too and is deliberately not used here.
#
# `failsafe` is the profile that binds maven-failsafe-plugin, and naming any profile on the command
# line deactivates the activeByDefault generation, so the two have to be named together.
#
# Both flags are needed. As of 3.6.0 failsafe no longer binds its own skipTests parameter to the
# skipTests user property, so -DskipTests reaches surefire alone and the ITs would still run;
# -DskipITs is what stops them. For either half on its own, see ./_mvn_jakarta_ee_11_verify.sh.
cd "$(dirname "$0")" || exit 1

exec ./mvnw -Pfailsafe,jakarta-ee-11 clean verify -DskipTests -DskipITs "$@"
