rem Makefile for DAV Explorer (parser part)
rem
rem Copyright (c) 1999-2001 Regents of the University of California.
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

@echo on
del dso\*.class
del om\*.class
del parser\*.class
del util\*.class
del xmlstream\*.class
javac dso\*.java om\*.java parser\*.java util\*.java xmlstream\*.java
@echo off
