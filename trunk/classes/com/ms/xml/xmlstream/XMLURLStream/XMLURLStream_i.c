/* this file contains the actual definitions of */
/* the IIDs and CLSIDs */

/* link this file in with the server and any clients */


/* File created by MIDL compiler version 3.01.75 */
/* at Wed Oct 22 20:07:54 1997
 */
/* Compiler settings for XMLURLStream.idl:
    Oicf (OptLev=i2), W1, Zp8, env=Win32, ms_ext, c_ext
    error checks: none
*/
//@@MIDL_FILE_HEADING(  )
#ifdef __cplusplus
extern "C"{
#endif 


#ifndef __IID_DEFINED__
#define __IID_DEFINED__

typedef struct _IID
{
    unsigned long x;
    unsigned short s1;
    unsigned short s2;
    unsigned char  c[8];
} IID;

#endif // __IID_DEFINED__

#ifndef CLSID_DEFINED
#define CLSID_DEFINED
typedef IID CLSID;
#endif // CLSID_DEFINED

const IID IID_IXMLStream = {0xC8AB904D,0x4132,0x11D1,{0xA2,0xCC,0x00,0xC0,0x4F,0xD7,0x35,0x33}};


const IID LIBID_XMLURLSTREAMLib = {0xC8AB9040,0x4132,0x11D1,{0xA2,0xCC,0x00,0xC0,0x4F,0xD7,0x35,0x33}};


const CLSID CLSID_XMLStream = {0x0C97E34E,0x412B,0x11D1,{0xA2,0xCB,0x00,0xC0,0x4F,0xD7,0x35,0x33}};


#ifdef __cplusplus
}
#endif

