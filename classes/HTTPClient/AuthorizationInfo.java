/*
 * @(#)AuthorizationInfo.java				0.3 30/01/1998
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


import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;

import java.awt.Frame;
import java.awt.Panel;
import java.awt.Label;
import java.awt.Button;
import java.awt.Dimension;
import java.awt.TextField;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;


/**
 * Holds the information for an authorization response.
 *
 * @version	0.3  30/01/1998
 * @author	Ronald Tschal&auml;r
 */

public class AuthorizationInfo implements GlobalConstants
{
    // class fields

    /** Holds the list of lists of authorization info structures */
    private static Hashtable     CntxtList = new Hashtable();

    /** A pointer to the handler to be called when we need authorization info */
    private static AuthorizationHandler
				 AuthHandler = new MyAuthHandler();

    static
    {
	CntxtList.put(HTTPConnection.getDefaultContext(), new Hashtable());
    }


    // the instance oriented stuff

    /** the host */
    private String Host;

    /** the port */
    private int Port;

    /** the scheme. (e.g. "basic") */
    private String Scheme;

    /** the realm */
    private String Realm;

    /** the string used for the "basic" authorization scheme */
    private String basic_cookie;

    /** any parameters */
    private Vector auth_params = new Vector();

    /** additional info which won't be displayed in the toString() */
    private Object extra_info = null;

    /** a list of paths where this realm has been known to be required */
    private String[] paths = new String[0];


    // Constructors

    /**
     * Creates an new info structure for the specified host and port.
     *
     * @param host   the host
     * @param port   the port
     */
    AuthorizationInfo(String host, int port)
    {
	this.Host = host;
	this.Port = port;
    }


    /**
     * Creates a new info structure for the specified host and port with the
     * specified scheme, realm, params. The "basic" cookie is set to null.
     *
     * @param host   the host
     * @param port   the port
     * @param scheme the scheme
     * @param realm  the realm
     * @param params the parameters as an array of name/value pairs, or null
     * @param info   arbitrary extra info, or null
     */
    public AuthorizationInfo(String host, int port, String scheme,
			     String realm, NVPair params[], Object info)
    {
	this.Scheme       = scheme.trim();
	this.Host         = host.trim();
	this.Port         = port;
	this.Realm        = realm.trim();
	this.basic_cookie = null;

	if (params != null)
	{
	    auth_params.ensureCapacity(params.length);
	    for (int idx=0; idx < params.length; idx++)
		auth_params.addElement(params[idx]);
	}

	this.extra_info   = info;
    }


    /**
     * Creates a new info structure for the specified host and port with the
     * specified scheme, realm and "basic" cookie. The params is set to
     * a zero-length array, and the extra_info is set to null.
     *
     * @param host   the host
     * @param port   the port
     * @param scheme the scheme
     * @param realm  the realm
     * @param cookie the encoded username/password for the "basic" scheme
     */
    public AuthorizationInfo(String host, int port, String scheme,
			     String realm, String cookie)
    {
	this.Scheme       = scheme.trim();
	this.Host         = host.trim();
	this.Port         = port;
	this.Realm        = realm.trim();
	if (cookie != null)
	    this.basic_cookie = cookie.trim();
	else
	    this.basic_cookie = null;
    }


    /**
     * Creates a new copy of the given AuthorizationInfo.
     *
     * @param templ the info to copy
     */
    AuthorizationInfo(AuthorizationInfo templ)
    {
	this.Scheme       = templ.Scheme;
	this.Host         = templ.Host;
	this.Port         = templ.Port;
	this.Realm        = templ.Realm;
	this.basic_cookie = templ.basic_cookie;
	this.auth_params  = (Vector) templ.auth_params.clone();
	this.extra_info   = templ.extra_info;
    }


    // Class Methods

    /**
     * Set's the authorization handler. This handler is called whenever
     * the server requests authorization and no entry for the requested
     * scheme and realm can be found in the list. The handler must implement
     * the AuthorizationHandler interface.
     * <BR>If no handler is set then a default handler is used. This handler
     * currently only handles the "basic" scheme and brings up a popup which
     * prompts for the username and password.
     * <BR>The default handler can be disabled by setting the auth handler
     * to <var>null</var>.
     *
     * @param  handler the new authorization handler
     * @return the old authorization handler
     * @see    AuthorizationHandler
     */
    public static AuthorizationHandler
		    setAuthHandler(AuthorizationHandler handler)
    {
	AuthorizationHandler tmp = AuthHandler;
	AuthHandler = handler;

	return tmp;
    }


    /**
     * Get's the current authorization handler.
     *
     * @return the current authorization handler, or null if none is set.
     * @see    AuthorizationHandler
     */
    public static AuthorizationHandler getAuthHandler()
    {
	return AuthHandler;
    }


    /**
     * Searches for the authorization info using the given host, port,
     * scheme and realm. The context is the default context.
     *
     * @param  host         the host
     * @param  port         the port
     * @param  scheme       the scheme
     * @param  realm        the realm
     * @return a pointer to the authorization data or null if not found
     */
    public static AuthorizationInfo getAuthorization(
						String host, int port,
						String scheme, String realm)
    {
	return getAuthorization(host, port, scheme, realm,
				HTTPConnection.getDefaultContext());
    }


    /**
     * Searches for the authorization info in the given context using the
     * given host, port, scheme and realm.
     *
     * @param  host         the host
     * @param  port         the port
     * @param  scheme       the scheme
     * @param  realm        the realm
     * @param  context      the context this info is associated with
     * @return a pointer to the authorization data or null if not found
     */
    public static synchronized AuthorizationInfo getAuthorization(
						String host, int port,
						String scheme, String realm,
						Object context)
    {
	Hashtable AuthList = Util.getList(CntxtList, context);

	AuthorizationInfo auth_info =
	    new AuthorizationInfo(host.trim(), port, scheme.trim(),
				  realm.trim(), (NVPair[]) null, null);

	return (AuthorizationInfo) AuthList.get(auth_info);
    }


