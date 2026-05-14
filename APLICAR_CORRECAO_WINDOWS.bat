@echo off
chcp 65001 > nul
echo Limpando pastas antigas de pacote...
if exist app\src\main\java\com\rodrigues\entregador rmdir /s /q app\src\main\java\com\rodrigues\entregador
echo OK. Agora faça commit e push no GitHub.
pause
