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

// WebDAV Method class library.
// We simply use the HTTPClient's extension method for
// sending all the requests.
//
// Version: 0.1
// Author:  Robert Emmery
// Date:    1/20/98
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
//
// Change List:


package WebDAV;

import HTTPClient.*;
import java.io.*;

public class WebDAVConnection extends HTTPConnection
{
    static final int DEFAULT_PORT = 80;

    public WebDAVConnection(String HostName)
    {
        super(HostName, DEFAULT_PORT);
    }

    public WebDAVConnection(String HostName, int Port)
    {
        super(HostName, Port);
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
