@echo off
cd classes
call make.bat
cd ..
del DAVExplorer.jar
move classes\DAVExplorer.jar .
