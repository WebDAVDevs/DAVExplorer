/*
 * Copyright (c) 2003 Regents of the University of California.
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
 * Copyright:   Copyright (c) 2003 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        23 September 2003
 */
public class DeltaVResponseInterpreter extends WebDAVResponseInterpreter
{
    /**
     * 
     */
    public DeltaVResponseInterpreter()
    {
        super();
    }


    /**
     * @param rg
     */
    public DeltaVResponseInterpreter(WebDAVRequestGenerator rg)
    {
        super(rg);
    }


    public void handleResponse(WebDAVResponseEvent e)
        throws ResponseException
    {
        if( GlobalData.getGlobalData().getDebugResponse() )
        {
            System.err.println( "DeltaVResponseInterpreter::handleResponse" );
        }
    
        res = e.getResponse();
        Method = e.getMethodName();
        Extra = e.getExtraInfo();
        HostName = e.getHost();
        Port = e.getPort();

        // get the resource name, and unescape it
        // TODO: get encoding
        Resource = GlobalData.getGlobalData().unescape( e.getResource(), null, true );
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


    public void parseVersionControl()
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

    
    public void parseCheckout()
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

    
    public void parseUnCheckout()
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

    
    public void parseCheckin()
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


    public void parseReport()
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
                GlobalData.getGlobalData().errorMsg("DAV Interpreter:\n\nMissing XML body in\nPROPFIND response.");
                return;
            }
        
            ByteArrayInputStream byte_in = new ByteArrayInputStream(body);
        
