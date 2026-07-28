#!/bin/bash
set -e

cd "$(dirname "$0")"

WAR_NAME=framework-test.war
SRC_DIR="$(pwd)"
TMP_DIR="$SRC_DIR/war-tmp"
TOMCAT_WEBAPPS=/var/lib/tomcat10/webapps

mvn clean compile

echo "Nettoyage du dossier temporaire..."
rm -rf "$TMP_DIR"

echo "Création du dossier temporaire..."
mkdir -p "$TMP_DIR"

echo "Copie des fichiers nécessaires..."
cp -r "$SRC_DIR/WEB-INF" "$TMP_DIR/WEB-INF"
mkdir -p "$TMP_DIR/WEB-INF/classes"
cp -r "$SRC_DIR/target/classes/." "$TMP_DIR/WEB-INF/classes/"
mkdir -p "$TMP_DIR/templates"
cp -r "$SRC_DIR/src/main/webapp/templates/." "$TMP_DIR/templates/"

echo "Suppression ancien WAR..."
rm -f "$SRC_DIR/$WAR_NAME"

echo "Création du WAR..."
(cd "$TMP_DIR" && jar -cf "$SRC_DIR/$WAR_NAME" .)

echo "Copie vers Tomcat webapps (sudo requis)..."
sudo cp "$SRC_DIR/$WAR_NAME" "$TOMCAT_WEBAPPS/"

echo "Nettoyage du dossier temporaire..."
rm -rf "$TMP_DIR"

echo "Déployé : $WAR_NAME -> $TOMCAT_WEBAPPS"
