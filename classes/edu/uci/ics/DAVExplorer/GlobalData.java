/*
 * Copyright (c) 1999-2003 Regents of the University of California.
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
 * Title:       GlobalData
 * Description: This singleton class defines various global data structures
 *              and functions useful everywhere
 * Copyright:   Copyright (c) 1999-2003 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        1999
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        9 January 2001
 * Changes:     Added support for https (SSL), moved reading of debug properties here
 * @date        29 May 2001
 * Changes:     Support for reading/writing configuration file
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
 * @date        7 April 2003
 * Changes:     Improved reading/writing of configuration entries. 
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        27 April 2003
 * Changes:     added support for default config entries.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        3 November 2003
 * Changes:     Added support for proxy server in applet settings (it always worked through
 *              the "Edit Proxy Info" menu entry.)
 */

package edu.uci.ics.DAVExplorer;

import java.awt.Cursor;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import java.util.StringTokenizer;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.File;
import java.io.IOException;


class GlobalData
{
    /** Debug variables */
    boolean debugAll      = false;
    boolean debugRequest  = debugAll | false;
    boolean debugResponse = debugAll | false;
    boolean debugTreeView = debugAll | false;
    boolean debugTreeNode = debugAll | false;
    boolean debugFileView = debugAll | false;

    /** SSL variable */
    boolean ssl = false;

    JFrame mainFrame = null;
    Cursor origCursor = null;
    private static final String fileName = "DAVExplorer.dat";
    private static final String tmpFileName = "DAVExplorer.tmp";
    private boolean isAppletMode = false;
    private boolean hideURIBox = false;
    private String[][] initialSites = new String[][] { {} };
    private String appletProxy;
    private boolean doAddStartDir = true;
    private URIBox uriBox;
    private WebDAVTreeView tree;


    private static GlobalData globalData = null;

    public static final String WebDAVPrefix = "http://";
    public static final String WebDAVPrefixSSL = "https://";

    protected GlobalData()
    {
        init( true );
    }


    static void reset()
    {
        // reset this class
        globalData = null;

        // reset all static variables in all other classes
        AsGen.clear();
        WebDAVRequestGenerator.reset();
        WebDAVResponseInterpreter.reset();
        WebDAVTreeNode.reset();

        // clean up
        System.gc();
    }


    static GlobalData getGlobalData()
    {
        if( globalData == null )
            globalData = new GlobalData();
        return globalData;
    }


    public WebDAVTreeView getTree()
    {
        return tree;
    }


    public void setTree(WebDAVTreeView theTree)
    {
        tree = theTree;
    }


    public boolean isAppletMode()
    {
        return isAppletMode;
    }


    public void setAppletMode(boolean isAnApplet)
    {
        isAppletMode = isAnApplet;
    }


    public void setInitialSites(String[][] initialSiteList)
    {
        if (initialSiteList != null)
            initialSites = initialSiteList;
    }


    public String[][] getInitialSites()
    {
        return initialSites;
    }


    public void setProxy( String proxy )
    {
        appletProxy = proxy;
    }


    public String getProxy()
    {
        return appletProxy;
    }


    public boolean hideURIBox()
    {
        return hideURIBox;
    }


    public void setHideURIBox(boolean visible)
    {
        hideURIBox = visible;
    }


    public boolean doAddStartDir()
    {
        return doAddStartDir;
    }


    public void setAddStartDir(boolean doIt)
    {
        doAddStartDir = doIt;
    }


    public URIBox getURIBox()
    {
        return uriBox;
    }


    public void setURIBox(URIBox theURIBox)
    {
        uriBox = theURIBox;
    }


    public boolean getDebugAll()
    {
        return debugAll;
    }


    public void setDebugAll( boolean debug )
    {
        debugAll = debug;
        init( false );
    }


    public boolean getDebugRequest()
    {
        return debugRequest;
    }


    public void setDebugRequest( boolean debug )
    {
        debugRequest = debug;
        init( false );
    }


    public boolean getDebugResponse()
    {
        return debugResponse;
    }


    public void setDebugResponse( boolean debug )
    {
        debugResponse = debug;
        init( false );
    }


    public boolean getDebugTreeView()
    {
        return debugTreeView;
    }


    public void setDebugTreeView( boolean debug )
    {
        debugTreeView = debug;
        init( false );
    }


    public boolean getDebugTreeNode()
    {
        return debugTreeNode;
    }


