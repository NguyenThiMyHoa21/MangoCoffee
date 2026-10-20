@echo off
chcp 65001 > nul
title Coffee Mango - Spring Boot Server
echo ==========================================================
echo    ĐANG KHỞI ĐỘNG HỆ THỐNG COFFEE MANGO (PORT 8082)
echo ==========================================================
set "JAVA_HOME=C:\Users\Admin\.jdks\jbr-17.0.14"
set "PATH=%JAVA_HOME%\bin;%PATH%"

cd /d "F:\TichHopAI\coffeemango"
call mvnw.cmd spring-boot:run
pause
