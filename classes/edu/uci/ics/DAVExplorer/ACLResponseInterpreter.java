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
import java.io.IOException;

import com.ms.xml.om.Document;
import com.ms.xml.om.Element;
import com.ms.xml.om.TreeEnumeration;
import com.ms.xml.util.Name;

/**
 * Title:       
 * Description: 
 * Copyright:   Copyright (c) 2004-2005 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        
 */

public class ACLResponseInterpreter extends DeltaVResponseInterpreter
{

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
    public void handleResponse( WebDAVResponseEvent e )
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

        // get the resource name, and unescape it
        Resource = GlobalData.getGlobalData().unescape( e.getResource(), "ISO-8859-1", null );
        Node = e.getNode();

        try
        {
            if (res.getStatusCode() < 300)
            {
                if( Method.equals("PROPFIND"))
                    parsePropFind();
                else if( Method.equals("ACL"))
                    parseACL();
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
                super.handleResponse(e);
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

            case WebDAVResponseEvent.ACL_SUPPORTED_PRIVILEGES:
                handlePrivileges( xml_doc, true );
                break;
            case WebDAVResponseEvent.ACL_USER_PRIVILEGES:
                handlePrivileges( xml_doc, false );
                break;
            case WebDAVResponseEvent.ACL:
                break;
            case WebDAVResponseEvent.SUPPORTED_ACL:
                break;
            case WebDAVResponseEvent.INHERITED_ACL:
                break;
            case WebDAVResponseEvent.ACL_PRINCIPALS:
                break;
            default:
                super.parsePropFind();
        }
        printXML( body );
    }


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

            case WebDAVResponseEvent.ACL_SUPPORTED_PRIVILEGES:
                break;
            case WebDAVResponseEvent.ACL_USER_PRIVILEGES:
                break;
            case WebDAVResponseEvent.ACL:
                break;
            case WebDAVResponseEvent.SUPPORTED_ACL:
                break;
            case WebDAVResponseEvent.INHERITED_ACL:
                break;
            case WebDAVResponseEvent.ACL_PRINCIPALS:
                break;
            default:
                super.parsePropPatch();
        }
    }
    
    
    protected void parseACL()
    {
        
    }
    

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
                            String host = HostName;
                            if (Port != 0)
                                host = HostName + ":" + Port;
                            ACLPrivilegesDialog pd = new ACLPrivilegesDialog( rootElem, Resource, host, supported );
                        }
                    }
                }
            }
        }
    }


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

    
    public boolean isACL( String resource )
    {
        Enumeration enum = acl.keys();
        while( enum.hasMoreElements() )
        {
            String res = (String)enum.nextElement();
            if( resource.indexOf(res) >= 0 )
                return( ((Boolean)acl.get(res)).booleanValue() );
        }
        return false;
    }


    protected Hashtable acl = new Hashtable();
}
