/*
 * Copyright (c) 1999-2005 Regents of the University of California.
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

import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.Dialog;

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
import java.io.UnsupportedEncodingException;


/**
 * Title:       GlobalData
 * Description: This singleton class defines various global data structures
 *              and functions useful everywhere
 * Copyright:   Copyright (c) 1999-2005 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         1999
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         9 January 2001
 * Changes:     Added support for https (SSL), moved reading of debug properties here
 * date         29 May 2001
 * Changes:     Support for reading/writing configuration file
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         1 October 2001
 * Changes:     Change of package name
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         2 April 2002
 * Changes:     Updated for JDK 1.4
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         17 March 2003
 * Changes:     Integrated Brian Johnson's applet changes.
 *              Added better error reporting.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         7 April 2003
 * Changes:     Improved reading/writing of configuration entries. 
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         27 April 2003
 * Changes:     added support for default config entries.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         3 November 2003
 * Changes:     Added support for proxy server in applet settings (it always
 *              worked through the "Edit Proxy Info" menu entry.)
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         06 February 2004
 * Changes:     Added support disabling compression encoding
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         08 February 2004
 * Changes:     Added Javadoc templates
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         09 February 2004
 * Changes:     Improved unescaping
  * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         14 February 2005
 * Changes:     Moved center function here
*/
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

    /** Allow compression for transfer */
    boolean compression = true;
    
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


    /**
     * Protected constructor.
     */
    protected GlobalData()
    {
        init( true );
    }


    /**
     * Reset static variables to default values, and run the garbage collector.
     */
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


    /**
     * Get the singleton instance.
     *  
     * @return  The singleton GlobalData instance
     */
    static GlobalData getGlobalData()
    {
        if( globalData == null )
            globalData = new GlobalData();
        return globalData;
    }


    /**
     * Store the root of the navigation tree view.
     * 
     * @param theTree   The object representing the root of the tree view
     */
    public void setTree(WebDAVTreeView theTree)
    {
        tree = theTree;
    }


    /**
     * Get the root of the navigation tree view.
     * 
     * @return  The WebDAVTreeView object representing the root of the tree view 
     */
    public WebDAVTreeView getTree()
    {
        return tree;
    }


    /**
     * Check if DAV Explorer is running as applet.
     * 
     * @return  True if running as applet, false else
     */
    public boolean isAppletMode()
    {
        return isAppletMode;
    }


    /**
     * Set/unset applet mode.
     * 
     * @param isAnApplet    Indicates if applet mode is set or unset
     */
    public void setAppletMode(boolean isAnApplet)
    {
        isAppletMode = isAnApplet;
    }


    /**
     * Set the initial WebDAV sites to connect to. Used in applet mode
     * from parameters given on the webpage.
     * 
     * @param initialSiteList   Array of sites, together with usernames and passwords
     */
    public void setInitialSites(String[][] initialSiteList)
    {
        if (initialSiteList != null)
            initialSites = initialSiteList;
    }


    /**
     * Get the list of initial WebDAV sites to connect to. Used in applet mode.
     * 
     * @return  Array of sites, together with usernames and passwords
     */
    public String[][] getInitialSites()
    {
        return initialSites;
    }


    /**
     * 
     * Set a proxy server to use. Used in applet mode from parameters given
     * on the webpage.
     * 
     * @param proxy The URI of the proxy server to use
     */
    public void setProxy( String proxy )
    {
        appletProxy = proxy;
    }


    /**
     * Get the URI of the proxy server in use. Used in applet mode.
     * 
     * @return  The URI of the proxy server in use, or null if none
     */
    public String getProxy()
    {
        return appletProxy;
    }


    /**
     * The URI box is hidden in applet mode.
     *   
     * @return  True if the URI box is hidden, false else
     */
    public boolean hideURIBox()
    {
        return hideURIBox;
    }


    /**
     * Hide or show the URI box.
     *  
     * @param visible   True if URI box should be visible, false else
     */
    public void setHideURIBox(boolean visible)
    {
        hideURIBox = visible;
    }


    /**
     * Store the object representing the URI box.
     *   
     * @param theURIBox The object representing the URI box
     */
    public void setURIBox(URIBox theURIBox)
    {
        uriBox = theURIBox;
    }


    /**
     * Get the object representing the URI box.
     *  
     * @return  The object representing the URI box
     */
    public URIBox getURIBox()
    {
        return uriBox;
    }


    /**
     * Check if the start directory of a WebDAV server should be
     * added to the treeview.
     * 
     * @return  True if the start directory should be added, false else
     */
    public boolean doAddStartDir()
    {
        return doAddStartDir;
    }


    /**
     * Configure if the start directory of a WebDAV server should be
     * added to the tree view.
     * 
     * @param doIt  True if the start directory should be added, false else
     */
    public void setAddStartDir(boolean doIt)
    {
        doAddStartDir = doIt;
    }


    /**
     * Returns the debug all flag as set on the command line.
     * 
     * @return  True if debug all flag was set, false else
     */
    public boolean getDebugAll()
    {
        return debugAll;
    }


    /**
     * Set/unset the debug all flag.
     * 
     * @param debug The debug all flag
     */
    public void setDebugAll( boolean debug )
    {
        debugAll = debug;
        init( false );
    }


    /**
     * Returns true if the flag to debug requests was set on the command line.
     *  
     * @return  True if the debug requests flag was set, false else 
     */
    public boolean getDebugRequest()
    {
        return debugRequest;
    }


    /**
     * Set/unset the debug request flag.
     * 
     * @param debug The debug request flag
     */
    public void setDebugRequest( boolean debug )
    {
        debugRequest = debug;
        init( false );
    }


    /**
     * Returns true if the flag to debug responses was set on the command line.
     *  
     * @return  True if the debug response flag was set, false else 
     */
    public boolean getDebugResponse()
    {
        return debugResponse;
    }


    /**
     * Set/unset the debug response flag.
     * 
     * @param debug The debug response flag
     */
    public void setDebugResponse( boolean debug )
    {
        debugResponse = debug;
        init( false );
    }


    /**
     * Returns true if the flag to debug the treeview was set on the command line.
     *  
     * @return  True if the debug treeview flag was set, false else 
     */
    public boolean getDebugTreeView()
    {
        return debugTreeView;
    }


    /**
     * Set/unset the debug treeview flag.
     * 
     * @param debug The debug treeview flag
     */
    public void setDebugTreeView( boolean debug )
    {
        debugTreeView = debug;
        init( false );
    }


    /**
     * Returns true if the flag to debug treenodes within the treeview
     *  was set on the command line.
     *  
     * @return  True if the debug treenode flag was set, false else 
     */
    public boolean getDebugTreeNode()
    {
        return debugTreeNode;
    }


    /**
     * Set/unset the debug treenode flag.
     * 
     * @param debug The debug treenode flag
     */
    public void setDebugTreeNode( boolean debug )
    {
        debugTreeNode = debug;
        init( false );
    }


    /**
     * Returns true if the flag to debug the file view was set on the command line.
     *  
     * @return  True if the debug fileview flag was set, false else 
     */
    public boolean getDebugFileView()
    {
        return debugFileView;
    }


    /**
     * Set/unset the debug fileview flag.
     * 
     * @param debug The debug fileview flag
     */
    public void setDebugFileView( boolean debug )
    {
        debugFileView = debug;
        init( false );
    }


    /**
     * Returns a reference to the main window object.
     *  
     * @return  The main window object 
     */
    public JFrame getMainFrame()
    {
        return mainFrame;
    }


    /**
     * Store a reference to the main window object.
     *  
     * @param frame The main window object
     */
    public void setMainFrame( JFrame frame )
    {
        mainFrame = frame;
        if( mainFrame != null )
            origCursor = mainFrame.getCursor(); // save original cursor
    }


    /**
     * Show a dialog box with an error message.
     *  
     * @param str   The error message to show
     */
    public void errorMsg(String str)
    {
        Object[] options = { "OK" };
		JOptionPane.showOptionDialog(mainFrame,str,"Error Message", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
    }


    /**
     * Set a new cursor
     * 
     * @param c The new cursor
     */
    public void setCursor( Cursor c )
    {
        if( mainFrame != null )
            mainFrame.setCursor( c );
    }


    /**
     * Reset the cursor to the original one.
     */
    public void resetCursor()
    {
        if( mainFrame != null && origCursor != null )
            mainFrame.setCursor( origCursor );
    }


    /**
     * HTTP-unescape string
     * @param text              Escaped string
     * @param sourceEncoding    Source encoding
     * @param targetEncoding    Target encoding
     * @return                  The unescaped string, or an empty string if the
     *                          function failed.
     */
    public String unescape( String text, String sourceEncoding, String targetEncoding )
    {
        ByteArrayInputStream byte_in;
        ByteArrayInputStream byte_test;
        try
        {
            if( (sourceEncoding==null) || (sourceEncoding.length()==0) )
            {
                // assume the text is UTF-8 encoded
                byte_in = new ByteArrayInputStream( text.getBytes("UTF-8") );
                byte_test = new ByteArrayInputStream( text.getBytes("UTF-8") );
            }
            else
            {
                byte_in = new ByteArrayInputStream( text.getBytes(sourceEncoding) );
                byte_test = new ByteArrayInputStream( text.getBytes(sourceEncoding) );
            }
        }
        catch( UnsupportedEncodingException e )
        {
            byte_in = new ByteArrayInputStream( text.getBytes() );
            byte_test = new ByteArrayInputStream( text.getBytes() );
        }
        
        EscapeInputStream iStream;
        boolean uni = true;
        if( (targetEncoding==null) || (targetEncoding.length()==0) )
        {
            iStream = new EscapeInputStream( byte_test, true );
            try
            {
                int i;
                do
                {
                    i = iStream.read();
                    if( i == -1)
                        break;
                    uni = checkUTFFormed( i, iStream );
                }
                while( uni && i != -1 );
            }
            catch(IOException e)
            {
            }
        }
        iStream = new EscapeInputStream( byte_in, true );

        try
        {
            InputStreamReader isr = null;
            if( (targetEncoding==null) || (targetEncoding.length()==0) )
            {
                if( uni )
                    isr = new InputStreamReader( iStream, "UTF-8" );
                else
                    isr = new InputStreamReader( iStream );
            }
            else
                isr = new InputStreamReader( iStream, targetEncoding );

            BufferedReader br = new BufferedReader( isr );
            String out = br.readLine();
            return (out == null)? "" : out;
        }
        catch( IOException e )
        {
            GlobalData.getGlobalData().errorMsg("String unescaping error: \n" + e);
        }
        return "";
    }


    /**
     * Determine if the current character in a stream represents an allowed
     * Unicode character.
     * 
     * @param i         The character to check
     * @param stream    The stream to read additional characters from to
     *                  make the determination
     * @return          True if the current character is a Unicode character
     * 
     * @see <a href"http://www.unicode.org/versions/Unicode4.0.0/ch03.pdf">The
     * Unicode Standard, Section 3.10, Table 3.6</a>
     */
    private boolean checkUTFFormed( int i, InputStream stream )
    {
        try
        {
            if( i < 128 )                   // 00..7F
                return true;
            else if( i >= 194 && i <= 223 ) // C2..DF
            {
                i = stream.read();
                if( i >= 128 && i <= 191 )  // 80..BF
                    return true;
            }
            else if( i == 224 )             // E0
            {
                i = stream.read();
                if( i >= 160 && i <= 191 )  // A0..BF
                {
                    i = stream.read();
                    if( i >= 128 && i <= 191 )  // 80..BF
                        return true;
                }
            }
            else if( i >= 225 && i <= 236 ) // E1..EC
            {
                i = stream.read();
                if( i >= 128 && i <= 191 )  // 80..BF
                {
                    i = stream.read();
                    if( i >= 128 && i <= 191 )  // 80..BF
                        return true;
                }
            }
            else if( i == 237 )             // ED
            {
                i = stream.read();
                if( i >= 128 && i <= 159 )  // 80..9F
                {
                    i = stream.read();
                    if( i >= 128 && i <= 191 )  // 80..BF
                        return true;
                }
            }
            else if( i >= 238 && i <= 239 ) // EE..EF
            {
                i = stream.read();
                if( i >= 128 && i <= 191 )  // 80..BF
                {
                    i = stream.read();
                    if( i >= 128 && i <= 191 )  // 80..BF
                        return true;
                }
            }
            else if( i == 240 )             // F0
            {
                i = stream.read();
                if( i >= 144 && i <= 191 )  // 90..BF
                {
                    i = stream.read();
                    if( i >= 128 && i <= 191 )  // 80..BF
                    {
                        i = stream.read();
                        if( i >= 128 && i <= 191 )  // 80..BF
                            return true;
                    }
                }
            }
            else if( i >=241 && i<=243 )    // F1..F3
            {
                i = stream.read();
                if( i >= 128 && i <= 191 )  // 80..BF
                {
                    i = stream.read();
                    if( i >= 128 && i <= 191 )  // 80..BF
                    {
                        i = stream.read();
                        if( i >= 128 && i <= 191 )  // 80..BF
                            return true;
                    }
                }
            }
            else if( i == 244 )
            {
                i = stream.read();
                if( i >= 128 && i <= 143 )  // 90..8F
                {
                    i = stream.read();
                    if( i >= 128 && i <= 191 )  // 80..BF
                    {
                        i = stream.read();
                        if( i >= 128 && i <= 191 )  // 80..BF
                            return true;
                    }
                }
            }
        }
        catch( IOException e )
        {
            return false;
        }

        return false;
    }
    

    /**
     * Set a flag to indicate if the program is using SSL or not.
     *  
     * @param SSL   True for using SSL, false else
     */
    public void setSSL( boolean SSL )
    {
        ssl = SSL;
        WriteConfigEntry( "UseSSL", Boolean.toString(SSL) );
    }


    /**
     * Return true when the program is using SSL.
     *  
     * @return  True if using SSL, false else
     */
    public boolean getSSL()
    {
        return ssl;
    }


    /**
     * Set/unset a flag to control the compression of the HTTP datastream
     * 
     * @param compression   True if compression is allowed, false else
     */
    public void setCompressions( boolean compression )
    {
        this.compression = compression;
    }
    
    
    /**
     * Return true if compression of the HTTP datastream is allowed.
     * 
     * @return  True if compression is allowed, false else
     */
    public boolean getCompression()
    {
        return compression;
    }
    

    /**
     * Reads the entry defined by the token from the configuration file. 
     * @param token         The token to look for
     * @param defaultString The default return if the token is not found
     * @return              The entry referenced by the token
     */
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


    /**
     * Reads the entry defined by the token from the configuration file. 
     * @param token The token to look for
     * @return      The entry referenced by the token
     */
    public String ReadConfigEntry( String token )
    {
        return ReadConfigEntry( token, "" );
    }


    /**
     * Reads multiple entries defined by the token from the configuration file. 
     * @param token     The token to look for
     * @param multiple  True if multiple entries should be returned
     * @return          The entries referenced by the token
     */
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


    /**
     * Write an entry to the configuration file.
     * Entries are property-value pairs in the form of "property=value".
     * 
     * @param token The property string
     * @param data  The value string
     */
    public void WriteConfigEntry( String token, String data )
    {
        WriteConfigEntry( token, data, true );
    }


    /**
     * Write an entry to the configuration file.
     * Entries are property-value pairs in the form of "property=value".
     * This method associates multiple values with a particular property.
     * 
     * @param token The property string
     * @param data  Array of value strings associated with the property
     */
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


    /**
     * Write an entry to the configuration file, possibly overwriting
     * existing entries.
     * Entries are property-value pairs in the form of "property=value".
     * This method associates multiple values with a particular property.
     * 
     * @param token     The property string
     * @param data      Array of value strings associated with the property
     * @param overwrite True if overwriting is allowed, false else
     */
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


    /**
     * Write an entry to the configuration file, possibly overwriting
     * existing entries.
     * Entries are property-value pairs in the form of "property=value".
     * 
     * @param token     The property string
     * @param data      The value string associated with the property
     * @param overwrite True if overwriting is allowed, false else
     */
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


    /**
     * Initialize the static variables, possibly from user-defined
     * properties entered on the command line.
     *  
     * @param readFromProperties    True to trigger initialization from
     *                              the user-defined properties
     */
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
            // check for SSL
            // Enabling SSL on the command line always overwrites the config value
            String doSSL = ReadConfigEntry( "UseSSL", "no" );
            doSSL = System.getProperty( "ssl", doSSL );
            if( doSSL.equalsIgnoreCase( "yes" ) || doSSL.equalsIgnoreCase( "true" ) )
                setSSL( true );
            else
            {
                doSSL = System.getProperty( "SSL", "no" );
                if( doSSL.equalsIgnoreCase( "yes" ) || doSSL.equalsIgnoreCase( "true" ) )
                    setSSL( true );
            }
            
            String noCompression = System.getProperty( "compress", "yes" );
            if( noCompression.equalsIgnoreCase( "no" ) || noCompression.equalsIgnoreCase( "false" ) )
                compression = false;
        }

        debugRequest |= debugAll;
        debugResponse |= debugAll;
        debugTreeView |= debugAll;
        debugTreeNode |= debugAll;
        debugFileView |= debugAll;

    }


    /**
     * Get a named icon from the DAV Explorer jar file.
     *  
     * @param name          The icon's name
     * @param description   The icon's tooltip
     * @return              The ImageIcon object containing the icon data
     */
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


    /**
     * Convert a Java InputStream to a byte array.
     *  
     * @param is        InputStream to convert to a byte array
     * @return byte[]   The data from the InputStream as byte array
     * @throws IOException
     */
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


    /**
     * 
     * @param methodName
     * @param className
     * @param debugFlag
     */
    public static void methodEnter( String methodName, String className, boolean debugFlag )
    {
        if( debugFlag )
        {
            System.err.println( className + "::" + methodName + " entered." );
        }
    }
    
    
    /**
     * 
     * @param methodName
     * @param className
     * @param debugFlag
     */
    public static void methodLeave( String methodName, String className, boolean debugFlag )
    {
        if( debugFlag )
        {
            System.err.println( className + "::" + methodName + " left." );
        }
    }


    /**
    *
    */
    protected void center( Dialog dlg )
    {
        Rectangle recthDimensions = dlg.getParent().getBounds();
        Rectangle bounds = dlg.getBounds();
        int x = recthDimensions.x + (recthDimensions.width-bounds.width)/2;
        if( x < 0 ) x = 0;
        int y = recthDimensions.y + (recthDimensions.height - bounds.height)/2;
        if( y < 0 ) y = 0;
        dlg.setBounds( x, y, bounds.width, bounds.height );
    }
}
