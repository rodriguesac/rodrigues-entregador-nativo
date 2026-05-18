@echo off
chcp 65001 >nul
title Rodrigues Entregador V5.3.3 - Corrige radar Stroke

echo.
echo ==========================================================
echo  RODRIGUES ENTREGADOR V5.3.3
echo  CORRIGE IMPORT DO STROKE/RADAR
echo ==========================================================
echo.
echo Corrige o erro:
echo - Unresolved reference 'Stroke'
echo.
echo No navegador traduzido pode aparecer como:
echo - Referencia nao resolvida 'Derrame'
echo.
echo Mantem a regra da V5.3.2:
echo - pedido so chega no motoboy depois da loja aceitar/liberar
echo.

cd /d "%~dp0"

if not exist "app" (
  echo ERRO: execute este BAT na raiz do projeto rodrigues-entregador-nativo.
  pause
  exit /b 1
)

set "BACKUP=_backup_antigos_%date:~-4%%date:~3,2%%date:~0,2%_%time:~0,2%%time:~3,2%%time:~6,2%"
set "BACKUP=%BACKUP: =0%"
mkdir "%BACKUP%" >nul 2>nul

echo Movendo arquivos antigos para %BACKUP% ...

for %%F in (*.bat README*.md README*.txt LEIA*.txt) do (
  if /I not "%%~nxF"=="APLICAR_E_LIMPAR_V5_3_3_WINDOWS.bat" (
    move "%%F" "%BACKUP%\" >nul 2>nul
  )
)

if exist "docs" move "docs" "%BACKUP%\docs" >nul 2>nul

for /d %%D in (_backup*) do (
  if /I not "%%~nxD"=="%BACKUP%" (
    move "%%D" "%BACKUP%\%%~nxD" >nul 2>nul
  )
)

echo Limpando cache/build local...
if exist ".gradle" rmdir /s /q ".gradle"
if exist "build" rmdir /s /q "build"
if exist "app\build" rmdir /s /q "app\build"

echo.
echo Enviando para o GitHub...
git add -A
git commit -m "V5.3.3 corrige import Stroke do radar"
git push

echo.
echo Finalizado. Agora rode o GitHub Actions para gerar o APK.
pause
