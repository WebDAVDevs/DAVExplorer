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

import java.io.ByteArrayInputStream;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

import com.ms.xml.om.Document;
import com.ms.xml.om.Element;
import com.ms.xml.om.TreeEnumeration;
import com.ms.xml.util.Name;


/**
 * Title:       ACL Response interpreter       
 * Description: Processes ACL-specific data from a server.
 * Copyright:   Copyright (c) 2004-2005 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         14 Feb 2005
 */

public class ACLResponseInterpreter extends DeltaVResponseInterpreter
{
    public static int RESOURCETYPE_PRINCIPAL = 2;

    /**
     * Constructor 
     */
    public ACLResponseInterpreter()
    {
        super();
    }

    
    /**
     * Constructor
     * 
     * @param rg    Reference to the WebDAV request generator
     */
    public ACLResponseInterpreter( WebDAVRequestGenerator rg )
    {
        super( rg );
    }

    
    /**
     * Process a response from the server
     * 
     * @param e WebDAVResponseEvent
     *          The event from the client library, containing the response data  
     */
    public boolean handleResponse( WebDAVResponseEvent e )
        throws ResponseException
    {
        if( GlobalData.getGlobalData().getDebugResponse() )
        {
            System.err.println( "ACLResponseInterpreter::handleResponse" );
        }
    
        res = e.getResponse();
        Method = e.getMethodName();
        extendedCode = e.getExtendedCode();
        HostName = e.getHost();
        Port = e.getPort();
        Charset = getCharset();
        Resource = e.getResource();

        // get the resource name, and unescape it
        Resource = GlobalData.getGlobalData().unescape( e.getResource(), "ISO-8859-1", null );
        Node = e.getNode();

        try
        {
            if (res.getStatusCode() < 300)
            {
                if( Method.equals("PROPFIND"))
                    parsePropFind();
                else if( Method.equals("REPORT"))
                    parseReport();
                else if( Method.equals("PROPPATCH"))
                    parsePropPatch();
                else
                {
                    super.handleResponse(e);
                }
            }
            else
            {
                if( (res.getStatusCode() == 302) || (res.getStatusCode() == 301) )
                {
                    switch( e.getExtendedCode() )
                    {
                        case WebDAVResponseEvent.ACL_PRINCIPAL_NAMES:
                            // redirect, restart the request
                            String location = res.getHeader( "Location" );
                            generator.setResource( location, null );
                            ((ACLRequestGenerator)generator).GetPrincipalNames();
                            return false;
                        default:
                            break;
                    }
                }
                super.handleResponse(e);
            }
        }
        catch (Exception ex)
         {
             // Most likely an error propagated from HTTPClient
             // We get this error if the server closes the connection
             // and the method is unknown to HTTPClient.
             // HTTPClient does an automatic retry for idempotent HTTP methods,
             // but not for our DeltaV methods, since it doesn't know about them.
             String debugOutput = System.getProperty( "debug", "false" );
             if( debugOutput.equals( "true" ) )
                 System.out.println(ex);
             throw new ResponseException( "HTTP error" );
         }
        return true;
    }


    /**
     * Parse the response to an OPTIONS request
     */
    public void parseOptions()
    {
        if( GlobalData.getGlobalData().getDebugResponse() )
        {
            System.err.println( "ACLResponseInterpreter::parseOptions" );
        }

        try
        {
            String davheader = res.getHeader( "DAV" );
            if( davheader == null )
            {
                // no WebDAV support
                GlobalData.getGlobalData().errorMsg("ACL Interpreter:\n\nThe server does not support DAV\nat Resource " + Resource + ".");
                return;
            }
            boolean aclFound = false;
            if( davheader.indexOf("access-control") >= 0 )
            {
                aclFound = true;
            }

            String full;
            if (Port == 0 || Port == WebDAVRequestGenerator.DEFAULT_PORT)
                full = HostName;
            else
                full = HostName +":" + Port;
            full += Resource;
            acl.put( full, Boolean.valueOf(aclFound) );
        }
        catch (Exception e)
        {
            GlobalData.getGlobalData().errorMsg("ACL Interpreter:\n\nError encountered \nwhile parsing OPTIONS Response:\n" + e);
            stream = null;
            return;
        }

        if( extendedCode == WebDAVResponseEvent.URIBOX )
        {
            generator.DoPropFind( Resource, false );
        }
    }


