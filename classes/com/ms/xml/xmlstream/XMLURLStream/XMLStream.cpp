// XMLStream.cpp : Implementation of CXMLStream
#include "stdafx.h"
#include "XMLURLStream.h"
#include "XMLStream.h"
#include "urlmon.h"
#include "stdio.h"

//########################
// WARING: - the SIZE variable has to be larger than the buffer length
// given in 'read' so that we do not lose first buffer in the case
// that setEncoding is called.  For example, switching from the default
// UTF8 to windows-cp1252 doesn't work if the buffers are the same size.
// 

/////////////////////////////////////////////////////////////////////////////
// CXMLStream
/////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////
// opens an input stream for @url and guesses encoding
// return: encoding, or -1 if failed
/////////////////////////////////////////////////////////////////////////////
STDMETHODIMP CXMLStream::Open(BSTR url, int * outEncoding)
{
	DWORD dwGot;
    unsigned char c1, c2, c3, c4;

	encoding = -1;
//    HRESULT hr = URLOpenBlockingStream(NULL, url, &pStream, 0, NULL);
    IMoniker *moniker;
	HRESULT hr = CreateURLMoniker(NULL, url, &moniker);
	if (hr != S_OK)
		return hr;
	IBindCtx *pbc;
	CreateBindCtx(0, &pbc);
	hr = moniker->BindToStorage(pbc, NULL, IID_IStream, (void **)(&pStream));
	
	if (hr == S_OK)
	{
		index = size = 0;
        littleendian   = false;
        byteOrderMark  = false;
        encoding       = UTF8;    // Default encoding
                 
		pStream->Read(buffer, SIZE, &dwGot);
		if (dwGot < 4)
			return -1;
		size = (int)dwGot;
		index = 0;

        // checks the first four bytes of the IStream in order to make a guess
        // as to the character encoding of the file.
        // Assumes that the document is following the XML standard and that
        // any non-UTF-8 file will begin with a <?XML> tag.
        c1 = buffer[0];
        c2 = buffer[1];
        c3 = buffer[2];
        c4 = buffer[3];
        if( c1 == 0xFE && c2 == 0xFF && c3 == 0x00 && c4 == 0x3C )
        {
            // UCS-2, big-endian
            byteOrderMark = true;
            encoding      = UCS2;
        }
        else if( c1 == 0xFF && c2 == 0xFE && c3 == 0x3C && c4 == 0x00 )
        {
            // UCS-2, little-endian
            littleendian  = true;
            byteOrderMark = true;
            encoding      = UCS2;
        }
        else if( c1 == 0x00 && c2 == 0x3C && c3 == 0x00 && c4 == 0x3F )
        {
            // UCS-2, big-endian, no Byte Order Mark
            encoding      = UCS2;
        }
        else if( c1 == 0x3C && c2 == 0x00 && c3 == 0x3F && c4 == 0x00 )
        {
            // UCS-2, little-endian, no Byte Order Mark
            littleendian  = true;
            encoding      = UCS2;           
        }
        else if( c1 == 0x3C && c2 == 0x3F && 
            c3 - 'x' + 'X' == 0x58 && 
            c4 - 'm' + 'M' == 0x4D )
        {
            // UTF-8, ISO 646, ASCII, some part of ISO 8859, Shift-JIS, EUC,
            // or any other encoding that ensures that ASCII has normal positions
            encoding      = ASCII;
        }
        else if( c1 == 0x00 && c2 == 0x00 && c3 == 0x00 && c4 == 0x3C )
        {
            // UCS-4, big-endian machine (1234 order)
            // Until UCS-4 is implemented
            encoding  = UCS4;
        }
        else if( c1 == 0x3C && c2 == 0x00 && c3 == 0x00 && c4 == 0x00 )
        {
            // UCS-4, little-endian machine (4321 order)
            // Until UCS-4 is implemented
            encoding  = UCS4;
        }
        else if( c1 == 0x00 && c2 == 0x00 && c3 == 0x3C && c4 == 0x00 )
        {
            // UCS-4, unusual octet order (2143 order)
            // Until UCS-4 is implemented
            encoding  = UCS4;
        }
        else if( c1 == 0x00 && c2 == 0x3C && c3 == 0x00 && c4 == 0x00 )
        {
            // UCS-4, unusual octet order (3412 order)
            // Until UCS-4 is implemented
            encoding  = UCS4;
        }
        else if( c1 == 0x4C && c2 == 0x6F && c3 == 0xE7 && c4 == 0xD4 )
        {
            // EBCDIC - We do NOT support this!
            // Until EBCDIC is implemented
            encoding  = EBCDIC;
        }

		if (encoding == UCS2 && byteOrderMark)
            index = 2;
	}
	else
		return hr;

	*outEncoding = encoding; 
	return S_OK;
}

