#!/bin/sh
# Runs `verify` under Jakarta EE 11: the whole lifecycle -- compile, package, javadoc, enforcer --
# with both the unit tests and the integration tests executed. Nothing needs a server; both read
# the SQLite file in db/.
#
# `failsafe` is the profile that binds maven-failsafe-plugin, and naming any profile on the command
# line deactivates the activeByDefault generation, so the two have to be named together.
#
# As of 3.6.0 failsafe no longer binds its own skipTests parameter to the skipTests user property,
# so the two halves are selected separately -- and that is a property of ${version.maven-failsafe};
# under 3.5.x and earlier -DskipTests reached both:
#
#     ./_mvn_jakarta_ee_11_verify.sh -DskipTests   # the ITs alone
#     ./_mvn_jakarta_ee_11_verify.sh -DskipITs     # the unit tests alone
#     ./_mvn_jakarta_ee_11_verify_notests.sh       # neither
cd "$(dirname "$0")" || exit 1

exec ./mvnw -Pfailsafe,jakarta-ee-11 clean verify "$@"
