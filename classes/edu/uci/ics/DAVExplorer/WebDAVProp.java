/*
 * Copyright (c) 1998-2001 Regents of the University of California.
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

/**
 * Title:       WebDAV Properties
 * Description: Simple list of all DAV: properties
 *              listed in section 12 of .07 spec
 * Copyright:   Copyright (c) 1998-2001 Regents of the University of California. All rights reserved.
 * @author      Undergraduate project team ICS 126B 1998
 * @date        1998
 * @author      Yuzo Kanomata, Joachim Feise (dav-exp@ics.uci.edu)
 * @date        17 March 1999
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        1 October 2001
 * Changes:     Change of package name
 */

package edu.uci.ics.DAVExplorer;

import java.util.Vector;
import java.util.Enumeration;

public class WebDAVProp
{
    public static final String DAV_SCHEMA = "DAV:";
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


    public WebDAVProp()
    {
    }

    public static Enumeration getDavProps()
    {
        Vector prop_list = new Vector();

        prop_list.addElement( PROP_CREATIONDATE );
        prop_list.addElement( PROP_DISPLAYNAME );
        prop_list.addElement( PROP_GETCONTENTLANGUAGE );
        prop_list.addElement( PROP_GETCONTENTLENGTH );
        prop_list.addElement( PROP_GETCONTENTTYPE );
        prop_list.addElement( PROP_GETETAG );
        prop_list.addElement( PROP_GETLASTMODIFIED );
        prop_list.addElement( PROP_LOCKDISCOVERY );
        prop_list.addElement( PROP_RESOURCETYPE );
        prop_list.addElement( PROP_SOURCE );
        prop_list.addElement( PROP_SUPPORTEDLOCK );

        return (prop_list.elements());
    }


    public WebDAVProp( String tag, String value, String schema )
    {
        this.tag = tag;
        this.value = value;
        this.schema = schema;
        this.children = null;
        this.leaf = true;
    }

    public WebDAVProp( String tag, String schema, WebDAVProp[] children )
    {
        this.tag = tag;
        this.value = null;
        this.schema = schema;
        this.children = children;
        this.leaf = false;
    }


    public String getTag()
    {
        return tag;
    }

    public String getValue()
    {
        return value;
    }

    public String getSchema()
    {
        return schema;
    }

    public boolean isLeaf()
    {
        return leaf;
    }

    public WebDAVProp[] getChildren()
    {
        return children;
    }

    protected String tag;
    protected String value;
    protected String schema;
    protected WebDAVProp[] children;
    protected boolean leaf;
}
