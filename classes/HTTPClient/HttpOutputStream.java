/*
 * @(#)HttpOutputStream.java				0.3 30/01/1998
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


import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * This class provides an output stream for requests. The stream must first
 * be associated with a request before it may be used; this is done by
 * passing it to one of the request methods in HTTPConnection.
 *
 * <P>There are two constructors for this class, one taking a length parameter
 * and one without any parameters. If the stream is created with a length
 * then the request will be sent with the corresponding Content-length header
 * and anything written to the stream will be written on the socket immediately.
 * This is the preferred way. If the stream is created without a length then
 * one of two things will happen: if, at the time of the request, the server
 * is known to understand HTTP/1.1 then each write() will send the data
 * immediately using the chunked encoding. If, however, either the server
 * version is unknown (because this is first request to that server) or the
 * server only understands HTTP/1.0 then all data will be written to a buffer
 * first, and only when the stream is closed will the request be sent.
 *
 * <P>Another reason that using the <var>HttpOutputStream(length)</var>
 * constructor is recommended over the <var>HttpOutputStream()</var> one is
 * that some HTTP/1.1 servers do not allow the chunked transfer encoding to
 * be used when POSTing to a cgi script. This is because the way the cgi API
 * is defined the cgi script expects a Content-length environment variable.
 * If the data is sent using the chunked transfer encoding however, then the
 * server would have to buffer all the data before invoking the cgi so that
 * this variable could be set correctly. Not all servers are willing to do
 * this.
 *
 * <P>The behaviour of a request sent with an output stream may differ from
 * that of request sent with a data parameter. The reason for this is that
 * the various modules cannot resend a request which used an output stream.
 * Therefore such things as authorization and retrying of requests won't be
 * done by the HTTPClient for such a request.
 *
 * @version	0.3  30/01/1998
 * @author	Ronald Tschal&auml;r
 * @since	V0.3
 */

public class HttpOutputStream extends OutputStream implements GlobalConstants
{
    /** the length of the data to be sent */
    private int length;

    /** the length of the data received so far */
    private int rcvd = 0;

    /** the request this stream is associated with */
    private Request req = null;

    /** the response from sendRequest if we stalled the request */
    private Response resp = null;

    /** the socket output stream */
    private OutputStream os = null;

    /** the buffer to be used if needed */
    private ByteArrayOutputStream bos = null;

    /** the timeout to pass to SendRequest() */
    private int con_to = 0;

    /** just ignore all the data if told to do so */
    private boolean ignore = false;


    // Constructors

    /**
     * Creates an output stream of unspecified length. Note that it is
     * <strong>highly</strong> recommended that this constructor be avoided
     * where possible and <code>HttpOutputStream(int)</code> used instead.
     *
     * @see HttpOutputStream#HttpOutputStream(int)
     */
    public HttpOutputStream()
    {
	length = -1;
    }


    /**
     * This creates an output stream which will take <var>length</var> bytes
     * of data.
     *
     * @param length the number of bytes which will be sent over this stream
     */
    public HttpOutputStream(int length)
    {
	if (length < 0)
	   throw new IllegalArgumentException("Length must be greater equal 0");
	this.length = length;
    }


    // Methods

    /**
     * Associates this stream with a request and the actual output stream.
     * No other methods in this class may be invoked until this method has
     * been invoked by the HTTPConnection.
     *
     * @param req    the request this stream is to be associated with
     * @param os     the underlying output stream to write our data to, or null
     *               if we should write to a ByteArrayOutputStream instead.
     * @param con_to connection timeout to use in sendRequest()
     */
    void goAhead(Request req, OutputStream os, int con_to)
    {
	this.req    = req;
	this.os     = os;
	this.con_to = con_to;

	if (os == null)
	    bos = new ByteArrayOutputStream();
    }


    /**
     * Setup this stream to dump the data to the great bit-bucket in the sky.
     * This is needed for when a module handles the request directly.
     *
     * @param req the request this stream is to be associated with
     */
    void ignoreData(Request req)
    {
	this.req = req;
	ignore = true;
    }


