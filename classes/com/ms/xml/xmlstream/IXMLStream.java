//
// Auto-generated using JActiveX.EXE 4.79.2228
//   (jactivex /javatlb /d . /p com.ms.xml /r xmlurlstream.tlb)
//
// WARNING: Do not remove the comments that include "@com" directives.
// This source file must be compiled by a @com-aware compiler.
// If you are using the Microsoft Visual J++ compiler, you must use
// version 1.02.3920 or later. Previous versions will not issue an error
// but will not generate COM-enabled class files.
//

package com.ms.xml.xmlstream;

import com.ms.com.*;
import com.ms.com.IUnknown;
import com.ms.com.Variant;

// Dual interface IXMLStream
/** @com.interface(iid=C8AB904D-4132-11D1-A2CC-00C04FD73533, thread=AUTO, type=DUAL) */
public interface IXMLStream extends IUnknown
{
  /** @com.method(vtoffset=4, dispid=1, type=METHOD, name="Open")
      @com.parameters([in,type=STRING] url, [type=I4] return) */
  public int Open(String url);

  /** @com.method(vtoffset=5, dispid=2, type=METHOD, name="Read")
      @com.parameters([out,size=1,elementType=I4,type=ARRAY] buf, [in,type=I4] len, [type=I4] return) */
  public int Read(int[] buf, int len);

  /** @com.method(vtoffset=6, dispid=3, type=METHOD, name="SetEncoding")
      @com.parameters([in,type=I4] encoding, [in,type=I4] offset) */
  public void SetEncoding(int encoding, int offset);

  /** @com.method(vtoffset=7, dispid=0, type=METHOD, name="Close")
      @com.parameters() */
  public void Close();

  public static final com.ms.com._Guid iid = new com.ms.com._Guid((int)0xc8ab904d, (short)0x4132, (short)0x11d1, (byte)0xa2, (byte)0xcc, (byte)0x0, (byte)0xc0, (byte)0x4f, (byte)0xd7, (byte)0x35, (byte)0x33);
}
