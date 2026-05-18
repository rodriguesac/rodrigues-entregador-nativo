@echo off
chcp 65001 >nul
title Rodrigues Entregador V5.4.1 - Limpa arquivos antigos e corrige build

echo.
echo ==========================================================
echo  RODRIGUES ENTREGADOR V5.4.1
echo  LIMPA ARQUIVOS ANTIGOS QUE ESTAVAM QUEBRANDO O BUILD
echo ==========================================================
echo.
echo Este BAT e para rodar dentro de:
echo   C:\RSITE\rodrigues-entregador-nativo
echo.
echo Corrige o erro do GitHub Actions:
echo - DriverForegroundService.kt usando serviceNotification antigo
echo - RodriguesMessagingService.kt usando showUrgent antigo
echo - RodriguesDriverApp.kt usando compose/viewModel antigo
echo.
echo Mantem:
echo - app 100%% nativo
echo - tela urgente
echo - Firebase
echo - alerta som/vibracao/notificacao
echo - compatibilidade com Torre V9.4
echo.

cd /d "%~dp0"

if not exist "app\src\main\java\com\rodriguesacai\entregador" (
  echo ERRO: execute este BAT na raiz do projeto rodrigues-entregador-nativo.
  pause
  exit /b 1
)

set "BACKUP=_backup_v5_4_1_%date:~-4%%date:~3,2%%date:~0,2%_%time:~0,2%%time:~3,2%%time:~6,2%"
set "BACKUP=%BACKUP: =0%"
mkdir "%BACKUP%" >nul 2>nul

echo.
echo Fazendo backup de arquivos antigos que podem quebrar a compilacao...

call :backup_delete "app\src\main\java\com\rodriguesacai\entregador\service\DriverForegroundService.kt"
call :backup_delete "app\src\main\java\com\rodriguesacai\entregador\service\RodriguesMessagingService.kt"
call :backup_delete "app\src\main\java\com\rodriguesacai\entregador\ui\RodriguesDriverApp.kt"

echo.
echo Limpando workflows antigos para rodar somente o build atual...
if exist ".github\workflows" (
  mkdir "%BACKUP%\.github\workflows" >nul 2>nul
  for %%F in (.github\workflows\*.yml .github\workflows\*.yaml) do (
    if /I not "%%~nxF"=="build-apk.yml" (
      move "%%F" "%BACKUP%\.github\workflows\" >nul 2>nul
    )
  )
)

echo.
echo Limpando cache/build...
if exist ".gradle" rmdir /s /q ".gradle"
if exist "build" rmdir /s /q "build"
if exist "app\build" rmdir /s /q "app\build"

echo.
echo Enviando V5.4.1 para o GitHub...
git add -A
git commit -m "V5.4.1 limpa arquivos antigos e corrige build nativo"
git push

echo.
echo Finalizado. Agora rode o GitHub Actions novamente.
pause
exit /b 0

:backup_delete
set "FILE=%~1"
if exist "%FILE%" (
  echo Movendo %FILE%
  mkdir "%BACKUP%\%~p1" >nul 2>nul
  move "%FILE%" "%BACKUP%\%~p1" >nul 2>nul
)
exit /b 0