    /**
     * Queries the AuthHandler for authorization info. It also adds this
     * info to the list.
     *
     * @param  auth_info  any info needed by the AuthHandler; at a minimum the
     *                    host, scheme and realm should be set.
     * @param  req        the request which initiated this query
     * @param  resp       the full response
     * @return a structure containing the requested info, or null if either
     *	       no AuthHandler is set or the user canceled the request.
     * @exception AuthSchemeNotImplException if this is thrown by
     *                                            the AuthHandler.
     */
    static AuthorizationInfo queryAuthHandler(AuthorizationInfo auth_info,
					      RoRequest req, RoResponse resp)
	throws AuthSchemeNotImplException
    {
	if (AuthHandler == null)
	    return null;

	AuthorizationInfo new_info =
		    AuthHandler.getAuthorization(auth_info, req, resp);
	if (new_info != null)
	{
	    if (req != null)
		addAuthorization(new_info, req.getConnection().getContext());
	    else
		addAuthorization(new_info, HTTPConnection.getDefaultContext());
	}

	return new_info;
    }


    /**
     * Searches for the authorization info using the host, port, scheme and
     * realm from the given info struct. If not found it queries the
     * AuthHandler (if set).
     *
     * @param  auth_info    the AuthorizationInfo
     * @param  request      the request which initiated this query
     * @param  resp         the full response
     * @param  query_auth_h if true, query the auth-handler if no info found.
     * @return a pointer to the authorization data or null if not found
     * @exception AuthSchemeNotImplException If thrown by the AuthHandler.
     */
    static synchronized AuthorizationInfo getAuthorization(
				    AuthorizationInfo auth_info, RoRequest req,
				    RoResponse resp, boolean query_auth_h)
	throws AuthSchemeNotImplException
    {
	Hashtable AuthList;
	if (req != null)
	    AuthList = Util.getList(CntxtList, req.getConnection().getContext());
	else
	    AuthList = Util.getList(CntxtList, HTTPConnection.getDefaultContext());

	AuthorizationInfo new_info =
	    (AuthorizationInfo) AuthList.get(auth_info);

	if (new_info == null  &&  query_auth_h)
	    new_info = queryAuthHandler(auth_info, req, resp);

	return new_info;
    }


    /**
     * Searches for the authorization info given a host, port, scheme and
     * realm. Queries the AuthHandler if not found in list.
     *
     * @param  host         the host
     * @param  port         the port
     * @param  scheme       the scheme
     * @param  realm        the realm
     * @param  query_auth_h if true, query the auth-handler if no info found.
     * @return a pointer to the authorization data or null if not found
     * @exception AuthSchemeNotImplException If thrown by the AuthHandler.
     */
    static AuthorizationInfo getAuthorization(String host, int port,
					      String scheme, String realm,
					      boolean query_auth_h)
	throws AuthSchemeNotImplException
    {
	return getAuthorization(new AuthorizationInfo(host.trim(), port,
				scheme.trim(), realm.trim(), (NVPair[]) null,
				null), null, null, query_auth_h);
    }


    /**
     * Adds an authorization entry to the list using the default context.
     * If an entry for the specified scheme and realm already exists then
     * its cookie and params are replaced with the new data.
     *
     * @param auth_info the AuthorizationInfo to add
     */
    public static void addAuthorization(AuthorizationInfo auth_info)
    {
	addAuthorization(auth_info, HTTPConnection.getDefaultContext());
    }


    /**
     * Adds an authorization entry to the list. If an entry for the
     * specified scheme and realm already exists then its cookie and
     * params are replaced with the new data.
     *
     * @param auth_info the AuthorizationInfo to add
     * @param context   the context to associate this info with
     */
    public static void addAuthorization(AuthorizationInfo auth_info,
					Object context)
    {
	Hashtable AuthList = Util.getList(CntxtList, context);

	// copy path list
	AuthorizationInfo old_info =
			    (AuthorizationInfo) AuthList.get(auth_info);
	if (old_info != null  &&  auth_info.paths.length == 0)
	    auth_info.paths = old_info.paths;

	AuthList.put(auth_info, auth_info);
    }


    /**
     * Adds an authorization entry to the list using the default context.
     * If an entry for the specified scheme and realm already exists then
     * its cookie and params are replaced with the new data.
     *
     * @param host   the host
     * @param port   the port
     * @param scheme the scheme
     * @param realm  the realm
     * @param cookie the string used for the "basic" authorization scheme
     * @param params an array of name/value pairs of parameters
     * @param info   arbitrary extra auth info
     */
    public static void addAuthorization(String host, int port, String scheme,
					String realm, String cookie,
					NVPair params[], Object info)
    {
	addAuthorization(host, port, scheme, realm, cookie, params, info,
			 HTTPConnection.getDefaultContext());
    }


    /**
     * Adds an authorization entry to the list. If an entry for the
     * specified scheme and realm already exists then its cookie and
     * params are replaced with the new data.
     *
     * @param host    the host
     * @param port    the port
     * @param scheme  the scheme
     * @param realm   the realm
     * @param cookie  the string used for the "basic" authorization scheme
     * @param params  an array of name/value pairs of parameters
     * @param info    arbitrary extra auth info
     * @param context the context to associate this info with
     */
    public static void addAuthorization(String host, int port, String scheme,
					String realm, String cookie,
					NVPair params[], Object info,
					Object context)
    {
	Vector vparams = null;

	if (params != null)
	{
	    vparams = new Vector(params.length);
	    for (int idx=0; idx < params.length; idx++)
		vparams.addElement(params[idx]);
	}

	addAuthorization(host, port, scheme, realm, cookie, vparams, info,
			 context);
    }


    /**
     * Adds an authorization entry to the list. If an entry for the
     * specified scheme and realm already exists then it's cookie and
     * params are overwritten with the new data.
     *
     * @param host    the host
     * @param port    the port
     * @param scheme  the scheme
     * @param realm   the realm
     * @param cookie  the string used for the "basic" authorization scheme
     * @param params  a vector of name/value pairs of parameters
     * @param info    arbitrary extra auth info
     * @param context the context to associate this info with
     */
    static synchronized void addAuthorization(String host, int port,
					      String scheme, String realm,
					      String cookie, Vector params,
					      Object info, Object context)
    {
	AuthorizationInfo auth =
	    new AuthorizationInfo(host, port, scheme, realm, cookie);
	if (params != null)
	    auth.auth_params = params;
	auth.extra_info = info;

	addAuthorization(auth, context);
    }


    /**
     * Adds an authorization entry for the "basic" authorization scheme to
     * the list using the default context. If an entry already exists for
     * the "basic" scheme and the specified realm then it is overwritten.
     *
     * @param host   the host
     * @param port   the port
     * @param realm  the realm
     * @param user   the username
     * @param passwd the password
     */
    public static void addBasicAuthorization(String host, int port,
					     String realm, String user,
					     String passwd)
    {
	addAuthorization(host, port, "Basic", realm,
			 Codecs.base64Encode(user + ":" + passwd),
			 (NVPair[]) null, null);
    }


