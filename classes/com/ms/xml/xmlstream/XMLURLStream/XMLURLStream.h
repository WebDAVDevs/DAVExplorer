/* this ALWAYS GENERATED file contains the definitions for the interfaces */


/* File created by MIDL compiler version 3.01.75 */
/* at Wed Oct 22 20:07:54 1997
 */
/* Compiler settings for XMLURLStream.idl:
    Oicf (OptLev=i2), W1, Zp8, env=Win32, ms_ext, c_ext
    error checks: none
*/
//@@MIDL_FILE_HEADING(  )
#include "rpc.h"
#include "rpcndr.h"
#ifndef COM_NO_WINDOWS_H
#include "windows.h"
#include "ole2.h"
#endif /*COM_NO_WINDOWS_H*/

#ifndef __XMLURLStream_h__
#define __XMLURLStream_h__

#ifdef __cplusplus
extern "C"{
#endif 

/* Forward Declarations */ 

#ifndef __IXMLStream_FWD_DEFINED__
#define __IXMLStream_FWD_DEFINED__
typedef interface IXMLStream IXMLStream;
#endif 	/* __IXMLStream_FWD_DEFINED__ */


#ifndef __XMLStream_FWD_DEFINED__
#define __XMLStream_FWD_DEFINED__

#ifdef __cplusplus
typedef class XMLStream XMLStream;
#else
typedef struct XMLStream XMLStream;
#endif /* __cplusplus */

#endif 	/* __XMLStream_FWD_DEFINED__ */


/* header files for imported files */
#include "oaidl.h"
#include "ocidl.h"

void __RPC_FAR * __RPC_USER MIDL_user_allocate(size_t);
void __RPC_USER MIDL_user_free( void __RPC_FAR * ); 

#ifndef __IXMLStream_INTERFACE_DEFINED__
#define __IXMLStream_INTERFACE_DEFINED__

/****************************************
 * Generated header for interface: IXMLStream
 * at Wed Oct 22 20:07:54 1997
 * using MIDL 3.01.75
 ****************************************/
/* [unique][helpstring][dual][uuid][object] */ 



EXTERN_C const IID IID_IXMLStream;

#if defined(__cplusplus) && !defined(CINTERFACE)
    
    interface DECLSPEC_UUID("C8AB904D-4132-11D1-A2CC-00C04FD73533")
    IXMLStream : public IDispatch
    {
    public:
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE Open( 
            /* [in] */ BSTR url,
            /* [retval][out] */ int __RPC_FAR *outEncoding) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE Read( 
            /* [out] */ int __RPC_FAR *buf,
            /* [in] */ int len,
            /* [retval][out] */ int __RPC_FAR *al) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE SetEncoding( 
            /* [in] */ int encoding,
            /* [in] */ int offset) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE Close( void) = 0;
        
    };
    
#else 	/* C style interface */

    typedef struct IXMLStreamVtbl
    {
        BEGIN_INTERFACE
        
        HRESULT ( STDMETHODCALLTYPE __RPC_FAR *QueryInterface )( 
            IXMLStream __RPC_FAR * This,
            /* [in] */ REFIID riid,
            /* [iid_is][out] */ void __RPC_FAR *__RPC_FAR *ppvObject);
        
        ULONG ( STDMETHODCALLTYPE __RPC_FAR *AddRef )( 
            IXMLStream __RPC_FAR * This);
        
        ULONG ( STDMETHODCALLTYPE __RPC_FAR *Release )( 
            IXMLStream __RPC_FAR * This);
        
        HRESULT ( STDMETHODCALLTYPE __RPC_FAR *GetTypeInfoCount )( 
            IXMLStream __RPC_FAR * This,
            /* [out] */ UINT __RPC_FAR *pctinfo);
        
        HRESULT ( STDMETHODCALLTYPE __RPC_FAR *GetTypeInfo )( 
            IXMLStream __RPC_FAR * This,
            /* [in] */ UINT iTInfo,
            /* [in] */ LCID lcid,
            /* [out] */ ITypeInfo __RPC_FAR *__RPC_FAR *ppTInfo);
        
        HRESULT ( STDMETHODCALLTYPE __RPC_FAR *GetIDsOfNames )( 
            IXMLStream __RPC_FAR * This,
            /* [in] */ REFIID riid,
            /* [size_is][in] */ LPOLESTR __RPC_FAR *rgszNames,
            /* [in] */ UINT cNames,
            /* [in] */ LCID lcid,
            /* [size_is][out] */ DISPID __RPC_FAR *rgDispId);
        
        /* [local] */ HRESULT ( STDMETHODCALLTYPE __RPC_FAR *Invoke )( 
            IXMLStream __RPC_FAR * This,
            /* [in] */ DISPID dispIdMember,
            /* [in] */ REFIID riid,
            /* [in] */ LCID lcid,
            /* [in] */ WORD wFlags,
            /* [out][in] */ DISPPARAMS __RPC_FAR *pDispParams,
            /* [out] */ VARIANT __RPC_FAR *pVarResult,
            /* [out] */ EXCEPINFO __RPC_FAR *pExcepInfo,
            /* [out] */ UINT __RPC_FAR *puArgErr);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE __RPC_FAR *Open )( 
            IXMLStream __RPC_FAR * This,
            /* [in] */ BSTR url,
            /* [retval][out] */ int __RPC_FAR *outEncoding);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE __RPC_FAR *Read )( 
            IXMLStream __RPC_FAR * This,
            /* [out] */ int __RPC_FAR *buf,
            /* [in] */ int len,
            /* [retval][out] */ int __RPC_FAR *al);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE __RPC_FAR *SetEncoding )( 
            IXMLStream __RPC_FAR * This,
            /* [in] */ int encoding,
            /* [in] */ int offset);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE __RPC_FAR *Close )( 
            IXMLStream __RPC_FAR * This);
        
        END_INTERFACE
    } IXMLStreamVtbl;

    interface IXMLStream
    {
        CONST_VTBL struct IXMLStreamVtbl __RPC_FAR *lpVtbl;
    };

    

