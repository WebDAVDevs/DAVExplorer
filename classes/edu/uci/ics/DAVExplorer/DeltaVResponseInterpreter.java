/*
 * Copyright (c) 2003-2005 Regents of the University of California.
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

import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.io.ByteArrayInputStream;
import com.ms.xml.om.Element;
import com.ms.xml.om.Document;
import com.ms.xml.om.TreeEnumeration;
import com.ms.xml.om.SiblingEnumeration;
import com.ms.xml.util.Name;


/**
 * Title:       DeltaVResponse Interpreter
 * Description: This is the interpreter module that parses DeltaV responses.
 *              Some of the methods are not parsed, and the functions are left
 *              empty intentionally.
 * Copyright:   Copyright (c) 2003-2005 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         23 September 2003
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         28 October 2003
 * Changes:     Fixed double insertion listener firing.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         07 February 2004
 * Changes:     Parsing OPTIONS response for activity data
 *              (needed for evtl. Subversion support.)
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         08 February 2004
 * Changes:     Added Javadoc templates
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         15 February 2005
 * Changes:     Some refactoring
 */
public class DeltaVResponseInterpreter extends WebDAVResponseInterpreter
{
    /**
     * Constructor, just initializing superclass  
     */
    public DeltaVResponseInterpreter()
    {
        super();
    }


    /**
     * Constructor, storing the request generator
     *   
     * @param rg    WebDAVRequestGenerator
     */
    public DeltaVResponseInterpreter( WebDAVRequestGenerator rg )
    {
        super(rg);
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
            System.err.println( "DeltaVResponseInterpreter::handleResponse" );
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
                if (Method.equals("VERSION-CONTROL"))
                    parseVersionControl();
                else if (Method.equals("CHECKOUT"))
                    parseCheckout();
                else if (Method.equals("UNCHECKOUT"))
                    parseUnCheckout();
                else if (Method.equals("CHECKIN"))
                    parseCheckin();
                else if (Method.equals("REPORT"))
                    parseReport();
                else if (Method.equals("MKACTIVITY"))
                    parseMkActivity();
                else if (Method.equals("MERGE"))
                    parseMerge();
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
        return true;
    }


    /**
     * Process the response to a VERSION-CONTROL request  
     */
    protected void parseVersionControl()
    {
        try
        {
            int code = res.getStatusCode();
            if ( code >= 200 && code < 300 )
                fireVersionControlEvent( Resource, code );
            else
                fireVersionControlEvent( res.getReasonLine(), code );
        }
        catch(Exception e)
        {
        }
    }

    
    /**
     * Process the response to a CHECKOUT request  
     */
    protected void parseCheckout()
    {
        try
        {
            int code = res.getStatusCode();
            if ( code >= 200 && code < 300 )
                fireCheckoutEvent( Resource, code );
            else
                fireCheckoutEvent( res.getReasonLine(), code );
        }
        catch(Exception e)
        {
        }
    }

    
    /**
     * Process the response to an UNCHECKOUT request  
     */
    protected void parseUnCheckout()
    {
        try{
            int code = res.getStatusCode();
            if ( code >= 200 && code < 300 )
                fireUnCheckoutEvent( Resource, code );
            else
                fireUnCheckoutEvent( res.getReasonLine(), code );
        }
        catch(Exception e)
        {
        }
    }

    
    /**
     * Process the response to a CHECKIN request  
     */
    protected void parseCheckin()
    {
        try{
            int code = res.getStatusCode();
            if ( code >= 200 && code < 300 )
                fireCheckinEvent( Resource, code );
            else
                fireCheckinEvent( res.getReasonLine(), code );
        }
        catch(Exception e)
        {
        }
    }


