// XMLStream.h : Declaration of the CXMLStream

#ifndef __XMLSTREAM_H_
#define __XMLSTREAM_H_

#include "resource.h"       // main symbols

#define  SIZE 4096

const int UTF8  = 0;
const int ASCII = 1;
const int UCS2  = 2;
const int UCS4  = 3;
const int EBCDIC= 4;
const int W1252 = 5;

/////////////////////////////////////////////////////////////////////////////
// CXMLStream
class ATL_NO_VTABLE CXMLStream : 
	public CComObjectRootEx<CComSingleThreadModel>,
	public CComCoClass<CXMLStream, &CLSID_XMLStream>,
	public IDispatchImpl<IXMLStream, &IID_IXMLStream, &LIBID_XMLURLSTREAMLib>
{
public:
	CXMLStream()
	{
	}

DECLARE_REGISTRY_RESOURCEID(IDR_XMLSTREAM)

BEGIN_COM_MAP(CXMLStream)
	COM_INTERFACE_ENTRY(IXMLStream)
	COM_INTERFACE_ENTRY(IDispatch)
END_COM_MAP()

// IXMLStream
public:
	STDMETHOD(SetEncoding)(/*[in]*/ int encoding, /*[in]*/ int offset);
	STDMETHOD(Read)(/*[out]*/ int * buf, /*[in]*/ int len, /*[out, retval]*/ int * al);
	STDMETHOD(Open)(/*[in]*/ BSTR url, /*[out, retval]*/ int * outEncoding);
	STDMETHOD(Close)();
private:
	bool littleendian;
	bool byteOrderMark;
	IStream * pStream;
	unsigned char buffer[SIZE];
	int index;
	int size;
	int encoding;
};

#endif //__XMLSTREAM_H_
