/*
 * Copyright (c) 1998-2004 Regents of the University of California.
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
 * Title:       WebDAV Tree Node
 * Description: Implementation of the nodes for the navigation tree
 * Copyright:   Copyright (c) 1998-2004 Regents of the University of California. All rights reserved.
 * @author      Robert Emmery
 * @date        2 April 1998
 * @author      Yuzo Kanomata, Joachim Feise (dav-exp@ics.uci.edu)
 * @date        17 March 1999
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        23 May 2000
 * Changes:     Added check for CDATA to improve interoperability for Sharemation's server
 *              Changed the enumeration in parseResponse() to SiblingEnumeration to
 *              avoid parsing the wrong href tag (thanks to Michelle Harris for
 *              alerting us to this problem)
 *              Fixed string comparison in case of multiple <propstat> tags
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        12 January 2001
 * Changes:     Added support for https (SSL)
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        1 October 2001
 * Changes:     Change of package name
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        2 April 2002
 * Changes:     Updated for JDK 1.4
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        17 March 2003
 * Changes:     Integrated Brian Johnson's applet changes.
 *              Added better error reporting.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        23 September 2003
 * Changes:     Refactored code during DeltaV integration.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        08 February 2004
 * Changes:     Added Javadoc templates
 */

package edu.uci.ics.DAVExplorer;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Date;
import java.util.Vector;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.text.DateFormat;
import com.ms.xml.om.Element;
import com.ms.xml.om.Document;
import com.ms.xml.om.TreeEnumeration;
import com.ms.xml.util.Name;


/**
 * 
 */
public class WebDAVTreeNode extends DefaultMutableTreeNode
{
    protected static DeltaVRequestGenerator generator = new DeltaVRequestGenerator();
    protected static DeltaVResponseInterpreter interpreter = new DeltaVResponseInterpreter();

    protected boolean hasLoaded = false;
    protected final static String WebDAVRoot = "DAV Explorer";
    protected DataNode dataNode;
    private String userAgent;

    protected boolean childrenLoaded = false;
    protected boolean localLoad = false;


    /**
     * 
     */
    public static void reset()
    {
        generator = new DeltaVRequestGenerator();
        interpreter = new DeltaVResponseInterpreter();
    }


    /**
     * Constructor
     * @param o
     * @param ua
     */    
    public WebDAVTreeNode( Object o, String ua )
    {
        super(o);
        userAgent = ua;
        generator.setUserAgent( ua );
        hasLoaded = true;
    }


    /**
     * Constructor
     * @param o
     * @param isRoot
     * @param ua
     */    
    public WebDAVTreeNode( Object o, boolean isRoot, String ua )
    {
        super(o);
        userAgent = ua;
        generator.setUserAgent( ua );
        hasLoaded = true;
        childrenLoaded = true;
        dataNode = new DataNode( true, false, null, o.toString(), "DAV Root Node", "", 0, "", null );
    }


    /**
     * 
     * @param ua
     */    
    public void setUserAgent( String ua )
    {
        userAgent = ua;
        generator.setUserAgent( userAgent );
    }


    /**
     * 
     * @return
     */
    public DataNode getDataNode()
    {
        return dataNode;
    }


    /**
     * 
     * @param newNode
     */
    public void setDataNode( DataNode newNode )
    {
        dataNode = newNode;
    }


    /**
     * 
     * 
     * @return
     */
    public boolean isLeaf()
    {
        return false;
    }


    /**
     * 
     * @return
     */
    public boolean hasLoadedChildren()
    {
        return childrenLoaded;
    }


    /**
     * 
     * @param b
     */
    public void setHasLoadedChildren( boolean b )
    {
        childrenLoaded = b;
    }


    /**
     * 
     */
    public void removeChildren()
    {
        if( GlobalData.getGlobalData().getDebugTreeNode() )
        {
            System.err.println( "WebDAVTreeNode::removeChildren" );
        }

        int count = super.getChildCount();
        for( int c=0; c<count; c++ )
        {
            remove(0);
        }

        dataNode = null;
    }


    /**
     * 
     * 
     * @return
     */
    public int getChildCount()
    {
        return super.getChildCount();
    }


