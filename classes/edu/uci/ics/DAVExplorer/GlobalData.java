/*
 * Copyright (c) 1999-2004 Regents of the University of California.
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
 * Copyright:   Copyright (c) 1999-2004 Regents of the University of California. All rights reserved.
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
 * Changes:     Added support for proxy server in applet settings (it always
 *              worked through the "Edit Proxy Info" menu entry.)
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        06 February 2004
 * Changes:     Added support disabling compression encoding
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        08 February 2004
 * Changes:     Added Javadoc templates
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        09 February 2004
 * Changes:     Improved unescaping
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
import java.io.UnsupportedEncodingException;


/**
 * This singleton class defines various global data structures
 * and functions useful everywhere.
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
     * Protected constructor
     */
    protected GlobalData()
    {
        init( true );
    }


    /**
     * 
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
     * Get the singleton instance
     *  
     * @return      The singleton GlobalData instance
     */
    static GlobalData getGlobalData()
    {
        if( globalData == null )
            globalData = new GlobalData();
        return globalData;
    }


    /**
     * 
     * @return
     */
    public WebDAVTreeView getTree()
    {
        return tree;
    }


    /**
     * 
     * @param theTree
     */
    public void setTree(WebDAVTreeView theTree)
    {
        tree = theTree;
    }


    /**
     * 
     * @return
     */
    public boolean isAppletMode()
    {
        return isAppletMode;
    }


    /**
     * 
     * @param isAnApplet
     */
    public void setAppletMode(boolean isAnApplet)
    {
        isAppletMode = isAnApplet;
    }


    /**
     * 
     * @param initialSiteList
     */
    public void setInitialSites(String[][] initialSiteList)
    {
        if (initialSiteList != null)
            initialSites = initialSiteList;
    }


    /**
     * 
     * @return
     */
    public String[][] getInitialSites()
    {
        return initialSites;
    }


    /**
     * 
     * @param proxy
     */
    public void setProxy( String proxy )
    {
        appletProxy = proxy;
    }


    /**
     * 
     * @return
     */
    public String getProxy()
    {
        return appletProxy;
    }


    /**
     * 
     * @return
     */
    public boolean hideURIBox()
    {
        return hideURIBox;
    }


    /**
     * 
     * @param visible
     */
    public void setHideURIBox(boolean visible)
    {
        hideURIBox = visible;
    }


    /**
     * 
     * @return
     */
    public boolean doAddStartDir()
    {
        return doAddStartDir;
    }


    /**
     * 
     * @param doIt
     */
    public void setAddStartDir(boolean doIt)
    {
        doAddStartDir = doIt;
    }


    /**
     * 
     * @return
     */
    public URIBox getURIBox()
    {
        return uriBox;
    }


    /**
     * 
     * @param theURIBox
     */
    public void setURIBox(URIBox theURIBox)
    {
        uriBox = theURIBox;
    }


    /**
     * 
     * @return
     */
    public boolean getDebugAll()
    {
        return debugAll;
    }


    /**
     * 
     * @param debug
     */
    public void setDebugAll( boolean debug )
    {
        debugAll = debug;
        init( false );
    }


    /**
     * 
     * @return
     */
    public boolean getDebugRequest()
    {
        return debugRequest;
    }


    /**
     * 
     * @param debug
     */
    public void setDebugRequest( boolean debug )
    {
        debugRequest = debug;
        init( false );
    }


    /**
     * 
     * @return
     */
    public boolean getDebugResponse()
    {
        return debugResponse;
    }


    /**
     * 
     * @param debug
     */
    public void setDebugResponse( boolean debug )
    {
        debugResponse = debug;
        init( false );
    }


    /**
     * 
     * @return
     */
    public boolean getDebugTreeView()
    {
        return debugTreeView;
    }


    /**
     * 
     * @param debug
     */
    public void setDebugTreeView( boolean debug )
    {
        debugTreeView = debug;
        init( false );
    }


    /**
     * 
     * @return
     */
    public boolean getDebugTreeNode()
    {
        return debugTreeNode;
    }


    /**
     * 
     * @param debug
     */
    public void setDebugTreeNode( boolean debug )
    {
        debugTreeNode = debug;
        init( false );
    }


    /**
     * 
     * @return
     */
    public boolean getDebugFileView()
    {
        return debugFileView;
    }


    /**
     * 
     * @param debug
     */
    public void setDebugFileView( boolean debug )
    {
        debugFileView = debug;
        init( false );
    }


    /**
     * 
     * @return
     */
    public JFrame getMainFrame()
    {
        return mainFrame;
    }


    /**
     * 
     * @param frame
     */
    public void setMainFrame( JFrame frame )
    {
        mainFrame = frame;
        if( mainFrame != null )
            origCursor = mainFrame.getCursor(); // save original cursor
    }


    /**
     * 
     * @param str
     */
    public void errorMsg(String str)
    {
        Object[] options = { "OK" };
		JOptionPane.showOptionDialog(mainFrame,str,"Error Message", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
    }


    /**
     * Set a new cursor
     * @param c     The new cursor
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
     * @param text          Escaped string
     * @param sourceEncoding
     * @param targetEncoding      Target encoding
     * @return              The unescaped string, or an empty string if the
     *                      function failed.
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
     * 
     * @param SSL
     */
    public void setSSL( boolean SSL )
    {
        ssl = SSL;
    }


    /**
     * 
     * @return
     */
    public boolean getSSL()
    {
        return ssl;
    }


    /**
     * 
     * @param compression
     */
    public void setCompressions( boolean compression )
    {
        this.compression = compression;
    }
    
    
    /**
     * 
     * @return
     */
    public boolean getCompression()
    {
        return compression;
    }
    

    /**
     * Reads the entry defined by the token from the configuration file. 
     * @param token             The token to look for
     * @param defaultString     The default return if the token is not found
     * @return                  The entry referenced by the token
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
     * @param token             The token to look for
     * @return                  The entry referenced by the token
     */
    public String ReadConfigEntry( String token )
    {
        return ReadConfigEntry( token, "" );
    }


    /**
     * Reads multiple entries defined by the token from the configuration file. 
     * @param token             The token to look for
     * @param multiple          True if multiple entries should be returned
     * @return                  The entries referenced by the token
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
     * 
     * @param token
     * @param data
     */
    public void WriteConfigEntry( String token, String data )
    {
        WriteConfigEntry( token, data, true );
    }


    /**
     * 
     * @param token
     * @param data
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
     * 
     * @param token
     * @param data
     * @param overwrite
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
     * 
     * @param token
     * @param data
     * @param overwrite
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
     * 
     * @param readFromProperties
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
            String doSSL = System.getProperty( "ssl", "no" );
            if( doSSL.equalsIgnoreCase( "yes" ) || doSSL.equalsIgnoreCase( "true" ) )
                ssl = true;
            else
            {
                doSSL = System.getProperty( "SSL", "no" );
                if( doSSL.equalsIgnoreCase( "yes" ) || doSSL.equalsIgnoreCase( "true" ) )
                    ssl = true;
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
     * 
     * @param name
     * @param description
     * @return
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
     * 
     * @param is
     * @return
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
}
