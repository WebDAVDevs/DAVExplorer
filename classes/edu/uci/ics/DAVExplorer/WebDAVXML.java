/*
 * Copyright (c) 1997-2003 Regents of the University of California.
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
 * Title:       WebDAVURIBox Listener
 * Description: Define the Generic DAV XML Elements from section 12 of the WebDAV spec
 * <draft-ietf-webdav-protocol-05>
 * Copyright:   Copyright (c) 1997-2001 Regents of the University of California. All rights reserved.
 * @author      Marc Eaddy, Jonathan Shapiro, Shao Rong
 * @date        23 November 1997
 * @author      Robert Emmery
 * @date        26 February 1998
 * Changes:     Modified by Robert Emmery to reflect the changes in .07 spec
 * @author      Yuzo Kanomata, Joachim Feise (dav-exp@ics.uci.edu)
 * @date        17 March 1999
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        1 October 2001
 * Changes:     Change of package name
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        17 March 2003
 * Changes:     Integrated Brian Johnson's applet changes.
 *              Added better error reporting.
 */

package edu.uci.ics.DAVExplorer;

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
    public static final ElementImpl elemNewline = new ElementImpl(null, Element.WHITESPACE);
    public static final ElementImpl elemTab = new ElementImpl(null, Element.WHITESPACE);
    public static final ElementImpl elemDSpace = new ElementImpl(null, Element.WHITESPACE);

    static
    {
        elemNewline.setText("\n");
        elemTab.setText("\t");
        elemDSpace.setText("    ");
    }

    public WebDAVXML()
    {
    }

    public static AsGen createNamespace( AsGen alias, String schema )
    {
        if( schema == null )
        {
            alias.createNamespace( WebDAVProp.DAV_SCHEMA );
        }
        else
        {
            alias.createNamespace( schema );
        }
        return alias;
    }


    public static AsGen findNamespace( AsGen alias, String schema )
    {
        if( schema == null )
            schema = WebDAVProp.DAV_SCHEMA;

        alias = alias.getFirst();
        while( (alias!=null) && (alias.getSchema()!=null) )
        {
            if( alias.getSchema().equals( schema ) )
                return alias;
            alias = alias.getNext();
        }
        return null;
    }


    public static Element createElement( String tag, int type, Element parent, AsGen namespace )
    {
        return createElement( tag, type, parent, namespace, false, false );
    }

    public static Element createElement( String tag, int type, Element parent, AsGen namespace, boolean declareNamespaces )
    {
        return createElement( tag, type, parent, namespace, declareNamespaces, false );
    }

    public static Element createElement( String tag, int type, Element parent, AsGen namespace, boolean declareNamespaces, boolean neverdeclare )
    {
        Element element = new ElementImpl(createName(tag, namespace.getAlias()), type );

        if( declareNamespaces )
        {
            // declare all our namespaces
            AsGen currentNS = namespace.getFirst();
            while( currentNS != null )
            {
                setNSAttribute( element, currentNS );
                currentNS = currentNS.getNext();
            }
        }

        // make sure we have the namespace declared
        if( !namespace.isAttributeSet() && !neverdeclare )
        {
            setNSAttribute( element, namespace );
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

    private static void setNSAttribute( Element element, AsGen currentNS )
    {
        if( currentNS.getSchema() != null && element != null )
        {
            element.setAttribute( Name.create( currentNS.getAlias(), "xmlns"), currentNS.getSchema() );
            currentNS.setAttribute();
        }
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