    /**
     * Adds an authorization entry for the "basic" authorization scheme to
     * the list. If an entry already exists for the "basic" scheme and the
     * specified realm then it is overwritten.
     *
     * @param host    the host
     * @param port    the port
     * @param realm   the realm
     * @param user    the username
     * @param passwd  the password
     * @param context the context to associate this info with
     */
    public static void addBasicAuthorization(String host, int port,
					     String realm, String user,
					     String passwd, Object context)
    {
	addAuthorization(host, port, "Basic", realm,
			 Codecs.base64Encode(user + ":" + passwd),
			 (NVPair[]) null, null, context);
    }


    /**
     * Adds an authorization entry for the "digest" authorization scheme to
     * the list using the default context. If an entry already exists for the
     * "digest" scheme and the specified realm then it is overwritten.
     *
     * @param host   the host
     * @param port   the port
     * @param realm  the realm
     * @param user   the username
     * @param passwd the password
     */
    public static void addDigestAuthorization(String host, int port,
					      String realm, String user,
					      String passwd)
    {
	addDigestAuthorization(host, port, realm, user, passwd,
			       HTTPConnection.getDefaultContext());
    }


    /**
     * Adds an authorization entry for the "digest" authorization scheme to
     * the list. If an entry already exists for the "digest" scheme and the
     * specified realm then it is overwritten.
     *
     * @param host    the host
     * @param port    the port
     * @param realm   the realm
     * @param user    the username
     * @param passwd  the password
     * @param context the context to associate this info with
     */
    public static void addDigestAuthorization(String host, int port,
					      String realm, String user,
					      String passwd, Object context)
    {
	NVPair[] params = new NVPair[4];
	params[0] = new NVPair("username", user);
	params[1] = new NVPair("uri", "");
	params[2] = new NVPair("nonce", "");
	params[3] = new NVPair("response", "");

	String extra = new MD5(user + ":" + realm + ":" + passwd).asHex();

	addAuthorization(host, port, "Digest", realm, null, params, extra,
			 context);
    }


    /**
     * Removes an authorization entry from the list using the default context.
     * If no entry for the specified host, port, scheme and realm exists then
     * this does nothing.
     *
     * @param auth_info the AuthorizationInfo to remove
     */
    public static void removeAuthorization(AuthorizationInfo auth_info)
    {
	removeAuthorization(auth_info, HTTPConnection.getDefaultContext());
    }


    /**
     * Removes an authorization entry from the list. If no entry for the
     * specified host, port, scheme and realm exists then this does nothing.
     *
     * @param auth_info the AuthorizationInfo to remove
     * @param context   the context this info is associated with
     */
    public static void removeAuthorization(AuthorizationInfo auth_info,
					   Object context)
    {
	Hashtable AuthList = Util.getList(CntxtList, context);
	AuthList.remove(auth_info);
    }


    /**
     * Removes an authorization entry from the list using the default context.
     * If no entry for the specified host, port, scheme and realm exists then
     * this does nothing.
     *
     * @param host   the host
     * @param port   the port
     * @param scheme the scheme
     * @param realm  the realm
     */
    public static void removeAuthorization(String host, int port, String scheme,
					   String realm)
    {
	removeAuthorization(
	    new AuthorizationInfo(host, port, scheme, realm, (NVPair[]) null,
				  null));
    }


    /**
     * Removes an authorization entry from the list. If no entry for the
     * specified host, port, scheme and realm exists then this does nothing.
     *
     * @param host    the host
     * @param port    the port
     * @param scheme  the scheme
     * @param realm   the realm
     * @param context the context this info is associated with
     */
    public static void removeAuthorization(String host, int port, String scheme,
					   String realm, Object context)
    {
	removeAuthorization(
	    new AuthorizationInfo(host, port, scheme, realm, (NVPair[]) null,
				  null), context);
    }


    /**
     * Tries to find the candidate in the current list of auth info for the
     * given request. The paths associated with each auth info are examined,
     * and the one with either the nearest direct parent or child is chosen.
     * This is used for preemptively sending auth info.
     *
     * @param  req  the Request
     * @return an AuthorizationInfo containing the info for the best match,
     *         or null if none found.
     */
    static AuthorizationInfo findBest(RoRequest req)
    {
	String path = Util.getPath(req.getRequestURI());
	String host = req.getConnection().getHost();
	int    port = req.getConnection().getPort();


	// First search for an exact match

	Hashtable AuthList =
		    Util.getList(CntxtList, req.getConnection().getContext());
	Enumeration list = AuthList.elements();
	while (list.hasMoreElements())
	{
	    AuthorizationInfo info = (AuthorizationInfo) list.nextElement();

	    if (!info.Host.equalsIgnoreCase(host)  ||  info.Port != port)
		continue;

	    for (int idx=0; idx<info.paths.length; idx++)
	    {
		if (path.equals(info.paths[idx]))
		    return info;
	    }
	}


	// Now find the closest parent or child

	AuthorizationInfo best = null;
	String base = path.substring(0, path.lastIndexOf('/')+1);
	int    min  = Integer.MAX_VALUE;

	list = AuthList.elements();
	while (list.hasMoreElements())
	{
	    AuthorizationInfo info = (AuthorizationInfo) list.nextElement();

	    if (!info.Host.equalsIgnoreCase(host)  ||  info.Port != port)
		continue;

	    for (int idx=0; idx<info.paths.length; idx++)
	    {
		// strip the last path segment, leaving a trailing "/"
		String ibase = info.paths[idx].substring(0,
					    info.paths[idx].lastIndexOf('/')+1);

		if (base.equals(ibase))
		    return info;

		if (base.startsWith(ibase))		// found a parent
		{
		    int num_seg = 0, pos = ibase.length()-1;
		    while ((pos = base.indexOf('/', pos+1)) != -1)  num_seg++;

		    if (num_seg < min)
		    {
			min  = num_seg;
			best = info;
		    }
		}
		else if (ibase.startsWith(base))	// found a child
		{
		    int num_seg = 0, pos = base.length();
		    while ((pos = ibase.indexOf('/', pos+1)) != -1)  num_seg++;

		    if (num_seg < min)
		    {
			min  = num_seg;
			best = info;
		    }
		}
	    }
	}

	return best;
    }


