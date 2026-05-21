@echo off
setlocal EnableExtensions

set "REPO=C:\RSITE\rodrigues-entregador-nativo"
set "ZIP=%~1"
if "%ZIP%"=="" set "ZIP=%USERPROFILE%\Downloads\rodrigues-entregador-v5-8-2-carrossel-painel.zip"
set "TMP=C:\TEMP\rodrigues-v582-painel"
set "BRANCH=main"

echo.
echo ================================================
echo RESTAURAR RODRIGUES ENTREGADOR V5.8.2 PAINEL
echo ================================================
echo.

if not exist "%REPO%\.git" (
  echo ERRO: repositorio nao encontrado em:
  echo %REPO%
  pause
  exit /b 1
)

if not exist "%ZIP%" (
  echo ERRO: ZIP nao encontrado:
  echo %ZIP%
  echo.
  echo Dica: arraste o ZIP em cima deste BAT ou coloque o ZIP na pasta Downloads.
  pause
  exit /b 1
)

echo Limpando temporario...
rmdir /s /q "%TMP%" 2>nul
mkdir "%TMP%" 2>nul

echo Extraindo ZIP...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -LiteralPath '%ZIP%' -DestinationPath '%TMP%' -Force"
if errorlevel 1 (
  echo ERRO ao extrair o ZIP.
  pause
  exit /b 1
)

set "SRC=%TMP%"
if exist "%TMP%\rodrigues-entregador-v5-8-2-carrossel-painel\settings.gradle.kts" set "SRC=%TMP%\rodrigues-entregador-v5-8-2-carrossel-painel"

if not exist "%SRC%\settings.gradle.kts" (
  for /d %%D in ("%TMP%\*") do (
    if exist "%%D\settings.gradle.kts" set "SRC=%%D"
    if exist "%%D\settings.gradle" set "SRC=%%D"
  )
)

if not exist "%SRC%\settings.gradle.kts" if not exist "%SRC%\settings.gradle" (
  echo ERRO: nao achei settings.gradle no ZIP extraido.
  echo Pasta temporaria:
  echo %TMP%
  pause
  exit /b 1
)

echo Origem:
echo %SRC%
echo.

cd /d "%REPO%"

echo Criando branch backup local...
git branch backup-antes-v582-painel 2>nul

echo Limpando repositorio preservando .git...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Get-ChildItem -Force '%REPO%' | Where-Object { $_.Name -ne '.git' } | Remove-Item -Recurse -Force"

echo Copiando projeto novo...
robocopy "%SRC%" "%REPO%" /E /XD .git
if %ERRORLEVEL% GEQ 8 (
  echo ERRO no ROBOCOPY.
  pause
  exit /b 1
)

cd /d "%REPO%"

echo Conferindo versao:
findstr /R /C:"versionCode" /C:"versionName" app\build.gradle.kts

echo.
echo Enviando para Git...
git add -A
git commit -m "Restaura V5.8.2 carrossel integrado ao painel"
if errorlevel 1 (
  echo Nao houve commit novo ou ocorreu erro. Vou tentar push mesmo assim.
)

git push origin %BRANCH%
if errorlevel 1 (
  echo ERRO no push. Confira login do GitHub ou conexao.
  pause
  exit /b 1
)

echo.
echo FINALIZADO COM SUCESSO.
echo Abra o GitHub Actions e procure o commit:
echo Restaura V5.8.2 carrossel integrado ao painel
pause
