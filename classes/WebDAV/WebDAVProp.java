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

/* Simple list of all DAV: properties
   listed in section 12 of .07 spec

*/


package WebDAV;


import java.util.*;
import com.ms.xml.util.*;
import WebDAV.AsGen;

public class WebDAVProp
{

        public WebDAVProp()
	{
	}


	public static final String DAV_SCHEMA	= new String("DAV:");

        public static final Name PROP_CREATIONDATE = Name.create("creationdate",AsGen.DAV_AS);
        public static final Name PROP_DISPLAYNAME = Name.create("displayname",AsGen.DAV_AS);
        public static final Name PROP_GETCONTENTLANGUAGE = Name.create("getcontentlanguage",AsGen.DAV_AS);
        public static final Name PROP_GETCONTENTLENGTH = Name.create("getcontentlength",AsGen.DAV_AS);
        public static final Name PROP_GETCONTENTTYPE = Name.create("getcontenttype", AsGen.DAV_AS);
        public static final Name PROP_GETETAG = Name.create("getetag",AsGen.DAV_AS);
        public static final Name PROP_GETLASTMODIFIED = Name.create("getlastmodified",AsGen.DAV_AS);
        public static final Name PROP_LOCKDISCOVERY = Name.create("lockdiscovery",AsGen.DAV_AS);
        public static final Name PROP_RESOURCETYPE = Name.create("resourcetype", AsGen.DAV_AS);
        public static final Name PROP_SOURCE = Name.create("source", AsGen.DAV_AS);
        public static final Name PROP_SUPPORTEDLOCK = Name.create("supportedlock", AsGen.DAV_AS);

	public static Enumeration getDavProps() {
	  Vector prop_list = new Vector();

	  prop_list.addElement(PROP_CREATIONDATE);
	  prop_list.addElement(PROP_DISPLAYNAME);
	  prop_list.addElement(PROP_GETCONTENTLANGUAGE);
	  prop_list.addElement(PROP_GETCONTENTLENGTH);
	  prop_list.addElement(PROP_GETCONTENTTYPE);
	  prop_list.addElement(PROP_GETETAG);
	  prop_list.addElement(PROP_GETLASTMODIFIED);
	  prop_list.addElement(PROP_LOCKDISCOVERY);
	  prop_list.addElement(PROP_RESOURCETYPE);
	  prop_list.addElement(PROP_SOURCE);
	  prop_list.addElement(PROP_SUPPORTEDLOCK);

	  return (prop_list.elements());
	}
}
