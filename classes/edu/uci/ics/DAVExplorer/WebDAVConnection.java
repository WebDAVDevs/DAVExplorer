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
 * Title:       WebDAVConnection
 * Description: WebDAV Method class library.
 *              We simply use the HTTPClient's extension method for
 *              sending all the requests.
 * Copyright:   Copyright (c) 1998-2004 Regents of the University of California. All rights reserved.
 * @author      Robert Emmery (dav-exp@ics.uci.edu)
 * @date        2 April 1998
 * @author      Yuzo Kanomata, Joachim Feise (dav-exp@ics.uci.edu)
 * @date        17 March 1999
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        12 January 2001
 * Changes:     Added support for https (SSL)
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        1 October 2001
 * Changes:     Change of package name
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        25 June 2002
 * Changes:     Added a Put method to support files > 2GB
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        06 February 2004
 * Changes:     Refactoring, adding option to disable compression
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        08 February 2004
 * Changes:     Added Javadoc templates
 */

package edu.uci.ics.DAVExplorer;

import HTTPClient.HTTPConnection;
import HTTPClient.HTTPResponse;
import HTTPClient.NVPair;
import HTTPClient.HttpOutputStream;
import HTTPClient.ModuleException;
import HTTPClient.ProtocolNotSuppException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


/**
 * Wrapper around HTTPClient.HTTPConnection 
 */
public class WebDAVConnection extends HTTPConnection
{
    static final int DEFAULT_PORT = 80;


    /**
     * Constructor
     * @param HostName
     */
    public WebDAVConnection(String HostName)
    {
        super(HostName, DEFAULT_PORT);
        removeModules();
        addModules();
        changeModules();
    }


    /**
     * Constructor
     * @param Protocol
     * @param HostName
     * @exception HTTPClient.ProtocolNotSuppException
     */
    public WebDAVConnection( String Protocol, String HostName )
        throws ProtocolNotSuppException
    {
        super( Protocol, HostName, DEFAULT_PORT);
        removeModules();
        addModules();
        changeModules();
    }


    /**
     * Constructor
     * @param HostName
     * @param Port
     */
    public WebDAVConnection(String HostName, int Port)
    {
        super(HostName, Port);
        removeModules();
        addModules();
        changeModules();
    }


    /**
     * Constructor
     * @param Protocol
     * @param HostName
     * @param Port
     * @exception HTTPClient.ProtocolNotSuppException
     */
    public WebDAVConnection(String Protocol, String HostName, int Port )
        throws ProtocolNotSuppException
    {
        super( Protocol, HostName, Port );
        removeModules();
        addModules();
        changeModules();
    }


    /**
     * 
     * @param filename
     * @param source
     * @param headers
     * @exception IOException
     * @exception HTTPClient.ModuleException
     *
     * @return 
     */
    public HTTPResponse Put( String filename, String source, NVPair[] headers )
        throws IOException, ModuleException
    {
        File file = new File( source );
        long fileSize = file.length();

        HttpOutputStream out = new HttpOutputStream( fileSize );
        HTTPResponse response = null;
        /* The do ... while loop is needed in case authentication is required.
         * See @link HTTPOutputStream HTTPOutputStream
         * and @link HTTPResponse#retryRequest() HTTPResponse.retryRequest
         */ 
        do
        {
            response = ExtensionMethod( "PUT", filename, out, headers );

            FileInputStream file_in = new FileInputStream( file );
            byte[] b = new byte[65536];     // in my MacOS9 tests, this value seemed to work best
                                            // The 1MB value I had here before resulted in timeouts
            long off = 0;
            int rcvd = 0;
            do
            {
                off += rcvd;
                rcvd = file_in.read(b);
                if( rcvd != -1 )
                    out.write(b, 0, rcvd);
            }
            while (rcvd != -1 && off+rcvd < fileSize);
            out.close();
        } while( response.retryRequest() );        
        return response;
    }


    /**
     * 
     * @param file
     * @param body
     * @param headers
     * @exception IOException
     * @exception HTTPClient.ModuleException
     *
     * @return 
     */
    public HTTPResponse PropFind(String file, byte[] body, NVPair[] headers)
        throws IOException, ModuleException
    {
        return ExtensionMethod("PROPFIND",file, body, headers);
    }


    /**
     * 
     * @param file
     * @param body
     * @param headers
     * @exception IOException
     * @exception HTTPClient.ModuleException
     *
     * @return 
     */
    public HTTPResponse PropPatch(String file, byte[] body, NVPair[] headers)
        throws IOException, ModuleException
    {
        return ExtensionMethod("PROPPATCH", file, body, headers);
    }


