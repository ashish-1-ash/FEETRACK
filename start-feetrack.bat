@echo off
echo Starting FeeTrack Backend (Spring Boot)...
cd backend
set JAVA_HOME=C:\Program Files\Java\jdk-25
start "FeeTrack Backend" "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.1.3\plugins\maven\lib\maven3\bin\mvn.cmd" spring-boot:run

echo Starting FeeTrack Frontend (Vite React)...
cd ..\frontend
start "FeeTrack Frontend" npm run dev

echo.
echo ========================================================
echo  FeeTrack Full-Stack Application Launched!
echo  Frontend URL: http://localhost:5173
echo  Backend API:  http://localhost:8080/api/v1
echo.
echo  Demo Accounts:
echo  1. Student Portal:
echo     Email:    ashish@student.edu
echo     Password: student123
echo.
echo  2. Admin Portal:
echo     Email:    admin@feetrack.com
echo     Password: admin123
echo ========================================================
