@echo off
setlocal
cd /d "%~dp0"
echo Limpando workflows antigos para rodar somente a V17...
if exist ".github\workflows" (
  for %%F in (.github\workflows\*.yml .github\workflows\*.yaml) do (
    if /I not "%%~nxF"=="build-apk.yml" del "%%F"
  )
) else (
  mkdir ".github\workflows"
)
echo Mantido: .github\workflows\build-apk.yml
echo.
echo Agora rode:
echo git add -A
echo git commit -m "Rodrigues Entregador V17 Compose Material"
echo git push origin main
pause
