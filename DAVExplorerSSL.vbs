option explicit
dim wshell
set wshell = createobject("wscript.shell")
wshell.run "javaw -jar -DSSL=yes DAVExplorer.jar", 0, false