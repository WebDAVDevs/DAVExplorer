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
// This code was originally written by an undergraduate project
// team at UCI.
//
////////////////////////////////////////////////////////////////
// The code has been modified to include povisions for the final
// WebDAV xml namespaces.  A small number of program errors have
// been corrected.
//
// Please use the following contact:
//
// dav-exp@ics.uci.edu
//
// Version: 0.4
// Changes by: Yuzo Kanomata and Joe Feise
// Date: 3/17/99
//
// Change List:



package DAVExplorer;


import java.util.*;
import com.ms.xml.util.*;

public class WebDAVProp
{

        public WebDAVProp()
	{
	}


	public static final String DAV_SCHEMA	= new String("DAV:");
        public static final String PROP_CREATIONDATE = "creationdate";
        public static final String PROP_DISPLAYNAME = "displayname";
        public static final String PROP_GETCONTENTLANGUAGE = "getcontentlanguage";
        public static final String PROP_GETCONTENTLENGTH = "getcontentlength";
        public static final String PROP_GETCONTENTTYPE = "getcontenttype";
        public static final String PROP_GETETAG = "getetag";
        public static final String PROP_GETLASTMODIFIED = "getlastmodified";
        public static final String PROP_LOCKDISCOVERY = "lockdiscovery";
        public static final String PROP_RESOURCETYPE = "resourcetype";
        public static final String PROP_SOURCE = "source";
        public static final String PROP_SUPPORTEDLOCK = "supportedlock";

	public static Enumeration getDavProps() {
	  Vector prop_list = new Vector();

	  prop_list.addElement( Name.create( PROP_CREATIONDATE ) );
	  prop_list.addElement( Name.create( PROP_DISPLAYNAME ) );
	  prop_list.addElement( Name.create( PROP_GETCONTENTLANGUAGE ) );
	  prop_list.addElement( Name.create( PROP_GETCONTENTLENGTH ) );
	  prop_list.addElement( Name.create( PROP_GETCONTENTTYPE ) );
	  prop_list.addElement( Name.create( PROP_GETETAG ) );
	  prop_list.addElement( Name.create( PROP_GETLASTMODIFIED ) );
	  prop_list.addElement( Name.create( PROP_LOCKDISCOVERY ) );
	  prop_list.addElement( Name.create( PROP_RESOURCETYPE ) );
	  prop_list.addElement( Name.create( PROP_SOURCE ) );
	  prop_list.addElement( Name.create( PROP_SUPPORTEDLOCK ) );

	  return (prop_list.elements());
	}
}
