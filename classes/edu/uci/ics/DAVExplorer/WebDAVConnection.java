/*
 * Copyright (c) 1998-2001 Regents of the University of California.
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
 * Copyright:   Copyright (c) 1998-2001 Regents of the University of California. All rights reserved.
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
 */

package edu.uci.ics.DAVExplorer;

import HTTPClient.HTTPConnection;
import HTTPClient.HTTPResponse;
import HTTPClient.NVPair;
import HTTPClient.HttpOutputStream;
import HTTPClient.ModuleException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class WebDAVConnection extends HTTPConnection
{
    static final int DEFAULT_PORT = 80;

    public WebDAVConnection(String HostName)
    {
        super(HostName, DEFAULT_PORT);
        try
        {
            removeModule( Class.forName("HTTPClient.RedirectionModule") );
        }
        catch (ClassNotFoundException cnfe)
        {
            // just ignore it
        }
    }


    public WebDAVConnection( String Protocol, String HostName )
        throws HTTPClient.ProtocolNotSuppException
    {
        super( Protocol, HostName, DEFAULT_PORT);
        try
        {
            removeModule( Class.forName("HTTPClient.RedirectionModule") );
        }
        catch (ClassNotFoundException cnfe)
        {
            // just ignore it
        }
    }


    public WebDAVConnection(String HostName, int Port)
    {
        super(HostName, Port);
        try
        {
            removeModule( Class.forName("HTTPClient.RedirectionModule") );
        }
        catch (ClassNotFoundException cnfe)
        {
            // just ignore it
        }
    }


    public WebDAVConnection(String Protocol, String HostName, int Port )
        throws HTTPClient.ProtocolNotSuppException
    {
        super( Protocol, HostName, Port );
        try
        {
            removeModule( Class.forName("HTTPClient.RedirectionModule") );
        }
        catch( ClassNotFoundException cnfe )
        {
            // just ignore it
        }
    }


    public HTTPResponse Put( String filename, String source, NVPair[] headers )
        throws IOException, ModuleException
    {
        File file = new File( source );
        long fileSize = file.length();

        HttpOutputStream out = new HttpOutputStream( fileSize );
        //HTTPResponse response = Put( filename, out, headers );
        HTTPResponse response = ExtensionMethod( "PUT", filename, out, headers );

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
        return response;
    }


    public HTTPResponse PropFind(String file, byte[] body, NVPair[] headers)
        throws IOException, ModuleException
    {
        return ExtensionMethod("PROPFIND",file, body, headers);
    }

    public HTTPResponse PropPatch(String file, byte[] body, NVPair[] headers)
        throws IOException, ModuleException
    {
        return ExtensionMethod("PROPPATCH", file, body, headers);
    }

    public HTTPResponse MkCol(String file, NVPair[] headers)
        throws IOException, ModuleException
    {
        return ExtensionMethod("MKCOL", file, (byte []) null, headers);
    }

    public HTTPResponse AddRef(String file, NVPair[] headers)
        throws IOException, ModuleException
    {
        return ExtensionMethod("ADDREF", file, (byte[]) null, headers);
    }

    public HTTPResponse DelRef(String file, NVPair[] headers)
        throws IOException, ModuleException
    {
        return ExtensionMethod("DELREF", file, (byte[]) null, headers);
    }

    public HTTPResponse Copy(String file, byte[] body, NVPair[] headers)
        throws IOException, ModuleException
    {
        return ExtensionMethod("COPY", file, body, headers);
    }

    public HTTPResponse Copy(String file, NVPair[] headers)
        throws IOException, ModuleException
    {
        return Copy(file, null, headers);
    }

    public HTTPResponse Move(String file, byte[] body, NVPair[] headers)
        throws IOException, ModuleException
    {
        return ExtensionMethod("MOVE", file, body, headers);
    }

    public HTTPResponse Move(String file, NVPair[] headers)
        throws IOException, ModuleException
    {
        return Move(file, null, headers);
    }

    public HTTPResponse Lock(String file, byte[] body, NVPair[] headers)
        throws IOException, ModuleException
    {
        return ExtensionMethod("LOCK", file, body, headers);
    }

    public HTTPResponse Lock(String file, NVPair[] headers)
        throws IOException, ModuleException
    {
        return Lock(file, null, headers);
    }

    public HTTPResponse Unlock(String file, NVPair[] headers)
        throws IOException, ModuleException
    {
        return ExtensionMethod("UNLOCK", file, (byte[]) null, headers);
    }

    public HTTPResponse Generic(String Method, String file, byte[] body, NVPair[] headers)
        throws IOException, ModuleException
    {
        return ExtensionMethod(Method, file, body, headers);
    }
}