/////////////////////////////////////////////////////////////////////////////
// close the stream
/////////////////////////////////////////////////////////////////////////////
STDMETHODIMP CXMLStream::Close()
{
	if (pStream)
	{
		pStream->Release();
		pStream = NULL;
	}
	return S_OK;
}

/////////////////////////////////////////////////////////////////////////////
// reads up to @len characters into @buf 
// return: number of characters in @buf
/////////////////////////////////////////////////////////////////////////////
STDMETHODIMP CXMLStream::Read(int * buf, int len, int * al)
{
    DWORD dwGot;
	HRESULT hr = NOERROR;
    int l = 0;
	int state = 0, v = 0, c = 0;
	bool eof = false;

	switch (encoding)
	{
		case UCS2:
			while (l < len)
			{
                if (index >= size)
				{
					*al = l;
					if (eof)
						return S_OK;
                    hr = pStream->Read(buffer, SIZE, &dwGot);
					if (dwGot == 0)
					{
						return S_OK;
					}
					else if (dwGot < SIZE)
						eof = true;
					index = 0;
					size = (int)dwGot;
				}
               
                int b1 = buffer[index++];
				int b2 = buffer[index++];
                if (littleendian)  // byte swap
                    buf[l++] = ((b2 << 8) | b1);
				else 
					buf[l++] = ((b1 << 8) | b2);
			}
			*al = l;
			return S_OK;
		case UTF8:
			while (l < len)
			{
                if (index >= size)
				{
					*al = l;
					if (eof)
						return S_OK;
                    hr = pStream->Read(buffer, SIZE, &dwGot);
					if (dwGot == 0)
					{
						return S_OK;
					}
					else if (dwGot < SIZE)
						eof = true;
					index = 0;
					size = dwGot;
				}
				c = (int)buffer[index++];
                switch (state) 
				{
				    case 0: // first byte of characters
						if (c >> 7 == 0)  // 0xxxxxxx, one byte, \u0000 - \u007F
						{
							buf[l++] = (int)c;
						}
						else if (c >> 5 == 0x06) // 110xxxxx, 10xxxxxx, two bytes, \u0080 - \u07FF
						{
                            v = (int) c & 0x1f;
							state = 1;
						}
						else // 1110xxxx, 10xxxxxx, 10xxxxxx, three bytes, \u0800 - \uFFFF
						{
                            v = (int) c & 0x0f;
							state = 2;
						}
						break;
					case 1: // second byte of a two-byte character, 10xxxxxx
						buf[l++] = (v << 6) + (c & 0x3f);
						state = 0;
						break;
					case 2: // second byte of a three-byte character, 10xxxxxx
						v = (v << 6) + (c & 0x3f);
						state = 3;
						break;
					case 3: // third byte of a three-byte character, 10xxxxxx
						buf[l++] = (v << 6) + (c & 0x3f);
						state = 0;
						break;
				}
			}
			*al = l;
			return S_OK;
		case UCS4: // until UCS4 & EBCDIC are implemented, uses ASCII decoding
		case EBCDIC:
		case ASCII:
		case W1252:
		{
			while (l < len)
			{
                if (index >= size)
				{
					*al = l;
					if (eof)
						return S_OK;
                    hr = pStream->Read(buffer, SIZE, &dwGot);
					
					if (dwGot == 0)
					{
						return S_OK;
					}
					else if (dwGot < SIZE)
						eof = true;
					index = 0;
					size = dwGot;
				}
                buf[l++] = (int)buffer[index++];
			}
			*al = l;
			return S_OK;
		}
		default: *al = -1; return -1;
	}
}

/////////////////////////////////////////////////////////////////////////////
// resets encoding. 
// we assume that setEncoding happens within the first SIZE bytes of input
// <code>index is reseted to @offset
/////////////////////////////////////////////////////////////////////////////
STDMETHODIMP CXMLStream::SetEncoding(int encoding, int offset)
{
	int c = 0, l = 0;

    // reset index
    switch (encoding)
	{
		case UCS2:
			index = 2 * offset;
			if (byteOrderMark)
				index += 2;
			break;
		case UTF8:
			index = 0;
			while (l < offset)
			{
				c = (int)buffer[index];
				if (c >> 7 == 0) // one byte
				{
					index++;
				}
				else if (c >> 5 == 0x06) // two bytes
				{
				    index += 2;
				}
				else // three bytes
				{
					index += 3;
				}
				l++;
			}
			break;
		case UCS4: // until UCS4 & EBCDIC are implemented, uses ASCII decoding
		case EBCDIC:
		case ASCII:
		case W1252:
		default:
			index = offset;
			break;
	}

    this->encoding = encoding;
	return S_OK;
}
