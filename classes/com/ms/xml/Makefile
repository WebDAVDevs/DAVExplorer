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

JC	 = javac
JFLAGS   = 

SRC = .

### java extensions

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $<


### all source files

classes = dso/XMLDSO.class \
	  om/Document.class \
	  om/DocumentBeanInfo.class \
	  om/Element.class \
	  om/ElementCollection.class \
	  om/ElementDeclEnumeration.class \
	  om/ElementEnumeration.class \
	  om/ElementFactory.class \
	  om/ElementFactoryImpl.class \
	  om/ElementImpl.class \
	  om/ElementImplBeanInfo.class \
	  om/SiblingEnumeration.class \
	  om/TreeEnumeration.class \
	  parser/AttDef.class \
	  parser/ContentModel.class \
	  parser/Context.class \
	  parser/DTD.class \
	  parser/ElementDecl.class \
	  parser/ElementDeclEnumeration.class \
	  parser/Entity.class \
	  parser/EntityReader.class \
	  parser/Notation.class \
	  parser/ParseException.class \
	  parser/Parser.class \
	  util/Atom.class \
	  util/Attribute.class \
	  util/Attributes.class \
	  util/ByteSwapInputStream.class \
	  util/ByteSwapOutputStream.class \
	  util/EnumWrapper.class \
	  util/Name.class \
	  util/NameSpaceContext.class \
	  util/Queue.class \
	  util/ReadOnlyAttributes.class \
	  util/StringHashtable.class \
	  util/StringInputStream.class \
	  util/XMLInputStream.class \
	  util/XMLOutputStream.class \
	  util/XMLStreamReader.class \
	  xmlstream/IXMLStream.class \
	  xmlstream/XMLStream.class

### targets 

all: $(classes)

clean::
	- cd $(SRC)/dso; rm -f *.class
	- cd $(SRC)/om; rm -f *.class
	- cd $(SRC)/parser; rm -f *.class
	- cd $(SRC)/util; rm -f *.class
	- cd $(SRC)/xmlstream; rm -f *.class

