# Makefile for DAV Explorer (uci part)
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

all:	ics

ics::
	@( CLASSPATH=`pwd`:$(CLASSPATH); export CLASSPATH; cd $(SRC)/ics; make all )

clean::
	- cd $(SRC)/ics; make clean

dist::
	- cd $(SRC)/ics; make dist
	- rm -rf $(SRC)/.svn

