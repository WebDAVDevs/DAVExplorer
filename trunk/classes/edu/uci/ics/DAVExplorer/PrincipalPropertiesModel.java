/*
 * Copyright (C) 2005 Regents of the University of California.
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

import java.util.Enumeration;
import java.util.Vector;

import com.ms.xml.om.Element;
import com.ms.xml.om.TreeEnumeration;
import com.ms.xml.util.Name;


/**
 * Title:       Principal properties model
 * Description: Datamodel for showing principals
 * Copyright:   Copyright (c) 2005 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         14 Feb 2005
 */
public class PrincipalPropertiesModel extends PropModel
{
    /**
     * @param properties
     */
    public PrincipalPropertiesModel( Element properties )
    {
        super( properties );
    }


    /**
     * @param properties
     */
    public PrincipalPropertiesModel( Vector properties )
    {
        super();
        parseProperties( properties, root );
    }


    /**
     * 
     * @param properties
     * @param currentNode
     */
    protected void parseProperties( Vector properties, PropNode currentNode )
    {
        if( properties != null )
        {
            for( int i = 0; i < properties.size(); i++ )
                parseProperties( (Element)properties.get(i), currentNode );
        }
    }


    /**
     * 
     * @param properties
     * @param currentNode
     */
    protected void parseProperties( Element properties, PropNode currentNode )
    {
        if( GlobalData.getGlobalData().getDebugResponse() )
        {
            System.err.println( "PrincipalPropertiesModel::parseProperties" );
        }

        if( properties != null )
        {
            TreeEnumeration enumTree =  new TreeEnumeration( properties );
            Element current = (Element)enumTree.nextElement();
            Name currentTag = current.getTagName();
            if( currentTag != null )
            {
                try
                {
                    // create a tree of all property tags
                    Enumeration propValEnum = current.getElements();
                    while (propValEnum.hasMoreElements())
                    {
                        Element propValEl = (Element) propValEnum.nextElement();
                        Name tagname = propValEl.getTagName();
                        if (propValEl.getType() != Element.ELEMENT)
                            continue;
                        String ns = WebDAVProp.locateNamespace( propValEl, tagname );
                        Element token = getChildElement(propValEl);
                        // ignore <status> tag
                        if( tagname.getName().equals( WebDAVXML.ELEM_STATUS ) )
                            continue;
                        if( (token != null) && (token.getType() == Element.PCDATA || token.getType() == Element.CDATA) )
                        {
                            PropNode node = new PropNode( tagname.getName(), ns, getValue(propValEl) );
                            // add to tree
                            currentNode.addChild( node );
                            node.setParent( currentNode );
                            // make <href> tage the parent of the related properties
                            if( tagname.getName().equals( WebDAVXML.ELEM_HREF ) )
                                currentNode = node;
                        }
                        else
                        {
                            PropNode node;
                            if( tagname.getName().equals( WebDAVXML.ELEM_PROPSTAT ) )
                            {
                                // ignore <propstat> element
                                node = currentNode;
                            }
                            else
                            {
                                node = new PropNode( tagname.getName(), ns, null );
                                // add to tree
                                currentNode.addChild( node );
                                node.setParent( currentNode );
                            }
                            parseProperties( propValEl, node );     // add child nodes
                        }
                    }
                }
                catch( Exception e )
                {
                    GlobalData.getGlobalData().errorMsg("DAV Interpreter:\n\nError encountered \nwhile parsing PROPFIND Response.\n" + e);
                }
            }
        }
    }
}
