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

### general JDK definitions

JC	 = javac
JFLAGS   = 


### java extensions

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $<


### all source files

classes = AppletAuthorizationPrompter.class \
	  AppletMain.class \
	  AsGen.class \
	  CopyResponseEvent.class \
	  CopyResponseListener.class \
	  DataNode.class \
	  EscapeInputStream.class \
	  EscapeReader.class \
	  GlobalData.class \
	  WebDAVFileView.class \
	  PropDialog.class \
	  PutEvent.class \
	  PutListener.class \
	  TableMap.class \
	  TableSorter.class \
	  URIBox.class \
	  ViewSelectionEvent.class \
	  ViewSelectionListener.class \
	  WebDAVConnection.class \
	  WebDAVLockInfo.class \
	  WebDAVLoginDialog.class \
	  WebDAVManager.class \
	  WebDAVMenu.class \
	  WebDAVMenuListener.class \
	  WebDAVProp.class \
	  WebDAVRequestEvent.class \
	  WebDAVRequestGenerator.class \
	  WebDAVRequestListener.class \
	  WebDAVResponseEvent.class \
	  WebDAVResponseInterpreter.class \
	  WebDAVResponseListener.class \
	  WebDAVToolBar.class \
	  WebDAVTreeNode.class \
	  WebDAVTreeView.class \
	  WebDAVURIBoxListener.class \
	  WebDAVXML.class \
          GlobalData.class \
          JTreeTable.class \
          PropAddDialog.class \
          PropModel.class \
          PropNode.class \
          TreeTableModel.class \
          TreeTableModelAdapter.class \
          WebDAVCompletionEvent.class \
          WebDAVCompletionListener.class \
	  Main.class


### targets 

all: $(classes)

clean::
	- rm -f *.class

dist::
	- rm -f *.class
	- cd icons; rm -rf CVS
	- rm -rf CVS