    /**
     * Process the response to a REPORT request  
     */
    protected void parseReport()
    {
        Vector nodesChildren = new Vector();
        String ResourceName = getResource();

        byte[] body = null;
        Document xml_doc = null;

        try
        {
            body = res.getData();
            stream = body;
        
            if (body == null)
            {
                GlobalData.getGlobalData().errorMsg("DeltaV Interpreter:\n\nMissing XML body in\nREPORT response.");
                return;
            }
        
            ByteArrayInputStream byte_in = new ByteArrayInputStream(body);
        
            xml_doc = new Document();
            xml_doc.load( byte_in );
        }
        catch (Exception e)
        {
            GlobalData.getGlobalData().errorMsg("DeltaV Interpreter:\n\nError encountered \nwhile parsing REPORT Response.\n" + e);
            stream = null;
            return;
        }

        printXML( body );
        
        DataNode dataNode = null;
        
        // expecting a <multistatus> tag, skipping everything up to it
        String[] token = new String[1];
        token[0] = new String( WebDAVXML.ELEM_MULTISTATUS );
        Element rootElem = skipElements( xml_doc, token );
        int count = 0;
        
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
                        dataNode = parseResponse( current, ResourceName, nodesChildren );
                    }
                }
            }
        }

        if( extendedCode == WebDAVResponseEvent.DISPLAY )
        {
            String host = HostName;
            if (Port != 0)
                host = HostName + ":" + Port;
            VersionInfoDialog dlg = new VersionInfoDialog( nodesChildren, ResourceName, host );
            dlg.addGetVersionListener( new GetVersionListener() );
            dlg.show();
                            
        }
    }


    /**
     * Generate the request to get a specific version  
     */
    class GetVersionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            if( e.getActionCommand() != null )
                generator.setResource( e.getActionCommand(), null );
            if( generator.GenerateGet( WebDAVResponseEvent.SAVE_AS ) )
                generator.execute();
        }
    }


    /**
     * Parse a subtree of a <response> tag
     * 
     * @param respElem      the root of the <response> tree
     * @param resourceName  the relative URL of the resource this tree refers to
     * @param nodesChildren internal structure holding data about child nodes
     * 
     * @return              structure holding version information for the resource
     */
    protected DataNode parseResponse( Element respElem, String resourceName, Vector nodesChildren )
    {
        return parseResponse( respElem, resourceName, nodesChildren, null, null, null );
    }


    /**
     * Parse a subtree of a <response> tag
     * 
     * @param respElem      the root of the <response> tree
     * @param resourceName  the relative URL of the resource this tree refers to
     * @param nodesChildren internal structure holding data about child nodes
     * @param dataNode      structure holding version information for the resource
     * @param userAgent     user agent string
     * @param treeNode      node holding the collection URL for this resource
     * 
     * @return              structure holding version information for the resource
     */
    protected DataNode parseResponse( Element respElem, String resourceName, Vector nodesChildren,
                                      DataNode dataNode, String userAgent, DefaultMutableTreeNode treeNode )
    {
        if( GlobalData.getGlobalData().getDebugTreeNode() )
        {
            System.err.println( "DeltaVResponseInterpreter::parseResponse" );
        }
    
        DataNode node = null;
        String resName = "";
        String fullName = "";
        Element current = null;
    
        if( respElem.numElements() == 0 )
            return null;
    
        SiblingEnumeration enumTree =  new SiblingEnumeration( respElem.getChild(0) );
        while( enumTree.hasMoreElements() )
        {
            current = (Element)enumTree.nextElement();
            Name currentTag = current.getTagName();
            if( currentTag != null )
            {
                if( currentTag.getName().equals( WebDAVXML.ELEM_HREF ) )
                {
                    TreeEnumeration enumHref =  new TreeEnumeration( current );
                    while( enumHref.hasMoreElements() )
                    {
                        Element token = (Element)enumHref.nextElement();
                        if( (token != null) && (token.getType() == Element.PCDATA || token.getType() == Element.CDATA) )
                        {
                            resName = truncateResource(GlobalData.getGlobalData().unescape(token.getText(), Charset, null ) );
                            fullName = getFullResource(GlobalData.getGlobalData().unescape(token.getText(), Charset, null ) );
                        }
                    }
                }
                else if( currentTag.getName().equals( WebDAVXML.ELEM_PROPSTAT ) )
                {
                    if( resName != "" )
                    {
                        DataNode curnode = parseProps( current, resourceName, resName );
                        if( node == null )
                            node = curnode;
                        else
                        {
                            // update node values as necessary
                            if( curnode.getDisplay().length() != 0 )
                                node.setDisplay( curnode.getDisplay() );    // overwrite any old value
                            if( curnode.isLocked() && !node.isLocked() )
                            {
                                node.lock( curnode.getLockToken() );        // never change back to unlocked here
                            }
                            if( curnode.isCollection() )
                                node.makeCollection();                      // never change back to normal node
                            if( curnode.getType().length() != 0 )
                                node.setType( curnode.getType() );          // overwrite any old value
                            if( curnode.getSize()!=0 )
                                node.setSize( curnode.getSize() );          // overwrite any old value
                            if( curnode.getDate() != null )
                                node.setDate( curnode.getDate() );          // overwrite any old value

                            if( ((DeltaVDataNode)node).getComment().length() == 0 )
                                ((DeltaVDataNode)node).setComment( ((DeltaVDataNode)curnode).getComment() );
                            if( ((DeltaVDataNode)node).getCreatorDisplayName().length() == 0 )
                                ((DeltaVDataNode)node).setCreatorDisplayName( ((DeltaVDataNode)curnode).getCreatorDisplayName() );
                            if( ((DeltaVDataNode)node).getSupportedMethodSet().length() == 0 )
                                ((DeltaVDataNode)node).setSupportedMethodSet( ((DeltaVDataNode)curnode).getSupportedMethodSet() );
                            if( ((DeltaVDataNode)node).getSupportedLivePropertySet().length() == 0 )
                                ((DeltaVDataNode)node).setSupportedLivePropertySet( ((DeltaVDataNode)curnode).getSupportedLivePropertySet() );
                            if( ((DeltaVDataNode)node).getSupportedReportSet().length() == 0 )
                                ((DeltaVDataNode)node).setSupportedReportSet( ((DeltaVDataNode)curnode).getSupportedReportSet() );
                            if( ((DeltaVDataNode)node).getCheckedIn().length() == 0 )
                                ((DeltaVDataNode)node).setCheckedIn( ((DeltaVDataNode)curnode).getCheckedIn() );
                            if( ((DeltaVDataNode)node).getAutoVersion().length() == 0 )
                                ((DeltaVDataNode)node).setAutoVersion( ((DeltaVDataNode)curnode).getAutoVersion() );
                            if( ((DeltaVDataNode)node).getCheckedOut().length() == 0 )
                                ((DeltaVDataNode)node).setCheckedOut( ((DeltaVDataNode)curnode).getCheckedOut() );
                            if( ((DeltaVDataNode)node).getPredecessorSet().length() == 0 )
                                ((DeltaVDataNode)node).setPredecessorSet( ((DeltaVDataNode)curnode).getPredecessorSet() );
                            if( ((DeltaVDataNode)node).getSuccessorSet().length() == 0 )
                                ((DeltaVDataNode)node).setSuccessorSet( ((DeltaVDataNode)curnode).getSuccessorSet() );
                            if( ((DeltaVDataNode)node).getCheckoutSet().length() == 0 )
                                ((DeltaVDataNode)node).setCheckoutSet( ((DeltaVDataNode)curnode).getCheckoutSet() );
                            if( ((DeltaVDataNode)node).getVersionName().length() == 0 )
                                ((DeltaVDataNode)node).setVersionName( ((DeltaVDataNode)curnode).getVersionName() );
                        }
                    }
                }
            }
        }

        String fullResName;
        if( GlobalData.getGlobalData().getSSL() )
            fullResName = GlobalData.WebDAVPrefixSSL + getHost();
        else
            fullResName = GlobalData.WebDAVPrefix + getHost();
        if( Port != 0 )
            fullResName += ":" + Integer.toString( Port );
        fullResName += "/" + fullName;

        // save data node
        if( node != null )
        {
            String resourceNameStrp = "";
            if( resourceName.endsWith("/") )
                resourceNameStrp = resourceName.substring( 0, resourceName.length() - 1 );
            if ( (fullName.equals(resourceName)) || (fullName.equals(resourceNameStrp)) )
            {
                // this is the container
                String hostName = resName;
                if( treeNode != null )
                {
                    int pathLen = treeNode.getPath().length;
                    if (pathLen == 2)
                    {
                        if( GlobalData.getGlobalData().getSSL() )
                            hostName = GlobalData.WebDAVPrefixSSL + getHost() + "/" + resourceName;
                        else
                            hostName = GlobalData.WebDAVPrefix + getHost() + "/" + resourceName;
                    }
                }
                // update node values
                dataNode = new DeltaVDataNode( node.isCollection(), node.isLocked(), node.getLockToken(),
                                         hostName, node.getDisplay(), node.getType(), node.getSize(),
                                         node.getDate(), null );
                ((DeltaVDataNode)dataNode).copyFrom( (DeltaVDataNode)node );
                ((DeltaVDataNode)dataNode).setHref( fullResName );
            }
            else
            {
                ((DeltaVDataNode)node).setHref( fullResName );
                if( node.isCollection() )
                {
                    if( treeNode != null )
                    {
                        WebDAVTreeNode childNode = new WebDAVTreeNode( resName, userAgent );
                        childNode.setDataNode( node );
                        treeNode.insert( childNode, 0 );
                    }
                }
                else
                {
                    DataNode lastNode = null; 
                    if( nodesChildren.size() > 0 )
                        lastNode = (DataNode)nodesChildren.get(nodesChildren.size()-1 );
                    if ( lastNode == null || !lastNode.getName().equals(node.getName()) )
                    {
                        nodesChildren.addElement(node);
                    }
                    else
                    {
                        ((DeltaVDataNode)lastNode).addVersion( node );
                    }
                }
            }
        }
    
        // handle the case when the server doesn't send properties for the container
        // itself
        if( dataNode == null && treeNode != null )
        {
            // create a container with as much data as we have
            int pathLen = treeNode.getPath().length;
            String hostName = resName;
            if (pathLen == 2)
            {
                if( GlobalData.getGlobalData().getSSL() )
                    hostName = GlobalData.WebDAVPrefixSSL + getHost() + "/" + resourceName;
                else
                    hostName = GlobalData.WebDAVPrefix + getHost() + "/" + resourceName;
            }
            // update node values
            dataNode = new DeltaVDataNode( true, false, null,
                                     hostName, resourceName, "httpd/unix-directory", 0,
                                     "", null );
            ((DeltaVDataNode)dataNode).setHref( fullResName );
        }
        
        return dataNode;
    }
    
    
    /**
     * Parse the DeltaV properties in a subtree of a <prop> tag
     * 
     * @param propElem      the root of the <prop> tree
     * @param resourceName  the relative URL of the resource this tree refers to
     * @param resName       the relative URL of a version of the resource
     * 
     * @return              structure holding version information for the resource
     */
    protected DataNode parseProps( Element propElem, String ResourceName, String resName )
    {
        if( GlobalData.getGlobalData().getDebugTreeNode() )
        {
            System.err.println( "DeltaVResponseInterpreter::parseProps" );
        }

        String comment = "";
        String creatorDisplayName = "";
        String supportedMethodSet = "";
        String supportedLivePropertySet = "";
        String supportedReportSet = "";
        String checkedIn = "";
        String autoVersion = "";
        String checkedOut = "";
        String predecessorSet = "";
        String successorSet = "";
        String checkoutSet = "";
        String versionName = "";
    
        String[] token = new String[1];
        token[0] = new String( WebDAVXML.ELEM_PROP );
    
        Element rootElem = skipElements( propElem, token );
        if( rootElem != null )
        {
            TreeEnumeration enumTree =  new TreeEnumeration( rootElem );
            while( enumTree.hasMoreElements() )
            {
                Element current = (Element)enumTree.nextElement();
                Name currentTag = current.getTagName();
                if( currentTag != null )
                {
                    if( currentTag.getName().equals( DeltaVProp.PROP_COMMENT ) )
                    {
                        comment = getPropValue( current );
                    }
                    else if( currentTag.getName().equals( DeltaVProp.PROP_CREATOR_DISPLAYNAME ) )
                    {
                        creatorDisplayName = getPropValue( current );
                    }
                    else if( currentTag.getName().equals( DeltaVProp.PROP_SUPPORTED_METHOD_SET ) )
                    {
                        supportedMethodSet = getPropValue( current );
                    }
                    else if( currentTag.getName().equals( DeltaVProp.PROP_SUPPORTED_LIVE_PROPERTY_SET ) )
                    {
                        supportedLivePropertySet = getPropValue( current );
                    }
                    else if( currentTag.getName().equals( DeltaVProp.PROP_SUPPORTED_REPORT_SET ) )
                    {
                        supportedReportSet = getPropValue( current );
                    }
                    else if( currentTag.getName().equals( DeltaVProp.PROP_CHECKED_IN ) )
                    {
                        checkedIn = getPropValue( current );
                    }
                    else if( currentTag.getName().equals( DeltaVProp.PROP_AUTO_VERSION ) )
                    {
                        autoVersion = getPropValue( current );
                    }
                    else if( currentTag.getName().equals( DeltaVProp.PROP_CHECKED_OUT ) )
                    {
                        checkedOut = getPropValue( current );
                    }
                    else if( currentTag.getName().equals( DeltaVProp.PROP_PREDECESSOR_SET ) )
                    {
                        predecessorSet = getPropValue( current );
                    }
                    else if( currentTag.getName().equals( DeltaVProp.PROP_SUCCESSOR_SET ) )
                    {
                        successorSet = getPropValue( current );
                    }
                    else if( currentTag.getName().equals( DeltaVProp.PROP_CHECKOUT_SET ) )
                    {
                        checkoutSet = getPropValue( current );
                    }
                    else if( currentTag.getName().equals( DeltaVProp.PROP_VERSION_NAME ) )
                    {
                        versionName = getPropValue( current );
                    }
                }
            }
        }
    
        // This is where we fill out the data node
        DataNode newNode = new DeltaVDataNode(super.parseProps( propElem, ResourceName, resName ));
        ((DeltaVDataNode)newNode).setComment( comment );
        ((DeltaVDataNode)newNode).setCreatorDisplayName( creatorDisplayName );
        ((DeltaVDataNode)newNode).setSupportedMethodSet( supportedMethodSet );
        ((DeltaVDataNode)newNode).setSupportedLivePropertySet( supportedLivePropertySet );
        ((DeltaVDataNode)newNode).setSupportedReportSet( supportedReportSet );
        ((DeltaVDataNode)newNode).setCheckedIn( checkedIn );
        ((DeltaVDataNode)newNode).setAutoVersion( autoVersion );
        ((DeltaVDataNode)newNode).setCheckedOut( checkedOut );
        ((DeltaVDataNode)newNode).setPredecessorSet( predecessorSet );
        ((DeltaVDataNode)newNode).setSuccessorSet( successorSet );
        ((DeltaVDataNode)newNode).setCheckoutSet( checkoutSet );
        ((DeltaVDataNode)newNode).setVersionName( versionName );
        return newNode;
    }


    /**
     * Parse a subtree and return the value of the first <href> tag 
     * 
     * @param href      the root of the tree
     * 
     * @return          the href, or an empty if no <href> tag was found
     */
    protected String getHref( Element href )
    {
            if( GlobalData.getGlobalData().getDebugResponse() )
            {
                System.err.println( "DeltaVResponseInterpreter::getHref" );
            }

            TreeEnumeration treeEnum = new TreeEnumeration( href );
            while(treeEnum.hasMoreElements() )
            {
                Element current = (Element)treeEnum.nextElement();
                Name tag = current.getTagName();
                if( (tag != null) && tag.getName().equalsIgnoreCase( "href" ) )
                {
                    current = (Element)treeEnum.nextElement();
                    if( (current != null) && (current.getType() == Element.PCDATA || current.getType() == Element.CDATA) )
                        return current.getText();
                        
                }
            }
            
            return "";
    }


    /**
     * Parse a subtree and return the value of the first element 
     * 
     * @param value     root of the subtree
     * @return          the value of the first element
     */
    protected String getPropValue( Element value )
    {
        if( GlobalData.getGlobalData().getDebugResponse() )
        {
            System.err.println( "DeltaVResponseInterpreter::getPropValue" );
        }
    
        TreeEnumeration treeEnum = new TreeEnumeration( value );
        while(treeEnum.hasMoreElements() )
        {
            Element current = (Element)treeEnum.nextElement();
            if( (current != null) && (current.getType() == Element.PCDATA || current.getType() == Element.CDATA) )
                return current.getText();
        }
        return "";
    }


    /**
     * Parse the response to an OPTIONS request
     */
    public void parseOptions()
    {
        if( GlobalData.getGlobalData().getDebugResponse() )
        {
            System.err.println( "DeltaVResponseInterpreter::parseOptions" );
        }

        try
        {
            String davheader = res.getHeader( "DAV" );
            if( davheader == null )
            {
                // no DeltaV support
                GlobalData.getGlobalData().errorMsg("DeltaV Interpreter:\n\nThe server does not support DAV\nat Resource " + Resource + ".");
                return;
            }
            boolean deltaVBase = false;
            boolean reports = false;
            boolean activityFound = false;
            if( davheader.indexOf("version-control") >= 0 )
            {
                deltaVBase = true;
                if( davheader.indexOf("report") >= 0 )
                    reports = true;
                if( davheader.indexOf("activity") >= 0 )
                    activityFound = true;
            }
            String full;
            if (Port == 0 || Port == WebDAVRequestGenerator.DEFAULT_PORT)
                full = HostName;
            else
                full = HostName +":" + Port;
            full += Resource;
            deltaV.put( full, Boolean.valueOf(deltaVBase) );
            deltaVReports.put( full, Boolean.valueOf(reports) );
            deltaVActivity.put( full, Boolean.valueOf(activityFound) );
            
            if( activityFound )
            {
                byte[] body = null;
                body = res.getData();
                if( body == null )
                {
                    if( extendedCode == WebDAVResponseEvent.URIBOX )
                    {
                        // we got here from entering a URI, we only want to get the activity
                        // name in this case
                        String str = HostName;
                        if (Port > 0)
                            str += ":" + Port;
                        str += Resource;
                        if( ((DeltaVRequestGenerator)generator).GenerateOptions( str, true ) )
                        {
                            generator.execute();
                            return;
                        }
                    }
                }
                else
                {
                    parseOptionsXML();
                }
            }
        }
        catch (Exception e)
        {
            GlobalData.getGlobalData().errorMsg("DeltaV Interpreter:\n\nError encountered \nwhile parsing OPTIONS Response:\n" + e);
            stream = null;
            return;
        }

        if( extendedCode == WebDAVResponseEvent.URIBOX )
        {
            // we got here from entering a URI, so now we need to do a PROPFIND
            generator.DoPropFind( Resource, false );
        }
    }


    /**
     * Parse the XML tree for an options-response entry
     *
     * @see RFC 3253, section 13.7
     */
    public void parseOptionsXML()
    {
        if( GlobalData.getGlobalData().getDebugResponse() )
        {
            System.err.println( "DeltaVResponseInterpreter::parseOptionsActivity" );
        }
        Vector nodesChildren = new Vector();
        String ResourceName = getResource();

        byte[] body = null;
        Document xml_doc = null;

        try
        {
            body = res.getData();
            stream = body;
        
            if (body == null)
            {
                GlobalData.getGlobalData().errorMsg("DeltaV Interpreter:\n\nMissing XML body in\nOPTIONS response.");
                return;
            }
        
            ByteArrayInputStream byte_in = new ByteArrayInputStream(body);
        
            xml_doc = new Document();
            xml_doc.load( byte_in );
        }
        catch (Exception e)
        {
            GlobalData.getGlobalData().errorMsg("DeltaV Interpreter:\n\nError encountered \nwhile parsing OPTIONS Response.\n" + e);
            stream = null;
            return;
        }

        printXML( body );
        
        // skip everything up to <options-response> tag
        String[] token = new String[1];
        token[0] = new String( DeltaVXML.ELEM_OPTIONS_RESPONSE );
        Element rootElem = skipElements( xml_doc, token );
        int count = 0;
        String activityHref = null;
        
        if( rootElem != null )
        {
            TreeEnumeration enumTree =  new TreeEnumeration( rootElem );
            while( enumTree.hasMoreElements() )
            {
                Element current = (Element)enumTree.nextElement();
                Name currentTag = current.getTagName();
                if( currentTag != null )
                {
                    // handle <activity-collection-set> tag
                    if( currentTag.getName().equals( DeltaVXML.ELEM_ACTIVITY_COLLECTION_SET ) )
                    {
                        // tag encloses <href>
                        activityHref = getHref( current );
                        String full;
                        if (Port == 0 || Port == WebDAVRequestGenerator.DEFAULT_PORT)
                            full = HostName;
                        else
                            full = HostName +":" + Port;
                        full += Resource;
                        activityCollection.put( full, activityHref );
                    }
                }
            }
        }
        ((DeltaVRequestGenerator)generator).SetActivityCollection( activityCollection );
    }


    /**
     * Process the response to a MKACTIVITY request  
     */
    public void parseMkActivity()
    {
        try
        {
            int code = res.getStatusCode();
            if ( code >= 200 && code < 300 )
            {
                String host;
                if (Port == 0 || Port == WebDAVRequestGenerator.DEFAULT_PORT)
                    host = HostName;
                else
                    host = HostName +":" + Port;
                activity.put( host, Resource );
                ((DeltaVRequestGenerator)generator).SetActivity( activity );

                fireMkActivityEvent( Resource, code );
            }
            else
                fireMkActivityEvent( res.getReasonLine(), code );
        }
        catch(Exception e)
        {
        }
    }

    
    /**
     * Process the response to a MERGE request  
     */
    public void parseMerge()
    {
        try
        {
            int code = res.getStatusCode();
            if ( code >= 200 && code < 300 )
            {
                String host;
                if (Port == 0 || Port == WebDAVRequestGenerator.DEFAULT_PORT)
                    host = HostName;
                else
                    host = HostName +":" + Port;
                activity.put( host, Resource );
                ((DeltaVRequestGenerator)generator).SetActivity( activity );

                fireMergeEvent( Resource, code );
            }
            else
                fireMergeEvent( res.getReasonLine(), code );
        }
        catch(Exception e)
        {
        }
    }

    
    /**
     * Inform listeners of an insertion event
     * 
     * @param str       info of the event
     */
    protected void fireInsertionEvent( String str )
    {
        boolean deltaVBase = false;
        boolean reports = false;
        
        Enumeration enum = deltaV.keys();
        while( enum.hasMoreElements() )
        {
            String base = (String)enum.nextElement();
            if( Resource.startsWith( base ) )
            {
                deltaVBase = ((Boolean)deltaV.get( base )).booleanValue();
                reports = ((Boolean)deltaVReports.get( base )).booleanValue();
                break;
            }
        }
        if( !deltaVBase && !reports )
        {
            super.fireInsertionEvent( str );
            return;
        }

        Vector ls;
        synchronized( this )
        {
            ls = (Vector)listeners.clone();
        }
        ActionEvent e = new ActionEvent( this, 0, str );
        for( int i=0; i<ls.size(); i++ )
        {
            InsertionListener l = (InsertionListener)ls.elementAt(i);
            l.actionPerformed( e, deltaVBase );
        }
    }


    /**
     * Add a version control listener
     * 
     * @param l     listener to add
     */
    public synchronized void addVersionControlListener(ActionListener l)
    {
        versionControlListeners.addElement(l);  
    }


    /**
     * Add a checkout listener
     * 
     * @param l     listener to add
     */
    public synchronized void addCheckoutListener(ActionListener l)
    {
        checkoutListeners.addElement(l);    
    }  


    /**
     * Add an uncheckout listener
     * 
     * @param l     listener to add
     */
    public synchronized void addUnCheckoutListener(ActionListener l)
    {
        unCheckoutListeners.addElement(l);  
    }


    /**
     * Add a checkin listener
     * 
     * @param l     listener to add
     */
    public synchronized void addCheckinListener(ActionListener l)
    {
        checkinListeners.addElement(l); 
    }   


    /**
     * Add a mkactivity listener
     * 
     * @param l     listener to add
     */
    public synchronized void addMkActivityListener(ActionListener l)
    {
        mkActivityListeners.addElement(l); 
    }   


    /**
     * Add a merge listener
     * 
     * @param l     listener to add
     */
    public synchronized void addMergeListener(ActionListener l)
    {
        mergeListeners.addElement(l); 
    }   


    /**
     * Remove a version control listener
     * 
     * @param l     listener to remove
     */
    public synchronized void removeVersionControlListener(ActionListener l)
    {
        versionControlListeners.removeElement(l);   
    }


    /**
     * Remove a checkout listener
     * 
     * @param l     listener to remove
     */
    public synchronized void removeCheckoutListener(ActionListener l)
    {
        checkoutListeners.removeElement(l); 
    }  


    /**
     * Remove an uncheckout listener
     * 
     * @param l     listener to remove
     */
    public synchronized void removeUnCheckoutListener(ActionListener l)
    {
        unCheckoutListeners.removeElement(l);   
    }


    /**
     * Remove a checkin listener
     * 
     * @param l     listener to remove
     */
    public synchronized void removeCheckinListener(ActionListener l)
    {
        checkinListeners.removeElement(l);  
    }   


    /**
     * Remove a mkactivity listener
     * 
     * @param l     listener to remove
     */
    public synchronized void removeMkActivityListener(ActionListener l)
    {
        mkActivityListeners.removeElement(l);  
    }   


    /**
     * Remove a merge listener
     * 
     * @param l     listener to remove
     */
    public synchronized void removeMergeListener(ActionListener l)
    {
        mergeListeners.removeElement(l);  
    }   


    /**
     * Inform listeners of a version control event
     * 
     * @param str       info of the event
     */
    protected void fireVersionControlEvent( String str, int code )
    {
        Vector ls;

        synchronized (this)
        {
            ls = (Vector) versionControlListeners.clone();
        }
        ActionEvent e = new ActionEvent( this, code, str );
        for( int i=0; i<ls.size(); i++ )
        {
            ActionListener l = (ActionListener) ls.elementAt(i);
            l.actionPerformed(e);
        }
    }


    /**
     * Inform listeners of a checkout event
     * 
     * @param str       info of the event
     */
    protected void fireCheckoutEvent( String str, int code )
    {
        Vector ls;

        synchronized (this)
        {
            ls = (Vector) checkoutListeners.clone();
        }
        ActionEvent e = new ActionEvent( this, code, str );
        for (int i=0;i<ls.size();i++)
        {
            ActionListener l = (ActionListener) ls.elementAt(i);
            l.actionPerformed(e);
        }
    }
    

    /**
     * Inform listeners of an uncheckout event
     * 
     * @param str       info of the event
     */
    protected void fireUnCheckoutEvent( String str, int code )
    {
        Vector ls;

        synchronized (this)
        {
            ls = (Vector) unCheckoutListeners.clone();
        }
        ActionEvent e = new ActionEvent( this, code, str );
        for (int i=0;i<ls.size();i++)
        {
            ActionListener l = (ActionListener) ls.elementAt(i);
            l.actionPerformed(e);
        }       
    }
    

    /**
     * Inform listeners of a checkin event
     * 
     * @param str       info of the event
     */
    protected void fireCheckinEvent( String str, int code )
    {
        Vector ls;

        synchronized (this)
        {
            ls = (Vector) checkinListeners.clone();
        }
        ActionEvent e = new ActionEvent( this, code, str );
        for (int i=0;i<ls.size();i++)
        {
            ActionListener l = (ActionListener) ls.elementAt(i);
            l.actionPerformed(e);
        }
    }


    /**
     * Inform listeners of a mkactivity event
     * 
     * @param str       info of the event
     */
    protected void fireMkActivityEvent( String str, int code )
    {
        Vector ls;

        synchronized (this)
        {
            ls = (Vector) mkActivityListeners.clone();
        }
        ActionEvent e = new ActionEvent( this, code, str );
        for (int i=0;i<ls.size();i++)
        {
            ActionListener l = (ActionListener) ls.elementAt(i);
            l.actionPerformed(e);
        }
    }


    /**
     * Inform listeners of a merge event
     * 
     * @param str       info of the event
     */
    protected void fireMergeEvent( String str, int code )
    {
        Vector ls;

        synchronized (this)
        {
            ls = (Vector) mergeListeners.clone();
        }
        ActionEvent e = new ActionEvent( this, code, str );
        for (int i=0;i<ls.size();i++)
        {
            ActionListener l = (ActionListener) ls.elementAt(i);
            l.actionPerformed(e);
        }
    }


    /**
     * Strip a resource to just the resource name
     * 
     * @param res       Resource to strip
     * 
     * @return          the stripped resource
     */
    protected String truncateResource( String res )
    {
        if( GlobalData.getGlobalData().getDebugTreeNode() )
        {
            System.err.println( "DeltaVResponseInterpreter::truncateResource" );
        }
    
        int pos = res.indexOf(GlobalData.WebDAVPrefixSSL);
        if (pos >= 0)
            res = res.substring(GlobalData.WebDAVPrefixSSL.length());
        pos = res.indexOf(GlobalData.WebDAVPrefix);
        if (pos >= 0)
            res = res.substring(GlobalData.WebDAVPrefix.length());
        pos = res.indexOf("/");
        if( pos >= 0 )
            res = res.substring(pos);
    
        if (res.endsWith("/"))
            res = res.substring(0, res.length() - 1);
        pos = res.lastIndexOf("/");
        if (pos >= 0)
            res = res.substring(pos);
        if ((res.startsWith("/")) && (res.length() > 1))
            res = res.substring(1);
        if (res.length() == 0)
            res = "/";
        return res;
    }
    

    /**
     * Strip the protocol from a resource
     * 
     * @param res       Resource to strip
     * @return          the stripped resource
     */
    protected String getFullResource(String res)
    {
        if( GlobalData.getGlobalData().getDebugTreeNode() )
        {
            System.err.println( "DeltaVResponseInterpreter::getFullResource" );
        }
    
        int pos = res.indexOf(GlobalData.WebDAVPrefixSSL);
        if (pos >= 0)
            res = res.substring(GlobalData.WebDAVPrefixSSL.length());
        else
        {
            pos = res.indexOf(GlobalData.WebDAVPrefix);
            if (pos >= 0)
                res = res.substring(GlobalData.WebDAVPrefix.length());
        }
        pos = res.indexOf("/");
        if( pos >= 0 )
            res = res.substring(pos);
        if (res.endsWith("/"))
            res = res.substring(0,res.length() - 1);
        if (res.length() == 0)
            res = "/";
        if ( (res.startsWith("/")) && (res.length() > 1) )
            res = res.substring(1);
        return res;
    }


    public boolean isDeltaV( String resource )
    {
        Enumeration enum = deltaV.keys();
        while( enum.hasMoreElements() )
        {
            String res = (String)enum.nextElement();
            if( resource.indexOf(res) >= 0 )
                return( ((Boolean)deltaV.get(res)).booleanValue() );
        }
        return false;
    }


    public boolean isDeltaVReports( String resource )
    {
        Enumeration enum = deltaVReports.keys();
        while( enum.hasMoreElements() )
        {
            String res = (String)enum.nextElement();
            if( resource.indexOf(res) >= 0 )
                return( ((Boolean)deltaVReports.get(res)).booleanValue() );
        }
        return false;
    }


    public boolean isDeltaVActivity( String resource )
    {
        Enumeration enum = deltaVActivity.keys();
        while( enum.hasMoreElements() )
        {
            String res = (String)enum.nextElement();
            if( resource.indexOf(res) >= 0 )
                return( ((Boolean)deltaVActivity.get(res)).booleanValue() );
        }
        return false;
    }


    protected static Vector versionControlListeners = new Vector();
    protected static Vector checkoutListeners = new Vector();
    protected static Vector unCheckoutListeners = new Vector();
    protected static Vector checkinListeners = new Vector();
    protected static Vector mkActivityListeners = new Vector();
    protected static Vector mergeListeners = new Vector();

    // hashtables keeping track of which base url supports DeltaV and/or
    // specifics of DeltaV
    protected Hashtable deltaV = new Hashtable();
    protected Hashtable deltaVReports = new Hashtable();
    protected Hashtable deltaVActivity = new Hashtable();
    
    // hashtables keeping track of open activities
    protected Hashtable activityCollection = new Hashtable();
    protected Hashtable activity = new Hashtable();
}
