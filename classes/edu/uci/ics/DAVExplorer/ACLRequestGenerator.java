/*
 * Copyright (C) 2004-2005 Regents of the University of California.
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

import java.io.ByteArrayOutputStream;
import java.util.Vector;

import HTTPClient.NVPair;

import com.ms.xml.om.Document;
import com.ms.xml.om.Element;
import com.ms.xml.util.XMLOutputStream;


/**
 * Title:       
 * Description: 
 * Copyright:   Copyright (c) 2004-2005 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        
 */
public class ACLRequestGenerator extends DeltaVRequestGenerator
{
    /**
     * 
     */
    public ACLRequestGenerator()
    {
        super();
    }


    public synchronized void GetOwner()
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "ACLRequestGenerator::GetOwner" );
        }

        extendedCode = WebDAVResponseEvent.ACL_OWNER;
        String[] props = new String[1];
        props[0] = "owner";
        if( GeneratePropFind( null, "prop", "one", props, null, false ) )
        {
            execute();
        }
    }


    public synchronized void SetOwner( Element owner, String resource )
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "ACLRequestGenerator::SetOwner" );
        }

        GeneratePropPatch( resource, owner, null, null );
    }


    public synchronized void GetGroup()
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "ACLRequestGenerator::GetGroup" );
        }

        extendedCode = WebDAVResponseEvent.ACL_GROUP;
        String[] props = new String[1];
        props[0] = "group";
        if( GeneratePropFind( null, "prop", "one", props, null, false ) )
        {
            execute();
        }
    }


    public synchronized void SetGroup( Element group, String resource )
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "ACLRequestGenerator::SetGroup" );
        }

        GeneratePropPatch( resource, group, null, null );
    }


    public synchronized void GetSupportedPrivileges()
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "ACLRequestGenerator::GetSupportedPrivileges" );
        }

        extendedCode = WebDAVResponseEvent.ACL_SUPPORTED_PRIVILEGES;
        String[] props = new String[1];
        props[0] = "supported-privilege-set";
        if( GeneratePropFind( null, "prop", "one", props, null, false ) )
        {
            execute();
        }
    }


    public synchronized void GetUserPrivileges()
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "ACLRequestGenerator::GetUserPrivileges" );
        }

        extendedCode = WebDAVResponseEvent.ACL_USER_PRIVILEGES;
        String[] props = new String[1];
        props[0] = "current-user-privilege-set";
        if( GeneratePropFind( null, "prop", "one", props, null, false ) )
        {
            execute();
        }
    }


    public synchronized void GetACL()
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "ACLRequestGenerator::GetACL" );
        }

        extendedCode = WebDAVResponseEvent.ACL;
        String[] props = new String[1];
        props[0] = "acl";
        if( GeneratePropFind( null, "prop", "one", props, null, false ) )
        {
            execute();
        }
    }


    public synchronized void GetACLRestrictions()
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "ACLRequestGenerator::GetACLRestrictions" );
        }

        extendedCode = WebDAVResponseEvent.SUPPORTED_ACL;
        String[] props = new String[1];
        props[0] = "acl-restrictions";
        if( GeneratePropFind( null, "prop", "one", props, null, false ) )
        {
            execute();
        }
    }

    
    public synchronized void GetInheritedACLs()
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "ACLRequestGenerator::GetInheritedACLs" );
        }

        extendedCode = WebDAVResponseEvent.INHERITED_ACL;
        String[] props = new String[1];
        props[0] = "inherited-acl-set";
        if( GeneratePropFind( null, "prop", "one", props, null, false ) )
        {
            execute();
        }
    }

    
    public synchronized void GetPrincipalCollections()
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "ACLRequestGenerator::GetPrincipalCollections" );
        }

        extendedCode = WebDAVResponseEvent.ACL_PRINCIPALS;
        String[] props = new String[1];
        props[0] = "principal-collection-set";
        if( GeneratePropFind( null, "prop", "one", props, null, false ) )
        {
            execute();
        }
    }


    public synchronized boolean GenerateACL( ACLPrincipal[] principals )
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "ACLRequestGenerator::GenerateACL" );
        }

        Headers = null;
        Body = null;
        StrippedResource = parseResourceName( true );
        boolean ok = (StrippedResource != null);

        if (!ok)
        {
                return false;
        }

        extendedCode = WebDAVResponseEvent.ACL;
        Method = "ACL";
        Document miniDoc = new Document();
        miniDoc.setVersion("1.0");
        miniDoc.addChild(WebDAVXML.elemNewline,null);

        AsGen asgen = WebDAVXML.findNamespace( new AsGen(), null );
        if( asgen == null )
            asgen = WebDAVXML.createNamespace( new AsGen(), null );

        Element topElem = WebDAVXML.createElement( ACLXML.ELEM_ACL, Element.ELEMENT, null, asgen );
        topElem.addChild( WebDAVXML.elemNewline, null );
        for( int p = 0; p < principals.length; p++ )
        {
            Element ace = WebDAVXML.createElement( ACLXML.ELEM_ACE, Element.ELEMENT, topElem, asgen );
            // set principal
            Element princ = WebDAVXML.createElement( ACLXML.ELEM_PRINCIPAL, Element.ELEMENT, ace, asgen );
            Element princHref = WebDAVXML.createElement( WebDAVXML.ELEM_HREF, Element.ELEMENT, princ, asgen );
            Element princVal = WebDAVXML.createElement( null, Element.PCDATA, princ, asgen );
            princVal.setText( principals[p].getPrincipal() );
            // keep on same line without whitespace
            addChild( princHref, princVal, 0, 0, false, false );
            addChild( princ, princHref, 3, 0, true, true );
            addChild( ace, princ, 2, 0, true, true );

            Vector grant = principals[p].getGrant();
            Vector deny = principals[p].getDeny();
            // set grant privileges
            if( grant.size() > 0 )
            {
                Element grantEl = WebDAVXML.createElement( ACLXML.ELEM_GRANT, Element.ELEMENT, ace, asgen );
                for( int g = 0; g < grant.size(); g++ )
                {
                    Element priv = WebDAVXML.createElement( ACLXML.ELEM_PRIVILEGE, Element.ELEMENT, grantEl, asgen );
                    Element privVal = WebDAVXML.createElement( (String)grant.get(g), Element.ELEMENT, priv, asgen );
                    // keep on same line without whitespace
                    addChild( priv, privVal, 0, 0, false, false );
                    addChild( grantEl, priv, 3, 0, true, true );
                }
                addChild( ace, grantEl, 2, 0, true, true );
            }
            else if( deny.size() > 0 )
            {
                // set deny privileges
                // can't have both grant and deny in the same ace element (see
                // pp 45-46 in RFC 3744)
                Element denyEl = WebDAVXML.createElement( ACLXML.ELEM_DENY, Element.ELEMENT, ace, asgen );
                for( int d = 0; d < grant.size(); d++ )
                {
                    Element priv = WebDAVXML.createElement( ACLXML.ELEM_PRIVILEGE, Element.ELEMENT, denyEl, asgen );
                    Element privVal = WebDAVXML.createElement( (String)deny.get(d), Element.ELEMENT, priv, asgen );
                    // keep on same line without whitespace
                    addChild( priv, privVal, 0, 0, false, false );
                    addChild( denyEl, priv, 3, 0, true, true );
                }
                addChild( ace, denyEl, 2, 0, true, true );
            }
            addChild( topElem, ace, 1, 0, false, true );
        }
        miniDoc.addChild( topElem, null );
        
        ByteArrayOutputStream byte_str = new ByteArrayOutputStream();
        XMLOutputStream xml_out = new XMLOutputStream(byte_str);
        try
        {
            miniDoc.save(xml_out);
            Body = byte_str.toByteArray();

            Headers = new NVPair[1];
            if (Port == 0 || Port == DEFAULT_PORT)
            {
                Headers[0] = new NVPair("Host", HostName);
            }
            else
                Headers[0] = new NVPair("Host", HostName + ":" + Port);
        }
        catch (Exception e)
        {
            GlobalData.getGlobalData().errorMsg("XML generation error: \n" + e);
            return false;
        }

        return true;
    }
    

    
/*    protected String prepareResource( String resource, boolean fullPath )
    {
        String str = resource;
        if( !fullPath )
        {
            str = HostName;
            if (Port > 0)
                str += ":" + Port;
            str += resource;
        }
        return str;
    }*/
}