    /**
     * 
     * @param byte_xml
     */    
    protected void loadRemote( byte[] byte_xml )
    {
        if( GlobalData.getGlobalData().getDebugTreeNode() )
        {
            System.err.println( "WebDAVTreeNode::loadRemote" );
        }

        Vector nodesChildren = new Vector();
        Document xml_doc = null;
        String ResourceName = interpreter.getResource();

        if( (ResourceName.startsWith("/")) && (ResourceName.length() > 1) )
            ResourceName = ResourceName.substring(1);

        try
        {
            ByteArrayInputStream byte_in = new ByteArrayInputStream( byte_xml );
            xml_doc = new Document();
            xml_doc.load( byte_in );
        }
        catch( Exception e )
        {
            System.out.println("Exception: loadRemote: " + e );
            interpreter.clearStream();
            return;
        }

		dataNode = null;	// we are building a new subtree
        String[] token = new String[1];
        token[0] = new String( WebDAVXML.ELEM_MULTISTATUS );

        Element rootElem = interpreter.skipElements( xml_doc, token );
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
                        dataNode = interpreter.parseResponse( current, ResourceName, nodesChildren, dataNode, userAgent, this );
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
    }


    /**
     * 
     * @param name
     * @param full_path
     */
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
                // Note: f.list() returns null on JDK 1.4.0
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
                    DataNode newNode = new DataNode( isDir, false, null, fileList[i],
                                                     "Local File", "", aFile.length(),
                                                     DateFormat.getDateTimeInstance().format(newDate),
                                                     null);

                    if( isDir )
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
            catch( Exception e )
            {
                System.out.println(e);
            }
            Date fileDate = new Date(f.lastModified());
            dataNode = new DataNode( true, false, null, name, "Local File", "", f.length(),
                                     DateFormat.getDateTimeInstance().format(fileDate),
                                     nodesChildren );
        }
        else {
            hasLoaded = false;
            dataNode = null;
            return;
        }
        hasLoaded = true;
    }


    /**
     * This finishes the Load Children when a call is made to a DAV server
     */
    public void finishLoadChildren()
    {
        byte[] byte_xml = interpreter.getXML();
        if( byte_xml != null )
        {
            loadRemote(byte_xml);
        }
        interpreter.ResetRefresh();

        childrenLoaded = true;
    }


    /**
     * 
     * @param select
     */
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
        if( name.startsWith(GlobalData.WebDAVPrefix) || name.startsWith(GlobalData.WebDAVPrefixSSL) )
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
                    generator.setExtendedInfo( WebDAVResponseEvent.SELECT, null );
                }
                else
                {
                    generator.setExtendedInfo( WebDAVResponseEvent.INDEX, null );
                }

                String pathToResource = name;
                for (int i=2; i < full_path.length; i++)
                {
                    pathToResource = pathToResource + "/" + full_path[i].toString();
                }
                pathToResource = pathToResource + "/";

                generator.setResource(pathToResource, null);

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
                    if( generator.GeneratePropFindForNode( pathToResource, "allprop", "one", null, null, true, this ) )
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
                generator.setExtendedInfo( WebDAVResponseEvent.INDEX, null );

                String pathToResource = name;
                for (int i=2; i < full_path.length; i++)
                {
                    pathToResource = pathToResource + "/" + full_path[i].toString();
                }
                pathToResource = pathToResource + "/";

                // 1999-June-08, Joachim Feise (dav-exp@ics.uci.edu):
                // workaround for IBM's DAV4J, which does not handle propfind properly
                // with the prop tag. To use the workaround, run DAV Explorer with
                // 'java -jar -Dpropfind=allprop DAVExplorer.jar'
                // Note that this prevents the detection of DeltaV information, since
                // RFC 3253 states in section 3.11 that "A DAV:allprop PROPFIND request
                // SHOULD NOT return any of the properties defined by this document."
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


    /**
     * 
     * @return
     */
    public boolean isLocalLoad()
    {
        return localLoad;
    }


    /**
     * 
     * @return
     */
    public boolean isDeltaV()
    {
        return getDeltaV();
    }
    

    /**
     * 
     * @return
     */
    public boolean getDeltaV()
    {
        if( !(dataNode instanceof DeltaVDataNode ) )
            return false;
        return ((DeltaVDataNode)dataNode).getDeltaV();
    }
    

    /**
     * 
     * @param deltaV
     */
    public void setDeltaV( boolean deltaV )
    {
        if( !(dataNode instanceof DeltaVDataNode ) )
            return;
        ((DeltaVDataNode)dataNode).setDeltaV( deltaV );
    }
    

    /**
     * 
     * @return
     */
    public boolean getDeltaVReports()
    {
        if( !(dataNode instanceof DeltaVDataNode ) )
            return false;
        return ((DeltaVDataNode)dataNode).getDeltaVReports();
    }


    /**
     * 
     * @param reports
     */
    public void setDeltaVReports( boolean reports )
    {
        if( !(dataNode instanceof DeltaVDataNode ) )
            return;
        ((DeltaVDataNode)dataNode).setDeltaVReports( reports );
    }
}
