
XMLURLStreamps.dll: dlldata.obj XMLURLStream_p.obj XMLURLStream_i.obj
	link /dll /out:XMLURLStreamps.dll /def:XMLURLStreamps.def /entry:DllMain dlldata.obj XMLURLStream_p.obj XMLURLStream_i.obj kernel32.lib rpcndr.lib rpcns4.lib rpcrt4.lib oleaut32.lib uuid.lib 

.c.obj:
	cl /c /Ox /DWIN32 /D_WIN32_WINNT=0x0400 /DREGISTER_PROXY_DLL $<

clean:
	@del XMLURLStreamps.dll
	@del XMLURLStreamps.lib
	@del XMLURLStreamps.exp
	@del dlldata.obj
	@del XMLURLStream_p.obj
	@del XMLURLStream_i.obj