    /**
     * Return the response we got from sendRequest(). This waits until
     * the request has actually been sent.
     *
     * @return the response returned by sendRequest()
     */
    synchronized Response getResponse()
    {
	while (resp == null)
	    try { wait(); } catch (InterruptedException ie) { }

	return resp;
    }


    /**
     * Returns the number of bytes this stream is willing to accept, or -1
     * if it is unbounded.
     *
     * @return the number of bytes
     */
    public int getLength()
    {
	return length;
    }


    /**
     * Writes a single byte on the stream. It is subject to the same rules
     * as <code>write(byte[], int, int)</code>.
     *
     * @param b the byte to write
     * @exception IOException if any exception is thrown by the socket
     * @see #write(byte[], int, int)
     */
    public void write(int b)  throws IOException, IllegalAccessError
    {
	byte[] tmp = { (byte) b };
	write(tmp, 0, 1);
    }


    /**
     * Writes an array of bytes on the stream. This method may not be used
     * until this stream has been passed to one of the methods in
     * HTTPConnection (i.e. until it has been associated with a request).
     *
     * @param buf an array containing the data to write
     * @param off the offset of the data whithin the buffer
     * @param len the number bytes (starting at <var>off</var>) to write
     * @exception IOException if any exception is thrown by the socket, or
     *            if writing <var>len</var> bytes would cause more bytes to
     *            be written than this stream is willing to accept.
     * @exception IllegalAccessError if this stream has not been associated
     *            with a request yet
     */
    public synchronized void write(byte[] buf, int off, int len)
	    throws IOException, IllegalAccessError
    {
	if (req == null)
	    throw new IllegalAccessError("Stream not associated with a request");

	if (ignore) return;

	try
	{
	    if (length != -1  &&  rcvd+len > length)
		throw new IOException("Tried to write too many bytes (" +
				      (rcvd+len) + " > " + length + ")");

	    if (bos != null)
		bos.write(buf, off, len);
	    else
	    {
		if (length != -1)
		    os.write(buf, off, len);
		else
		    os.write(Codecs.chunkedEncode(buf, off, len, null, false));
	    }
	}
	catch (IOException ioe)
	{
	    req.getConnection().closeDemux(ioe);
	    req.getConnection().outputFinished();
	    throw ioe;
	}

	rcvd += len;
    }


    /**
     * Closes the stream and causes the data to be sent if it has not already
     * been done so. This method <strong>must</strong> be invoked when all
     * data has been written.
     *
     * @exception IOException if any exception is thrown by the underlying
     *            socket, or if too few bytes were written.
     * @exception IllegalAccessError if this stream has not been associated
     *            with a request yet.
     */
    public synchronized void close()  throws IOException, IllegalAccessError
    {
	if (req == null)
	    throw new IllegalAccessError("Stream not associated with a request");

	if (ignore) return;

	if (bos != null)
	{
	    req.setData(bos.toByteArray());
	    req.setStream(null);
	    try
		{ resp = req.getConnection().sendRequest(req, con_to); }
	    catch (ModuleException me)
		{ throw new IOException(me.toString()); }
	    notify();
	}
	else
	{
	    try
	    {
		if (length == -1)
		    os.write(Codecs.chunkedEncode(null, 0, 0, null, true));
		else if (rcvd < length)
		    throw new IOException("Premature close: only " + rcvd +
					  " bytes written instead of exptected "
					  + length);

		os.flush();
	    }
	    catch (IOException ioe)
	    {
		req.getConnection().closeDemux(ioe);
		throw ioe;
	    }
	    finally
	    {
		req.getConnection().outputFinished();
	    }
	}
    }


    /**
     * produces a string describing this stream.
     *
     * @return a string containing the name and the length
     */
    public String toString()
    {
	return getClass().getName() + "[length=" + length + "]";
    }
}

