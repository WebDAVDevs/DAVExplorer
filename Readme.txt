Changes for version 0.73-dev:
- Makefile for HTTPClient now works with JSSE installed as "bundled" extension.
- Fixed handling of EOF for whitespace after the final tag (interoperability
  bug with Adobe InScope).
- Changed the parser code to allow tag names starting with a digit
  (interoperability problem with Microsoft SharePoint).
- Removed whitespace in the created XML for lockowner and keepalive properties.
- Interoperability problem fixed for cases when properties for a collection
  and its contents are requested and the server doesn't send properties for the
  collection itself.
- Modified the authentication code to make use of the HTTPClient functionality
  for Digest authentication.
- Copy and Move now allow entering the target, making them more flexible.

Changes for version 0.72:
- Support for operation through proxy servers
- Update to HTTPClient 0.3.3

Changes for version 0.71:
- Fixed bug in HTTPClient that prevented proper handling of authentication in
  case the server allows multiple possible authentication headers (thanks to Thierry
  Janaudy for alerting us to this problem).
- Fixed broken handling of & in filenames in the MS parser (thanks to Dennis Craig for
  alerting us to this problem).
- Now using the standard https port (443) for requests with SSL.
- Closing the properties dialog with the close button does not exit the application
  anymore.

Changes for version 0.70:
- Support for SSL (https protocol)
  The SSL package supported is Sun's Java Secure Socket Extensions 1.0.2 (JSSE),
  available at http://java.sun.com/products/jsse/
  SSL is activated with the following command line:
  java -jar -Dssl=true DAVExplorer.jar
- Compatibility with JDK 1.3 was confirmed. The DAVExplorer.jar file is now
  created with JDK 1.3.0
- Interoperability problem (another SiblingEnumeration bug in the parser) with IIS 5.0
  fixed (thanks to Ron Gutfinger for alerting us to this problem).

Changes for version 0.62:
- Improved recovery after server timeout

Changes for version 0.61:
- Added check for CDATA to improve interoperability for Sharemation's server.
- Changed the enumeration in parseResponse() to SiblingEnumeration to
  avoid parsing the wrong href tag (thanks to Michelle Harris for
  alerting us to this problem).
- Fixed string comparison in case of multiple <propstat> tags.
- href tag is now stripped from lock owner string (thanks to Eric Giguere
  for submitting this fix).

Changes for version 0.60:
- Fixed logging of incoming data.
- The DAV4J workaround (see below in the changes for version 0.57) is also required
  for locking support with mod_dav up to version 0.9.16.

Changes for version 0.59:
- Interoperability problem with www.sharemation.com fixed.
- The authentication dialog now acts as Authentication Handler for
  HTTPClient.

Changes for version 0.58:
- Fix interoperability problem with Glyphica server:
  The Glyphica server returns a wrong locktoken field, and DAV Explorer
  did not react to this properly.

Changes for version 0.57:
- Workaround for interoperability with DAV4J
  The workaround is activated with the following command line:
  java -jar -Dpropfind=allprop DAVExplorer.jar

Changes for version 0.56:
- DAVExplorer is now instrumented for function trace output.
  Function trace output is enabled by running DAVExplorer with the following
  command line:
  java -jar -Ddebug=option DAVExplorer.jar
  where option is one of:
  all                   all function traces are enabled
  request               function traces related to HTTP requests are enabled
  response              function traces related to HTTP responses are enabled
  treeview              function traces related to the tree view on the left side
                        of the DAVExplorer window are enabled
  treenode              function traces related to each node in the tree view are
                        enabled
  fileview              function traces related to the file view on the right side
                        of the DAVExplorer window are enabled
  The trace output is written to stderr.
