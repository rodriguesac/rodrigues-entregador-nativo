@echo off
cd /d "%~dp0"
echo Aplicando V3 schema real do gestor...
git add -A
git commit -m "V3 corrige schema real entregadores pedidos rotas"
git push
echo Pronto. Abra o GitHub Actions e gere o APK.
pause
