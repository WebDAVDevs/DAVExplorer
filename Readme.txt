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
  all			all function traces are enabled
  request		function traces related to HTTP requests are enabled
  response		function traces related to HTTP responses are enabled
  treeview		function traces related to the tree view on the left side
                        of the DAVExplorer window are enabled
  treenode		function traces related to each node in the tree view are
                        enabled
  fileview		function traces related to the file view on the right side
                        of the DAVExplorer window are enabled

  The trace output is written to stderr.