    /**
     * 
     * @param file
     * @param headers
     * @exception IOException
     * @exception HTTPClient.ModuleException
     *
     * @return 
     */
    public HTTPResponse MkCol(String file, NVPair[] headers)
        throws IOException, ModuleException
    {
        return ExtensionMethod("MKCOL", file, (byte []) null, headers);
    }


    /**
     * 
     * @param file
     * @param headers
     * @exception IOException
     * @exception HTTPClient.ModuleException
     *
     * @return 
     */
    public HTTPResponse AddRef(String file, NVPair[] headers)
        throws IOException, ModuleException
    {
        return ExtensionMethod("ADDREF", file, (byte[]) null, headers);
    }


    /**
     * 
     * @param file
     * @param headers
     * @exception IOException
     * @exception HTTPClient.ModuleException
     *
     * @return 
     */
    public HTTPResponse DelRef(String file, NVPair[] headers)
        throws IOException, ModuleException
    {
        return ExtensionMethod("DELREF", file, (byte[]) null, headers);
    }


    /**
     * 
     * @param file
     * @param body
     * @param headers
     * @exception IOException
     * @exception HTTPClient.ModuleException
     *
     * @return 
     */
    public HTTPResponse Copy(String file, byte[] body, NVPair[] headers)
        throws IOException, ModuleException
    {
        return ExtensionMethod("COPY", file, body, headers);
    }


    /**
     * 
     * @param file
     * @param headers
     * @exception IOException
     * @exception HTTPClient.ModuleException
     *
     * @return 
     */
    public HTTPResponse Copy(String file, NVPair[] headers)
        throws IOException, ModuleException
    {
        return Copy(file, null, headers);
    }


    /**
     * 
     * @param file
     * @param body
     * @param headers
     * @exception IOException
     * @exception HTTPClient.ModuleException
     *
     * @return 
     */
    public HTTPResponse Move(String file, byte[] body, NVPair[] headers)
        throws IOException, ModuleException
    {
        return ExtensionMethod("MOVE", file, body, headers);
    }


    /**
     * 
     * @param file
     * @param headers
     * @exception IOException
     * @exception HTTPClient.ModuleException
     *
     * @return 
     */
    public HTTPResponse Move(String file, NVPair[] headers)
        throws IOException, ModuleException
    {
        return Move(file, null, headers);
    }


    /**
     * 
     * @param file
     * @param body
     * @param headers
     * @exception IOException
     * @exception HTTPClient.ModuleException
     *
     * @return 
     */
    public HTTPResponse Lock(String file, byte[] body, NVPair[] headers)
        throws IOException, ModuleException
    {
        return ExtensionMethod("LOCK", file, body, headers);
    }


    /**
     * 
     * @param file
     * @param headers
     * @exception IOException
     * @exception HTTPClient.ModuleException
     *
     * @return 
     */
    public HTTPResponse Lock(String file, NVPair[] headers)
        throws IOException, ModuleException
    {
        return Lock(file, null, headers);
    }


    /**
     * 
     * @param file
     * @param headers
     * @exception IOException
     * @exception HTTPClient.ModuleException
     *
     * @return 
     */
    public HTTPResponse Unlock(String file, NVPair[] headers)
        throws IOException, ModuleException
    {
        return ExtensionMethod("UNLOCK", file, (byte[]) null, headers);
    }


    /**
     * 
     * @param file
     * @param body
     * @param headers
     * @exception IOException
     * @exception HTTPClient.ModuleException
     *
     * @return 
     */
    public HTTPResponse Generic(String Method, String file, byte[] body, NVPair[] headers)
        throws IOException, ModuleException
    {
        return ExtensionMethod(Method, file, body, headers);
    }
    

    /**
     * 
     */
    protected void removeModules()
    {
        try
        {
            removeModule( Class.forName("HTTPClient.RedirectionModule") );
        }
        catch (ClassNotFoundException cnfe)
        {
            // just ignore it
        }
    }
    
    
    /**
     * 
     */
    protected void addModules()
    {
    }
    
    
    /**
     * 
     */
    protected void changeModules()
    {
        try
        {
            if( !GlobalData.getGlobalData().getCompression() )
            {
                // remove the original transfer and content encoding modules
                // and replace them with the ones that don't allow compressed data
                removeModule( Class.forName("HTTPClient.TransferEncodingModule") );
                removeModule( Class.forName("HTTPClient.ContentEncodingModule") );
                addModule( Class.forName("edu.uci.ics.DAVExplorer.TransferEncodingModule"), -1 );
                addModule( Class.forName("edu.uci.ics.DAVExplorer.ContentEncodingModule"), -1 );
            }
        }
        catch (ClassNotFoundException cnfe)
        {
            // just ignore it
        }
    }
}