    /**
     * Adds the path from the given resource to our path list.
     *
     * @param resource the resource from which to extract the path
     */
    void addPath(String resource)
    {
	String path = Util.getPath(resource);

	// First check that we don't already have this one
	for (int idx=0; idx<paths.length; idx++)
	    if (paths[idx].equals(path)) return;

	// Ok, add it
	paths = Util.resizeArray(paths, paths.length+1);
	paths[paths.length-1] = path;
    }


    /**
     * Parses the authentication challenge(s) into an array of new info
     * structures for the specified host and port.
     *
     * @param challenge a string containing authentication info. This must
     *                  have the same format as value part of a
     *                  WWW-authenticate response header field, and may
     *                  contain multiple authentication challenges.
     * @param req       the original request.
     * @exception ProtocolException if any error during the parsing occurs.
     */
    static AuthorizationInfo[] parseAuthString(String challenge, RoRequest req,
					       RoResponse resp)
	    throws ProtocolException
    {
	int    beg = 0,
	       end = 0;
	char[] buf = challenge.toCharArray();
	int    len = buf.length;

	AuthorizationInfo auth_arr[] = new AuthorizationInfo[0],
			  curr;

	while (true)			// get all challenges
	{
	    // get scheme
	    beg  = Util.skipSpace(buf, beg);
	    if (beg == len)  break;

	    end         = Util.skipToken(buf, beg+1);

	    int sts;
	    try
		{ sts = resp.getStatusCode(); }
	    catch (IOException ioe)
		{ throw new ProtocolException(ioe.toString()); }
	    if (sts == 401)
		curr = new AuthorizationInfo(req.getConnection().getHost(),
					     req.getConnection().getPort());
	    else
		curr = new AuthorizationInfo(req.getConnection().getProxyHost(),
					    req.getConnection().getProxyPort());
	    curr.Scheme = challenge.substring(beg, end);

	    // get auth-parameters
	    boolean first = true;
	    while (true)
	    {
		beg = Util.skipSpace(buf, end);		// find ","
		if (beg == len)  break;

		if (!first)
		{
		    if (buf[beg] != ',')
			throw new ProtocolException("Bad Authentication header "
						    + "format: '" + challenge +
						    "'\nExpected \",\" at position "+
						    beg);

		    beg = Util.skipSpace(buf, beg+1);	// find param name
		    if (beg == len)  break;
		    if (buf[beg] == ',')	// skip empty params
		    {
			end = beg;
			continue;
		    }
		}
		first = false;

		NVPair param  = new NVPair();
		int    pstart = beg;

		end = Util.skipToken(buf, beg+1);	// extract name
		param.name = challenge.substring(beg, end).trim();

		beg = Util.skipSpace(buf, end);	// find "=" or ","

		if (beg < len  &&  buf[beg] != '='  &&  buf[beg] != ',')
		{  		// It's not a param, but another challenge
		    beg = pstart;
		    break;
		}


		if (buf[beg] == '=')		// we have a value
		{
		    beg = Util.skipSpace(buf, beg+1);
		    if (beg == len)
			throw new ProtocolException("Bad Authentication header "
						    + "format: " + challenge +
						    "\nUnexpected EOL after token" +
						    " at position " + (end-1));
		    if (buf[beg] != '"')	// it's a token
		    {
			end = Util.skipToken(buf, beg);
			if (end == beg)
			    throw new ProtocolException("Bad Authentication header "
				+ "format: " + challenge + "\nToken expected at " +
				"position " + beg);
			param.value = challenge.substring(beg, end);
		    }
		    else			// it's a quoted-string
		    {
			end = beg++;
			do
			    end = challenge.indexOf('"', end+1);
			while (end != -1  &&  challenge.charAt(end-1) == '\\');
			if (end == -1)
			    throw new ProtocolException("Bad Authentication header "
				+ "format: " + challenge + "\nClosing <\"> for "
				+ "quoted-string starting at position " + beg
				+ " not found");
			param.value = challenge.substring(beg, end);
			end++;
		    }
		}
		else				// this is not strictly allowed
		    param.value = null;

		if (param.name.equalsIgnoreCase("realm"))
		    curr.Realm = param.value;
		else
		    curr.auth_params.addElement(param);
	    }

	    if (curr.Realm == null)
		throw new ProtocolException("Bad Authentication header "
		    + "format: " + challenge + "\nNo realm value found");

	    auth_arr = Util.resizeArray(auth_arr, auth_arr.length+1);
	    auth_arr[auth_arr.length-1] = curr;
	}

	return auth_arr;
    }


    // Instance Methods

    /**
     * Get the host.
     *
     * @return a string containing the host name.
     */
    public final String getHost()
    {
	return Host;
    }


    /**
     * Get the port.
     *
     * @return an int containing the port number.
     */
    public final int getPort()
    {
	return Port;
    }


    /**
     * Get the scheme.
     *
     * @return a string containing the scheme.
     */
    public final String getScheme()
    {
	return Scheme;
    }


    /**
     * Get the realm.
     *
     * @return a string containing the realm.
     */
    public final String getRealm()
    {
	return Realm;
    }


    /**
     * Get the authentication parameters.
     *
     * @return an array of name/value pairs.
     */
    public final NVPair[] getParams()
    {
	NVPair[] params = new NVPair[auth_params.size()];
	auth_params.copyInto(params);
	return params;
    }


    /**
     * Set the authentication parameters.
     *
     * @param an array of name/value pairs.
     */
    public final void setParams(NVPair[] params)
    {
	auth_params.removeAllElements();
	auth_params.ensureCapacity(params.length);
	for (int idx=0; idx < params.length; idx++)
	    auth_params.addElement(params[idx]);
    }


    /**
     * Get the extra info.
     *
     * @return the extra_info object
     */
    public final Object getExtraInfo()
    {
	return extra_info;
    }


    /**
     * Set the extra info.
     *
     * @param info the extra info
     */
    public final void setExtraInfo(Object info)
    {
	extra_info = info;
    }


    /**
     * Constructs a string containing the authorization info. The format
     * is that of the http Authorization header.
     *
     * @return a String containing all info.
     */
    public String toString()
    {
	StringBuffer field = new StringBuffer(100);

	field.append(Scheme);
	field.append(" ");

	if (Scheme.equalsIgnoreCase("basic"))
	{
	    field.append(basic_cookie);
	}
	else
	{
	    field.append("realm=\"");
	    field.append(Realm);
	    field.append('"');

	    Enumeration params = auth_params.elements();
	    while (params.hasMoreElements())
	    {
		NVPair param = (NVPair) params.nextElement();
		field.append(',');
		field.append(param.name);
		field.append("=\"");
		field.append(param.value);
		field.append('"');
	    }
	}

	return field.toString();
    }


