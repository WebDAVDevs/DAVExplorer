/*
 * @(#)Handler.java					0.3 30/01/1998
 *
 *  This file is part of the HTTPClient.shttp package 
 *  Copyright (C) 1996-1998  Ronald Tschalaer
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this library; if not, write to the Free
 *  Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 *  MA 02111-1307, USA
 *
 *  For questions, suggestions, bug-reports, enhancement-requests etc.
 *  I may be contacted at:
 *
 *  ronald@innovation.ch
 *  Ronald.Tschalaer@psi.ch
 *
 */

package HTTPClient.shttp;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.UnknownHostException;
import HTTPClient.ProtocolNotSuppException;


/**
 * This class implements a URLStreamHandler for shttp URLs. With this you
 * can use the HTTPClient package as a replacement for the JDKs client.
 * To do so define the property java.protocol.handler.pkgs=HTTPClient .
 *
 * @version	0.3  30/01/1998
 * @author	Ronald Tschal&auml;r
 */

public class Handler extends URLStreamHandler
{
    public Handler()  throws ProtocolNotSuppException
    {
	new HTTPClient.HTTPConnection("shttp", "", -1);
    }

    public URLConnection openConnection(URL url)
	    throws UnknownHostException, ProtocolNotSuppException
    {
        return new HTTPClient.HttpURLConnection(url);
    }
}

