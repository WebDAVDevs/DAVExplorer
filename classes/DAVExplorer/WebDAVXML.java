/*
 * Copyright (c) 1999-2001 Regents of the University of California.
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
Authors: Marc Eaddy, Jonathan Shapiro, Shao Rong
*/


/*  Modified by Robert Emmery to reflect the changes in .07 spec

            02/26/98
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
//



package DAVExplorer;

import com.ms.xml.util.Name;
import com.ms.xml.om.Element;
import com.ms.xml.om.ElementImpl;
import java.util.Enumeration;

/**
 * Define the Generic DAV XML Elements from section 12 of the WebDAV spec
 * <draft-ietf-webdav-protocol-05>
 *
 * @author      Jonathan Shapiro
 * @version     1.0, 23 Nov 1997
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
        elemDSpace.setText("    ");
    }

    public WebDAVXML()
    {
    }

    public static void createNamespace( AsGen alias, String schema )
    {
        if( schema == null )
        {
            alias.createNamespace( WebDAVProp.DAV_SCHEMA );
        }
        else
        {
            alias.createNamespace( schema );
        }
    }

    public static Element createElement( String tag, int type, Element parent, AsGen alias )
    {
        Element element = null;
        if( alias.isAttributeSet() )
        {
            if( parent != null )
            {
                // check if parent has same namespace
                // if not, remove the current alias node and use the previous one
                String parentNS = parent.getTagName().getNameSpace().toString();
                while( !parentNS.equals( alias.getAlias() ) )
                {
                    alias.up();
                }
            }
            element = new ElementImpl(createName(tag, alias.getAlias()), type );
        }
        else
        {
            element = new ElementImpl(createName(tag, alias.getAlias()), type );
            if( alias.getSchema() != null )
            {
                element.setAttribute( Name.create(alias.getAlias(), "xmlns"), alias.getSchema() );
                alias.setAttribute();
            }
        }
        return element;
    }

    protected static Name createName( String tag, String alias )
    {
        if( alias == null )
            return Name.create( tag );
        else
            return Name.create( tag, alias );
    }

    //11.1
    public static final String ELEM_ACTIVE_LOCK = "activelock";

    //11.1.1
    public static final String ELEM_DEPTH = "depth";

    //11.1.2
    public static final String ELEM_LOCK_TOKEN = "locktoken";
    public static final String ELEM_LOCK_DEPTH = "depth";

    //11.1.3
    public static final String ELEM_TIMEOUT = "timeout";

    //11.2
    public static final String ELEM_COLLECTION = "collection";

    //11.3
    public static final String ELEM_HREF = "href";

    //11.4
    public static final String ELEM_LINK = "link";

    //11.4.1
    public static final String ELEM_DST = "dst";

    //11.4.2
    public static final String ELEM_SRC = "src";

    //11.5
    public static final String ELEM_LOCK_ENTRY = "lockentry";

    //11.6
    public static final String ELEM_LOCK_INFO = "lockinfo";

    //11.7
    public static final String ELEM_LOCK_SCOPE = "lockscope";

    //11.7.1
    public static final String ELEM_EXCLUSIVE = "exclusive";

    //11.7.2
    public static final String ELEM_SHARED = "shared";

    //11.8
    public static final String ELEM_LOCK_TYPE = "locktype";

    //11.8.1
    public static final String ELEM_WRITE = "write";

    //11.9
    public static final String ELEM_MULTISTATUS = "multistatus";

    //11.9.1
    public static final String ELEM_RESPONSE = "response";

    //11.9.1.1
    public static final String ELEM_PROPSTAT = "propstat";

    //11.9.1.2
    public static final String ELEM_STATUS = "status";

    //11.9.2
    public static final String ELEM_RESPONSE_DESCRIPTION = "responsedescription";

    //11.10
    public static final String ELEM_OWNER = "owner";

    //11.11
    public static final String ELEM_PROP = "prop";

    //11.12
    public static final String ELEM_PROPERTY_BEHAVIOR = "propertybehavior";

    //11.12.1
    public static final String ELEM_KEEP_ALIVE = "keepalive";

    //11.12.2
    public static final String ELEM_OMIT = "omit";

    //11.13
    public static final String ELEM_PROPERTY_UPDATE = "propertyupdate";

    //11.13.1
    public static final String ELEM_REMOVE = "remove";

    //11.13.2
    public static final String ELEM_SET = "set";

    //11.14
    public static final String ELEM_PROPFIND = "propfind";

    //11.14.1
    public static final String ELEM_ALLPROP = "allprop";

    //11.14.2
    public static final String ELEM_PROPNAME = "propname";


    public static Name getNonNullTagName(Element elem)
    {
        Name name = null;
        Enumeration enum = elem.getElements();
        while (enum.hasMoreElements())
        {
            Element elemTmp = (Element)enum.nextElement();
            name = elemTmp.getTagName();
            if (name != null) break;
        }
        return name;
    }
}
