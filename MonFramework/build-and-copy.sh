#!/bin/bash
set -e

cd "$(dirname "$0")"

mvn clean install

# Copier le jar généré vers ../MonProjetTest/WEB-INF/lib/
cp target/framework-sprint-1.jar ../MonProjetTest/WEB-INF/lib/

echo "OK : jar copié dans MonProjetTest/WEB-INF/lib/"
