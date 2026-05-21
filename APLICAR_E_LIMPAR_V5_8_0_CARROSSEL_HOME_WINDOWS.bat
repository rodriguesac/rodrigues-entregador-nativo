@echo off
chcp 65001 >nul
title Rodrigues Entregador V5.8.0 - Carrossel Home

echo.
echo ==========================================================
echo  RODRIGUES ENTREGADOR V5.8.0
echo  CARROSSEL NA HOME COMPATIVEL COM PAINEL
echo ==========================================================
echo.
echo Rode dentro da raiz do projeto:
echo   C:\RSITE\rodrigues-entregador-nativo
echo.
echo Inclui:
echo - Carrossel na tela inicial quando nao houver corrida ativa/oferta.
echo - Leitura de banners do painel com title, badge, description, buttonText, imageUrl, active, order, actionType e actionTarget.
echo - Fallback local bonito quando ainda nao houver banner cadastrado no Firebase.
echo - Versao Android alterada para 5.8.0-carrossel e versionCode 70.
echo - Workflow usando gradle instalado no GitHub Actions, sem depender de gradlew.
echo.

cd /d "%~dp0"

if not exist "app\src\main\java\com\rodriguesacai\entregador" (
  echo ERRO: execute este BAT na raiz do projeto rodrigues-entregador-nativo.
  pause
  exit /b 1
)

set "BACKUP=_backup_v5_8_0_carrossel_%date:~-4%%date:~3,2%%date:~0,2%_%time:~0,2%%time:~3,2%%time:~6,2%"
set "BACKUP=%BACKUP: =0%"
mkdir "%BACKUP%" >nul 2>nul

echo Movendo arquivos antigos/inuteis para backup...
for %%F in (APLICAR_E_LIMPAR_*.bat CORRIGIR_*.bat LEIA*.txt README*.md *.zip) do (
  if /I not "%%~nxF"=="APLICAR_E_LIMPAR_V5_8_0_CARROSSEL_HOME_WINDOWS.bat" move "%%F" "%BACKUP%\" >nul 2>nul
)
if exist "docs" move "docs" "%BACKUP%\docs" >nul 2>nul
if exist "arquivos" move "arquivos" "%BACKUP%\arquivos" >nul 2>nul

echo Criando .gitignore limpo...
(
echo _backup*/
echo *.zip
echo arquivos/
echo docs/
echo rodrigues-entregador-nativo-v*/
echo .gradle/
echo build/
echo app/build/
echo local.properties
echo *.apk
echo *.aab
) > .gitignore

echo Configurando Git...
git config --global gc.auto 0
git config --global core.autocrlf false

echo Limpando build/cache...
if exist ".gradle" rmdir /s /q ".gradle"
if exist "build" rmdir /s /q "build"
if exist "app\build" rmdir /s /q "app\build"

echo Limpando stage anterior...
git reset

echo Adicionando somente arquivos uteis...
git add -A .gitignore
git add -A .github/workflows/build-apk.yml
git add -A build.gradle.kts settings.gradle.kts gradle.properties
git add -A app
git add -A APLICAR_E_LIMPAR_V5_8_0_CARROSSEL_HOME_WINDOWS.bat

echo.
echo Commit e push...
git commit -m "V5.8.0 carrossel home entregador"
git push

echo.
echo Finalizado. Abra o GitHub Actions e baixe o artefato rodrigues-entregador-nativo-debug-v5-8-0-carrossel.
pause
