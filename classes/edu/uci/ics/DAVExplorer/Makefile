
### general JDK definitions

JC	 = javac
JFLAGS   = 


### java extensions

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $<


### all source files

classes = AsGen.class \
	  CopyResponseEvent.class \
	  CopyResponseListener.class \
	  DataNode.class \
	  EscapeInputStream.class \
	  EscapeReader.class \
	  Main.class \
	  PropDialog.class \
	  PropDialogEvent.class \
	  PutEvent.class \
	  PutListener.class \
	  TableMap.class \
	  TableSorter.class \
	  URIBox.class \
	  ViewSelectionEvent.class \
	  ViewSelectionListener.class \
	  WebDAVConnection.class \
	  WebDAVFileView.class \
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


### targets 

all: $(classes)

clean::
	- rm -f *.class
