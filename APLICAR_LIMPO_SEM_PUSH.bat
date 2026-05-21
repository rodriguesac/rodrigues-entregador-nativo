@echo off
setlocal
chcp 65001 >nul
echo.
echo Aplicando Rodrigues Entregador V11 Nativo V8 Fiel UI Real...
echo Este BAT copia arquivos para a pasta atual e NAO faz commit nem push.
echo.
if not exist app (
  echo ERRO: execute este BAT dentro da pasta do repositorio rodrigues-entregador-nativo.
  pause
  exit /b 1
)
echo Limpando arquivos de documentacao antigos se existirem...
for %%F in (README.md LEIA*.txt *.md) do if exist "%%F" del /f /q "%%F"
echo Pronto. Agora use: git status, git add -A, git commit, git push.
pause
