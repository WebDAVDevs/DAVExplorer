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
import com.ms.xml.util.XMLStreamReader;

/** @com.class(classid=0C97E34E-412B-11D1-A2CB-00C04FD73533,DynamicCasts)
*/
public class XMLStream
	implements IUnknown,com.ms.com.NoAutoScripting,IXMLStream,XMLStreamReader
{
  /** @com.method() */
  public native int Open(String url);

  /** @com.method() */
  public native int Read(int[] buf, int len);

  /** @com.method() */
  public native void SetEncoding(int encoding, int offset);

  /** @com.method() */
  public native void Close();

  public static final com.ms.com._Guid clsid = new com.ms.com._Guid((int)0xc97e34e, (short)0x412b, (short)0x11d1, (byte)0xa2, (byte)0xcb, (byte)0x0, (byte)0xc0, (byte)0x4f, (byte)0xd7, (byte)0x35, (byte)0x33);

	/** Implementation of XMLStreamReader interface through which XMLInputStream
	 *	accesses XMLStream indirectly to remove direct dependency on native code.
	 *
	 *	Don Park
	 */

		public int
	open (String url)
	{
		return ((IXMLStream)this).Open(url);
	}

		public int
	read (int[] buf, int len)
	{
		return ((IXMLStream)this).Read(buf, len);
	}

		public void
	setEncoding (int encoding, int offset)
	{
		((IXMLStream)this).SetEncoding(encoding, offset);
	}

		public void
	close()
	{
		((IXMLStream)this).Close();
	}
}
