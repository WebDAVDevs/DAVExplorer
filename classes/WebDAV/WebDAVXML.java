/*
 * Copyright (c) 1999 Regents of the University of California.
 * All rights reserved.
 *
 * This software was developed at the University of California, Irvine.
 *
 * Redistribution and use in source and binary forms are permitted
 * provided that the above copyright notice and this paragraph are
 * duplicated in all such forms and that any documentation,
 * advertising materials, and other materials related to such
 * distribution and use acknowledge that the software was developed
 * by the University of California, Irvine.  The name of the
 * University may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 */

/*
Copyright 1997-1998 by Marc Eaddy, Jonathan Shapiro, Shao Rong;
ALL RIGHTS RESERVED
 
Permission to use, copy, modify, and distribute this software and its
documentation for research and educational purpose and without fee is
hereby granted, provided that the above copyright notice appear in all
copies and that both that the copyright notice and warranty disclaimer
appear in supporting documentation, and that the names of the
copyright holders or any of their entities not be used in advertising
or publicity pertaining to distribution of the software without
specific, written prior permission.  Use of this software in whole or
in parts for direct commercial advantage requires explicit prior
permission.
 
The copyright holders disclaim all warranties with regard to this
software, including all implied warranties of merchantability and
fitness.  In no event shall the copyright holders be liable for any
special, indirect or consequential damages or any damages whatsoever
resulting from loss of use, data or profits, whether in an action of
contract, negligence or other tortuous action, arising out of or in
connection with the use or performance of this software.
*/


/*	Modified by Robert Emmery to reflect the changes in .07 spec

			02/26/98
*/


package WebDAV;

import com.ms.xml.util.Name;
import com.ms.xml.om.Element;
import com.ms.xml.om.ElementImpl;
import java.util.Enumeration;

/**
 * Define the Generic DAV XML Elements from section 12 of the WebDAV spec
 * <draft-ietf-webdav-protocol-05>
 *
 * @author		Jonathan Shapiro
 * @version		1.0, 23 Nov 1997
 */
public class WebDAVXML
{
	public static final ElementImpl elemNewline =
			new ElementImpl(null, Element.WHITESPACE);

	public static final ElementImpl elemTab =
			new ElementImpl(null, Element.WHITESPACE);
        public static final ElementImpl elemDSpace =
			new ElementImpl(null, Element.WHITESPACE);

	static
	{
		elemNewline.setText("\n");
		elemTab.setText("\t");
                elemDSpace.setText("          ");
	}

	public WebDAVXML()
	{
	}
	public static void addNamespace(Element doc, String Schema, String As ) {
	  
	  Element ns = new ElementImpl(Name.create("namespace","xml"), Element.NAMESPACE);
          ns.setAttribute(Name.create("ns","xml"),Schema);
          ns.setAttribute(Name.create("prefix","xml"),As);
	  doc.addChild(ns, null);
	  doc.addChild(elemNewline, null);
 

	}
	//11.1
	public static final Name ELEM_ACTIVE_LOCK = Name.create("activelock", AsGen.DAV_AS);

	//11.1.1
	public static final Name ELEM_DEPTH = Name.create("depth", AsGen.DAV_AS);

	//11.1.2
	public static final Name ELEM_LOCK_TOKEN = Name.create("locktoken", AsGen.DAV_AS);  
        public static final Name ELEM_LOCK_DEPTH = Name.create("depth", AsGen.DAV_AS);

        //11.1.3
	public static final Name ELEM_TIMEOUT = Name.create("timeout", AsGen.DAV_AS);

	//11.2
	public static final Name ELEM_COLLECTION = Name.create("collection", AsGen.DAV_AS);

	//11.3
	public static final Name ELEM_HREF = Name.create("href", AsGen.DAV_AS);

	//11.4
	public static final Name ELEM_LINK = Name.create("link", AsGen.DAV_AS);

	//11.4.1
	public static final Name ELEM_DST = Name.create("dst", AsGen.DAV_AS);

	//11.4.2
	public static final Name ELEM_SRC = Name.create("src", AsGen.DAV_AS);

	//11.5
	public static final Name ELEM_LOCK_ENTRY = Name.create("lockentry", AsGen.DAV_AS);

	//11.6
	public static final Name ELEM_LOCK_INFO = Name.create("lockinfo", AsGen.DAV_AS);

	//11.7
        public static final Name ELEM_LOCK_SCOPE = Name.create("lockscope", AsGen.DAV_AS);

	//11.7.1
	public static final Name ELEM_EXCLUSIVE = Name.create("exclusive", AsGen.DAV_AS);

	//11.7.2
	public static final Name ELEM_SHARED = Name.create("shared", AsGen.DAV_AS);

 	//11.8 
	public static final Name ELEM_LOCK_TYPE = Name.create("locktype", AsGen.DAV_AS);

	//11.8.1
	public static final Name ELEM_WRITE = Name.create("write", AsGen.DAV_AS);

	//11.9
	public static final Name ELEM_MULTISTATUS = Name.create("multistatus", AsGen.DAV_AS);

	//11.9.1
	public static final Name ELEM_RESPONSE = Name.create("response", AsGen.DAV_AS);

	//11.9.1.1
	public static final Name ELEM_PROPSTAT = Name.create("propstat", AsGen.DAV_AS);

	//11.9.1.2
	public static final Name ELEM_STATUS = Name.create("status", AsGen.DAV_AS);

	//11.9.2
	public static final Name ELEM_RESPONSE_DESCRIPTION = Name.create("responsedescription", AsGen.DAV_AS);

	//11.10
	public static final Name ELEM_OWNER = Name.create("owner", AsGen.DAV_AS);

	//11.11
	public static final Name ELEM_PROP = Name.create("prop", AsGen.DAV_AS);

	//11.12
	public static final Name ELEM_PROPERTY_BEHAVIOR = Name.create("propertybehavior", AsGen.DAV_AS);

	//11.12.1
	public static final Name ELEM_KEEP_ALIVE = Name.create("keepalive", AsGen.DAV_AS);

	//11.12.2
	public static final Name ELEM_OMIT = Name.create("omit", AsGen.DAV_AS);

	//11.13
	public static final Name ELEM_PROPERTY_UPDATE = Name.create("propertyupdate", AsGen.DAV_AS);

	//11.13.1
	public static final Name ELEM_REMOVE = Name.create("remove", AsGen.DAV_AS);

	//11.13.2
	public static final Name ELEM_SET = Name.create("set", AsGen.DAV_AS);

	//11.14
	public static final Name ELEM_PROPFIND = Name.create("propfind", AsGen.DAV_AS);


	//11.14.1
	public static final Name ELEM_ALLPROP = Name.create("allprop", AsGen.DAV_AS);

	//11.14.2
	public static final Name ELEM_PROPNAME = Name.create("propname", AsGen.DAV_AS);


	public static Name getNonNullTagName(Element elem) {
		Name name = null;
		Enumeration enum = elem.getElements();
		while (enum.hasMoreElements()) {
			Element elemTmp = (Element)enum.nextElement();
			name = elemTmp.getTagName();
			if (name != null) break;
		}
		return name;
	}
}
