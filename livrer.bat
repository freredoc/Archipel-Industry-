@echo off
setlocal EnableDelayedExpansion

rem ===========================================================================
rem  ARCHIPEL INDUSTRY - livraison automatique
rem
rem  Ce script fait TOUT SEUL, depuis votre PC Windows :
rem    1. envoie la branche de travail sur GitHub
rem    2. ouvre la Pull Request (ou reprend celle deja ouverte)
rem    3. la fusionne dans main
rem    4. suit le build des APK et vous donne le lien quand c est pret
rem
rem  UTILISATION : double-cliquez ce fichier. Il doit se trouver a la RACINE
rem  du depot (a cote de Archipel_industry_alpha-7.html).
rem
rem  PREPARATION, une seule fois dans votre vie (dans PowerShell) :
rem      winget install --id GitHub.cli
rem      gh auth login
rem  (choisissez GitHub.com, HTTPS, puis connexion par navigateur)
rem
rem  NOTE : la CI de ce depot ne tourne QUE sur main (pas sur les PR).
rem  C est donc apres la fusion que les APK se construisent - le script
rem  attend ce build-la, pas un controle de PR qui n existerait jamais.
rem ===========================================================================

cd /d "%~dp0"

echo.
echo ==========================================================
echo    ARCHIPEL INDUSTRY - livraison automatique
echo ==========================================================
echo.

rem ---------------------------------------------------------------- 0. depot
git rev-parse --is-inside-work-tree >nul 2>&1
if errorlevel 1 (
  echo [ERREUR] Ce dossier n est pas un depot git.
  echo          Placez livrer.bat a la racine du depot Archipel-Industry-
  goto :fin
)

rem ------------------------------------------------------------- 0bis. gh ?
where gh >nul 2>&1
if errorlevel 1 (
  echo [ERREUR] L outil GitHub "gh" n est pas installe.
  echo.
  echo    Installez-le une seule fois, dans PowerShell :
  echo        winget install --id GitHub.cli
  echo.
  echo    Puis connectez-vous une seule fois :
  echo        gh auth login
  echo.
  goto :fin
)

gh auth status >nul 2>&1
if errorlevel 1 (
  echo [INFO] Pas encore connecte a GitHub. Ouverture de la connexion...
  echo.
  gh auth login
  gh auth status >nul 2>&1
  if errorlevel 1 (
    echo [ERREUR] Connexion GitHub echouee. Relancez le script.
    goto :fin
  )
)

rem -------------------------------------------------------- 1. branche
for /f "delims=" %%i in ('git rev-parse --abbrev-ref HEAD') do set "BR=%%i"
echo Branche courante : !BR!
if /i "!BR!"=="main" (
  echo.
  echo [ERREUR] Vous etes sur main : il n y a rien a livrer.
  echo          Placez-vous sur la branche de travail, par exemple :
  echo              git checkout claude/new-session-swpzcs
  goto :fin
)

rem ------------------------------------------- 2. modifications non commitees
set "SALE="
for /f "delims=" %%i in ('git status --porcelain') do set "SALE=1"
if defined SALE (
  echo.
  echo [ATTENTION] Des modifications ne sont pas encore enregistrees :
  echo.
  git status --short
  echo.
  set /p "REP=Les enregistrer maintenant ? (o/n) : "
  if /i "!REP!"=="o" (
    set /p "MSG=Message de commit : "
    git add -A
    git commit -m "!MSG!"
    if errorlevel 1 ( echo [ERREUR] Echec du commit. & goto :fin )
  ) else (
    echo Abandon : enregistrez d abord vos modifications, puis relancez.
    goto :fin
  )
)

rem ------------------------------------------------------- 3. rien a livrer ?
git fetch origin main >nul 2>&1
git merge-base --is-ancestor HEAD origin/main >nul 2>&1
if not errorlevel 1 (
  echo.
  echo [INFO] Cette branche est deja entierement fusionnee dans main.
  echo        Il n y a rien de nouveau a livrer.
  goto :fin
)

rem ------------------------------------------------------------- 4. push
echo.
echo [1/4] Envoi de la branche sur GitHub...
git push -u origin "!BR!"
if errorlevel 1 (
  echo [ERREUR] Echec de l envoi. Verifiez votre connexion, puis relancez.
  goto :fin
)

rem --------------------------------------------------------------- 5. PR
echo.
echo [2/4] Pull Request...
set "PR="
for /f "delims=" %%i in ('gh pr list --head "!BR!" --state open --json number --jq ".[0].number" 2^>nul') do set "PR=%%i"
if not defined PR (
  echo       Aucune PR ouverte : creation en cours...
  gh pr create --base main --head "!BR!" --fill
  if errorlevel 1 (
    echo [ERREUR] Echec de la creation de la Pull Request.
    goto :fin
  )
  for /f "delims=" %%i in ('gh pr list --head "!BR!" --state open --json number --jq ".[0].number" 2^>nul') do set "PR=%%i"
) else (
  echo       Une PR est deja ouverte.
)
if not defined PR (
  echo [ERREUR] Impossible de retrouver la Pull Request.
  goto :fin
)
echo       PR #!PR!

rem ------------------------------------------------------------ 6. fusion
echo.
echo [3/4] Fusion de la PR #!PR! dans main...
gh pr merge !PR! --merge
if errorlevel 1 (
  echo.
  echo [ERREUR] Fusion impossible - conflit avec main, ou droits insuffisants.
  echo          Pour voir ce qui bloque :
  echo              gh pr view !PR! --web
  goto :fin
)
echo       Fusionnee.

git checkout main >nul 2>&1
git pull --ff-only origin main >nul 2>&1

rem -------------------------------------------------------- 7. suivi du build
echo.
echo [4/4] Suivi du build des APK ^(declenche par la fusion^)...
echo       Patientez, cela prend generalement quelques minutes.
timeout /t 15 /nobreak >nul

set "RUN="
for /f "delims=" %%i in ('gh run list --workflow=android.yml --branch main --limit 1 --json databaseId --jq ".[0].databaseId" 2^>nul') do set "RUN=%%i"

if not defined RUN (
  echo.
  echo [INFO] Aucun build declenche : la fusion ne touchait aucun fichier
  echo        surveille par la CI ^(le jeu, android/, sw.js, manifest.json^).
  goto :bilan
)

gh run watch !RUN! --exit-status
if errorlevel 1 (
  echo.
  echo [ECHEC] Le build a echoue. Pour voir l erreur :
  echo             gh run view !RUN! --log-failed
  goto :fin
)

echo.
echo [OK] Build termine. Les APK sont ici :
echo         https://github.com/freredoc/Archipel-Industry-/releases/tag/apk-latest

:bilan
echo.
echo ==========================================================
echo    TERMINE - version en ligne :
git show origin/main:version.json 2>nul | findstr /C:"build" /C:"version"
echo ==========================================================

:fin
echo.
pause
endlocal
