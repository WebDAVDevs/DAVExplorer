# Makefile for DAV Explorer
#
# Copyright (c) 1999-2001 Regents of the University of California.
# All rights reserved.
#
# Redistribution and use in source and binary forms are permitted
# provided that the above copyright notice and this paragraph are
# duplicated in all such forms and that any documentation,
# advertising materials, and other materials related to such
# distribution and use acknowledge that the software was developed
# by the University of California, Irvine.  The name of the
# University may not be used to endorse or promote products derived
# from this software without specific prior written permission.
# THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
# IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
# WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.

SRC = .

all:	com HTTPClient edu

com::
	@( CLASSPATH=`pwd`:$(CLASSPATH); export CLASSPATH; cd $(SRC)/com; make all )

HTTPClient::
	@( CLASSPATH=`pwd`:$(CLASSPATH); export CLASSPATH; cd $(SRC)/HTTPClient; make all )

edu::
	@( CLASSPATH=`pwd`:$(CLASSPATH); export CLASSPATH; cd $(SRC)/edu; make all )

DAVExplorer.jar::
	jar -cfm DAVExplorer.jar DAVManifest \
                                 edu/uci/ics/DAVExplorer/*.class \
                                 edu/uci/ics/DAVExplorer/icons/* \
                                 HTTPClient/*.class \
                                 HTTPClient/http/*.class \
                                 HTTPClient/https/*.class \
                                 HTTPClient/shttp/*.class \
                                 com/ms/xml/dso/*.class \
                                 com/ms/xml/om/*.class \
                                 com/ms/xml/parser/*.class \
                                 com/ms/xml/util/*.class \
                                 com/ms/xml/xmlstream/*.class

clean::
	- cd $(SRC)/com; make clean
	- cd $(SRC)/HTTPClient; make clean
	- cd $(SRC)/edu; make clean
	- rm -f DAVExplorer.jar

dist::
	- cd $(SRC)/com; make dist
	- cd $(SRC)/HTTPClient; make dist
	- cd $(SRC)/edu; make dist
	- rm -f DAVExplorer.jar
	- rm -rf $(SRC)/CVS

