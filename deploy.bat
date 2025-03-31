@echo off
setlocal enabledelayedexpansion

:: Déclaration des variables
set "work_dir=D:\ITU\semestre5\baov\projetS5\backup\boulangerie"
@REM set "work_dir=D:\boulangerie"
set "temp=%work_dir%\temp"
set "web=%work_dir%\web"
set "web_xml=%work_dir%\web.xml"
set "lib=%work_dir%\lib"
set "web_apps=C:\Program Files\Apache Software Foundation\Tomcat 10.1\webapps"
set "war_name=boulangerie"
set "src=%work_dir%\src"

:: Effacer le dossier [temp]
if exist "%temp%" (
    rd /s /q "%temp%"
)

:: Créer la structure de dossier
mkdir "%temp%\WEB-INF\lib"
mkdir "%temp%\WEB-INF\classes"

:: Copier le contenu de [web] dans [temp]
xcopy /s /y "%web%\*.*" "%temp%"

:: Copier le fichier [web_xml] vers [temp] + "\WEB-INF"
copy "%web_xml%" "%temp%\WEB-INF"

:: Copier les fichiers .jar dans [lib] vers [temp] + "\WEB-INF\lib"
xcopy /s /i "%lib%\*.jar" "%temp%\WEB-INF\lib"

:: Copier la structure de dossier de src dans WEB-INF/classes
xcopy /t /e "%src%" "%temp%\WEB-INF\classes"

:: Compilation des fichiers .java dans src avec les options suivantes
:: Note: Assurez-vous que le chemin vers le compilateur Java (javac) est correctement configuré dans votre variable d'environnement PATH.
:: Créer une liste de tous les fichiers .java dans le répertoire src et ses sous-répertoires
dir /s /B "%src%\*.java" > sources.txt
:: Exécuter la commande javac
javac -d "%temp%\WEB-INF\classes" -cp "%lib%\*" @sources.txt
:: Supprimer le fichiers sources.txt après la compilation
del sources.txt

:: Créer un fichier .war nommé [war_name].war à partir du dossier [temp] et son contenu dans le dossier [work_dir]
cd "%temp%"
jar cf "%work_dir%\%war_name%.war" *

:: Effacer le fichier .war dans [web_apps] s'il existe
if exist "%web_apps%\%war_name%.war" (
    del /f /q "%web_apps%\%war_name%.war"
)

:: Copier le fichier .war vers [web_apps]
copy /y "%work_dir%\%war_name%.war" "%web_apps%"

del "%work_dir%\%war_name%.war"

echo Deploy finished.
pause