    /**
     * Produces a hash code based on Host, Scheme and Realm. Port is not
     * included for simplicity (and because it probably won't make much
     * difference). Used in the AuthorizationInfo.AuthList hash table.
     *
     * @return the hash code
     */
    public int hashCode()
    {
	return (Host.toLowerCase()+Scheme.toLowerCase()+Realm).hashCode();
    }

    /**
     * Two AuthorizationInfos are considered equal if their Host, Port,
     * Scheme and Realm match. Used in the AuthorizationInfo.AuthList hash
     * table.
     *
     * @param obj another AuthorizationInfo against which this one is
     *            to be compared.
     * @return true if they match in the above mentioned fields; false
     *              otherwise.
     */
    public boolean equals(Object obj)
    {
	if ((obj != null)  &&  (obj instanceof AuthorizationInfo))
	{
	    AuthorizationInfo auth = (AuthorizationInfo) obj;
	    if (Host.equalsIgnoreCase(auth.Host)  &&
		(Port == auth.Port)  &&
		Scheme.equalsIgnoreCase(auth.Scheme)  &&
		Realm.equals(auth.Realm))
		    return true;
	}
	return false;
    }
}


/**
 * A simple authorization handler that throws up a message box requesting
 * both a username and password. This is default authorization handler.
 * Currently only handles the authentication types "Basic", "Digest" and
 * "SOCKS5" (used for the SocksClient and not part of HTTP per se).
 */

class MyAuthHandler implements AuthorizationHandler, GlobalConstants
{
    private BasicAuthBox inp = null;


    /**
     * For Digest authentication we need to set the uri, response and
     * opaque parameters. For "Basic" and "SOCKS5" nothing is done.
     */
    public AuthorizationInfo fixupAuthInfo(AuthorizationInfo info,
					   RoRequest req,
					   AuthorizationInfo challenge,
					   RoResponse resp)
		    throws AuthSchemeNotImplException
    {
	// nothing to do for Basic and SOCKS5 schemes

	if (info.getScheme().equalsIgnoreCase("Basic")  ||
	    info.getScheme().equalsIgnoreCase("SOCKS5"))
	    return info;
	else if (!info.getScheme().equalsIgnoreCase("Digest"))
	    throw new AuthSchemeNotImplException(info.getScheme());

	if (DebugAuth)
	    System.err.println("Auth:  fixing up Authorization for host " +
				info.getHost()+":"+info.getPort() +
				"; scheme: " + info.getScheme() +
				"; realm: " + info.getRealm());


	// get various parameters from info

	int uri=-1, user=-1, alg=-1, response=-1, nonce=-1, opaque=-1,
	    digest=-1, dreq=-1;
	NVPair[] params = info.getParams();

	for (int idx=0; idx<params.length; idx++)
	{
	    String name = params[idx].name;
	    if (name.equalsIgnoreCase("uri"))
		uri = idx;
	    if (name.equalsIgnoreCase("username"))
		user = idx;
	    else if (name.equalsIgnoreCase("algorithm"))
		alg = idx;
	    else if (name.equalsIgnoreCase("response"))
		response = idx;
	    else if (name.equalsIgnoreCase("nonce"))
		nonce = idx;
	    else if (name.equalsIgnoreCase("opaque"))
		opaque = idx;
	    else if (name.equalsIgnoreCase("digest"))
		digest = idx;
	    else if (name.equalsIgnoreCase("digest-required"))
		dreq = idx;
	}


	// get various parameters from challenge

	int ch_domain=-1, ch_nonce=-1, ch_alg=-1, ch_opaque=-1, ch_stale=-1,
	    ch_dreq=-1;
	NVPair[] ch_params = null;
	if (challenge != null)
	{
	    ch_params = challenge.getParams();

	    for (int idx=0; idx<ch_params.length; idx++)
	    {
		String name = ch_params[idx].name;
		if (name.equalsIgnoreCase("domain"))
		    ch_domain = idx;
		if (name.equalsIgnoreCase("nonce"))
		    ch_nonce = idx;
		if (name.equalsIgnoreCase("opaque"))
		    ch_opaque = idx;
		if (name.equalsIgnoreCase("algorithm"))
		    ch_alg = idx;
		if (name.equalsIgnoreCase("stale"))
		    ch_stale = idx;
		if (name.equalsIgnoreCase("digest-required"))
		    ch_dreq = idx;
	    }
	}


	// currently only MD5 hash is supported

	if (alg != -1  &&  !params[alg].value.equalsIgnoreCase("MD5"))
	    throw new AuthSchemeNotImplException("Algorithm " +params[alg].value
				+ " not implemented for digest auth scheme");

	if (ch_alg != -1  &&  !ch_params[ch_alg].value.equalsIgnoreCase("MD5"))
	    throw new AuthSchemeNotImplException("Algorithm " +
				ch_params[ch_alg].value
				+ " not implemented for digest auth scheme");


	// we need to fix up uri and response, possibly also opaque

	params[uri] = new NVPair("uri", req.getRequestURI());
	if (ch_nonce != -1)
	    params[nonce] = ch_params[ch_nonce];

	String A2 = req.getMethod() + ":" + req.getRequestURI();
	String resp_val = new MD5(info.getExtraInfo() + ":" +
				  params[nonce].value + ":" +
				  new MD5(A2).asHex()).asHex();
	params[response] = new NVPair("response", resp_val);

	if (ch_opaque != -1)
	{
	    if (opaque == -1)
	    {
		params = Util.resizeArray(params, params.length+1);
		opaque = params.length-1;
	    }
	    params[opaque] = ch_params[ch_opaque];
	}

	AuthorizationInfo new_info;


	// calc digest if necessary

	boolean ch_dreq_val = false;
	if (ch_dreq != -1  && 
	    (ch_params[ch_dreq].getValue() == null  ||
	     ch_params[ch_dreq].getValue().equalsIgnoreCase("true")  ||
	     ch_params[ch_dreq].getValue().equalsIgnoreCase("\"true\"")))
	    ch_dreq_val = true;

	if ((ch_dreq_val  ||  digest != -1)  &&  req.getStream() == null)
	{
	    NVPair[] d_params;
	    if (digest == -1)
	    {
		d_params = Util.resizeArray(params, params.length+1);
		digest = params.length;
	    }
	    else
		d_params = params;
	    d_params[digest] = new NVPair("digest", calc_digest(req,
						 (String) info.getExtraInfo(),
						 params[nonce].value));

	    if (dreq == -1)	// if server requires digest, then so do we...
	    {
		dreq = d_params.length;
		d_params = Util.resizeArray(d_params, d_params.length+1);
		d_params[dreq] = new NVPair("digest-required", "true");
	    }

	    new_info = new AuthorizationInfo(info.getHost(), info.getPort(),
					     info.getScheme(), info.getRealm(),
					     d_params, info.getExtraInfo());
	}
	else if (ch_dreq_val)
	    new_info = null;
	else
	    new_info = new AuthorizationInfo(info.getHost(), info.getPort(),
					     info.getScheme(), info.getRealm(),
					     params, info.getExtraInfo());


	// add info for other domains, if listed

	if (ch_domain != -1)
	{
	    URL base = null;
	    try
	    {
		base = new URL(req.getConnection().getProtocol(),
			       req.getConnection().getHost(),
			       req.getConnection().getPort(),
			       req.getRequestURI());
	    }
	    catch (MalformedURLException mue)
		{ }

	    StringTokenizer tok =
			    new StringTokenizer(ch_params[ch_domain].value);
	    while (tok.hasMoreTokens())
	    {
		URL Uri;
		try
		    { Uri = new URL(base, tok.nextToken()); }
		catch (MalformedURLException mue)
		    { continue; }

		params[uri] = new NVPair("uri", Uri.getFile());
		AuthorizationInfo tmp =
		    new AuthorizationInfo(Uri.getHost(), Uri.getPort(),
					  info.getScheme(), info.getRealm(),
					  params, info.getExtraInfo());
		tmp.addPath(Uri.getFile());
		AuthorizationInfo.addAuthorization(tmp);
	    }
	}


	// now return the one to use

	return new_info;
    }


