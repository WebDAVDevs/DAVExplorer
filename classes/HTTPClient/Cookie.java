/*
 * @(#)Cookie.java					0.3 30/01/1998
 *
 *  This file is part of the HTTPClient package
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

package HTTPClient;

import java.io.File;
import java.net.ProtocolException;
import java.util.Date;
import java.util.Hashtable;


/**
 * This class represents an http cookie as specified in
 * <a href="http://home.netscape.com/newsref/std/cookie_spec.html">Netscape's
 * cookie spec</a>
 *
 * @version	0.3  30/01/1998
 * @author	Ronald Tschal&auml;r
 * @since	V0.3
 */

public class Cookie
{
    protected String  name;
    protected String  value;
    protected Date    expires;
    protected String  domain;
    protected String  path;
    protected boolean secure;


    /**
     * Use <code>parse()</code> to create cookies.
     *
     * @see #parse(java.lang.String, HTTPClient.RoRequest)
     */
    protected Cookie(RoRequest req)
    {
	name    = null;
	value   = null;
	expires = null;
	domain  = req.getConnection().getHost().toLowerCase();
	path    = Util.getPath(req.getRequestURI());

	String prot = req.getConnection().getProtocol();
	if (prot.equals("https")  ||  prot.equals("shttp"))
	    secure = true;
	else
	    secure  = false;
    }


    /**
     * Parses the Set-Cookie header into an array of Cookies.
     *
     * @param set_cookie the Set-Cookie header received from the server
     * @param req the request used
     * @return an array of Cookies as parsed from the Set-Cookie header
     * @exception ProtocolException if an error occurs during parsing
     */
    protected static Cookie[] parse(String set_cookie, RoRequest req)
		throws ProtocolException
    {
        int    beg = 0,
               end = 0,
	       start = 0;
        char[] buf = set_cookie.toCharArray();
        int    len = buf.length;

        Cookie cookie_arr[] = new Cookie[0], curr;


        cookies: while (true)                    // get all cookies
        {
            beg = Util.skipSpace(buf, beg);
            if (beg >= len)  break;	// no more left
	    if (buf[beg] == ',')	// empty header
	    {
		beg++;
		continue;
	    }

	    curr  = new Cookie(req);
	    start = beg;

	    boolean legal = true;

	    parts: while (true)			// parse all parts
	    {
		if (beg >= len  ||  buf[beg] == ',')  break;

		// skip empty fields
		if (buf[beg] == ';')
		{
		    beg = Util.skipSpace(buf, beg+1);
		    continue;
		}

		// first check for secure, as this is the only one w/o a '='
		if (set_cookie.regionMatches(true, beg, "secure", 0, 6))
		{
		    curr.secure = true;
		    beg += 6;

		    beg = Util.skipSpace(buf, beg);
		    if (beg < len  &&  buf[beg] == ';')	// consume ";"
			beg = Util.skipSpace(buf, beg+1);
		    else if (beg < len  &&  buf[beg] != ',')
			throw new ProtocolException("Bad Set-Cookie header: " +
						    set_cookie + "\nExpected " +
						    "';' or ',' at position " +
						    beg);

		    continue;
		}

		// alright, must now be of the form x=y
		end = set_cookie.indexOf('=', beg);
		if (end == -1)
		    throw new ProtocolException("Bad Set-Cookie header: " +
						set_cookie + "\nNo '=' found " +
						"for token starting at " +
						"position " + beg);

		String name = set_cookie.substring(beg, end).trim();
		beg = Util.skipSpace(buf, end+1);

		if (name.equalsIgnoreCase("expires"))
		    beg += 4;		// cut off the "Wdy," in the expires

		int comma = set_cookie.indexOf(',', beg);
		int semic = set_cookie.indexOf(';', beg);
		if (comma == -1  &&  semic == -1)  end = len;
		else if (comma == -1)  end = semic;
		else if (semic == -1)  end = comma;
		else end = Math.min(comma, semic);

		String value = set_cookie.substring(beg, end).trim();

		if (name.equalsIgnoreCase("expires"))
		{
		    try
			{ curr.expires = new Date(value); }
		    catch (IllegalArgumentException iae)
			{ throw new ProtocolException("Bad Set-Cookie header: "+
					set_cookie + "\nInvalid date found at "+
					"position " + beg);
			}
		}
		else if (name.equalsIgnoreCase("domain"))
		{
		    // domains are case insensitive.
		    value = value.toLowerCase();

		    // add leading dot, if missing
		    if (value.charAt(0) != '.')  value = '.' + value;

		    // must be the same domain as in the url
		    if (!curr.domain.endsWith(value))  legal = false;

		    // must have 2 or 3 dots in domain name
		    int dots;
		    String top = null;
		    if (value.length() > 3 )
			top = value.substring(value.length()-4).toLowerCase();
		    if (top != null  &&  (
			top.equals(".com")  ||  top.equals(".edu")  ||
			top.equals(".net")  ||  top.equals(".org")  ||
			top.equals(".gov")  ||  top.equals(".mil")  ||
			top.equals(".int")))
			dots = 2;
		    else
			dots = 3;

		    int pos = -1;
		    while ((pos = value.indexOf('.', pos+1)) != -1) dots--;
		    if (dots > 0)  legal = false;

		    curr.domain = value;
		}
		else if (name.equalsIgnoreCase("path"))
		    curr.path = value;
		else
		{
		    curr.name  = name;
		    curr.value = value;
		}

		beg = end;
		if (beg < len  &&  buf[beg] == ';')	// consume ";"
		    beg = Util.skipSpace(buf, beg+1);
	    }

	    if (curr.name == null  ||  curr.value == null)
                throw new ProtocolException("Bad Set-Cookie header: " +
					    set_cookie + "\nNo Name=Value found"
					    + " for cookie starting at " +
					    "posibition " + start);

	    if (legal)
	    {
		cookie_arr = Util.resizeArray(cookie_arr, cookie_arr.length+1);
		cookie_arr[cookie_arr.length-1] = curr;
	    }
	}

	return cookie_arr;
    }


