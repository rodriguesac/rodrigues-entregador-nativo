@echo off
chcp 65001 >nul
title Rodrigues Entregador V5.5 - Radar real tocando

echo.
echo ==========================================================
echo  RODRIGUES ENTREGADOR V5.5
echo  RADAR REAL - CONECTADO COM TORRE V7.6
echo ==========================================================
echo.
echo Rode dentro de:
echo   C:\RSITE\rodrigues-entregador-nativo
echo.
echo Este pacote:
echo - mantem APK 100%% nativo
echo - mantem tela urgente
echo - reforca escuta do Firebase
echo - aceita radar da Torre mesmo se ela gravar so BUSCANDO_ENTREGADOR
echo - escuta entregadorAtualOferta/targetDriverId
echo - remove arquivos antigos que quebravam build
echo.

cd /d "%~dp0"

if not exist "app\src\main\java\com\rodriguesacai\entregador" (
  echo ERRO: execute este BAT na raiz do projeto rodrigues-entregador-nativo.
  pause
  exit /b 1
)

set "BACKUP=_backup_v5_5_radar_%date:~-4%%date:~3,2%%date:~0,2%_%time:~0,2%%time:~3,2%%time:~6,2%"
set "BACKUP=%BACKUP: =0%"
mkdir "%BACKUP%" >nul 2>nul

echo Movendo arquivos/pastas antigas que confundem o build...
call :backup_delete "app\src\main\java\com\rodriguesacai\entregador\service\DriverForegroundService.kt"
call :backup_delete "app\src\main\java\com\rodriguesacai\entregador\service\RodriguesMessagingService.kt"
call :backup_delete "app\src\main\java\com\rodriguesacai\entregador\ui\RodriguesDriverApp.kt"

for %%F in (APLICAR_E_LIMPAR_V5_3_3_WINDOWS.bat APLICAR_E_LIMPAR_V5_4_1_WINDOWS.bat) do (
  if exist "%%F" move "%%F" "%BACKUP%\" >nul 2>nul
)

for %%F in (*.zip) do (
  move "%%F" "%BACKUP%\" >nul 2>nul
)

for /d %%D in (arquivos rodrigues-entregador-nativo-v55-radar-login rodrigues-entregador-nativo-v56-radar-direto-login-pro) do (
  if exist "%%D" move "%%D" "%BACKUP%\" >nul 2>nul
)

if exist ".github\workflows" (
  mkdir "%BACKUP%\.github\workflows" >nul 2>nul
  for %%F in (.github\workflows\*.yml .github\workflows\*.yaml) do (
    if /I not "%%~nxF"=="build-apk.yml" (
      move "%%F" "%BACKUP%\.github\workflows\" >nul 2>nul
    )
  )
)

echo Limpando build/cache...
if exist ".gradle" rmdir /s /q ".gradle"
if exist "build" rmdir /s /q "build"
if exist "app\build" rmdir /s /q "app\build"

echo.
echo Enviando V5.5 para o GitHub...
git add -A
git commit -m "V5.5 radar real tocando com Torre V7.6"
git push

echo.
echo Finalizado. Rode o GitHub Actions e baixe o APK.
pause
exit /b 0

:backup_delete
set "FILE=%~1"
if exist "%FILE%" (
  mkdir "%BACKUP%\%~p1" >nul 2>nul
  move "%FILE%" "%BACKUP%\%~p1" >nul 2>nul
)
exit /b 0