    /**
     * returns the requested authorization, or null if none was given.
     *
     * @param challenge the parsed challenge from the server.
     * @param req the request which solicited this response
     * @param resp the full response received
     * @return a structure containing the necessary authorization info,
     *         or null
     * @exception AuthSchemeNotImplException if the authentication scheme
     *             in the challenge cannot be handled.
     */
    public AuthorizationInfo getAuthorization(AuthorizationInfo challenge,
					      RoRequest req, RoResponse resp)
		    throws AuthSchemeNotImplException
    {
	AuthorizationInfo cred;


	if (DebugAuth)
	    System.err.println("Auth:  Requesting Authorization for host " +
				challenge.getHost()+":"+challenge.getPort() +
				"; scheme: " + challenge.getScheme() +
				"; realm: " + challenge.getRealm());


	// we only handle Basic, Digest and SOCKS5 authentication

	if (!challenge.getScheme().equalsIgnoreCase("basic")  &&
	    !challenge.getScheme().equalsIgnoreCase("Digest")  &&
	    !challenge.getScheme().equalsIgnoreCase("SOCKS5"))
	    throw new AuthSchemeNotImplException(challenge.getScheme());


	// For digest authentication, check if stale is set

	if (challenge.getScheme().equalsIgnoreCase("Digest"))
	{
	    NVPair[] params = challenge.getParams();
	    for (int idx=0; idx<params.length; idx++)
	    {
		String name = params[idx].name;
		if (name.equalsIgnoreCase("stale")  &&
		    params[idx].value.equalsIgnoreCase("true"))
		{
		    cred = AuthorizationInfo.getAuthorization(challenge, req,
							      resp, false);
		    if (cred != null)	// should always be the case
			return fixupAuthInfo(cred, req, challenge, resp);
		    break;		// should never be reached
		}
	    }
	}


	// Ask the user for username/password

	if (!req.allowUI())
	    return null;

	if (inp == null)
	    inp = new BasicAuthBox();

	NVPair answer;
	if (challenge.getScheme().equalsIgnoreCase("basic")  ||
	    challenge.getScheme().equalsIgnoreCase("Digest"))
	{
	    answer = inp.getInput("Enter username and password for realm `" +
				  challenge.getRealm() + "'",
				  "on host " + challenge.getHost() + ":" +
				  challenge.getPort(),
				  "Authentication Scheme: " +
				  challenge.getScheme());
	}
	else
	{
	    answer = inp.getInput("Enter username and password for SOCKS " +
				  "server on host ", challenge.getHost(),
				  "Authentication Method: username/password");
	}

	if (answer == null)
	    return null;


	// Now process the username/password

	if (challenge.getScheme().equalsIgnoreCase("basic"))
	{
	    cred = new AuthorizationInfo(challenge.getHost(),
					 challenge.getPort(),
					 challenge.getScheme(),
					 challenge.getRealm(),
					 Codecs.base64Encode(
						answer.name + ":" +
						answer.value));
	}
	else if (challenge.getScheme().equalsIgnoreCase("Digest"))
	{
	    String A1 = answer.name + ":" + challenge.getRealm() + ":" +
			answer.value;
	    String A1_hash = new MD5(A1).asHex();

	    NVPair[] params = new NVPair[4];
	    params[0] = new NVPair("username", answer.name);
	    params[1] = new NVPair("nonce", "");
	    params[2] = new NVPair("uri", req.getRequestURI());
	    params[3] = new NVPair("response", "");

	    cred = new AuthorizationInfo(challenge.getHost(),
					 challenge.getPort(),
					 challenge.getScheme(),
					 challenge.getRealm(),
					 params, A1_hash);
	    cred = fixupAuthInfo(cred, req, challenge, null);
	}
	else	// SOCKS5
	{
	    NVPair[] upwd = { answer };
	    cred = new AuthorizationInfo(challenge.getHost(),
					 challenge.getPort(),
					 challenge.getScheme(),
					 challenge.getRealm(),
					 upwd, null);
	}

	if (DebugAuth) System.err.println("Auth:  Got Authorization");

	return cred;
    }


    /**
     * We handle the "Authentication-info" and "Proxy-Authentication-info"
     * headers here.
     */
    public void handleAuthHeaders(Response resp, RoRequest req,
				  AuthorizationInfo prev,
				  AuthorizationInfo prxy)
	    throws IOException
    {
	String auth_info = resp.getHeader("Authentication-info");
	String prxy_info = resp.getHeader("Proxy-Authentication-info");
	try
	    { handleAuthInfo(auth_info, prxy_info, prev, prxy, resp, req, true); }
	catch (ParseException pe)
	    { throw new IOException(pe.toString()); }
    }


