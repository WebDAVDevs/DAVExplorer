@echo off
set OLDCLASSPATH=%CLASSPATH%
for /f %%i in ('cd') do set CLASSPATH=%%i;%CLASSPATH%
cd com
call make.bat
cd ..\HTTPClient
call make.bat
cd ..\DAVExplorer
call make.bat
echo on
cd ..
c:\jdk1.3\bin\jar -cfm DAVExplorer.jar DAVManifest DAVExplorer\*.class DAVExplorer\icons\* HTTPClient\*.class HTTPClient\http\*.class HTTPClient\https\*.class HTTPClient\shttp\*.class com\ms\xml\dso\*.class com\ms\xml\om\*.class com\ms\xml\parser\*.class com\ms\xml\util\*.class com\ms\xml\xmlstream\*.class
set CLASSPATH=%OLDCLASSPATH%