#ifdef COBJMACROS


#define IXMLStream_QueryInterface(This,riid,ppvObject)	\
    (This)->lpVtbl -> QueryInterface(This,riid,ppvObject)

#define IXMLStream_AddRef(This)	\
    (This)->lpVtbl -> AddRef(This)

#define IXMLStream_Release(This)	\
    (This)->lpVtbl -> Release(This)


#define IXMLStream_GetTypeInfoCount(This,pctinfo)	\
    (This)->lpVtbl -> GetTypeInfoCount(This,pctinfo)

#define IXMLStream_GetTypeInfo(This,iTInfo,lcid,ppTInfo)	\
    (This)->lpVtbl -> GetTypeInfo(This,iTInfo,lcid,ppTInfo)

#define IXMLStream_GetIDsOfNames(This,riid,rgszNames,cNames,lcid,rgDispId)	\
    (This)->lpVtbl -> GetIDsOfNames(This,riid,rgszNames,cNames,lcid,rgDispId)

#define IXMLStream_Invoke(This,dispIdMember,riid,lcid,wFlags,pDispParams,pVarResult,pExcepInfo,puArgErr)	\
    (This)->lpVtbl -> Invoke(This,dispIdMember,riid,lcid,wFlags,pDispParams,pVarResult,pExcepInfo,puArgErr)


#define IXMLStream_Open(This,url,outEncoding)	\
    (This)->lpVtbl -> Open(This,url,outEncoding)

#define IXMLStream_Read(This,buf,len,al)	\
    (This)->lpVtbl -> Read(This,buf,len,al)

#define IXMLStream_SetEncoding(This,encoding,offset)	\
    (This)->lpVtbl -> SetEncoding(This,encoding,offset)

#define IXMLStream_Close(This)	\
    (This)->lpVtbl -> Close(This)

#endif /* COBJMACROS */


#endif 	/* C style interface */



/* [helpstring][id] */ HRESULT STDMETHODCALLTYPE IXMLStream_Open_Proxy( 
    IXMLStream __RPC_FAR * This,
    /* [in] */ BSTR url,
    /* [retval][out] */ int __RPC_FAR *outEncoding);


void __RPC_STUB IXMLStream_Open_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);


/* [helpstring][id] */ HRESULT STDMETHODCALLTYPE IXMLStream_Read_Proxy( 
    IXMLStream __RPC_FAR * This,
    /* [out] */ int __RPC_FAR *buf,
    /* [in] */ int len,
    /* [retval][out] */ int __RPC_FAR *al);


void __RPC_STUB IXMLStream_Read_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);


/* [helpstring][id] */ HRESULT STDMETHODCALLTYPE IXMLStream_SetEncoding_Proxy( 
    IXMLStream __RPC_FAR * This,
    /* [in] */ int encoding,
    /* [in] */ int offset);


void __RPC_STUB IXMLStream_SetEncoding_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);


/* [helpstring][id] */ HRESULT STDMETHODCALLTYPE IXMLStream_Close_Proxy( 
    IXMLStream __RPC_FAR * This);


void __RPC_STUB IXMLStream_Close_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);



#endif 	/* __IXMLStream_INTERFACE_DEFINED__ */



#ifndef __XMLURLSTREAMLib_LIBRARY_DEFINED__
#define __XMLURLSTREAMLib_LIBRARY_DEFINED__

/****************************************
 * Generated header for library: XMLURLSTREAMLib
 * at Wed Oct 22 20:07:54 1997
 * using MIDL 3.01.75
 ****************************************/
/* [helpstring][version][uuid] */ 



EXTERN_C const IID LIBID_XMLURLSTREAMLib;

#ifdef __cplusplus
EXTERN_C const CLSID CLSID_XMLStream;

class DECLSPEC_UUID("0C97E34E-412B-11D1-A2CB-00C04FD73533")
XMLStream;
#endif
#endif /* __XMLURLSTREAMLib_LIBRARY_DEFINED__ */

/* Additional Prototypes for ALL interfaces */

unsigned long             __RPC_USER  BSTR_UserSize(     unsigned long __RPC_FAR *, unsigned long            , BSTR __RPC_FAR * ); 
unsigned char __RPC_FAR * __RPC_USER  BSTR_UserMarshal(  unsigned long __RPC_FAR *, unsigned char __RPC_FAR *, BSTR __RPC_FAR * ); 
unsigned char __RPC_FAR * __RPC_USER  BSTR_UserUnmarshal(unsigned long __RPC_FAR *, unsigned char __RPC_FAR *, BSTR __RPC_FAR * ); 
void                      __RPC_USER  BSTR_UserFree(     unsigned long __RPC_FAR *, BSTR __RPC_FAR * ); 

/* end of Additional Prototypes */

#ifdef __cplusplus
}
#endif

#endif