    /**
     * We handle the "Authentication-info" and "Proxy-Authentication-info"
     * trailers here.
     */
    public void handleAuthTrailers(Response resp, RoRequest req,
				   AuthorizationInfo prev,
				   AuthorizationInfo prxy)
	    throws IOException
    {
	String auth_info = resp.getTrailer("Authentication-info");
	String prxy_info = resp.getTrailer("Proxy-Authentication-info");
	try
	    { handleAuthInfo(auth_info, prxy_info, prev, prxy, resp, req, false); }
	catch (ParseException pe)
	    { throw new IOException(pe.toString()); }
    }


    private static void handleAuthInfo(String auth_info, String prxy_info,
				AuthorizationInfo prev, AuthorizationInfo prxy,
				Response resp, RoRequest req,
				boolean in_headers)
	    throws ParseException, IOException
    {
	Vector elems;

	if (auth_info != null)
	{
	    elems = Util.parseHeader(auth_info);
	    handle_nextnonce(prev, Util.getElement(elems, "nextnonce"));
	    handle_discard(prev, Util.getElement(elems, "discard"));
	}

	if (prxy_info != null)
	{
	    elems = Util.parseHeader(prxy_info);
	    handle_nextnonce(prxy, Util.getElement(elems, "nextnonce"));
	    handle_discard(prxy, Util.getElement(elems, "discard"));
	}

	if (in_headers)
	{
	    if ((auth_info != null  &&  Util.hasToken(auth_info, "digest"))  ||
		Util.hasToken(resp.getHeader("Trailer"), "Authentication-Info"))
		handle_digest(prev, resp, req);
	}
    }


    /**
     * Handle nextnonce field.
     */
    private static void handle_nextnonce(AuthorizationInfo prev,
					 HttpHeaderElement nextnonce)
    {
	if (prev == null  ||  nextnonce == null  ||
	    nextnonce.getValue() == null)
	    return;

	NVPair[] params = prev.getParams();

	int nonce;
	for (nonce=0; nonce<params.length; nonce++)
	    if (params[nonce].name.equalsIgnoreCase("nonce"))  break;
	if (nonce == params.length)
	    params = Util.resizeArray(params, nonce+1);

	params[nonce] = new NVPair("nonce", nextnonce.getValue());
	prev.setParams(params);
    }


    /**
     * Handle digest field of the Authentication-Info response header.
     */
    private static void handle_digest(AuthorizationInfo prev, Response resp,
				      RoRequest req)
	    throws IOException
    {
	if (prev == null)
	    return;

	NVPair[] params = prev.getParams();
	int uri;
	for (uri=0; uri<params.length; uri++)
	    if (params[uri].name.equalsIgnoreCase("uri"))  break;
	int nonce;
	for (nonce=0; nonce<params.length; nonce++)
	    if (params[nonce].name.equalsIgnoreCase("nonce"))  break;

	resp.inp_stream = new MD5InputStream(resp.inp_stream,
				new VerifyDigest((String) prev.getExtraInfo(),
						 params[nonce].getValue(),
						 req.getMethod(),
						 params[uri].getValue(), resp));
    }


    /**
     * Calculates the digest of the request body.
     */
    private static String calc_digest(RoRequest req, String A1_hash,
				      String nonce)
    {
	if (req.getStream() != null)
	    return "";

	int ct=-1, ce=-1, lm=-1, ex=-1, dt=-1;
	for (int idx=0; idx<req.getHeaders().length; idx++)
	{
	    String name = req.getHeaders()[idx].getName();
	    if (name.equalsIgnoreCase("Content-type"))
		ct = idx;
	    else if (name.equalsIgnoreCase("Content-Encoding"))
		ce = idx;
	    else if (name.equalsIgnoreCase("Last-Modified"))
		lm = idx;
	    else if (name.equalsIgnoreCase("Expires"))
		ex = idx;
	    else if (name.equalsIgnoreCase("Date"))
		dt = idx;
	}


	// Adjust this for latest spec, when stable. TBD!!!

	NVPair[] hdrs = req.getHeaders();
	String entity_body = (req.getData() == null ? "" :
					    new String(req.getData(), 0));
	String entity_info = new MD5(req.getRequestURI() + ":" +
	     (ct == -1 ? "" : hdrs[ct].getValue()) + ":" +
	     entity_body.length() + ":" +
	     (ce == -1 ? "" : hdrs[ce].getValue()) + ":" +
	     (lm == -1 ? "" : hdrs[lm].getValue()) + ":" +
	     (ex == -1 ? "" : hdrs[ex].getValue())).asHex();
	String entity_digest = A1_hash + ":" + nonce + ":" + req.getMethod() +
			":" + (dt == -1 ? "" : hdrs[dt].getValue()) +
			":" + entity_info + ":" + new MD5(entity_body).asHex();

	if (DebugAuth)
	{
	    System.err.println("Auth:  Entity-Info: '" +
		 req.getRequestURI() + ":" +
		 (ct == -1 ? "" : hdrs[ct].getValue()) + ":" +
		 entity_body.length() + ":" +
		 (ce == -1 ? "" : hdrs[ce].getValue()) + ":" +
		 (lm == -1 ? "" : hdrs[lm].getValue()) + ":" +
		 (ex == -1 ? "" : hdrs[ex].getValue()) +"'");
	    System.err.println("Auth:  Entity-Body: '" + entity_body + "'");
	    System.err.println("Auth:  Entity-Digest: '" + entity_digest + "'");
	}

	return new MD5(entity_digest).asHex();
    }


    /**
     * Handle discard token
     */
    private static void handle_discard(AuthorizationInfo prev,
				       HttpHeaderElement discard)
    {
	if (discard != null  &&  prev != null)
	    AuthorizationInfo.removeAuthorization(prev);
    }
}


class VerifyDigest implements HashVerifier
{
    private String     HA1;
    private String     nonce;
    private String     method;
    private String     uri;
    private RoResponse resp;


    public VerifyDigest(String HA1, String nonce, String method, String uri,
			RoResponse resp)
    {
	this.HA1    = HA1;
	this.nonce  = nonce;
	this.method = method;
	this.uri    = uri;
	this.resp   = resp;
    }


