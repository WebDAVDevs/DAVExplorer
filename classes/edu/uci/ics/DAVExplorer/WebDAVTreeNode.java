/*
 * Copyright (c) 1999 Regents of the University of California.
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

// This is where we handle tree loading.
// Each time a folder is double clicked, getChildCount()
// will be called. This will in turn either form PROPFIND
// or itterate a local directory.
//
//  Version: 0.3
//  Author:  Robert Emmery
//  Date:    4/2/98
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
// Change List:
//
// Version: 0.61
// Changes by: Joe Feise
// Date: 5/23/2000
// Change List:
//   Added check for CDATA to improve interoperability for Sharemation's server
//   Changed the enumeration in parseResponse() to SiblingEnumeration to
//      avoid parsing the wrong href tag (thanks to Michelle Harris for
//      alerting us to this problem)
//   Fixed string comparison in case of multiple <propstat> tags

package DAVExplorer;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;
import java.io.*;
import java.text.DateFormat;

import com.ms.xml.om.*;
import com.ms.xml.parser.*;
import com.ms.xml.util.*;

public class WebDAVTreeNode extends DefaultMutableTreeNode
{
    protected boolean hasLoaded = false;
    protected final static String WebDAVPrefix = "http://";
    protected final static String HTTPPrefix = "http://";
    protected final static String WebDAVRoot = "DAV Explorer";
    protected DataNode dataNode;
    protected static WebDAVRequestGenerator generator = new WebDAVRequestGenerator();
    protected static WebDAVResponseInterpreter interpreter = new WebDAVResponseInterpreter();
    private String userAgent;

    protected boolean childrenLoaded = false;
    protected boolean localLoad = false;

    public WebDAVTreeNode (Object o, String ua )
    {
        super(o);
        userAgent = ua;
        generator.setUserAgent( ua );
        hasLoaded = true;
    }

    public WebDAVTreeNode (Object o, boolean isRoot, String ua )
    {
        super(o);
        userAgent = ua;
        generator.setUserAgent( ua );
        hasLoaded = true;
        childrenLoaded = true;
        dataNode = new DataNode(true,false,null, o.toString(),"DAV Root Node","",0,"",null);
    }

    public void setUserAgent( String ua )
    {
        userAgent = ua;
        generator.setUserAgent( userAgent );
    }

    public DataNode getDataNode()
    {
        return dataNode;
    }

    public void setDataNode(DataNode newNode)
    {
        dataNode = newNode;
    }

    public boolean isLeaf()
    {
        return false;
    }


// Yuzo
    public boolean hasLoadedChildren()
    {
        return childrenLoaded;
    }

    public void setHasLoadedChildren( boolean b)
    {
        childrenLoaded = b;
    }

// Yuzo Note They did this function wrong
    public void removeChildren()
    {
        if( GlobalData.getGlobalData().getDebugTreeNode() )
        {
            System.err.println( "WebDAVTreeNode::removeChildren" );
        }

        int count = super.getChildCount();
        for (int c=0;c<count;c++)
        {
            remove(0);
        }

    // They forgot this
    dataNode = null;

    }

    public int getChildCount()
    {
        return super.getChildCount();
    }

    protected void loadRemote(byte[] byte_xml)
    {
        if( GlobalData.getGlobalData().getDebugTreeNode() )
        {
            System.err.println( "WebDAVTreeNode::loadRemote" );
        }

        Vector nodesChildren = new Vector();
        Document xml_doc = null;
        Element multiElem = null;
        Element respElem = null;
        boolean found = false;
        String ResourceName = interpreter.getResource();

        if ((ResourceName.startsWith("/")) && (ResourceName.length() > 1) )
            ResourceName = ResourceName.substring(1);

        try
        {
            ByteArrayInputStream byte_in = new ByteArrayInputStream(byte_xml);
            EscapeInputStream iStream = new EscapeInputStream( byte_in, true );
            XMLInputStream xml_in = new XMLInputStream( iStream );
            xml_doc = new Document();
            xml_doc.load(xml_in);
        }
        catch (Exception inEx)
        {
            System.out.println("EXCEPTION:loadRemote, byte_array empty");
	    System.out.println(inEx);
	    System.out.println(inEx.getMessage());
            //dataNode = null;
            //hasLoaded = false;
            interpreter.clearStream();
            return;
        }

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
                    if( currentTag.getName().equals( WebDAVXML.ELEM_RESPONSE ) )
                    {
                        parseResponse( current, ResourceName, nodesChildren );
                    }
                }
            }
        }
        else
        {
            dataNode = null;
            hasLoaded = false;
        }

        interpreter.clearStream();
        interpreter.ResetRefresh();
        if (dataNode != null)
        {
            dataNode.setSubNodes(nodesChildren);
            hasLoaded = true;
        }
//        hasLoaded = true;
    }


    protected void parseResponse(Element respElem, String ResourceName, Vector nodesChildren)
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
            return;
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
                            resName = new String(truncateResource(token.getText()));
                            fullName = new String(getFullResource(token.getText()));
                        }
                    }
                }
                else if( currentTag.getName().equals( WebDAVXML.ELEM_PROPSTAT ) )
                {
                    if( resName != "" )
                    {
                        DataNode curnode = parseProps( current, ResourceName, resName );
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
                            if( curnode.getDate().length() != 0 )
                                node.setDate( curnode.getDate() );          // overwrite any old value
                        }
                    }
                }
            }
        }

        // save data node
        if( node != null )
        {
            String ResourceNameStrp = "";
            if (ResourceName.endsWith("/"))
                ResourceNameStrp = ResourceName.substring(0,ResourceName.length() - 1);
            if ( (fullName.equals(ResourceName)) || (fullName.equals(ResourceNameStrp)) )
            {
                // this is the container
                int pathLen = getPath().length;
                String hostName = resName;
                if (pathLen == 2)
                    hostName = WebDAVPrefix + interpreter.getHost() + "/" + ResourceName;
                // update node values
                dataNode = new DataNode( node.isCollection(), node.isLocked(), node.getLockToken(),
                                         hostName, node.getDisplay(), node.getType(), node.getSize(),
                                         node.getDate(), null );
            }
            else
            {
                if( node.isCollection() )
                {
                    WebDAVTreeNode childNode = new WebDAVTreeNode( resName, userAgent );
                    childNode.setDataNode(node);
                    insert(childNode,0);
                }
                else
                {
                    nodesChildren.addElement(node);
                }
            }
        }
    }


    protected DataNode parseProps( Element propElem, String ResourceName, String resName )
    {
        if( GlobalData.getGlobalData().getDebugTreeNode() )
        {
            System.err.println( "WebDAVTreeNode::parseProps" );
        }

        boolean isColl = false;
        boolean isLocked = false;
        String lockToken = null;
        String resDisplay = "";
        String resType = "";
        String resLength = "0";
        String resDate = "";

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
                    if( currentTag.getName().equals( WebDAVProp.PROP_DISPLAYNAME ) )
                    {
                        resDisplay = getDisplayName( current );
                    }
                    else if( currentTag.getName().equals( WebDAVProp.PROP_LOCKDISCOVERY ) )
                    {
                        lockToken = lockDiscovery( current );
                        if( lockToken != null )
                            isLocked = true;
                    }
                    else if( currentTag.getName().equals( WebDAVProp.PROP_RESOURCETYPE ) )
                    {
                        isColl = getResourceType( current );
                    }
                    else if( currentTag.getName().equals( WebDAVProp.PROP_GETCONTENTTYPE ) )
                    {
                        resType = getContentType( current );
                    }
                    else if( currentTag.getName().equals( WebDAVProp.PROP_GETCONTENTLENGTH ) )
                    {
                        resLength = getContentLength( current );
                    }
                    else if( currentTag.getName().equals( WebDAVProp.PROP_GETLASTMODIFIED ) )
                    {
                        resDate = getLastModified( current );
                    }
                }
            }
        }

        // This is where we fill out the data node
        long size = 0;
        try
        {
            size = Long.parseLong(resLength);
        }
        catch (Exception parseEx)
        {
            System.out.println(parseEx);
            return null;
        }
        DataNode newNode = new DataNode(isColl, isLocked, lockToken, resName,
                                        resDisplay, resType, size, resDate,null);
        return newNode;
    }


    protected void loadLocal(String name, Object[] full_path)
    {
        if( GlobalData.getGlobalData().getDebugTreeNode() )
        {
            System.err.println( "WebDAVTreeNode::loadLocal" );
        }

        String fileName = name;
        for (int i=2;i<full_path.length;i++)
        {
            if( !fileName.endsWith( String.valueOf(File.separatorChar) ) )
                fileName += File.separator;
            fileName += full_path[i];
        }
        name = full_path[full_path.length - 1].toString();
        File f = new File(fileName);
        if ((f != null) && (f.exists()) && (f.isDirectory()) )
        {
            Vector nodesChildren = new Vector();
            // Yuzo bug fix for empty sub dir
            try
            {
                String[] fileList = f.list();
                int len = fileList.length;
                for (int i=0;i<len;i++)
                {
                    String newFile = fileName;
                    if( !fileName.endsWith( String.valueOf(File.separatorChar) ) )
                        newFile += File.separatorChar;
                    newFile += fileList[i];
                    File aFile = new File( newFile );
                    boolean isDir = aFile.isDirectory();
                    Date newDate = new Date(aFile.lastModified());
                    DataNode newNode = new DataNode(isDir,
                                            false,
                                            null,
                                            fileList[i],
                                            "Local File",
                                            "",
                                            aFile.length(),
                                            DateFormat.getDateTimeInstance().format( newDate ), null);

                    if (isDir)
                    {
                        WebDAVTreeNode childNode = new WebDAVTreeNode( newNode.getName(), userAgent );
                        childNode.setDataNode(newNode);
                        insert(childNode,0);
                    }
                    else
                    {
                        nodesChildren.addElement(newNode);
                    }
                }
            }
            catch( Exception e)
            {
                System.out.println(e);
            }
            Date fileDate = new Date(f.lastModified());
            dataNode = new DataNode(true, false, null, name, "Local File", "",
                                    f.length(), DateFormat.getDateTimeInstance().format( fileDate ), nodesChildren);
        }
        else {
            hasLoaded = false;
            dataNode = null;
            return;
        }
        hasLoaded = true;
    }


    // This finishes the Load Children when a call is made to a
    // DAV server
    public void finishLoadChildren()
    {
        byte[] byte_xml = interpreter.getXML();
        loadRemote(byte_xml);
        interpreter.ResetRefresh();

        childrenLoaded = true;
    }

    public void loadChildren( boolean select )
    {
        if( GlobalData.getGlobalData().getDebugTreeNode() )
        {
            System.err.println( "WebDAVTreeNode::loadChildren" );
        }

        Object[] full_path = getPath();

        if( full_path == null || full_path.length <= 1 )
            return;

        String name = full_path[1].toString();
        if (name.startsWith(WebDAVPrefix))
        {
        localLoad = false;

            byte[] byte_xml = interpreter.getXML();
            if (byte_xml == null)
            {
                hasLoaded = false;
                dataNode = null;
                interpreter.ResetRefresh();
                if (select)
                {
                    generator.setExtraInfo("select");
                }
                else
                {
                    generator.setExtraInfo("index");
                }

                String pathToResource = name;
                for (int i=2; i < full_path.length; i++)
                {
                    pathToResource = pathToResource + "/" + full_path[i].toString();
                }
                pathToResource = pathToResource + "/";

                generator.setResource(pathToResource, null);

                // 1999-June-08, Joachim Feise (jfeise@ics.uci.edu):
                // workaround for IBM's DAV4J, which does not handle propfind properly
                // with the prop tag. To use the workaround, run DAV Explorer with
                // 'java -jar -Dpropfind=allprop DAVExplorer.jar'
                String doAllProp = System.getProperty( "propfind" );
                if( (doAllProp != null) && doAllProp.equalsIgnoreCase("allprop") )
                {
                    if( generator.GeneratePropFindForNode( pathToResource, "allprop", "one", null, null, true, this ) )
                    {
                        generator.execute();
	            }
                }
                else
	        {
                    String[] props = new String[6];
                    props[0] = "displayname";
                    props[1] = "resourcetype";
                    props[2] = "getcontenttype";
                    props[3] = "getcontentlength";
                    props[4] = "getlastmodified";
                    props[5] = "lockdiscovery";
                    if( generator.GeneratePropFindForNode( pathToResource, "prop", "one", props, null, true, this ) )
                    {
                        generator.execute();
	            }
		}
                return;
            }
            else
            {
                // This is that case of the Select/Expand being called to a new DAV Server.
                // The buffer should have data in it via the response.
                // This should change in the future, as processing here is
                // unsafe -- the thread that gets the buffer may not be finished yet.

                interpreter.clearStream();  // Added to finish after lock/unlock

                hasLoaded = false;
                dataNode = null;
                interpreter.ResetRefresh();
                generator.setExtraInfo("index");

                String pathToResource = name;
                for (int i=2; i < full_path.length; i++)
                {
                    pathToResource = pathToResource + "/" + full_path[i].toString();
                }
                pathToResource = pathToResource + "/";

                // 1999-June-08, Joachim Feise (jfeise@ics.uci.edu):
                // workaround for IBM's DAV4J, which does not handle propfind properly
                // with the prop tag. To use the workaround, run DAV Explorer with
                // 'java -jar -Dpropfind=allprop DAVExplorer.jar'
                String doAllProp = System.getProperty( "propfind" );
                if( doAllProp != null )
                {
                    if( doAllProp.equalsIgnoreCase("allprop") )
		    {
                        if( generator.GeneratePropFindForNode( pathToResource, "allprop", "one", null, null, true, this ) )
                        {
                            generator.execute();
	                }
                    }
                    else
		    {
                        String[] props = new String[6];
                        props[0] = "displayname";
                        props[1] = "resourcetype";
                        props[2] = "getcontenttype";
                        props[3] = "getcontentlength";
                        props[4] = "getlastmodified";
                        props[5] = "lockdiscovery";
                        if( generator.GeneratePropFindForNode( pathToResource, "prop", "one", props, null, true, this ) )
                        {
                            generator.execute();
	                }
		    }
		}
            }
        }
        else
        {
        localLoad = true;
            loadLocal(name,full_path);
        }
    }

    public boolean isLocalLoad(){
    return localLoad;
    }

    public String truncateResource(String res)
    {
        if( GlobalData.getGlobalData().getDebugTreeNode() )
        {
            System.err.println( "WebDAVTreeNode::truncateResource" );
        }

        int pos = res.indexOf(HTTPPrefix);
        if (pos >= 0)
            res = res.substring(HTTPPrefix.length());
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

        int pos = res.indexOf(HTTPPrefix);
        if (pos >= 0)
            res = res.substring(HTTPPrefix.length());
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


    private String getLockToken( Element locktoken )
    {
        if( GlobalData.getGlobalData().getDebugTreeNode() )
        {
            System.err.println( "WebDAVTreeNode::getLockToken" );
        }

        TreeEnumeration treeEnum = new TreeEnumeration( locktoken );
        while(treeEnum.hasMoreElements() )
        {
            Element current = (Element)treeEnum.nextElement();
            Name tag = current.getTagName();
            if( (tag!=null) && tag.getName().equals( WebDAVXML.ELEM_HREF ) )
            {
                Element token = (Element)treeEnum.nextElement();
                if( (token!=null) && (token.getType() == Element.PCDATA || token.getType() == Element.CDATA) )
                    return token.getText();
			}
        }
        return null;
    }


    private Element skipElements( Document xml_doc, String[] token )
    {
        Element rootElem = (Element)xml_doc.getRoot();
        return skipElements( rootElem, token );
    }

    private Element skipElements( Element rootElem, String[] token )
    {
        if( GlobalData.getGlobalData().getDebugTreeNode() )
        {
            System.err.println( "WebDAVTreeNode::skipElements" );
        }

        int index = 0;
        TreeEnumeration enumTree =  new TreeEnumeration( rootElem );
        while( enumTree.hasMoreElements() )
        {
            Element current = (Element)enumTree.nextElement();
            Name currentTag = current.getTagName();
            if( index >= 0 )
            {
                if( (currentTag != null) && (currentTag.getName().equals( token[index] )) )
                {
                    if( !currentTag.getName().equals( WebDAVXML.ELEM_HREF ) )
                    {
                        // we only care about the subtree from this point on
                        // NOTE: do not get the href subtree, since the href tree
                        // is a sibling to the tree we need
                        enumTree = new TreeEnumeration( current );
                    }
                    index++;
                }
                if( index >= token.length )
                    return current;
            }
        }
        return null;
    }

    private String getDisplayName( Element displayName )
    {
        if( GlobalData.getGlobalData().getDebugTreeNode() )
        {
            System.err.println( "WebDAVTreeNode::getDisplayName" );
        }

        TreeEnumeration treeEnum = new TreeEnumeration( displayName );
        while(treeEnum.hasMoreElements() )
        {
            Element current = (Element)treeEnum.nextElement();
            Name tag = current.getTagName();
            if( (tag != null) && tag.getName().equals( WebDAVProp.PROP_DISPLAYNAME ) )
            {
                Element token = (Element)treeEnum.nextElement();
                if( (token != null) && (token.getType() == Element.PCDATA || token.getType() == Element.CDATA) )
                    return token.getText();
            }
        }
        return "";
    }

    private String lockDiscovery( Element lockdiscovery )
    {
        if( GlobalData.getGlobalData().getDebugTreeNode() )
        {
            System.err.println( "WebDAVTreeNode::lockDiscovery" );
        }

        String[] token = new String[1];
        token[0] = new String( WebDAVXML.ELEM_ACTIVE_LOCK );
        int index = 0;
        TreeEnumeration enumTree =  new TreeEnumeration( lockdiscovery );
        while( enumTree.hasMoreElements() )
        {
            Element current = (Element)enumTree.nextElement();
            Name currentTag = current.getTagName();
            if( index >= 0 )
            {
                if( (currentTag != null) && (currentTag.getName().equals( token[index] )) )
                {
                    // we only care about the subtree from this point on
                    enumTree = new TreeEnumeration( current );
                    index++;
                }
                if( index >= token.length )
                    index = -1;
            }
            else if( currentTag != null )
            {
                if( currentTag.getName().equals( WebDAVXML.ELEM_LOCK_TOKEN ) )
                {
                    return getLockToken( current );
                }
            }
        }
        return null;
    }

    private boolean getResourceType( Element resourcetype )
    {
        if( GlobalData.getGlobalData().getDebugTreeNode() )
        {
            System.err.println( "WebDAVTreeNode::getResourceType" );
        }

        TreeEnumeration treeEnum = new TreeEnumeration( resourcetype );
        while(treeEnum.hasMoreElements() )
        {
            Element current = (Element)treeEnum.nextElement();
            Name tag = current.getTagName();
            if( (tag != null) && tag.getName().equals( WebDAVXML.ELEM_COLLECTION ) )
                return true;
        }
        return false;
    }

    private String getContentType( Element contenttype )
    {
        if( GlobalData.getGlobalData().getDebugTreeNode() )
        {
            System.err.println( "WebDAVTreeNode::getContentType" );
        }

        TreeEnumeration treeEnum = new TreeEnumeration( contenttype );
        while(treeEnum.hasMoreElements() )
        {
            Element token = (Element)treeEnum.nextElement();
            if( (token != null) && (token.getType() == Element.PCDATA || token.getType() == Element.CDATA) )
                return token.getText();
        }
        return "";
    }

    private String getContentLength( Element contentlength )
    {
        if( GlobalData.getGlobalData().getDebugTreeNode() )
        {
            System.err.println( "WebDAVTreeNode::getContentLength" );
        }

        TreeEnumeration treeEnum = new TreeEnumeration( contentlength );
        while(treeEnum.hasMoreElements() )
        {
            Element token = (Element)treeEnum.nextElement();
            if( (token != null) && (token.getType() == Element.PCDATA || token.getType() == Element.CDATA) )
                return token.getText();
        }
        return "0";
    }

    private String getLastModified( Element lastmodified )
    {
        if( GlobalData.getGlobalData().getDebugTreeNode() )
        {
            System.err.println( "WebDAVTreeNode::getLastModified" );
        }

        TreeEnumeration treeEnum = new TreeEnumeration( lastmodified );
        while(treeEnum.hasMoreElements() )
        {
            Element token = (Element)treeEnum.nextElement();
            if( (token != null) && (token.getType() == Element.PCDATA || token.getType() == Element.CDATA) )
                return token.getText();
        }
        return "";
    }
}