    public void setDebugTreeNode( boolean debug )
    {
        debugTreeNode = debug;
        init( false );
    }


    public boolean getDebugFileView()
    {
        return debugFileView;
    }


    public void setDebugFileView( boolean debug )
    {
        debugFileView = debug;
        init( false );
    }


    public JFrame getMainFrame()
    {
        return mainFrame;
    }


    public void setMainFrame( JFrame frame )
    {
        mainFrame = frame;
        if( mainFrame != null )
            origCursor = mainFrame.getCursor(); // save original cursor
    }


    public void errorMsg(String str)
    {
        Object[] options = { "OK" };
		JOptionPane.showOptionDialog(mainFrame,str,"Error Message", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
    }


    public void setCursor( Cursor c )
    {
        if( mainFrame != null )
            mainFrame.setCursor( c );
    }


    public void resetCursor()
    {
        if( mainFrame != null && origCursor != null )
            mainFrame.setCursor( origCursor );
    }


    public String unescape( String text, String encoding, boolean href )
    {
        ByteArrayInputStream byte_in = new ByteArrayInputStream( text.getBytes() );
        EscapeInputStream iStream = new EscapeInputStream( byte_in, true );
        try
        {
            InputStreamReader isr = null;
            if( (encoding==null) || (encoding.length()==0) )
                isr = new InputStreamReader( iStream, "UTF-8" );
            else
                isr = new InputStreamReader( iStream, encoding );

            BufferedReader br = new BufferedReader( isr );
            String out = br.readLine();
            return (out == null)? "" : out;
        }
        catch( IOException e )
        {
            if( href )
            {
                // the <href> tag doesn't necessarily need to be encoded in
                // the specified encoding
                try
                {
                    byte_in.reset();
                    iStream.reset();
                    InputStreamReader isr = new InputStreamReader( iStream );
                    BufferedReader br = new BufferedReader( isr );
                    String out = br.readLine();
                    return (out == null)? "" : out;
                }
                catch( IOException e2 )
                {
                    GlobalData.getGlobalData().errorMsg("String unescaping error: \n" + e);
                }
            }
            else
            {
                // the text may already be in UTF-8, so all we need is to unescape
                // this is a rather bad hack, but since we don't have control over
                // what kind of data the server sends...
                try
                {
                    byte_in = new ByteArrayInputStream( text.getBytes("UTF-8") );
                    iStream = new EscapeInputStream( byte_in, true );
                    InputStreamReader isr = new InputStreamReader( iStream, "UTF-8" );
                    BufferedReader br = new BufferedReader( isr );
                    String out = br.readLine();
                    return (out == null)? "" : out;
                }
                catch( IOException e2 )
                {
                    GlobalData.getGlobalData().errorMsg("String unescaping error: \n" + e);
                }
            }
        }
        return "";
    }


    public void setSSL( boolean SSL )
    {
        ssl = SSL;
    }


    public boolean getSSL()
    {
        return ssl;
    }


    public String ReadConfigEntry( String token, String defaultString )
    {
        Vector info = ReadConfigEntry( token, false );
        if( info.size() > 0 )
        {
            return (String)info.elementAt(0);
        }
        if( defaultString != null )
            return defaultString;
        return "";
    }


    public String ReadConfigEntry( String token )
    {
        return ReadConfigEntry( token, "" );
    }


    public Vector ReadConfigEntry( String token, boolean multiple )
    {
        Vector info = new Vector();
        String userPath = System.getProperty( "user.home" );
        if (userPath == null)
            userPath = "";
        else
            userPath += File.separatorChar;
        String filePath = null;
        File theFile = new File(userPath + fileName);
        if (theFile.exists())
            filePath = userPath + fileName;
        if (filePath != null)
        {
            try
            {
                FileInputStream fin = new FileInputStream(filePath);
                BufferedReader in = new BufferedReader(new InputStreamReader(fin));
                boolean found = false;
                do
                {
                    String line = in.readLine();
                    if( line == null )
                        break;
                    StringTokenizer filetokens = new StringTokenizer( line, "= \t" );
                    if( (filetokens.nextToken()).equals(token) )
                    {
                        String data = filetokens.nextToken(); 
                        info.addElement( data );
                        found = true;
                    }
                }
                while( multiple || !found );
                in.close();
            }
            catch (Exception fileEx)
            {
            }
        }
        return info;
    }


    public void WriteConfigEntry( String token, String data )
    {
        WriteConfigEntry( token, data, true );
    }


    public void WriteConfigEntry( String token, Vector data )
    {
        if( (data == null) || (data.size() == 0) )
            return;
        // this has the side effect of removing all old token entries
        WriteConfigEntry( token, (String)data.elementAt(0), true );
        for( int i=1; i<data.size(); i++ )
        {
            // it doesn't make sense here to overwrite entries
            WriteConfigEntry( token, (String)data.elementAt(i), false );
        }
    }


    public void WriteConfigEntry( String token, Vector data, boolean overwrite )
    {
        if( (data == null) || (data.size() == 0) )
            return;
        // this has the side effect of removing all old token entries
        WriteConfigEntry( token, (String)data.elementAt(0), overwrite );
        for( int i=1; i<data.size(); i++ )
        {
            // it doesn't make sense here to overwrite entries
            WriteConfigEntry( token, (String)data.elementAt(i), false );
        }
    }


    public void WriteConfigEntry( String token, String data, boolean overwrite )
    {
        String userPath = System.getProperty( "user.home" );
        if (userPath == null)
            userPath = "";
        else
            userPath += File.separatorChar;
        String filePath = userPath + fileName;
        String tmpFilePath = userPath + tmpFileName;
        try
        {
            FileOutputStream fout = new FileOutputStream( tmpFilePath );
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(fout));
            File theFile = new File(filePath);
            if ( theFile.exists() )
            {
                FileInputStream fin = new FileInputStream(filePath);
                BufferedReader in = new BufferedReader(new InputStreamReader(fin));
                String line = null;
                do
                {
                    line = in.readLine();
                    if( line != null )
                    {
                        StringTokenizer filetokens = new StringTokenizer( line, "= \t" );
                        if( !overwrite || !(filetokens.nextToken()).equals(token) )
                        {
                            // copy line to new file
                            out.write( line );
                            out.newLine();
                        }
                    }
                }
                while( line != null );
                in.close();
            }
            out.write( token );
            out.write( "=" );
            out.write( data );
            out.newLine();
            out.close();

            if( theFile.exists() )
                theFile.delete();
            File theNewFile = new File( tmpFilePath );
            theNewFile.renameTo( theFile );
        }
        catch (Exception fileEx)
        {
            System.out.println( fileEx.toString() );
        }
    }


