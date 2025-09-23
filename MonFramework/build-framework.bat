@echo off
setlocal

:: ===============================================
:: Script de build et copie du framework MonFramework
:: ===============================================

:: --- CONFIGURATION ---
set "PROJECT_NAME=MonFramework"
set "TARGET_PROJECT=C:\Users\Fetraniaina\OneDrive\Documents\framework\MonProjetTest"
set "LIB_PATH=%TARGET_PROJECT%\lib"
set "JAR_NAME=monframework-core-1.0-SNAPSHOT.jar"
set "SOURCE_JAR=%CD%\target\%JAR_NAME%"

:: --- AFFICHAGE DES INFORMATIONS INITIALES ---
echo ===============================================
echo Projet framework      : %PROJECT_NAME%
echo Chemin projet test    : %TARGET_PROJECT%
echo Dossier lib destination: %LIB_PATH%
echo Nom du JAR            : %JAR_NAME%
echo Chemin du JAR source  : %SOURCE_JAR%
echo ===============================================

:: --- ETAPE 1 : Compilation ---
echo === Compilation du framework ===
mvn clean package
if errorlevel 1 (
    echo Erreur lors du build Maven !
    pause
    exit /b 1
)

:: --- ETAPE 2 : Vérification que le JAR a bien été généré ---
if not exist "%SOURCE_JAR%" (
    echo JAR non trouve : "%SOURCE_JAR%"
    echo Contenu du dossier target :
    dir "%CD%\target"
    pause
    exit /b 1
)

:: --- ETAPE 3 : Création du dossier lib si nécessaire ---
echo === Vérification du dossier lib ===
if not exist "%LIB_PATH%" (
    echo Le dossier lib n'existe pas, création...
    mkdir "%LIB_PATH%"
    if errorlevel 1 (
        echo Erreur lors de la création du dossier lib !
        pause
        exit /b 1
    )
) else (
    echo Dossier lib existe déjà.
)

:: --- ETAPE 4 : Affichage du contenu du dossier cible avant copie ---
echo Contenu du dossier cible avant copie :
dir "%LIB_PATH%"

:: --- ETAPE 5 : Copie du JAR ---
echo === Copie du JAR vers le projet test ===
echo Copie depuis : "%SOURCE_JAR%"
echo Vers       : "%LIB_PATH%\%JAR_NAME%"
copy /Y "%SOURCE_JAR%" "%LIB_PATH%\%JAR_NAME%"
if errorlevel 1 (
    echo Erreur lors de la copie du JAR !
    pause
    exit /b 1
)

:: --- ETAPE 6 : Vérification du contenu après copie ---
echo Contenu du dossier cible après copie :
dir "%LIB_PATH%"

echo ===============================================
echo Copie terminée. Le JAR se trouve dans "%LIB_PATH%"
echo ===============================================
pause
