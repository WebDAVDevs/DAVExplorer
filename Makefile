# Makefile for DAV Explorer
#
# Copyright (c) 1999 Regents of the University of California.
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

classes = DAVExplorer/AsGen.class \
          DAVExplorer/AsNode.class 

all:	DAVExplorer DAVExplorer.jar

DAVExplorer::
	@ cd $(SRC)/classes; make all

DAVExplorer.jar::
	@ cd $(SRC)/classes; make DAVExplorer.jar
	@ mv $(SRC)/classes/DAVExplorer.jar $(SRC)

clean::
	- cd $(SRC)/classes; make clean
	- rm -f $(SRC)/DAVExplorer.jar

dist::
	- cd $(SRC)/classes; make dist
	- rm -f $(SRC)/DAVExplorer.jar
	- rm -rf $(SRC)/CVS
