
### general JDK definitions

JC	 = $(JAVA_HOME)/bin/javac
JDOC	 = $(JAVA_HOME)/bin/javadoc
JFLAGS   = -O
CLASSP   = -classpath "$(JAVA_HOME)/lib/classes.zip:.."
JDCLASSP = -classpath "$(JAVA_HOME)/lib/classes.zip:.."

RM       = /usr/bin/rm -f
MV       = /usr/bin/mv -f


### java extensions

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $(CLASSP) $<


### all source files

classes = HTTPConnection.class \
	  HTTPResponse.class \
	  HttpOutputStream.class \
	  NVPair.class \
	  AuthorizationHandler.class \
	  AuthorizationInfo.class \
	  MD5.class \
	  StreamDemultiplexor.class \
	  ExtBufferedInputStream.class \
	  IdempotentSequence.class \
	  SocksClient.class \
	  RoRequest.class \
	  RoResponse.class \
	  Request.class \
	  Response.class \
	  CIHashtable.class \
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
	  Util.class \
	  HttpHeaderElement.class \
	  Codecs.class \
	  LinkedList.class \
	  ModuleException.class \
	  ProtocolNotSuppException.class \
	  AuthSchemeNotImplException.class \
	  ParseException.class \
	  SocksException.class


### targets 

all: $(classes)

doc::
	- $(RM) `ls doc/api/*.html | grep -v API_users_guide`
	$(JDOC) $(JDCLASSP) -author -version -public -d doc/api HTTPClient HTTPClient.http HTTPClient.https
	cd doc/api; fixup_links

kit::
	- $(RM) HTTPClient.zip
	- $(RM) HTTPClient.tar.gz
	cd ../; zip -r9 HTTPClient.zip HTTPClient
	cd ../; tar hcf HTTPClient.tar HTTPClient
	cd ../; $(MV) HTTPClient.zip HTTPClient
	cd ../; $(MV) HTTPClient.tar HTTPClient
	gzip HTTPClient.tar


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
AuthorizationModule.class : AuthorizationHandler.class

CookieModule.class : CookiePolicyHandler.class

ContentMD5Module.class \
MD5InputStream.class \
AuthorizationInfo.class : HashVerifier.class