    /**
     * 
     */
    protected void parsePropFind()
    {
        if( GlobalData.getGlobalData().getDebugResponse() )
        {
            System.err.println( "ACLResponseInterpreter::parsePropFind" );
        }

        byte[] body = null;
        Document xml_doc = null;

        try
        {
            body = res.getData();
            stream = body;
            if (body == null)
            {
                GlobalData.getGlobalData().errorMsg("DAV Interpreter:\n\nMissing XML body in\nPROPFIND response.");
                return;
            }
            ByteArrayInputStream byte_in = new ByteArrayInputStream(body);
            xml_doc = new Document();
            xml_doc.load( byte_in );
        }
        catch (Exception e)
        {
            GlobalData.getGlobalData().errorMsg("DAV Interpreter:\n\nError encountered \nwhile parsing PROPFIND Response.\n" + e);
            stream = null;
            return;
        }

        switch( extendedCode )
        {
            case WebDAVResponseEvent.ACL_OWNER:
                handleOwner( xml_doc, true );
                break;
            case WebDAVResponseEvent.ACL_GROUP:
                handleOwner( xml_doc, false );
                break;

            case WebDAVResponseEvent.ACL_SUPPORTED_PRIVILEGE_SET:
                handlePrivileges( xml_doc, true );
                break;
            case WebDAVResponseEvent.ACL_USER_PRIVILEGES:
                handlePrivileges( xml_doc, false );
                break;
            case WebDAVResponseEvent.ACL:
            case WebDAVResponseEvent.SUPPORTED_ACL:
            case WebDAVResponseEvent.INHERITED_ACL:
                handleListACLs( xml_doc, extendedCode );
                break;
            case WebDAVResponseEvent.ACL_PRINCIPAL_COLLECTION_SET:
                handlePrincipalCollectionSet( xml_doc );
                break;
            case WebDAVResponseEvent.ACL_PRINCIPAL_NAMES:
                handlePrincipalNames( xml_doc );
                break;
            case WebDAVResponseEvent.ACL_PROPERTY_NAMES:
                handlePropertyNames( xml_doc );
                break;
            default:
                super.parsePropFind();
        }
        printXML( body );
    }


