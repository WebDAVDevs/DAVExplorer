/*
 * @(#)HttpURLConnection.java				0.3 30/01/1998
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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Enumeration;


/**
 * This class is a wrapper around HTTPConnection providing the interface
 * defined by java.net.URLConnection and java.net.HttpURLConnection.
 *
 * <P>This class can be used to replace the HttpClient in the JDK with this
 * HTTPClient by defining the property
 * <code>java.protocol.handler.pkgs=HTTPClient</code>.
 *
 * @version	0.3  30/01/1998
 * @author	Ronald Tschal&auml;r
 * @since	V0.3
 */

public class HttpURLConnection extends java.net.HttpURLConnection
			       implements GlobalConstants
{
    /** a list of HTTPConnections */
    private static CIHashtable  connections = new CIHashtable();

    /** the current connection */
    private HTTPConnection    con;

    /** the resource */
    private String            resource;

    /** the current method */
    private String            method;

    /** has the current method been set via setRequestMethod()? */
    private boolean           method_set;

    /** the default request headers */
    private static NVPair[]   default_headers = new NVPair[0];

    /** the request headers */
    private NVPair[]          headers;

    /** the response */
    private HTTPResponse      resp;

    /** is the redirection module activated? */
    private static boolean    doRedir = true;

    /** the output stream used for POST and PUT */
    private OutputStream      output_stream;


    static
    {
	// This is a hack for HotJava: and it does not use getURL() to
	// determine the final URL after redirecting - Remove when bug
	// fixed.
	try
	{
	    String browser = System.getProperties().getProperty("browser");
	    if (browser != null  &&  browser.equals("HotJava"))
	    {
		setFollowRedirects(false);
	    }
	}
	catch (SecurityException se)
	    { }

	// Set the User-Agent if the http.agent property is set
	try
	{
	    String agent = System.getProperties().getProperty("http.agent");
	    if (agent != null)
		setDefaultRequestProperty("User-Agent", agent);
	}
	catch (SecurityException se)
	    { }
    }


    // Constructors

    /**
     * Construct a connection to the specified url. A cache of
     * HTTPConnections is used to maximize the reuse of these across
     * multiple HttpURLConnections.
     *
     * <BR>The default method is "GET".
     *
     * @param url the url of the request
     * @exception UnknownHostException     if the host name translation failed
     * @exception ProtocolNotSuppException if the protocol is not supported
     */
    public HttpURLConnection(URL url)
	    throws UnknownHostException, ProtocolNotSuppException
    {
	super(url);

	con           = getConnection(url);
	method        = "GET";
	method_set    = false;
	resource      = url.getFile();
	headers       = default_headers;
	output_stream = null;
    }


    /**
     * Returns an HTTPConnection. A cache of connections is kept and first
     * consulted; only when the cache lookup fails is a new one created
     * and added to the cache.
     *
     * @param url the url
     * @return an HTTPConnection
     * @exception UnknownHostException     if the host name translation failed
     * @exception ProtocolNotSuppException if the protocol is not supported
     */
    private HTTPConnection getConnection(URL url)
	    throws UnknownHostException,ProtocolNotSuppException
    {
	// try the cache, using the host name

	String php = url.getProtocol() + ":" + url.getHost() + ":" +
		     ((url.getPort() != -1) ? url.getPort() :
					Util.defaultPort(url.getProtocol()));

	HTTPConnection con = (HTTPConnection) connections.get(php);
	if (con != null)  return con;


	// try the cache, using the ip address(es)

	InetAddress[] addr = InetAddress.getAllByName(url.getHost());
	String[]      pap  = new String[addr.length];
	for (int idx=0; idx<addr.length; idx++)
	{
	    pap[idx] = url.getProtocol() + ":" + addr[idx].getHostAddress() +
			":" + ((url.getPort() != -1) ? url.getPort() :
					Util.defaultPort(url.getProtocol()));

	    con = (HTTPConnection) connections.get(pap[idx]);
	    if (con != null)  return con;
	}


	// Not in cache, so create new one and cache it

	con = new HTTPConnection(url);
	connections.put(php, con);
	for (int idx=0; idx<pap.length; idx++)
	    connections.put(pap[idx], con);

	return con;
    }


    // Methods

    /**
     * Sets the request method (e.g. "PUT" or "HEAD"). Can only be set
     * before connect() is called.
     *
     * @param method the http method.
     * @exception ProtocolException if already connected.
     */
    public void setRequestMethod(String method)  throws ProtocolException
    {
	if (connected)
	    throw new ProtocolException("Already connected!");

	if (DebugURLC)
	    System.err.println("URLC:  (" + url + ") Setting request method: "
				+ method);

	this.method = method.trim().toUpperCase();
	method_set  = true;
    }


    /**
     * Return the request method used.
     *
     * @return the http method.
     */
    public String getRequestMethod()
    {
	return method;
    }


    /**
     * Get the response code. Calls connect() if not connected.
     *
     * @return the http response code returned.
     */
    public int getResponseCode()  throws IOException
    {
	if (!connected)  connect();

	try
	    { return resp.getStatusCode(); }
	catch (ModuleException me)
	    { throw new IOException(me.toString()); }
    }


    /**
     * Get the response message describing the response code. Calls connect()
     * if not connected.
     *
     * @return the http response message returned with the response code.
     */
    public String getResponseMessage()  throws IOException
    {
	if (!connected)  connect();

	try
	    { return resp.getReasonLine(); }
	catch (ModuleException me)
	    { throw new IOException(me.toString()); }
    }


    /**
     * Get the value part of a header. Calls connect() if not connected.
     *
     * @param  name the of the header.
     * @return the value of the header, or null if no such header was returned.
     */
    public String getHeaderField(String name)
    {
	try
	{
	    if (!connected)  connect();
	    return resp.getHeader(name);
	}
	catch (Exception e)
	    { return null; }
    }


    /**
     * Get the value part of a header and converts it to an int. If the
     * header does not exist or if its value could not be converted to an
     * int then the default is returned. Calls connect() if not connected.
     *
     * @param  name the of the header.
     * @param  def  the default value to return in case of an error.
     * @return the value of the header, or null if no such header was returned.
     */
    public int getHeaderFieldInt(String name, int def)
    {
	try
	{
	    if (!connected)  connect();
	    return resp.getHeaderAsInt(name);
	}
	catch (Exception e)
	    { return def; }
    }


    /**
     * Get the value part of a header, interprets it as a date and converts
     * it to a long representing the number of milliseconds since 1970. If
     * the header does not exist or if its value could not be converted to a
     * date then the default is returned. Calls connect() if not connected.
     *
     * @param  name the of the header.
     * @param  def  the default value to return in case of an error.
     * @return the value of the header, or def in case of an error.
     */
    public long getHeaderFieldDate(String name, long def)
    {
	try
	{
	    if (!connected)  connect();
	    return resp.getHeaderAsDate(name).getTime();
	}
	catch (Exception e)
	    { return def; }
    }


    /**
     * Gets header name of the n-th header. Calls connect() if not connected.
     *
     * @param n which header to return.
     * @return the header name, or null if not that many headers.
     */
    public String getHeaderFieldKey(int n)
    {
	Enumeration enum;

	try
	{
	    if (!connected)  connect();
	    enum = resp.listHeaders();
	}
	catch (Exception e)
	    { return null; }

	while (n-- > 0  &&  enum.hasMoreElements())
	    enum.nextElement();

	if (!enum.hasMoreElements())
	    return null;

	return (String) enum.nextElement();
    }


    /**
     * Gets header value of the n-th header. Calls connect() if not connected.
     *
     * @param n which header to return.
     * @return the header value, or null if not that many headers.
     */
    public String getHeaderField(int n)
    {
	String name = getHeaderFieldKey(n);
	if (name == null)
	    return null;

	try
	    { return resp.getHeader(name); }
	catch (Exception e)
	    { return null; }
    }


    /**
     * Gets an input stream from which the data in the response may be read.
     * Calls connect() if not connected.
     *
     * @return an InputStream
     * @exception ProtocolException if input not enabled.
     * @see java.net.URLConnection#setDoInput(boolean)
     */
    public InputStream getInputStream()  throws IOException
    {
	if (!doInput)
	    throw new ProtocolException("Input not enabled! (use setDoInput(true))");

	if (!connected)  connect();

	InputStream stream;
	try
	{
	    stream = resp.getInputStream();

	    if (resp.pe != null  &&  resp.getHeader("Content-length") != null)
	    {
		try
		    { stream = new sun.net.www.MeteredStream(stream, resp.pe); }
		catch (Throwable t)
		    { if (DebugURLC)  t.printStackTrace(); }
	    }
	    else
		// some things expect this stream to support mark/reset
		stream = new BufferedInputStream(stream);
	}
	catch (ModuleException e)
	    { throw new IOException(e.toString()); }

	return stream;
    }


    /**
     * Gets an output stream which can be used send an entity with the
     * request. Can be called multiple times, in which case always the
     * same stream is returned.
     *
     * <P>The default request method changes to "POST" when this method is
     * called. Cannot be called after connect().
     *
     * <P>If no Content-type has been set it defaults to
     * <var>application/x-www-form-urlencoded</var>. Furthermore, if the
     * Content-type is <var>application/x-www-form-urlencoded</var> then all
     * output will be collected in a buffer before sending it to the server;
     * otherwise an HttpOutputStream is used.
     *
     * @return an OutputStream
     * @exception ProtocolException if already connect()'ed, if output is not
     *                              enabled or if the request method does not
     *                              support output.
     * @see java.net.URLConnection#setDoOutput(boolean)
     * @see HTTPClient.HttpOutputStream
     */
    public synchronized OutputStream getOutputStream()  throws IOException
    {
	if (connected)
	    throw new ProtocolException("Already connected!");

	if (!doOutput)
	    throw new ProtocolException("Output not enabled! (use setDoOutput(true))");
	if (!method_set)
	    method = "POST";
	else if (method.equals("HEAD")  ||  method.equals("GET")  ||
		 method.equals("TRACE"))
	    throw new ProtocolException("Method "+method+" does not support output!");

	if (getRequestProperty("Content-type") == null)
	    setRequestProperty("Content-type", "application/x-www-form-urlencoded");

	if (output_stream == null)
	{
	    if (DebugURLC)
		System.err.println("URLC:  (" +url+ ") creating output stream");

	    // Hack: because of restrictions when using true output streams
	    // and because form-data is usually quite limited in size, we
	    // first collect all data before sending it if this is form-data.
	    if (getRequestProperty("Content-type").equals(
		"application/x-www-form-urlencoded"))
		output_stream = new ByteArrayOutputStream(300);
	    else
	    {
		output_stream = new HttpOutputStream();
		connect();
	    }
	}

	return output_stream;
    }


    /**
     * Gets the url for this connection. If we're connect()'d and the request
     * was redirected then the url returned is that of the final request.
     *
     * @return the final url, or null if any exception occured.
     */
    public URL getURL()
    {

	if (connected)
	{
	    try
	    {
		if (resp.getEffectiveURL() != null)
		    return resp.getEffectiveURL();
	    }
	    catch (Exception e)
		{ return null; }
	}

	return url;
    }


    /**
     * Sets the <var>If-Modified-Since</var> header.
     *
     * @param time the number of milliseconds since 1970.
     */
    public void setIfModifiedSince(long time)
    {
	super.setIfModifiedSince(time);
	setRequestProperty("If-Modified-Since", Util.httpDate(new Date(time)));
    }


    /**
     * Sets an arbitrary request header.
     *
     * @param name  the name of the header.
     * @param value the value for the header.
     */
    public void setRequestProperty(String name, String value)
    {
	if (DebugURLC)
	    System.err.println("URLC:  (" + url + ") Setting request property: "
				+ name + " : " + value);

	int idx;
	for (idx=0; idx<headers.length; idx++)
	{
	    if (headers[idx].getName().equalsIgnoreCase(name))
		break;
	}

	if (idx == headers.length)
	    headers = Util.resizeArray(headers, idx+1);

	headers[idx] = new NVPair(name, value);
    }


    /**
     * Gets the value of a given request header.
     *
     * @param name  the name of the header.
     * @return the value part of the header, or null if no such header.
     */
    public String getRequestProperty(String name)
    {
	for (int idx=0; idx<headers.length; idx++)
	{
	    if (headers[idx].getName().equalsIgnoreCase(name))
		return headers[idx].getValue();
	}

	return null;
    }


    /**
     * Sets an arbitrary default request header. All headers set here are
     * automatically sent with each request.
     *
     * @param name  the name of the header.
     * @param value the value for the header.
     */
    public static void setDefaultRequestProperty(String name, String value)
    {
	if (DebugURLC)
	    System.err.println("URLC:  Setting default request property: " +
				name + " : " + value);

	int idx;
	for (idx=0; idx<default_headers.length; idx++)
	{
	    if (default_headers[idx].getName().equalsIgnoreCase(name))
		break;
	}

	if (idx == default_headers.length)
	    default_headers = Util.resizeArray(default_headers, idx+1);

	default_headers[idx] = new NVPair(name, value);
    }


    /**
     * Gets the value for a given default request header.
     *
     * @param name  the name of the header.
     * @return the value part of the header, or null if no such header.
     */
    public static String getDefaultRequestProperty(String name)
    {
	for (int idx=0; idx<default_headers.length; idx++)
	{
	    if (default_headers[idx].getName().equalsIgnoreCase(name))
		return default_headers[idx].getValue();
	}

	return null;
    }


    /**
     * Enables or disables the automatic handling of redirection responses.
     *
     * @param set enables automatic redirection handling if true.
     */
    public static void setFollowRedirects(boolean set)
    {
	doRedir = set;

	Class redir;
	try
	    { redir = Class.forName("HTTPClient.RedirectionModule"); }
	catch (ClassNotFoundException cnfe)
	    { throw new NoClassDefFoundError(cnfe.getMessage()); }

	if (doRedir)
	    HTTPConnection.addDefaultModule(redir, 2);
	else
	    HTTPConnection.removeDefaultModule(redir);
    }


    /**
     * Says whether redirection responses are handled automatically or not.
     *
     * @return true if automatic redirection handling is enabled.
     */
    public static boolean getFollowRedirects()
    {
	return doRedir;
    }


    /**
     * Connects to the server (if connection not still kept alive) and
     * issues the request.
     */
    public synchronized void connect()  throws IOException
    {
	if (connected)  return;

	if (DebugURLC)
	    System.err.println("URLC:  (" + url + ") Connecting ...");

	// useCaches TBD!!!

	con.setAllowUserInteraction(allowUserInteraction);

	try
	{
	    if (output_stream instanceof ByteArrayOutputStream)
		resp = con.ExtensionMethod(method, resource,
			((ByteArrayOutputStream) output_stream).toByteArray(),
					 headers);
	    else
		resp = con.ExtensionMethod(method, resource,
				    (HttpOutputStream) output_stream, headers);
	}
	catch (ModuleException e)
	    { throw new IOException(e.toString()); }

	connected = true;

	try
	    { resp.setProgressEntry(new sun.net.ProgressEntry(url.getFile(), null)); }
	catch (Throwable t)
	    { if (DebugURLC)  t.printStackTrace(); }
    }


    /**
     * Closes all the connections to this server.
     */
    public void disconnect()
    {
	if (DebugURLC)
	    System.err.println("URLC:  (" + url + ") Disconnecting ...");

	con.stop();

	/*
	try
	{
	    if (connected)
		resp.getInputStream().close();
	}
	catch (Exception e)
	    { }
	*/

	if (resp != null)
	    resp.unsetProgressEntry();
    }


    protected void finalize()
    {
	if (resp != null)
	    resp.unsetProgressEntry();
    }


    /**
     * Shows if request are being made through an http proxy or directly.
     *
     * @return true if an http proxy is being used.
     */
    public boolean usingProxy()
    {
	return (con.getProxyHost() != null ? true : false);
    }


    /**
     * produces a string.
     * @return a string containing the HttpURLConnection
     */
    public String toString()
    {
	return getClass().getName() + "[" + url + "]";
    }
}

