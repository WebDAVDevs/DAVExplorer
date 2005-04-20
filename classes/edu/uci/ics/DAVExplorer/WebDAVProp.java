/*
 * Copyright (c) 1998-2005 Regents of the University of California.
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


package edu.uci.ics.DAVExplorer;

import java.util.Vector;
import java.util.Enumeration;

import com.ms.xml.om.Element;
import com.ms.xml.util.Atom;
import com.ms.xml.util.Name;


/**
 * Title:       WebDAV Properties
 * Description: Simple list of all DAV: properties
 *              listed in section 12 of .07 spec
 * Copyright:   Copyright (c) 1998-2005 Regents of the University of California. All rights reserved.
 * @author      Undergraduate project team ICS 126B 1998
 * date         1998
 * @author      Yuzo Kanomata, Joachim Feise (dav-exp@ics.uci.edu)
 * date         17 March 1999
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         1 October 2001
 * Changes:     Change of package name
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         8 February 2004
 * Changes:     Added Javadoc templates
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         10 February 2005
 * Changes:     Minor cleanup, javadocs
 */
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


    /**
     * Constructor
     */
    public WebDAVProp()
    {
    }


    /**
     * Constructor
     * 
     * @param tag
     *      the tag name
     * @param value
     *      the value (for tags like <tag>value</tag>
     * @param schema
     *      the namespace
     */
    public WebDAVProp( String tag, String value, String schema )
    {
        this.tag = tag;
        this.value = value;
        this.schema = schema;
        this.children = null;
        this.leaf = true;
    }


    /**
     * Constructor
     * 
     * @param tag
     *      the tag name
     * @param schema
     *      the namespace
     * @param children
     *      child tags, e.g., <parent><child1/><child2>value</child2></parent>
     */
    public WebDAVProp( String tag, String schema, WebDAVProp[] children )
    {
        this.tag = tag;
        this.value = null;
        this.schema = schema;
        this.children = children;
        this.leaf = false;
    }


    /**
     * Get an enumeration of all WebDAV properties.
     *  
     * @return
     *      an emumeration of all WebDAV properties
     */
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


    /**
     * Find a namespace declaration within the XML tree by walking
     * the tree towards the root.
     *  
     * @param parent
     *      the tree element to start walking 
     * @param tagname
     *      the desired namespace
     * @return
     *      a string containing the namespace 
     * 
     * Namespace Handling
     * Unfortunately, the 1997-era Microsoft parser does not properly
     * handle namespaces. It should really be replaced with a modern
     * DOM parser.
     * Until then, we are stuck with code like this to get the
     * actual namespace by walking up the tree.
     */
    public static String locateNamespace( Element parent, Name tagname )
    {
        String ns = null;
        Atom namespace = tagname.getNameSpace();
        if( namespace != null )
            ns = tagname.getNameSpace().toString();
        Name name = null;
        Element localParent = parent;
        if( ns != null )
            name = Name.create( "xmlns", ns );
        else
            name = Name.create( "xmlns" );
        while( localParent != null )
        {
            String attr = (String)localParent.getAttribute(name);
            if( attr == null )
                localParent = localParent.getParent();
            else
            {
                ns = attr;
                break;
            }
        }
        return ns;
    }


    /**
     * Get the tag name.
     *  
     * @return
     *      the tag name
     */
    public String getTag()
    {
        return tag;
    }


    /**
     * Get the value.
     * 
     * @return
     *  the value string
     */
    public String getValue()
    {
        return value;
    }


    /**
     * Get the namespace.
     * 
     * @return
     *      the namespace string
     */
    public String getSchema()
    {
        return schema;
    }


    /**
     * Check if this tag has children.
     * 
     * @return
     *      true if this tag is a leaf node, false else
     */
    public boolean isLeaf()
    {
        return leaf;
    }


    /**
     * Get all children of this tag.
     * 
     * @return
     *      an array of tags
     */
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
