/*
 * @(#)RedirectionModule.java				0.3 30/01/1998
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

import java.net.URL;
import java.net.InetAddress;
import java.net.ProtocolException;
import java.net.UnknownHostException;
import java.net.MalformedURLException;
import java.io.IOException;
import java.util.Hashtable;


/**
 * This module handles the redirection status codes 301, 302, 303, 305, 306
 * and 307.
 *
 * @version	0.3  30/01/1998
 * @author	Ronald Tschal&auml;r
 */

class RedirectionModule implements HTTPClientModule, GlobalConstants
{
    /** a list of permanent redirections (301) */
    private static Hashtable perm_redir_cntxt_list = new Hashtable();

    /** the level of redirection */
    private int level;

    /** the url used in the last redirection */
    private URL lastURL;


    // Constructors

    /**
     * Start with level 0.
     */
    RedirectionModule()
    {
	level   = 0;
	lastURL = null;
    }


    // Methods

    /**
     * Invoked by the HTTPClient.
     */
    public int requestHandler(Request req, Response[] resp)
    {
	HTTPConnection con = req.getConnection();
	URL new_loc,
	    cur_loc;
	    
	try
	{
	    cur_loc = new URL(con.getProtocol(), con.getHost().toLowerCase(),
			      con.getPort(), req.getRequestURI());
	}
	catch (MalformedURLException mue)
	{
	    throw new Error("HTTPClient Internal Error: unexpected exception '"
			    + mue + "'");
	}


	// handle permanent redirections

	Hashtable perm_redir_list = Util.getList(perm_redir_cntxt_list,
					    req.getConnection().getContext());
	if ((new_loc = (URL) perm_redir_list.get(cur_loc)) != null)
	{
	    /* copy params and query if present in old url but not
	     * in new url. This isn't strictly conforming, but some
	     * scripts fail to propogate the query properly in the
	     * Location header.
	     */
	    String nres    = new_loc.getFile(),
		   oparams = Util.getParams(req.getRequestURI()),
		   nparams = Util.getParams(nres),
		   oquery  = Util.getQuery(req.getRequestURI()),
		   nquery  = Util.getQuery(nres);
	    if (nparams == null  &&  oparams != null)
		nres += ";" + oparams;
	    if (nquery == null  &&  oquery != null)
		nres += "?" + oquery;
	    req.setRequestURI(nres);

	    try
		{ lastURL = new URL(new_loc, nres); }
	    catch (MalformedURLException mue)
		{ }

	    if (DebugMods)
		System.err.println("RdirM: matched request in permanent " +
				   "redirection list - redoing request to " +
				   lastURL);

	    if (!sameServer(con, new_loc))
	    {
		try
		    { con = new HTTPConnection(new_loc); }
		catch (ProtocolNotSuppException pnse)
		{
		    throw new Error("HTTPClient Internal Error: unexpected " +
				    "exception '" + pnse + "'");
		}

		req.setConnection(con);
		return REQ_NEWCON_RST;
	    }
	    else
	    {
		return REQ_RESTART;
	    }
	}

	return REQ_CONTINUE;
    }


    /**
     * Invoked by the HTTPClient.
     */
    public void responsePhase1Handler(Response resp, RoRequest req)
	    throws IOException
    {
	int sts  = resp.getStatusCode();
	if (sts < 301  ||  sts > 307  ||  sts == 304)
	{
	    if (lastURL != null)		// it's been redirected
		resp.setEffectiveURL(lastURL);
	}
    }