    /**
     * 
     */
    protected void parsePropPatch()
        throws Exception
    {
        if( GlobalData.getGlobalData().getDebugResponse() )
        {
            System.err.println( "ACLResponseInterpreter::parsePropPatch" );
        }

        byte[] body = null;
        Document xml_doc = null;

        try
        {
            body = res.getData();
            stream = body;
            if (body == null)
            {
                GlobalData.getGlobalData().errorMsg("DAV Interpreter:\n\nMissing XML body in\nPROPFIND response.");
                return;
            }
            ByteArrayInputStream byte_in = new ByteArrayInputStream(body);
            xml_doc = new Document();
            xml_doc.load( byte_in );
        }
        catch (Exception e)
        {
            GlobalData.getGlobalData().errorMsg("DAV Interpreter:\n\nError encountered \nwhile parsing PROPFIND Response.\n" + e);
            stream = null;
            return;
        }

        switch( extendedCode )
        {
            case WebDAVResponseEvent.ACL_OWNER:
            case WebDAVResponseEvent.ACL_GROUP:
                if( res.getStatusCode() == 207 )
                    handleMultiStatus( xml_doc );
                break;

            case WebDAVResponseEvent.ACL_SUPPORTED_PRIVILEGE_SET:
            case WebDAVResponseEvent.ACL_USER_PRIVILEGES:
            case WebDAVResponseEvent.ACL:
            case WebDAVResponseEvent.SUPPORTED_ACL:
            case WebDAVResponseEvent.INHERITED_ACL:
            case WebDAVResponseEvent.ACL_PRINCIPAL_COLLECTION_SET:
            case WebDAVResponseEvent.ACL_PRINCIPAL_NAMES:
                break;
            default:
                super.parsePropPatch();
        }
    }
    
    
    /**
     * Process the response to a REPORT request  
     */
    protected void parseReport()
    {
        if( GlobalData.getGlobalData().getDebugResponse() )
        {
            System.err.println( "ACLResponseInterpreter::parseReport" );
        }

        byte[] body = null;
        Document xml_doc = null;

        try
        {
            body = res.getData();
            stream = body;
            if (body == null)
            {
                GlobalData.getGlobalData().errorMsg("DAV Interpreter:\n\nMissing XML body in\nPROPFIND response.");
                return;
            }
            ByteArrayInputStream byte_in = new ByteArrayInputStream(body);
            xml_doc = new Document();
            xml_doc.load( byte_in );
        }
        catch (Exception e)
        {
            GlobalData.getGlobalData().errorMsg("DAV Interpreter:\n\nError encountered \nwhile parsing PROPFIND Response.\n" + e);
            stream = null;
            return;
        }

        switch( extendedCode )
        {
            case WebDAVResponseEvent.ACL_PRINCIPAL_PROP_SET:
                handlePrincipalPropSet( xml_doc );
                break;
            case WebDAVResponseEvent.PRINCIPAL_MATCH:
                handlePrincipalMatch( xml_doc );
                break;
            case WebDAVResponseEvent.PRINCIPAL_PROPERTY_SEARCH:
                handlePrincipalPropertySearch( xml_doc );
                break;
            case WebDAVResponseEvent.PRINCIPAL_SEARCH_PROPERTY_SET:
                handlePrincipalSearchPropertySet( xml_doc );
                break;
            default:
                super.parseReport();
        }
        printXML( body );
    }


    /**
     * 
     * @param xml_doc
     * @param owner
     */
    protected void handleOwner( Document xml_doc, boolean owner )
    {
        String[] token = new String[1];
        token[0] = new String( WebDAVXML.ELEM_RESPONSE );
        Element rootElem = skipElements( xml_doc, token );
        if( rootElem != null )
        {
            TreeEnumeration enumTree =  new TreeEnumeration( rootElem );
            while( enumTree.hasMoreElements() )
            {
                Element current = (Element)enumTree.nextElement();
                Name currentTag = current.getTagName();
                if( currentTag != null )
                {
                    if( currentTag.getName().equals( WebDAVXML.ELEM_PROPSTAT ) )
                    {
                        token = new String[2];
                        token[0] = new String( WebDAVXML.ELEM_PROPSTAT );
                        token[1] = new String( WebDAVXML.ELEM_PROP );
                        rootElem = skipElements( current, token );
                        if( rootElem != null )
                        {
                            String host = HostName;
                            if (Port != 0)
                                host = HostName + ":" + Port;
                            ACLOwnerDialog pd = new ACLOwnerDialog( rootElem, Resource, host, owner, true );
                        }
                    }
                }
            }
        }
    }


    /**
     * 
     * @param xml_doc
     * @param supported
     */
    protected void handlePrivileges( Document xml_doc, boolean supported )
    {
        String[] token = new String[1];
        token[0] = new String( WebDAVXML.ELEM_RESPONSE );
        Element rootElem = skipElements( xml_doc, token );
        if( rootElem != null )
        {
            TreeEnumeration enumTree =  new TreeEnumeration( rootElem );
            while( enumTree.hasMoreElements() )
            {
                Element current = (Element)enumTree.nextElement();
                Name currentTag = current.getTagName();
                if( currentTag != null )
                {
                    if( currentTag.getName().equals( WebDAVXML.ELEM_PROPSTAT ) )
                    {
                        token = new String[2];
                        token[0] = new String( WebDAVXML.ELEM_PROPSTAT );
                        token[1] = new String( WebDAVXML.ELEM_PROP );
                        rootElem = skipElements( current, token );
                        if( rootElem != null )
                        {
                            if( supported )
                            {
                                // extract the supported privileges
                                handleSupportedPrivileges( rootElem );
                            }
                            else
                            {
                                // show the user privileges
                                String host = HostName;
                                if (Port != 0)
                                    host = HostName + ":" + Port;
                                ACLPrivilegesDialog pd = new ACLPrivilegesDialog( rootElem, Resource, host, supported );
                            }
                            break;
                        }
                    }
                }
            }
        }
    }


