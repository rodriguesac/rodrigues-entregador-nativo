@echo off
chcp 65001 >nul
title Rodrigues Entregador V5.6.1 - Corrige KeyboardPassword

echo.
echo ==========================================================
echo  RODRIGUES ENTREGADOR V5.6.1
echo  CORRIGE BUILD - KEYBOARDPASSWORD
echo ==========================================================
echo.
echo Rode dentro de:
echo   C:\RSITE\rodrigues-entregador-nativo
echo.
echo Corrige o erro:
echo   Unresolved reference 'KeyboardPassword'
echo.
echo Mantem:
echo - V5.6
echo - tema claro/escuro
echo - login enxuto
echo - tela urgente com Ver detalhes
echo - corrida aceita como EM ANDAMENTO
echo - olhinho de ocultar valores
echo.

cd /d "%~dp0"

if not exist "app\src\main\java\com\rodriguesacai\entregador" (
  echo ERRO: execute este BAT na raiz do projeto rodrigues-entregador-nativo.
  pause
  exit /b 1
)

set "BACKUP=_backup_v5_6_1_%date:~-4%%date:~3,2%%date:~0,2%_%time:~0,2%%time:~3,2%%time:~6,2%"
set "BACKUP=%BACKUP: =0%"
mkdir "%BACKUP%" >nul 2>nul

echo Movendo BATs/ZIPs antigos para backup...
for %%F in (APLICAR_E_LIMPAR_*.bat CORRIGIR_*.bat) do (
  if /I not "%%~nxF"=="APLICAR_E_LIMPAR_V5_6_1_CORRIGE_BUILD_WINDOWS.bat" move "%%F" "%BACKUP%\" >nul 2>nul
)
for %%F in (*.zip) do (
  move "%%F" "%BACKUP%\" >nul 2>nul
)

echo Criando .gitignore limpo...
(
echo _backup*/
echo *.zip
echo arquivos/
echo rodrigues-entregador-nativo-v*/
echo .gradle/
echo build/
echo app/build/
echo local.properties
echo *.apk
echo *.aab
) > .gitignore

echo Configurando Git para nao travar limpando pack .idx...
git config --global gc.auto 0
git config --global core.autocrlf false

echo Limpando build/cache...
if exist ".gradle" rmdir /s /q ".gradle"
if exist "build" rmdir /s /q "build"
if exist "app\build" rmdir /s /q "app\build"

echo Limpando stage anterior...
git reset

echo Adicionando somente arquivos uteis...
git add .gitignore
git add APLICAR_E_LIMPAR_V5_6_1_CORRIGE_BUILD_WINDOWS.bat
git add .github/workflows/build-apk.yml
git add build.gradle.kts
git add settings.gradle.kts
git add gradle.properties
git add app

echo.
echo Commit e push...
git commit -m "V5.6.1 corrige KeyboardPassword build"
git push

echo.
echo Finalizado. Rode o GitHub Actions novamente.
pause