    private void init( boolean readFromProperties )
    {
        if( readFromProperties )
        {
            String doLog = System.getProperty( "debug" );
            if( doLog != null )
            {
                if( doLog.equalsIgnoreCase("all") )
                    setDebugAll( true );
                else if( doLog.equalsIgnoreCase( "request" ) )
                    setDebugRequest( true );
                else if( doLog.equalsIgnoreCase( "response" ) )
                    setDebugResponse( true );
                else if( doLog.equalsIgnoreCase( "treeview" ) )
                    setDebugTreeView( true );
                else if( doLog.equalsIgnoreCase( "treenode" ) )
                    setDebugTreeNode( true );
                else if( doLog.equalsIgnoreCase( "fileview" ) )
                    setDebugFileView( true );
            }
            String doSSL = System.getProperty( "ssl", "no" );
            if( doSSL.equalsIgnoreCase( "yes" ) || doSSL.equalsIgnoreCase( "true" ) )
                ssl = true;
            else
            {
                doSSL = System.getProperty( "SSL", "no" );
                if( doSSL.equalsIgnoreCase( "yes" ) || doSSL.equalsIgnoreCase( "true" ) )
                    ssl = true;
            }
        }

        debugRequest |= debugAll;
        debugResponse |= debugAll;
        debugTreeView |= debugAll;
        debugTreeNode |= debugAll;
        debugFileView |= debugAll;

    }


    public ImageIcon getImageIcon(String name, String description)
    {
        try
        {
            InputStream is = getClass().getResourceAsStream("icons/" + name);
            return new ImageIcon( toByteArray(is), description );
        }
        catch( Exception e )
        {
            errorMsg("Icon load failure: " + e );
        }
        return null;
    }


    public static byte[] toByteArray(InputStream is)
        throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] chunk = new byte[10000];
        while (true)
        {
            int bytesRead = is.read(chunk, 0, chunk.length);
            if (bytesRead <= 0)
            {
                break;
            }
            output.write(chunk, 0, bytesRead);
        }
        return output.toByteArray();
    }
}