    /**
     * 
     * @param xml_doc
     * @param code
     */
    protected void handleListACLs( Document xml_doc, int code )
    {
        String[] token = new String[1];
        token[0] = new String( WebDAVXML.ELEM_RESPONSE );
        Element rootElem = skipElements( xml_doc, token );
        if( rootElem != null )
        {
            TreeEnumeration enumTree =  new TreeEnumeration( rootElem );
            while( enumTree.hasMoreElements() )
            {
                Element current = (Element)enumTree.nextElement();
                Name currentTag = current.getTagName();
                if( currentTag != null )
                {
                    if( currentTag.getName().equals( WebDAVXML.ELEM_PROPSTAT ) )
                    {
                        token = new String[2];
                        token[0] = new String( WebDAVXML.ELEM_PROPSTAT );
                        token[1] = new String( WebDAVXML.ELEM_PROP );
                        rootElem = skipElements( current, token );
                        if( rootElem != null )
                        {
                            String host = HostName;
                            if (Port != 0)
                                host = HostName + ":" + Port;
                            switch( code )
                            {
                                case WebDAVResponseEvent.ACL:
                                {
                                    //ACLListDialog pd = new ACLListDialog( rootElem, Resource, host );
                                    ACLDialog pd = new ACLDialog( rootElem, Resource, host, null );
                                    break;
                                }
                                case WebDAVResponseEvent.SUPPORTED_ACL:
                                {
                                    ACLRestrictionDialog pd = new ACLRestrictionDialog( rootElem, Resource, host );
                                    break;
                                }
                                case WebDAVResponseEvent.INHERITED_ACL:
                                {
                                    ACLInheritedDialog pd = new ACLInheritedDialog( rootElem, Resource, host );
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * 
     * @param xml_doc
     */
    protected void handlePrincipalCollectionSet( Document xml_doc )
    {
        String[] skiptoken = new String[1];
        skiptoken[0] = new String( ACLXML.ELEM_PRINCIPAL_COLLECTION_SET );
        Element rootElem = skipElements( xml_doc, skiptoken );
        principalCollectionSet = new Vector();
        if( rootElem != null )
        {
            TreeEnumeration enumTree =  new TreeEnumeration( rootElem );
            while( enumTree.hasMoreElements() )
            {
                Element current = (Element)enumTree.nextElement();
                Name currentTag = current.getTagName();
                if( currentTag != null )
                {
                    if( currentTag.getName().equals( WebDAVXML.ELEM_HREF ) )
                    {
                        Element token = (Element)enumTree.nextElement();
                        if( (token != null) && (token.getType() == Element.PCDATA || token.getType() == Element.CDATA) )
                        {
                            principalCollectionSet.add( GlobalData.getGlobalData().unescape( token.getText(), "UTF-8", null ) );
                        }
                    }
                }
            }
        }
    }


    /**
     * 
     * @param xml_doc
     */
    protected void handlePrincipalNames( Document xml_doc )
    {
        principalNames = new Vector();
        String[] token = new String[1];
        token[0] = new String( WebDAVXML.ELEM_MULTISTATUS );

        // get the base these principals belong to
        // needed to disambiguate the principal names
        int slashpos;
        if( Resource.endsWith("/" ) )
            slashpos = Resource.lastIndexOf( '/', Resource.length()-2 );
        else
            slashpos = Resource.lastIndexOf( '/', Resource.length() );
        String base = Resource.substring( slashpos );
        if( !base.endsWith("/") )
            base += "/";

        String href = null;
        Element curProp = null;
        Element rootElem = skipElements( xml_doc, token );
        if( rootElem != null )
        {
            TreeEnumeration enumTree =  new TreeEnumeration( rootElem );
            while( enumTree.hasMoreElements() )
            {
                Element current = (Element)enumTree.nextElement();
                Name currentTag = current.getTagName();
                if( currentTag != null )
                {
                    if( currentTag.getName().equals( WebDAVXML.ELEM_HREF ) )
                        href = getHref( enumTree, current );
                    if( currentTag.getName().equals( WebDAVXML.ELEM_PROP ) )
                        curProp = current;
                    if( currentTag.getName().equals( WebDAVProp.PROP_RESOURCETYPE ) )
                    {
                        // we only care about resources that are principal types
                        if( getResourceType( current ) == RESOURCETYPE_PRINCIPAL )
                        {
                            String[] names = new String[2];
                            names[0] = href;
                            names[1] = base + getDisplayName( curProp );
                            principalNames.add( names );
                        }
                    }
                }
            }
        }
    }


    /**
     * 
     * @param rootElem
     */
    protected void handleSupportedPrivileges( Element rootElem )
    {
        supportedPrivilegeSet = new Vector();
        if( rootElem != null )
        {
            TreeEnumeration enumTree =  new TreeEnumeration( rootElem );
            while( enumTree.hasMoreElements() )
            {
                Element current = (Element)enumTree.nextElement();
                Name currentTag = current.getTagName();
                if( currentTag != null )
                {
                    if( currentTag.getName().equals( ACLXML.ELEM_PRIVILEGE ) )
                    {
                        Element token; 
                        while( enumTree.hasMoreElements() )
                        {
                            token = (Element)enumTree.nextElement();
                            if( token == null || token.getTagName() == null )
                                continue;
                            ACLPrivilege privilege = new ACLPrivilege();
                            privilege.setPrivilege(token.getTagName().getName());
                            String ns = WebDAVProp.locateNamespace(token, token.getTagName());
                            privilege.setNamespace(ns);
                            supportedPrivilegeSet.add( privilege );
                            break;
                        }
                    }
                }
            }
        }
    }


    /**
     * 
     * @param xml_doc
     */
    protected void handlePropertyNames( Document xml_doc )
    {
        propertyNames = new Vector();
        String[] token = new String[2];
        token[0] = new String( WebDAVXML.ELEM_MULTISTATUS );
        token[1] = new String( WebDAVXML.ELEM_RESPONSE );

        String href = null;
        Element curProp = null;
        Element rootElem = skipElements( xml_doc, token );
        if( rootElem != null )
        {
            TreeEnumeration enumTree =  new TreeEnumeration( rootElem );
            while( enumTree.hasMoreElements() )
            {
                Element current = (Element)enumTree.nextElement();
                Name currentTag = current.getTagName();
                if( currentTag != null )
                {
                    if( currentTag.getName().equals( WebDAVXML.ELEM_PROP ) )
                    {
                        parsePropertyNames( current );
                    }
                }
            }
        }
    }


    /**
     * 
     * @param rootElem
     */
    protected void parsePropertyNames( Element rootElem )
    {
        if( rootElem != null )
        {
            TreeEnumeration enumTree =  new TreeEnumeration( rootElem );
            while( enumTree.hasMoreElements() )
            {
                Element current = (Element)enumTree.nextElement();
                Name currentTag = current.getTagName();
                if( currentTag != null )
                {
                    if( currentTag.getName().equals( WebDAVXML.ELEM_PROP ) )
                        continue;
                    String[] prop = new String[2];
                    prop[0] = currentTag.getName();
                    prop[1] = WebDAVProp.locateNamespace( current, currentTag );
                    propertyNames.add( prop );
                }
            }
        }
    }


    /**
     * 
     * @param xml_doc
     */
    protected void handleMultiStatus( Document xml_doc )
    {
        int status = 0;
        String description = "";
        
        String[] token = new String[1];
        token[0] = new String( WebDAVXML.ELEM_RESPONSE );
        Element rootElem = skipElements( xml_doc, token );
        if( rootElem != null )
        {
            TreeEnumeration enumTree =  new TreeEnumeration( rootElem );
            while( enumTree.hasMoreElements() )
            {
                Element current = (Element)enumTree.nextElement();
                Name currentTag = current.getTagName();
                if( currentTag != null )
                {
                    if( currentTag.getName().equals( ACLXML.ELEM_STATUS ) )
                    {
                        status = getStatus( current );
                    }
                    else if( currentTag.getName().equals( ACLXML.ELEM_RESPONSEDESCRIPTION ) )
                    {
                        description = getDescription( current );
                    }
                }
            }
        }
        if( status >= 400 )
        {
            GlobalData.getGlobalData().errorMsg( description );
        }
    }


    /**
     * 
     * @param description
     * @return
     */
    protected String getDescription( Element description )
    {
        if( GlobalData.getGlobalData().getDebugTreeNode() )
        {
            System.err.println( "ACLResponseInterpreter::getDescription" );
        }

        TreeEnumeration treeEnum = new TreeEnumeration( description );
        while(treeEnum.hasMoreElements() )
        {
            Element token = (Element)treeEnum.nextElement();
            if( (token != null) && (token.getType() == Element.PCDATA || token.getType() == Element.CDATA) )
            {
                return GlobalData.getGlobalData().unescape( token.getText(), Charset, null );
            }
        }
        return "";
    }


    /**
     * 
     */
    protected int getResourceType( Element resourcetype )
    {
        if( GlobalData.getGlobalData().getDebugTreeNode() )
        {
            System.err.println( "ACLResponseInterpreter::getResourceType" );
        }

        TreeEnumeration treeEnum = new TreeEnumeration( resourcetype );
        while(treeEnum.hasMoreElements() )
        {
            Element current = (Element)treeEnum.nextElement();
            Name tag = current.getTagName();
            if( (tag != null) && tag.getName().equals( ACLXML.ELEM_PRINCIPAL ) )
                return RESOURCETYPE_PRINCIPAL;
        }
        return super.getResourceType( resourcetype );
    }


    /**
     * 
     * @param xml_doc
     */
    protected void handlePrincipalPropSet( Document xml_doc )
    {
        // expecting a <multistatus> tag, skipping everything up to it
        String[] token = new String[1];
        token[0] = new String( WebDAVXML.ELEM_MULTISTATUS );
        Element rootElem = skipElements( xml_doc, token );
        Vector subtrees = new Vector();
        
        if( rootElem != null )
        {
            TreeEnumeration enumTree =  new TreeEnumeration( rootElem );
            while( enumTree.hasMoreElements() )
            {
                Element current = (Element)enumTree.nextElement();
                Name currentTag = current.getTagName();
                if( currentTag != null )
                {
                    // expecting a <response> tag
                    if( currentTag.getName().equals( WebDAVXML.ELEM_RESPONSE ) )
                    {
                        subtrees.add( current );
                    }
                }
            }
            PrincipalPropertiesModel model = new PrincipalPropertiesModel( subtrees );
            PropDialog dlg = new PropDialog( model, Resource, HostName, "View Principal Property Set", null, false );
        }
    }


    /**
     * 
     * @param xml_doc
     */
    protected void handlePrincipalMatch( Document xml_doc )
    {
        // expecting a <multistatus> tag, skipping everything up to it
        String[] token = new String[1];
        token[0] = new String( WebDAVXML.ELEM_MULTISTATUS );
        Element rootElem = skipElements( xml_doc, token );
        Vector subtrees = new Vector();
        
        if( rootElem != null )
        {
            TreeEnumeration enumTree =  new TreeEnumeration( rootElem );
            while( enumTree.hasMoreElements() )
            {
                Element current = (Element)enumTree.nextElement();
                Name currentTag = current.getTagName();
                if( currentTag != null )
                {
                    // expecting a <response> tag
                    if( currentTag.getName().equals( WebDAVXML.ELEM_RESPONSE ) )
                    {
                        subtrees.add( current );
                    }
                }
            }
            PrincipalPropertiesModel model = new PrincipalPropertiesModel( subtrees );
            PropDialog dlg = new PropDialog( model, Resource, HostName, "View Principal Match", null, false );
        }
    }


    /**
     * 
     * @param xml_doc
     */
    protected void handlePrincipalPropertySearch( Document xml_doc )
    {
        // expecting a <multistatus> tag, skipping everything up to it
        String[] token = new String[1];
        token[0] = new String( WebDAVXML.ELEM_MULTISTATUS );
        Element rootElem = skipElements( xml_doc, token );
        
        if( rootElem != null )
        {
            TreeEnumeration enumTree =  new TreeEnumeration( rootElem );
            while( enumTree.hasMoreElements() )
            {
                Element current = (Element)enumTree.nextElement();
                Name currentTag = current.getTagName();
                if( currentTag != null )
                {
                    // expecting a <response> tag
                    if( currentTag.getName().equals( WebDAVXML.ELEM_RESPONSE ) )
                    {
                        PrincipalPropertiesModel model = new PrincipalPropertiesModel( current );
                        PropDialog dlg = new PropDialog( model, Resource, HostName, "View Principal Properties", null, false );
                    }
                }
            }
        }
    }


    /**
     * 
     * @param xml_doc
     */
    protected void handlePrincipalSearchPropertySet( Document xml_doc )
    {
        // expecting a <principal-search-property-set> tag, skipping everything up to it
        String[] token = new String[1];
        token[0] = new String( ACLXML.ELEM_PRINCIPAL_SEARCH_PROPERTY_SET );
        Element rootElem = skipElements( xml_doc, token );
        Vector subtrees = new Vector();
        
        if( rootElem != null )
        {
            TreeEnumeration enumTree =  new TreeEnumeration( rootElem );
            while( enumTree.hasMoreElements() )
            {
                Element current = (Element)enumTree.nextElement();
                Name currentTag = current.getTagName();
                if( currentTag != null )
                {
                    // expecting a <principal-search-property> tag
                    if( currentTag.getName().equals( ACLXML.ELEM_PRINCIPAL_SEARCH_PROPERTY ) )
                    {
                        subtrees.add( current );
                    }
                }
            }
            PrincipalPropertiesModel model = new PrincipalPropertiesModel( subtrees );
            PropDialog dlg = new PropDialog( model, Resource, HostName, "View Principal Search Properties", null, false );
        }
    }


    /**
     * 
     * @param resource
     * @return
     */
    public boolean isACL( String resource )
    {
        Enumeration ACLEnum = acl.keys();
        while( ACLEnum.hasMoreElements() )
        {
            String _res = (String)ACLEnum.nextElement();
            if( resource.indexOf(_res) >= 0 )
                return( ((Boolean)acl.get(_res)).booleanValue() );
        }
        return false;
    }


    /**
     * 
     * @return
     */
    public Vector getPrincipalCollectionSet()
    {
        return principalCollectionSet;
        
    }


    /**
     * 
     * @return
     */
    public Vector getPrincipalNames()
    {
        return principalNames;
    }


    /**
     * 
     * @return
     */
    public Vector getSupportedPrivilegeSet()
    {
        return supportedPrivilegeSet;
    }


    /**
     * 
     * @return
     */
    public Vector getPropertyNames()
    {
        return propertyNames;
    }


    protected Hashtable acl = new Hashtable();
    protected Vector principalCollectionSet;
    protected Vector principalNames;
    protected Vector supportedPrivilegeSet;
    protected Vector propertyNames;
}