    public void verifyHash(byte[] hash, long len)  throws IOException
    {
	String auth_info = resp.getHeader("Authorization-Info");
	if (auth_info == null)
	    auth_info = resp.getTrailer("Authorization-Info");
	if (auth_info == null)
	    return;

	Vector pai;
	try
	    { pai = Util.parseHeader(auth_info); }
	catch (ParseException pe)
	    { throw new IOException(pe.toString()); }
	HttpHeaderElement elem = Util.getElement(pai, "digest");
	if (elem == null  ||  elem.getValue() == null)
	    return;

	byte[] digest = unHex(elem.getValue());

	String entity_info = new MD5(
				uri + ":" +
				header_val("Content-type", resp) +
				len + ":" +
				header_val("Content-Encoding", resp) + ":" +
				header_val("Last-Modified", resp) + ":" +
				header_val("Expires", resp)).asHex();
	hash = new MD5(HA1 + ":" + nonce + ":" + method + ":" +
		       header_val("Date", resp) +
		       ":" + entity_info + ":" + MD5.asHex(hash)).Final();

	for (int idx=0; idx<hash.length; idx++)
	{
	    if (hash[idx] != digest[idx])
		throw new IOException("MD5-Digest mismatch: expected " +
				      hex(digest) + " but calculated " +
				      hex(hash));
	}
    }


    private static final byte[] unHex(String hex)
    {
	byte[] digest = new byte[hex.length()/2];

	for (int idx=0; idx<digest.length; idx++)
	{
	    digest[idx] = (byte) (0xFF & Integer.parseInt(
				  hex.substring(2*idx, 2*(idx+1)), 16));
	}

	return digest;
    }


    private static final String header_val(String hdr_name, RoResponse resp)
	    throws IOException
    {
	String hdr = resp.getHeader(hdr_name);
	String tlr = resp.getTrailer(hdr_name);
	return (hdr != null ? hdr : (tlr != null ? tlr : ""));
    }


    private static final String[] split(String header, char sep)
    {
	String[] tmp = new String[5];
	char[] buf = header.toCharArray();

	int last = 0, idx = 0;
	for (int pos=0; pos<buf.length; pos++)
	{
	    if (buf[pos] == sep)
	    {
		tmp[idx++] = header.substring(last, pos);
		last = pos+1;
		if (tmp.length == idx)
		    tmp = Util.resizeArray(tmp, idx+5);
	    }
	}
	tmp[idx++] = header.substring(last, buf.length);
	if (tmp.length != idx)
	    tmp = Util.resizeArray(tmp, idx);

	return tmp;
    }


    /**
     * Produce a string of the form "A5:22:F1:0B:53"
     */
    private static String hex(byte[] buf)
    {
	StringBuffer str = new StringBuffer(buf.length*3);
	for (int idx=0; idx<buf.length; idx++)
	{
	    str.append(Character.forDigit(buf[idx] >>> 4, 16));
	    str.append(Character.forDigit(buf[idx] & 16, 16));
	    str.append(':');
	}
	str.setLength(str.length()-1);

	return str.toString();
    }
}


/**
 * This class implements a simple popup that request username and password
 * used for the "basic" and "digest" authentication schemes.
 *
 * @version	0.3  10/12/1997
 * @author	Ronald Tschal&auml;r
 */
class BasicAuthBox extends Frame
{
    private final static String title = "Authorization Request";
    private Dimension           screen;
    private Label		line1, line2, line3;
    private TextField		user, pass;
    private int                 done;
    private final static int    OK = 1, CANCEL = 0;


    /**
     * Constructs the popup with two lines of text above the input fields
     */
    BasicAuthBox()
    {
	super(title);

        screen = getToolkit().getScreenSize();

	addNotify();
	addWindowListener(new Close());
	setLayout(new BorderLayout());

	Panel p = new Panel(new GridLayout(3,1));
	p.add(line1 = new Label());
	p.add(line2 = new Label());
	p.add(line3 = new Label());
	add("North", p);

	p = new Panel(new GridLayout(2,1));
	p.add(new Label("Username:"));
	p.add(new Label("Password:"));
	add("West", p);
	p = new Panel(new GridLayout(2,1));
	p.add(user = new TextField(30));
	p.add(pass = new TextField(30));
	pass.addActionListener(new Ok());
	pass.setEchoChar('*');
	add("East", p);

	GridBagLayout gb = new GridBagLayout();
	p = new Panel(gb);
	GridBagConstraints constr = new GridBagConstraints();
	Panel pp = new Panel();
	p.add(pp);
	constr.gridwidth = GridBagConstraints.REMAINDER;
	gb.setConstraints(pp, constr);
	constr.gridwidth = 1;
	constr.weightx = 1.0;
	Button b;
	p.add(b = new Button("  OK  "));
	b.addActionListener(new Ok());
	constr.weightx = 1.0;
	gb.setConstraints(b, constr);
	p.add(b = new Button("Clear"));
	b.addActionListener(new Clear());
	constr.weightx = 2.0;
	gb.setConstraints(b, constr);
	p.add(b = new Button("Cancel"));
	b.addActionListener(new Cancel());
	constr.weightx = 1.0;
	gb.setConstraints(b, constr);
	add("South", p);

	pack();
	setResizable(false);
    }


    /**
     * our event handlers
     */
    class Ok implements ActionListener
    {
	public void actionPerformed(ActionEvent ae)
	{
	    done = OK;
	    synchronized (BasicAuthBox.this) { BasicAuthBox.this.notifyAll(); }
	}
    }

    class Clear implements ActionListener
    {
	public void actionPerformed(ActionEvent ae)
	{
	    user.setText("");
	    pass.setText("");
	    user.requestFocus();
	}
    }

    class Cancel implements ActionListener
    {
	public void actionPerformed(ActionEvent ae)
	{
	    done = CANCEL;
	    synchronized (BasicAuthBox.this) { BasicAuthBox.this.notifyAll(); }
	}
    }


    class Close extends WindowAdapter
    {
	public void windowClosing(WindowEvent we)
	{
	    new Cancel().actionPerformed(null);
	}
    }


    /**
     * the method called by MyAuthHandler.
     *
     * @return the username/password pair
     */
    synchronized NVPair getInput(String l1, String l2, String l3)
    {
	line1.setText(l1);
	line2.setText(l2);
	line3.setText(l3);

	line1.invalidate();
	line2.invalidate();
	line3.invalidate();

	pack();
	setLocation((screen.width-getPreferredSize().width)/2,
		    (int) ((screen.height-getPreferredSize().height)/2*.7));
	user.requestFocus();
	setVisible(true);

	try { wait(); } catch (InterruptedException e) { }

	setVisible(false);

	NVPair result = new NVPair(user.getText(), pass.getText());
	user.setText("");
	pass.setText("");

	if (done == CANCEL)
	    return null;
	else
	    return result;
    }
}

