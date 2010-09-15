option explicit
dim wshell
set wshell = createobject("wscript.shell")
wshell.run "javaw -jar DAVExplorer.jar", 0, false