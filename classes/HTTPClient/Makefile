# Makefile for DAV Explorer (HTTPClient part)
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

classes = NVPair.class \
	  AuthorizationHandler.class \
	  AuthorizationInfo.class \
	  DefaultAuthHandler.class \
	  StreamDemultiplexor.class \
	  ExtBufferedInputStream.class \
	  IdempotentSequence.class \
	  SocksClient.class \
	  RoRequest.class \
	  RoResponse.class \
	  Request.class \
	  HTTPResponse.class \
	  Response.class \
	  ResponseHandler.class \
	  RespInputStream.class \
	  CIHashtable.class \
	  HttpOutputStream.class \
	  HTTPClientModule.class \
	  HTTPClientModuleConstants.class \
	  RedirectionModule.class \
	  RetryModule.class \
	  RetryException.class \
	  AuthorizationModule.class \
	  CookieModule.class \
	  Cookie.class \
	  Cookie2.class \
	  CookiePolicyHandler.class \
	  ContentMD5Module.class \
	  MD5.class \
	  MD5InputStream.class \
	  HashVerifier.class \
	  DefaultModule.class \
	  TransferEncodingModule.class \
	  ContentEncodingModule.class \
	  ChunkedInputStream.class \
	  UncompressInputStream.class \
	  HttpURLConnection.class \
	  http/Handler.class \
	  https/Handler.class \
	  shttp/Handler.class \
	  GlobalConstants.class \
	  URI.class \
	  Util.class \
	  HttpHeaderElement.class \
	  FilenameMangler.class \
	  Codecs.class \
	  LinkedList.class \
	  ModuleException.class \
	  ProtocolNotSuppException.class \
	  AuthSchemeNotImplException.class \
	  ParseException.class \
	  SocksException.class \
	  HTTPConnection.class


### targets 

all: $(classes)

clean::
	- rm -f *.class
	- cd http; rm -f *.class
	- cd https; rm -f *.class
	- cd shttp; rm -f *.class

dist::
	- rm -rf *.class CVS
	- cd http; rm -rf *.class CVS
	- cd https; rm -rf *.class CVS
	- cd shttp; rm -rf *.class CVS

### Interface Dependencies

HTTPConnection.class \
HTTPResponse.class \
HttpOutputStream.class \
Response.class \
StreamDemultiplexor.class \
AuthorizationInfo.class \
SocksClient.class \
AuthorizationModule.class \
ContentEncodingModule.class \
ContentMD5Module.class \
CookieModule.class \
DefaultModule.class \
RedirectionModule.class \
RetryModule.class \
TransferEncodingModule.class \
HttpURLConnection.class : GlobalConstants.class

Request.class \
HTTPClientModule.class \
CookiePolicyHandler.class \
AuthorizationHandler.class : RoRequest.class

Response.class \
HTTPClientModule.class \
CookiePolicyHandler.class \
AuthorizationHandler.class : RoResponse.class

HTTPConnection.class \
HTTPResponse.class \
RetryModule.class \
CookieModule.class \
RedirectionModule.class \
AuthorizationModule.class \
ContentMD5Module.class \
TransferEncodingModule.class \
ContentEncodingModule.class \
DefaultModule.class : HTTPClientModule.class

HTTPClientModule.class : HTTPClientModuleConstants.class

AuthorizationInfo.class \
AuthorizationModule.class \
DefaultAuthHandler.class : AuthorizationHandler.class

CookieModule.class : CookiePolicyHandler.class

ContentMD5Module.class \
MD5InputStream.class \
AuthorizationInfo.class : HashVerifier.class

Codecs.class : FilenameMangler.class