    /**
     * Return the name of this cookie.
     */
    public String getName()
    {
	return name;
    }


    /**
     * Return the value of this cookie.
     */
    public String getValue()
    {
	return value;
    }


    /**
     * @return the expiry date of this cookie, or null if none set.
     */
    public Date expires()
    {
	return expires;
    }


    /**
     * @return true if the cookie should be discarded at the end of the
     *         session; false otherwise
     */
    public boolean discard()
    {
	return (expires == null);
    }


    /**
     * Return the domain this cookie is valid in.
     */
    public String getDomain()
    {
	return domain;
    }


    /**
     * Return the path this cookie is associated with.
     */
    public String getPath()
    {
	return path;
    }


    /**
     * Return whether this cookie should only be sent over secure connections.
     */
    public boolean isSecure()
    {
	return secure;
    }


    /**
     * @return true if this cookie has expired
     */
    public boolean hasExpired()
    {
	return (expires != null  &&  expires().before(new Date()));
    }


    /**
     * @param  req  the request to be sent
     * @return true if this cookie should be sent with the request
     */
    protected boolean sendWith(RoRequest req)
    {
	HTTPConnection con = req.getConnection();

	return (con.getHost().endsWith(domain)  &&
		Util.getPath(req.getRequestURI()).startsWith(path)  &&
		(!secure || con.getProtocol().equals("https") ||
		 con.getProtocol().equals("shttp")));
    }


    /**
     * Hash up name, path and domain into new hash.
     */
    public int hashCode()
    {
	return (name.hashCode() + path.hashCode() + domain.hashCode());
    }


    /**
     * Two cookies match if the name, path and domain match.
     */
    public boolean equals(Object obj)
    {
	if ((obj != null) && (obj instanceof Cookie))
	{
	    Cookie other = (Cookie) obj;
	    return  (this.name.equals(other.name)  &&
		     this.path.equals(other.path)  &&
		     this.domain.equals(other.domain));
	}
	return false;
    }


    /**
     * @return a string suitable for sending in a Cookie header.
     */
    protected String toExternalForm()
    {
	return name + "=" + value;
    }


    /**
     * Create a string containing all the cookie fields. The format is that
     * used in the Set-Cookie header.
     */
    public String toString()
    {
	String string = name + "=" + value;
	if (expires != null)  string += "; expires=" + expires;
	if (path != null)     string += "; path=" + path;
	if (domain != null)   string += "; domain=" + domain;
	if (secure)           string += "; secure";
	return string;
    }


    /**
     * Read cookies from a file into a hashtable. Each entry in the
     * hashtable will have the same key and value (i.e. we do a
     * put(cookie, cookie)).
     *
     * @param file the cookie file
     * @param list the hashtable to fill
     */
    static void readFromFile(File file, Hashtable list)
    {
	// in windows, skip the mm2048.dat and mm256.dat files
    }


    /**
     * Saves the cookies in the hashtable to a file.
     *
     * @param file the cookie file
     * @param list the hashtable of cookies
     */
    static void saveToFile(File file, Hashtable list)
    {
    }
}
