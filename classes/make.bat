@echo off
rem Makefile for DAV Explorer
rem
rem Copyright (c) 1999-2002 Regents of the University of California.
rem All rights reserved.
rem
rem Redistribution and use in source and binary forms are permitted
rem provided that the above copyright notice and this paragraph are
rem duplicated in all such forms and that any documentation,
rem advertising materials, and other materials related to such
rem distribution and use acknowledge that the software was developed
rem by the University of California, Irvine.  The name of the
rem University may not be used to endorse or promote products derived
rem from this software without specific prior written permission.
rem THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
rem IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
rem WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.

set OLDCLASSPATH=%CLASSPATH%

rem The following line does not work on Win9x or WinME.
rem for /f %%i in ('cd') do set CLASSPATH=%%i;%CLASSPATH%

rem This is the ugly workaround for the above 'for /f ...' line
call pwd.bat
call cur.bat
set CLASSPATH=%PWD%;%CLASSPATH%

cd com
call make.bat
cd ..\HTTPClient
call make.bat
cd ..\edu
call make.bat
echo on
cd ..
jar -cfm DAVExplorer.jar DAVManifest edu\uci\ics\DAVExplorer\*.class edu\uci\ics\DAVExplorer\icons\*.GIF edu\uci\ics\DAVExplorer\icons\*.gif HTTPClient\*.class HTTPClient\http\*.class HTTPClient\https\*.class HTTPClient\shttp\*.class com\ms\xml\dso\*.class com\ms\xml\om\*.class com\ms\xml\parser\*.class com\ms\xml\util\*.class com\ms\xml\xmlstream\*.class
set CLASSPATH=%OLDCLASSPATH%
set OLDCLASSPATH=
set PWD=
