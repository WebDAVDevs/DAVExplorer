# Makefile for DAV Explorer
#
# Copyright (c) 1999-2005 Regents of the University of California.
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

classes = ACLAddDialog.class \
	ACLChangePrivilegesDialog.class \
	ACLDialog.class \
	ACLInheritedDialog.class \
	ACLModel.class \
	ACLNode.class \
	ACLOwnerDialog.class \
	ACLPrivilegesDialog.class \
	ACLPropModel.class \
	ACLPropertySearchModel.class \
	ACLPropertySearchNode.class \
	ACLReportChangeSearchPropertiesDialog.class \
	ACLReportPropertiesDialog.class \
	ACLReportSearchPropertyDialog.class \
	ACLRequestGenerator.class \
	ACLResponseInterpreter.class \
	ACLRestrictionDialog.class \
	ACLXML.class \
	AppletMain.class \
	AsGen.class \
	AuthDialog.class \
	ContentEncoding.class \
	CopyResponseEvent.class \
	CopyResponseListener.class \
	DataNode.class \
	DeltaVDataNode.class \
	DeltaVProp.class \
	DeltaVRequestGenerator.class \
	DeltaVResponseInterpreter.class \
	DeltaVXML.class \
	EscapeInputStream.class \
	EscapeReader.class \
	GlobalData.class \
	InsertionListener.class \
	JTreeTable.class \
	Main.class \
	PrincipalPropertiesModel.class \
	ProgressBar.class \
	PropAddDialog.class \
	PropDialog.class \
	PropModel.class \
	PropNode.class \
	PutEvent.class \
	PutListener.class \
	RelaxedX509TrustManager.class \
	ResponseException.class \
	SSLTrustDialog.class \
	TableMap.class \
	TableSorter.class \
	TransferEncoding.class \
	TreeTableModel.class \
	TreeTableModelAdapter.class \
	URIBox.class \
	URIContainer.class \
	VersionInfoDialog.class \
	ViewSelectionEvent.class \
	ViewSelectionListener.class \
	WebDAVCompletionEvent.class \
	WebDAVCompletionListener.class \
	WebDAVConnection.class \
	WebDAVFileView.class \
	WebDAVLockInfo.class \
	WebDAVLoginDialog.class \
	WebDAVManager.class \
	WebDAVMenu.class \
	WebDAVMenuListener.class \
	WebDAVProp.class \
	WebDAVProxyInfo.class \
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
	WebDAVXML.class

### targets 

all: $(classes)

clean::
	- rm -f *.class

dist::
	- rm -f *.class
	- cd icons; rm -rf CVS
	- rm -rf CVS
