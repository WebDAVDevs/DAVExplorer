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
 * Title:       ACL Request generator
 * Description: Constructs ACL requests to send to the server.
 * Copyright:   Copyright (c) 2004-2005 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         15 Feb 2005
 */
public class ACLRequestGenerator extends DeltaVRequestGenerator
{
    /**
     * Constructor
     */
    public ACLRequestGenerator()
    {
        super();
    }


    /**
     * 
     *
     */
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


    /**
     * 
     * @param owner
     * @param resource
     */
    public synchronized void SetOwner( Element owner, String resource )
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "ACLRequestGenerator::SetOwner" );
        }

        GeneratePropPatch( resource, owner, null, null );
    }


    /**
     * 
     *
     */
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


    /**
     * 
     * @param group
     * @param resource
     */
    public synchronized void SetGroup( Element group, String resource )
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "ACLRequestGenerator::SetGroup" );
        }

        GeneratePropPatch( resource, group, null, null );
    }


    /**
     * 
     *
     */
    public synchronized void GetSupportedPrivilegeSet()
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "ACLRequestGenerator::GetSupportedPrivileges" );
        }

        extendedCode = WebDAVResponseEvent.ACL_SUPPORTED_PRIVILEGE_SET;
        String[] props = new String[1];
        props[0] = "supported-privilege-set";
        if( GeneratePropFind( null, "prop", "one", props, null, false ) )
        {
            execute();
        }
    }


    /**
     * 
     *
     */
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


    /**
     * 
     *
     */
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


    /**
     * 
     *
     */
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


    /**
     * 
     *
     */
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


    /**
     * 
     *
     */
    public synchronized void GetPrincipalCollections()
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "ACLRequestGenerator::GetPrincipalCollections" );
        }

        extendedCode = WebDAVResponseEvent.ACL_PRINCIPAL_COLLECTION_SET;
        String[] props = new String[1];
        props[0] = "principal-collection-set";
        if( GeneratePropFind( null, "prop", "one", props, null, false ) )
        {
            execute();
        }
    }


    /**
     * 
     *
     */
    public synchronized void GetPrincipalNames()
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "ACLRequestGenerator::GetPrincipalNames" );
        }

        extendedCode = WebDAVResponseEvent.ACL_PRINCIPAL_NAMES;
        String[] props = new String[2];
        props[0] = "displayname";
        props[1] = "resourcetype";
        if( GeneratePropFind( null, "prop", "one", props, null, false ) )
        {
            execute();
        }
    }


    /**
     * 
     *
     */
    public synchronized void GetPropertyNames()
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "ACLRequestGenerator::GetPropertyNames" );
        }

        extendedCode = WebDAVResponseEvent.ACL_PROPERTY_NAMES;
        if( GeneratePropFind( null, "propname", "0", null, null, false ) )
        {
            execute();
        }
    }


    /**
     * 
     * @param props
     * @return
     */
    public synchronized boolean GetPrincipalPropSetReport( Vector props )
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "ACLRequestGenerator::GetPrincipalPropSetReport" );
        }

        Headers = null;
        Body = null;
        StrippedResource = parseResourceName( true );
        if( StrippedResource == null )
            return false;

        extendedCode = WebDAVResponseEvent.ACL_PRINCIPAL_PROP_SET;
        Method = "REPORT";
        Document miniDoc = new Document();
        miniDoc.setVersion("1.0");
        miniDoc.addChild(WebDAVXML.elemNewline,null);

        AsGen asgen = WebDAVXML.findNamespace( new AsGen(), null );
        if( asgen == null )
            asgen = WebDAVXML.createNamespace( new AsGen(), null );

        Element topElem = WebDAVXML.createElement( ACLXML.ELEM_ACL_PRINCIPAL_PROP_SET, Element.ELEMENT, null, asgen );
        topElem.addChild( WebDAVXML.elemNewline, null );
        addProperties( topElem, asgen, props, 1 );
        miniDoc.addChild( topElem, null );
        
        ByteArrayOutputStream byte_str = new ByteArrayOutputStream();
        XMLOutputStream xml_out = new XMLOutputStream(byte_str);
        try
        {
            miniDoc.save( xml_out );
            Body = byte_str.toByteArray();

            Headers = new NVPair[2];
            if (Port == 0 || Port == DEFAULT_PORT)
            {
                Headers[0] = new NVPair( "Host", HostName );
            }
            else
                Headers[0] = new NVPair( "Host", HostName + ":" + Port );
            Headers[1] = new NVPair( "Depth", "0" );
        }
        catch (Exception e)
        {
            GlobalData.getGlobalData().errorMsg( "XML generation error: \n" + e );
            return false;
        }

        return true;
    }


    /**
     * 
     * @param criteria
     * @param props
     * @return
     */
    public synchronized boolean GetPrincipalMatchReport( Vector criteria, Vector props, boolean self )
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "ACLRequestGenerator::GetPrincipalMatchReport" );
        }

        Headers = null;
        Body = null;
        StrippedResource = parseResourceName( true );
        if( StrippedResource == null )
            return false;

        extendedCode = WebDAVResponseEvent.PRINCIPAL_MATCH;
        Method = "REPORT";
        Document miniDoc = new Document();
        miniDoc.setVersion("1.0");
        miniDoc.addChild(WebDAVXML.elemNewline,null);

        AsGen asgen = WebDAVXML.findNamespace( new AsGen(), null );
        if( asgen == null )
            asgen = WebDAVXML.createNamespace( new AsGen(), null );

        Element topElem = WebDAVXML.createElement( ACLXML.ELEM_PRINCIPAL_MATCH, Element.ELEMENT, null, asgen );
        topElem.addChild( WebDAVXML.elemNewline, null );
        Element propElem;
        if( self )
            propElem = WebDAVXML.createElement( ACLXML.ELEM_SELF, Element.ELEMENT, topElem, asgen );
        else
        {
            propElem = WebDAVXML.createElement( ACLXML.ELEM_PRINCIPAL_PROPERTY, Element.ELEMENT, topElem, asgen );
            propElem.addChild( WebDAVXML.elemNewline, null );
            for( int i=0; i<criteria.size(); i++ )
            {
                ACLPropertySearchNode node = (ACLPropertySearchNode)criteria.get( i );
                Vector searchProps = node.getProperties();
                addProperties( propElem, asgen, searchProps, 1, false );
            }
        }
        addChild( topElem, propElem, 1, false );
        if( props != null && props.size() > 0 )
            addProperties( topElem, asgen, props, 1 );
        miniDoc.addChild( topElem, null );

        ByteArrayOutputStream byte_str = new ByteArrayOutputStream();
        XMLOutputStream xml_out = new XMLOutputStream(byte_str);
        try
        {
            miniDoc.save(xml_out);
            Body = byte_str.toByteArray();

            Headers = new NVPair[2];
            if( Port == 0 || Port == DEFAULT_PORT )
            {
                Headers[0] = new NVPair( "Host", HostName );
            }
            else
                Headers[0] = new NVPair( "Host", HostName + ":" + Port );
            Headers[1] = new NVPair( "Depth", "0" );
        }
        catch (Exception e)
        {
            GlobalData.getGlobalData().errorMsg( "XML generation error: \n" + e );
            return false;
        }

        return true;
    }


    /**
     * 
     * @param criteria
     * @param props
     * @return
     */
    public synchronized boolean GetPrincipalPropertySearchReport( Vector criteria, Vector props )
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "ACLRequestGenerator::GetPrincipalPropertySearchReport" );
        }

        Headers = null;
        Body = null;
        StrippedResource = parseResourceName( true );
        if( StrippedResource == null )
            return false;

        extendedCode = WebDAVResponseEvent.PRINCIPAL_PROPERTY_SEARCH;
        Method = "REPORT";
        Document miniDoc = new Document();
        miniDoc.setVersion("1.0");
        miniDoc.addChild(WebDAVXML.elemNewline,null);

        AsGen asgen = WebDAVXML.findNamespace( new AsGen(), null );
        if( asgen == null )
            asgen = WebDAVXML.createNamespace( new AsGen(), null );

        Element topElem = WebDAVXML.createElement( ACLXML.ELEM_PRINCIPAL_PROPERTY_SEARCH, Element.ELEMENT, null, asgen );
        topElem.addChild( WebDAVXML.elemNewline, null );
        for( int i=0; i<criteria.size(); i++ )
        {
            Element searchElem = WebDAVXML.createElement( ACLXML.ELEM_PROPERTY_SEARCH, Element.ELEMENT, topElem, asgen );
            searchElem.addChild( WebDAVXML.elemNewline, null );
            ACLPropertySearchNode node = (ACLPropertySearchNode)criteria.get( i );
            Vector searchProps = node.getProperties();
            addProperties( searchElem, asgen, searchProps, 2 );

            Element matchElem = WebDAVXML.createElement( ACLXML.ELEM_MATCH, Element.ELEMENT, searchElem, asgen );
            addChild( searchElem, matchElem, 2, false );
            Element matchVal = WebDAVXML.createElement( null, Element.PCDATA, matchElem, asgen );
            matchVal.setText( node.getMatch() );
            // keep on same line without whitespace
            addChild( matchElem, matchVal, 0, 0, false, false );
            addChild( topElem, searchElem, 1, false );
        }
        if( props != null && props.size() > 0 )
            addProperties( topElem, asgen, props, 1 );
        miniDoc.addChild( topElem, null );

        ByteArrayOutputStream byte_str = new ByteArrayOutputStream();
        XMLOutputStream xml_out = new XMLOutputStream(byte_str);
        try
        {
            miniDoc.save(xml_out);
            Body = byte_str.toByteArray();

            Headers = new NVPair[2];
            if( Port == 0 || Port == DEFAULT_PORT )
            {
                Headers[0] = new NVPair( "Host", HostName );
            }
            else
                Headers[0] = new NVPair( "Host", HostName + ":" + Port );
            Headers[1] = new NVPair( "Depth", "0" );
        }
        catch (Exception e)
        {
            GlobalData.getGlobalData().errorMsg( "XML generation error: \n" + e );
            return false;
        }

        return true;
    }


    /**
     * 
     * @return
     */
    public synchronized boolean GetPrincipalSearchPropertySetReport()
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "ACLRequestGenerator::GetPrincipalSearchPropertySetReport" );
        }

        Headers = null;
        Body = null;
        StrippedResource = parseResourceName( true );
        if( StrippedResource == null )
            return false;

        extendedCode = WebDAVResponseEvent.PRINCIPAL_SEARCH_PROPERTY_SET;
        Method = "REPORT";
        Document miniDoc = new Document();
        miniDoc.setVersion("1.0");
        miniDoc.addChild(WebDAVXML.elemNewline,null);

        AsGen asgen = WebDAVXML.findNamespace( new AsGen(), null );
        if( asgen == null )
            asgen = WebDAVXML.createNamespace( new AsGen(), null );

        Element topElem = WebDAVXML.createElement( ACLXML.ELEM_PRINCIPAL_SEARCH_PROPERTY_SET, Element.ELEMENT, null, asgen );
        miniDoc.addChild( topElem, null );
        
        ByteArrayOutputStream byte_str = new ByteArrayOutputStream();
        XMLOutputStream xml_out = new XMLOutputStream(byte_str);
        try
        {
            miniDoc.save( xml_out );
            Body = byte_str.toByteArray();

            Headers = new NVPair[2];
            if (Port == 0 || Port == DEFAULT_PORT)
            {
                Headers[0] = new NVPair( "Host", HostName );
            }
            else
                Headers[0] = new NVPair( "Host", HostName + ":" + Port );
            Headers[1] = new NVPair( "Depth", "0" );
        }
        catch (Exception e)
        {
            GlobalData.getGlobalData().errorMsg( "XML generation error: \n" + e );
            return false;
        }

        return true;
    }


    /**
     * 
     * @param nodes
     * @return
     */
    public synchronized boolean GenerateACL( Vector nodes )
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "ACLRequestGenerator::GenerateACL" );
        }

        Headers = null;
        Body = null;
        StrippedResource = parseResourceName( true );
        if( StrippedResource == null )
            return false;

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
        for( int p = 0; p < nodes.size(); p++ )
        {
            ACLNode node = (ACLNode)nodes.get( p );
            Element ace = WebDAVXML.createElement( ACLXML.ELEM_ACE, Element.ELEMENT, topElem, asgen );
            // set principal
            Element princ = WebDAVXML.createElement( ACLXML.ELEM_PRINCIPAL, Element.ELEMENT, ace, asgen );
            Element princHref = WebDAVXML.createElement( WebDAVXML.ELEM_HREF, Element.ELEMENT, princ, asgen );
            Element princVal = WebDAVXML.createElement( null, Element.PCDATA, princ, asgen );
            princVal.setText( node.getPrincipal()[1] );
            // keep on same line without whitespace
            addChild( princHref, princVal, 0, 0, false, false );
            addChild( princ, princHref, 3, 2, true, true );
            addChild( ace, princ, 2, 1, true, true );

            // set grant privileges
            Vector privileges = node.getPrivileges();
            if( node.getGrant() )
            {
                Element grantEl = WebDAVXML.createElement( ACLXML.ELEM_GRANT, Element.ELEMENT, ace, asgen );
                for( int g = 0; g < privileges.size(); g++ )
                {
                    Element priv = WebDAVXML.createElement( ACLXML.ELEM_PRIVILEGE, Element.ELEMENT, grantEl, asgen );

                    ACLPrivilege currentPriv = (ACLPrivilege)privileges.get(g);
                    // use the appropriate privilege namespace
                    AsGen namespace = WebDAVXML.findNamespace( new AsGen(), currentPriv.getNamespace() );
                    if( namespace == null )
                        namespace = WebDAVXML.createNamespace( new AsGen(), currentPriv.getNamespace() );
                    Element privVal = WebDAVXML.createElement( currentPriv.getPrivilege(), Element.ELEMENT, priv, namespace );
                    // keep on same line without whitespace
                    addChild( priv, privVal, 0, 0, false, false );
                    addChild( grantEl, priv, 3, 2, true, true );
                }
                addChild( ace, grantEl, 2, 1, true, true );
            }
            else
            {
                // set deny privileges
                // can't have both grant and deny in the same ace element (see
                // pp 45-46 in RFC 3744)
                Element denyEl = WebDAVXML.createElement( ACLXML.ELEM_DENY, Element.ELEMENT, ace, asgen );
                for( int d = 0; d < privileges.size(); d++ )
                {
                    Element priv = WebDAVXML.createElement( ACLXML.ELEM_PRIVILEGE, Element.ELEMENT, denyEl, asgen );

                    ACLPrivilege currentPriv = (ACLPrivilege)privileges.get(d);
                    // use the appropriate privilege namespace
                    AsGen namespace = WebDAVXML.findNamespace( new AsGen(), currentPriv.getNamespace() );
                    if( namespace == null )
                        namespace = WebDAVXML.createNamespace( new AsGen(), currentPriv.getNamespace() );
                    Element privVal = WebDAVXML.createElement( currentPriv.getPrivilege(), Element.ELEMENT, priv, namespace );
                    // keep on same line without whitespace
                    addChild( priv, privVal, 0, 0, false, false );
                    addChild( denyEl, priv, 3, 2, true, true );
                }
                addChild( ace, denyEl, 2, 1, true, true );
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


    /**
     * 
     * @param parent
     * @param DAVns
     * @param props
     * @param indent
     */
    protected void addProperties( Element parent, AsGen DAVns, Vector props, int indent )
    {
        addProperties( parent, DAVns, props, indent, true );
    }


    /**
     * 
     * @param parent
     * @param DAVns
     * @param props
     * @param indent
     * @param usePropElem
     */
    protected void addProperties( Element parent, AsGen DAVns, Vector props, int indent, boolean usePropElem )
    {
        Element propElem;
        if( usePropElem )
        {
            propElem = WebDAVXML.createElement( WebDAVXML.ELEM_PROP, Element.ELEMENT, parent, DAVns );
            propElem.addChild( WebDAVXML.elemNewline, null );
        }
        else
            propElem = parent;
        for( int i=0; i<props.size(); i++ )
        {
            String[] prop = (String[])props.get( i );
            AsGen namespace = DAVns;
            if( prop[1] != null )
            {
                namespace = WebDAVXML.findNamespace( new AsGen(), prop[1] );
                if( namespace == null )
                    namespace = WebDAVXML.createNamespace( new AsGen(), prop[1] );
            }
            Element property = WebDAVXML.createElement( prop[0], Element.ELEMENT, propElem, namespace );
            addChild( propElem, property, indent+1, false );
        }
        if( usePropElem )
            addChild( parent, propElem, indent, false );
    }
}