    /**
     * Invoked by the HTTPClient.
     */
    public int responsePhase2Handler(Response resp, Request req)
	    throws IOException
    {
	/* handle various response status codes until satisfied */

	int sts  = resp.getStatusCode();
	switch(sts)
	{
	    case 302: // General (temporary) Redirection (handle like 303)

		if (DebugMods)
		    System.err.println("RdirM: Received status: " + sts +
				       " " + resp.getReasonLine() +
				       " - treating as 303");

		sts = 303;

	    case 301: // Moved Permanently
	    case 303: // See Other (use GET)
	    case 307: // Moved Temporarily (we mean it!)

		if (DebugMods)
		    System.err.println("RdirM: Handling status: " + sts +
				       " " + resp.getReasonLine());

		// the spec says automatic redirection may only be done if
		// the second request is a HEAD or GET.
		if (!req.getMethod().equalsIgnoreCase("GET")  &&
		    !req.getMethod().equalsIgnoreCase("HEAD")  &&
		    sts != 303)
		{
		    if (DebugMods)
			System.err.println("RdirM: not redirected because " +
					   "method is neither HEAD nor GET");

		    resp.setEffectiveURL(lastURL);
		    return RSP_CONTINUE;
		}

	    case 305: // Use Proxy (use GET)
	    case 306: // Switch Proxy

		if (DebugMods)
		    if (sts == 305  ||  sts == 306)
			System.err.println("RdirM: Handling status: " + sts +
					   " " + resp.getReasonLine());

		// Don't accept 305 from a proxy
		if (sts == 305  &&  req.getConnection().getProxyHost() != null)
		{
		    if (DebugMods)
			System.err.println("RdirM: 305 ignored because " +
					   "a proxy is already in use");

		    resp.setEffectiveURL(lastURL);
		    return RSP_CONTINUE;
		}


		// the level is a primitive way of preventing infinite
		// redirections
		if (level == 5  ||  (resp.getHeader("Location") == null  &&
				     resp.getHeader("Set-Proxy") == null))
		{
		    if (DebugMods)
		    {
			if (level == 5)
			    System.err.println("RdirM: not redirected because "+
					   "too many levels of redirection");
			else
			    System.err.println("RdirM: not redirected because "+
					   "no Location or Set-Proxy header " +
					   "present");
		    }

		    resp.setEffectiveURL(lastURL);
		    return RSP_CONTINUE;
		}
		level++;

		URL loc;
		try
		    { loc = new URL(resp.getHeader("Location")); }
		catch (MalformedURLException mue)
		{
		    // it might be a relative URL (i.e. another broken server)
		    try
		    {
			URL base = new URL(req.getConnection().getProtocol(),
					   req.getConnection().getHost(),
					   req.getConnection().getPort(),
					   req.getRequestURI());
			loc = new URL(base, resp.getHeader("Location"));
		    }
		    catch (java.net.MalformedURLException mue2)
		    {
			throw new ProtocolException("Malformed URL in " +
				"Location header received: " +
				resp.getHeader("Location"));
		    }
		}


		if (req.getStream() != null  &&  (sts == 306  ||  sts == 305))
		    return RSP_CONTINUE;

		HTTPConnection mvd;
		boolean        new_con = false;
		String nres;
		if (sts == 305)
		{
		    mvd = new HTTPConnection(req.getConnection().getProtocol(),
					     req.getConnection().getHost(),
					     req.getConnection().getPort());
		    mvd.setCurrentProxy(loc.getHost(), loc.getPort());
		    new_con = true;

		    nres = req.getRequestURI();
		    req.setMethod("GET");
		    req.setData(null);
		    req.setStream(null);
		}
		else if (sts == 306)
		{
		    // We'll have to wait for Josh to create a new spec here.
		    return RSP_CONTINUE;
		}
		else
		{
		    if (sameServer(req.getConnection(), loc))
			mvd = req.getConnection();
		    else
		    {
			mvd = new HTTPConnection(loc);
			new_con = true;
		    }

		    nres = loc.getFile();

		    /* copy params and query if present in old url but not
		     * in new url. This isn't strictly conforming, but some
		     * scripts fail to propogate the query properly in the
		     * Location header.
		     */
		    String oparams = Util.getParams(req.getRequestURI()),
			   nparams = Util.getParams(nres),
			   oquery  = Util.getQuery(req.getRequestURI()),
			   nquery  = Util.getQuery(nres);
		    if (nparams == null  &&  oparams != null)
			nres += ";" + oparams;
		    if (nquery == null  &&  oquery != null)
			nres += "?" + oquery;

		    if (sts == 303  &&  !req.getMethod().equals("HEAD"))
		    {
			// 303 means "use GET"

			req.setMethod("GET");
			req.setData(null);
			req.setStream(null);
		    }
		    else if (sts == 301)
		    {
			// update permanent redirection list

			HTTPConnection con = req.getConnection();
			URL cur_loc = new URL(con.getProtocol(),
					      con.getHost().toLowerCase(),
					      con.getPort(),
					      req.getRequestURI());
			if (!Util.sameHttpURL(cur_loc, new URL(loc, nres)))
			{
			    Hashtable perm_redir_list =
				Util.getList(perm_redir_cntxt_list,
					     req.getConnection().getContext());
			    perm_redir_list.put(cur_loc, loc);
			}
		    }

		    // Adjust Referer, if present
		    NVPair[] hdrs = req.getHeaders();
		    for (int idx=0; idx<hdrs.length; idx++)
			if (hdrs[idx].getName().equalsIgnoreCase("Referer"))
			{
			    HTTPConnection con = req.getConnection();
			    hdrs[idx] =
				new NVPair("Referer", con+req.getRequestURI());
			    break;
			}
		}

		req.setConnection(mvd);
		req.setRequestURI(nres);

		try { resp.getInputStream().close(); }
		catch (IOException ioe) { }

		if (sts != 305  &&  sts != 306)
		{
		    lastURL = new URL(loc, nres);

		    if (DebugMods)
			System.err.println("RdirM: request redirected to " + 
					    lastURL + " using method " +
					    req.getMethod());
		}
		else
		{
		    if (DebugMods)
			System.err.println("RdirM: resending request using " + 
					    "proxy " + mvd.getProxyHost() +
					    ":" + mvd.getProxyPort());
		}

		if (new_con)
		    return RSP_NEWCON_REQ;
		else
		    return RSP_REQUEST;

	    default:

		return RSP_CONTINUE;
	}
    }


    /**
     * Invoked by the HTTPClient.
     */
    public void responsePhase3Handler(Response resp, RoRequest req)
    {
    }


    /**
     * Invoked by the HTTPClient.
     */
    public void trailerHandler(Response resp, RoRequest req)
    {
    }


    /**
     * Tries to determine as best as possible if <var>url</var> refers
     * to the same server as <var>con</var> is talking with.
     *
     * @param con the HTTPConnection
     * @param url the http URL
     * @return true if the url refers to the same server as the connection,
     *         false otherwise.
     */
    private boolean sameServer(HTTPConnection con, URL url)
    {
	if (!url.getProtocol().equalsIgnoreCase(con.getProtocol()))
	    return false;

	try
	{
	    compAddr: if (!url.getHost().equalsIgnoreCase(con.getHost()))
	    {
		InetAddress[] list1 = InetAddress.getAllByName(url.getHost());
		InetAddress[] list2 = InetAddress.getAllByName(con.getHost());
		for (int idx1=0; idx1<list1.length; idx1++)
		    for (int idx2=0; idx2<list2.length; idx2++)
			if (list1[idx1].equals(list2[idx2]))
			    break compAddr;
		return false;
	    }
	}
	catch (UnknownHostException uhe)
	    { return false; }

	if (url.getPort() != con.getPort()  &&
	    !(url.getPort() == -1  &&
	      con.getPort() == Util.defaultPort(con.getProtocol())))
	    return false;

	return true;
    }
}