            xml_doc = new Document();
            xml_doc.load( byte_in );
        }
        catch (Exception e)
        {
            GlobalData.getGlobalData().errorMsg("DAV Interpreter:\n\nError encountered \nwhile parsing REPORT Response.\n" + e);
            stream = null;
            return;
        }

        printXML( body );
        
        DataNode dataNode = null;
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
                    if( currentTag.getName().equals( WebDAVXML.ELEM_RESPONSE ) )
                    {
                        dataNode = parseResponse( current, ResourceName, nodesChildren );
                    }
                }
            }
        }

        if(Extra.equals("display"))
        {
            String host = HostName;
            if (Port != 0)
                host = HostName + ":" + Port;
            VersionInfoDialog dlg = new VersionInfoDialog( nodesChildren, ResourceName, host );
            dlg.addGetVersionListener( new GetVersionListener() );
            dlg.show();
                            
        }
    }


    class GetVersionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            if( e.getActionCommand() != null )
                generator.setResource( e.getActionCommand(), null );
            if( generator.GenerateGet("saveas") )
                generator.execute();
        }
    }



    public DataNode parseResponse( Element respElem, String resourceName, Vector nodesChildren )
    {
        return parseResponse( respElem, resourceName, nodesChildren, null, null, null );
    }


    public DataNode parseResponse( Element respElem, String resourceName, Vector nodesChildren, DataNode dataNode, String userAgent, DefaultMutableTreeNode treeNode )
    {
        if( GlobalData.getGlobalData().getDebugTreeNode() )
        {
            System.err.println( "WebDAVTreeNode::parseResponse" );
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
                            // TODO: get encoding
                            resName = new String( truncateResource(GlobalData.getGlobalData().unescape(token.getText(), null, true)) );
                            fullName = new String( getFullResource(GlobalData.getGlobalData().unescape(token.getText(), null, true)) );
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
    
    
    protected DataNode parseProps( Element propElem, String ResourceName, String resName )
    {
        if( GlobalData.getGlobalData().getDebugTreeNode() )
        {
            System.err.println( "WebDAVTreeNode::parseProps" );
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


    protected String getHref( Element href )
    {
            if( GlobalData.getGlobalData().getDebugResponse() )
            {
                System.err.println( "WebDAVResponseInterpreter::getHref" );
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


    protected String getPropValue( Element value )
    {
        if( GlobalData.getGlobalData().getDebugResponse() )
        {
            System.err.println( "DeltaVResponseInterpreter::getComment" );
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


    public void parseOptions()
    {
        if( GlobalData.getGlobalData().getDebugResponse() )
        {
            System.err.println( "WebDAVResponseInterpreter::parseOptions" );
        }

        try
        {
            String davheader = res.getHeader( "DAV" );
            if( davheader == null )
            {
                // no WebDAV support
                GlobalData.getGlobalData().errorMsg("DAV Interpreter:\n\nThe server does not support WebDAV\nat Resource " + Resource + ".");
                return;
            }
            deltaV = false;
            deltaVReports = false;
            if( davheader.indexOf("version-control") >= 0 )
            {
                deltaV = true;
                if( davheader.indexOf("report") >= 0 )
                    deltaVReports = true;
            }
        }
        catch (Exception e)
        {
            GlobalData.getGlobalData().errorMsg("DAV Interpreter:\n\nError encountered \nwhile parsing OPTIONS Response:\n" + e);
            stream = null;
            return;
        }

        if (Extra.equals("uribox"))
        {
            // we got here from entering a URI, so now we need to do a PROPFIND
            String str = HostName;
            if (Port > 0)
                str += ":" + Port;
            str += Resource;
            // 1999-June-08, Joachim Feise (dav-exp@ics.uci.edu):
            // workaround for IBM's DAV4J, which does not handle propfind properly
            // with the prop tag. To use the workaround, run DAV Explorer with
            // 'java -jar -Dpropfind=allprop DAVExplorer.jar'
            // Note that this prevents the detection of DeltaV information, since
            // RFC 3253 states in section 3.11 that "A DAV:allprop PROPFIND request
            // SHOULD NOT return any of the properties defined by this document."
            String doAllProp = System.getProperty( "propfind" );
            if( (doAllProp != null) && doAllProp.equalsIgnoreCase("allprop") )
            {
                if( generator.GeneratePropFind( str, "allprop", "one", null, null, false ) )
                {
                    generator.execute();
                }
            }
            else
            {
                String[] props;
                props = new String[9];
                props[0] = "displayname";
                props[1] = "resourcetype";
                props[2] = "getcontenttype";
                props[3] = "getcontentlength";
                props[4] = "getlastmodified";
                props[5] = "lockdiscovery";
                // DeltaV support
                props[6] = "checked-in";
                props[7] = "checked-out";
                props[8] = "version-name";

                if( generator.GeneratePropFind( str, "prop", "one", props, null, false ) )
                {
                    generator.execute();
                }
            }
        }
    }



    public void fireInsertionEvent( String str )
    {
        if( !deltaV && !deltaVReports )
            super.fireInsertionEvent( str );

        Vector ls;
        synchronized( this )
        {
            ls = (Vector)listeners.clone();
        }
        ActionEvent e = new ActionEvent( this, 0, str );
        for( int i=0; i<ls.size(); i++ )
        {
            InsertionListener l = (InsertionListener)ls.elementAt(i);
            l.actionPerformed( e, deltaV );
        }
    }


    public synchronized void addVersionControlListener(ActionListener l)
    {
        versionControlListeners.addElement(l);  
    }


    public synchronized void addCheckoutListener(ActionListener l)
    {
        checkoutListeners.addElement(l);    
    }  


    public synchronized void addUnCheckoutListener(ActionListener l)
    {
        unCheckoutListeners.addElement(l);  
    }


    public synchronized void addCheckinListener(ActionListener l)
    {
        checkinListeners.addElement(l); 
    }   


    public synchronized void removeVersionControlListener(ActionListener l)
    {
        versionControlListeners.removeElement(l);   
    }


    public synchronized void removeCheckoutListener(ActionListener l)
    {
        checkoutListeners.removeElement(l); 
    }  


    public synchronized void removeUnCheckoutListener(ActionListener l)
    {
        unCheckoutListeners.removeElement(l);   
    }


    public synchronized void removeCheckinListener(ActionListener l)
    {
        checkinListeners.removeElement(l);  
    }   


    public void fireVersionControlEvent( String str, int code )
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


    public void fireCheckoutEvent( String str, int code )
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
    

    public void fireUnCheckoutEvent( String str, int code )
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
    
    public void fireCheckinEvent( String str, int code )
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


    public String truncateResource(String res)
    {
        if( GlobalData.getGlobalData().getDebugTreeNode() )
        {
            System.err.println( "WebDAVTreeNode::truncateResource" );
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
    
    public String getFullResource(String res) {
        if( GlobalData.getGlobalData().getDebugTreeNode() )
        {
            System.err.println( "WebDAVTreeNode::getFullResource" );
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
            res = res.substring(0,res.length() - 1);
        if (res.length() == 0)
            res = "/";
        if ( (res.startsWith("/")) && (res.length() > 1) )
            res = res.substring(1);
        return res;
    }


    protected static Vector versionControlListeners = new Vector();
    protected static Vector checkoutListeners = new Vector();
    protected static Vector unCheckoutListeners = new Vector();
    protected static Vector checkinListeners = new Vector();
    
    protected boolean deltaV = false;
    protected boolean deltaVReports = false;
}
