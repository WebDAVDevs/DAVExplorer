Changes for version 0.70:
- Support for SSL (https protocol)
  The SSL package supported is Sun's Java Secure Socket Extensions 1.0.2 (JSSE),
  available at http://java.sun.com/products/jsse/
  SSL is activated with the following command line:
  java -jar -Dssl=true DAVExplorer.jar
- Compatibility with JDK 1.3 was confirmed. The DAVExplorer.jar file is now
  created with JDK 1.3.0

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
