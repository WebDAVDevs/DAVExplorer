/*
 * @(#)StreamDemultiplexor.java				0.3 30/01/1998
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


import java.io.*;
import java.net.Socket;
import java.util.Vector;
import java.util.Enumeration;

/**
 * This class handles the demultiplexing of input stream. This is needed
 * for things like keep-alive in HTTP/1.0, persist in HTTP/1.1 and in HTTP-NG.
 *
 * @version	0.3  30/01/1998
 * @author	Ronald Tschal&auml;r
 */

class StreamDemultiplexor implements GlobalConstants
{
    /** the protocol were handling request for */
    private int                    Protocol;

    /** the connection we're working for */
    private HTTPConnection         Connection;

    /** the input stream to demultiplex */
    private ExtBufferedInputStream Stream;

    /** the socket this hangs off */
    private Socket                 Sock = null;

    /** signals after the closing of which stream to close the socket */
    private ResponseHandler        MarkedForClose;

    /** timer used to close the socket if unused for a given time */
    private SocketTimeout          Timer = null;

    /** a Vector to hold the list of response handlers were serving */
    private LinkedList             RespHandlerList;

    /** number of unread bytes in current chunk (if transf-enc == chunked) */
    private int                    chunk_len;

    /** the currently set timeout for the socket */
    private int                    cur_timeout = 0;


    // Constructors

    /**
     * a simple contructor.
     *
     * @param protocol   the protocol used on this stream.
     * @param sock       the socket which we're to demux.
     * @param connection the http-connection this socket belongs to.
     */
    StreamDemultiplexor(int protocol, Socket sock, HTTPConnection connection)
	    throws IOException
    {
	this.Protocol   = protocol;
	this.Connection = connection;
	RespHandlerList = new LinkedList();
	init(sock);
    }


    /**
     * Initializes the demultiplexor with a new socket.
     *
     * @param stream   the stream to demultiplex
     */
    private void init(Socket sock)  throws IOException
    {
	if (DebugDemux)
	    System.err.println("Demux: Initializing Stream Demultiplexor (" +
				this.hashCode() + ")");

	this.Sock       = sock;
	this.Stream     = new ExtBufferedInputStream(sock.getInputStream());
	MarkedForClose  = null;
	chunk_len       = -1;

	// start a timer to close the socket after 60 seconds
	Timer = new SocketTimeout(60000, this);
	Timer.start();
    }


    // Methods

    /**
     * Each Response must register with us.
     */
    void register(Response resp_handler, Request req)
    {
	RespHandlerList.addToEnd(new ResponseHandler(resp_handler, req, this));
    }

    /**
     * creates an input stream for the response.
     *
     * @param resp the response structure requesting the stream
     * @return an InputStream
     */
    RespInputStream getStream(Response resp)
    {
	ResponseHandler resph;
	for (resph = (ResponseHandler) RespHandlerList.enumerate();
	     resph != null; resph = (ResponseHandler) RespHandlerList.next())
	{
	    if (resph.resp == resp)  break;
	}

	if (resph != null)
	    return resph.stream;
	else
	    return null;
    }


    /**
     * Restarts the timer thread that will close an unused socket after
     * 60 seconds.
     */
    void restartTimer()
    {
	if (Timer != null)  Timer.reset();
    }


    /**
     * reads an array of bytes from the master stream.
     */
    int read(byte[] b, int off, int len, ResponseHandler resph, int timeout)
	    throws IOException
    {
	if (resph.exception != null)
	    throw (IOException) resph.exception.fillInStackTrace();

	if (resph.eof)
	    return -1;


	// read the headers and data for all responses preceding us.

	ResponseHandler head;
	while ((head = (ResponseHandler) RespHandlerList.getFirst()) != null  &&
		head != resph)
	{
	    try
		{ head.stream.readAll(timeout); }
	    catch (IOException ioe)
	    {
		if (ioe instanceof InterruptedIOException)
		    throw ioe;
		else
		    throw (IOException) resph.exception.fillInStackTrace();
	    }
	}


	// Now we can read from the stream.

	synchronized(this)
	{
	    if (resph.exception != null)
		throw (IOException) resph.exception.fillInStackTrace();

	    if (DebugDemux)
	    {
		if (resph.resp.cl_type != CL_HDRS)
		    System.err.println("Demux: Reading for stream " +
				       resph.stream.hashCode() +
				       " (" + Thread.currentThread() + ")");
	    }

	    if (Timer != null)  Timer.hyber();

	    try
	    {
		int rcvd = -1;

		if (timeout != cur_timeout)
		{
		    if (DebugDemux)
		    {
			System.err.println("Demux: Setting timeout to " +
					   timeout + " ms");
		    }

		    try
			{ Sock.setSoTimeout(timeout); }
		    catch (Throwable t)
			{ }
		    cur_timeout = timeout;
		}

		switch (resph.resp.cl_type)
		{
		    case CL_HDRS:
			rcvd = Stream.read(b, off, len);
			if (rcvd == -1)
			    throw new EOFException("Premature EOF encountered");
			break;
		    case CL_0:
			rcvd = -1;
			close(resph);
			break;
		    case CL_CLOSE:
			rcvd = Stream.read(b, off, len);
			if (rcvd == -1)
			    close(resph);
			break;
		    case CL_CONTLEN:
			int cl = resph.resp.ContentLength;
			if (len > cl - resph.stream.count)
			    len = cl - resph.stream.count;

			rcvd = Stream.read(b, off, len);
			if (rcvd == -1)
			    throw new EOFException("Premature EOF encountered");

			if (resph.stream.count+rcvd == cl)
			    close(resph);

			break;
		    case CL_CHUNKED:
			if (chunk_len == -1)	// it's a new chunk
			    chunk_len = Codecs.getChunkLength(Stream);

			if (chunk_len > 0)		// it's data
			{
			    if (len > chunk_len)  len = chunk_len;
			    rcvd = Stream.read(b, off, len);
			    if (rcvd == -1)
				throw new EOFException("Premature EOF encountered");
			    chunk_len -= rcvd;
			    if (chunk_len == 0)	// got the whole chunk
			    {
				Stream.read();	// CR
				Stream.read();	// LF
				chunk_len = -1;
			    }
			}
			else	// the footers (trailers)
			{
			    resph.resp.readTrailers(Stream);
			    rcvd = -1;
			    close(resph);
			    chunk_len = -1;
			}
			break;
		    case CL_MP_BR:
			byte[] endbndry = resph.getEndBoundary(Stream);
			int[]  end_cmp  = resph.getEndCompiled(Stream);

			rcvd = Stream.read(b, off, len);
			if (rcvd == -1)
			    throw new EOFException("Premature EOF encountered");

			int ovf = Stream.pastEnd(endbndry, end_cmp);
			if (ovf != -1)
			{
			    rcvd -= ovf;
			    Stream.reset();
			    close(resph);
			}

			break;
		    default:
			throw new Error("Internal Error in StreamDemultiplexor: " +
					"Invalid cl_type " + resph.resp.cl_type);
		}

		restartTimer();
		return rcvd;

	    }
	    catch (InterruptedIOException ie)	// don't intercept this one
	    {
		restartTimer();
		throw ie;
	    }
	    catch (IOException ioe)
	    {
		if (DebugDemux)
		{
		    System.err.print("Demux: (" + Thread.currentThread() + ") ");
		    ioe.printStackTrace();
		}

		close(ioe, true);
		throw resph.exception;		// set by retry_requests
	    }
	    catch (ParseException pe)
	    {
		if (DebugDemux)
		{
		    System.err.print("Demux: (" + Thread.currentThread() + ") ");
		    pe.printStackTrace();
		}

		close(new IOException(pe.toString()), true);
		throw resph.exception;		// set by retry_requests
	    }
	}
    }

    /**
     * skips a number of bytes in the master stream. This is done via a
     * dummy read, as the socket input stream doesn't like skip()'s.
     */
    synchronized long skip(long num, ResponseHandler resph) throws IOException
    {
	if (resph.exception != null)
	    throw (IOException) resph.exception.fillInStackTrace();

	if (resph.eof)
	    return 0;

	byte[] dummy = new byte[(int) num];
	int rcvd = read(dummy, 0, (int) num, resph, 0);
	if (rcvd == -1)
	    return 0;
	else
	    return rcvd;
    }

    /**
     * Determines the number of available bytes.
     */
    synchronized int available(ResponseHandler resph) throws IOException
    {
	int avail = Stream.available();
	if (resph == null)  return avail;

	if (resph.exception != null)
	    throw (IOException) resph.exception.fillInStackTrace();

	if (resph.eof)
	    return 0;

	switch (resph.resp.cl_type)
	{
	    case CL_0:
		return 0;
	    case CL_HDRS:
		// this is something of a hack; I could return 0, but then
		// if you were waiting for something on a response that
		// wasn't first in line (and you didn't try to read the
		// other response) you'd wait forever. On the other hand,
		// we might be making a false promise here...
		return (avail > 0 ? 1 : 0);
	    case CL_CLOSE:
		return avail;
	    case CL_CONTLEN:
		int cl = resph.resp.ContentLength;
		cl -= resph.stream.count;
		return (avail < cl ? avail : cl);
	    case CL_CHUNKED:
		return avail;	// not perfect...
	    case CL_MP_BR:
		return avail;	// not perfect...
	    default:
		throw new Error("Internal Error in StreamDemultiplexor: " +
				"Invalid cl_type " + resph.resp.cl_type);
	}

    }


    /**
     * Closes the socket and all associated streams. If <var>exception</var>
     * is not null then all active requests are retried.
     *
     * <P>There are five ways this method may be activated. 1) if an exception
     * occurs during read or write. 2) if the stream is marked for close but
     * no responses are outstanding (e.g. due to a timeout). 3) when the
     * markedForClose response is closed. 4) if all response streams up until
     * and including the markedForClose response have been closed. 5) if this
     * demux is finalized.
     *
     * @param exception the IOException to be sent to the streams.
     * @param was_reset if true then the exception is due to a connection
     *                  reset; otherwise it means we generated the exception
     *                  ourselves and this is a "normal" close.
     */
    synchronized void close(IOException exception, boolean was_reset)
    {
	if (Sock == null)	// already cleaned up
	    return;

	if (DebugDemux)
	    System.err.println("Demux: Closing all streams and socket (" +
				this.hashCode() + ")");

	try
	    { Stream.close(); }
	catch (IOException ioe) { }
	try
	    { Sock.close(); }
	catch (IOException ioe) { }
	Sock = null;

	if (Timer != null)
	{
	    Timer.kill();
	    Timer = null;
	}

	Connection.DemuxList.remove(this);


	// Here comes the tricky part: redo outstanding requests!

	if (exception != null)
	    retry_requests(exception, was_reset);
    }


    /**
     * Retries outstanding requests. Well, actually the RetryModule does
     * that. Here we just determine which request are to be retried and
     * set those up appropriately, including throwing the RetryException
     * for those so that the RetryModule can catch and handle it.
     *
     * @param exception the exception that led to this call.
     * @param was_reset this flag is passed to the RetryException and is
     *                  used by the RetryModule to distinguish abnormal closes
     *                  from expected closes.
     */
    private void retry_requests(IOException exception, boolean was_reset)
    {
	RetryException  prev  = null,
			first = null;
	ResponseHandler resph = (ResponseHandler) RespHandlerList.enumerate();
	IdempotentSequence seq = new IdempotentSequence();

	while (resph != null)
	{
	    // Don't retry if either we've already retried enough times,
	    // or the headers have been read and parsed already, or if
	    // an output stream was used (we don't have the data to resend)
	    // or if the sequence is not idempotent (Sec 8.1.4 and 9.1.2)
	    if (!seq.isIdempotent(resph.request)  ||
		(Connection.ServProtVersKnown  &&
		 Connection.ServerProtocolVersion >= HTTP_1_1  &&
		 resph.request.num_retries > 0)  ||
		((!Connection.ServProtVersKnown  ||
		  Connection.ServerProtocolVersion <= HTTP_1_0)  &&
		 resph.request.num_retries > 4)  ||
		resph.resp.got_headers  ||
		resph.request.getStream() != null)
	    {
		resph.exception = exception;
	    }
	    else
	    {
		RetryException tmp = new RetryException(exception.getMessage());
		if (first == null)  first = tmp;

		tmp.request    = resph.request;
		tmp.response   = resph.resp;
		tmp.conn_reset = was_reset;
		tmp.first      = first;
		tmp.addToListAfter(prev);

		prev = tmp;
		resph.exception = tmp;
	    }

	    RespHandlerList.remove(resph);
	    resph = (ResponseHandler) RespHandlerList.next();
	}
    }


    /**
     * Closes the associated stream. If this one has been markedForClose then
     * the socket is closed; else closeSocketIfAllStreamsClosed is invoked.
     */
    synchronized void close(ResponseHandler resph)
    {
	if (resph != (ResponseHandler) RespHandlerList.getFirst())
	    return;

	if (DebugDemux)
	    System.err.println("Demux: Closing stream " +
				resph.stream.hashCode() +
				" (" + Thread.currentThread() + ")");

	resph.eof = true;
	RespHandlerList.remove(resph);

	if (resph == MarkedForClose)
	    close(new IOException("Premature end of Keep-Alive"), false);
	else
	    closeSocketIfAllStreamsClosed();
    }


    /**
     * Close the socket if all the streams have been closed.
     *
     * <P>When a stream reaches eof it is removed from the response handler
     * list, but when somebody close()'s the response stream it is just
     * marked as such. This means that all responses in the list have either
     * not been read at all or only partially read, but they might have been
     * close()'d meaning that nobody is interested in the data. So If all the
     * response streams up till and including the one markedForClose have
     * been close()'d then we can remove them from our list and close the
     * socket.
     *
     * <P>Note: if the response list is emtpy or if no response is
     * markedForClose then this method does nothing. Specifically it does
     * not close the socket. We only want to close the socket if we've been
     * told to do so.
     *
     * <P>Also note that there might still be responses in the list after
     * the markedForClose one. These are due to us having pipelined more
     * requests to the server than it's willing to serve on a single
     * connection. These requests will be retried if possible.
     */
    synchronized void closeSocketIfAllStreamsClosed()
    {
	ResponseHandler resph = (ResponseHandler) RespHandlerList.enumerate();

	while (resph != null  &&  resph.stream.closed)
	{
	    if (resph == MarkedForClose)
	    {
		// remove all response handlers first
		ResponseHandler tmp;
		do
		{
		    tmp = (ResponseHandler) RespHandlerList.getFirst();
		    RespHandlerList.remove(tmp);
		}
		while (tmp != resph);

		// close the socket
		close(new IOException("Premature end of Keep-Alive"), false);
		return;
	    }

	    resph = (ResponseHandler) RespHandlerList.next();
	}
    }


    /**
     * returns the socket associated with this demux
     */
    synchronized Socket getSocket()
    {
	if (MarkedForClose != null)
	    return null;

	if (Timer != null)  Timer.hyber();
	return Sock;
    }


    /**
     * Mark this demux to not accept any more request and to close the
     * stream after this <var>resp</var>onse or all requests have been
     * processed, or close immediately if no requests are registered.
     *
     * @param response the Response after which the connection should
     *                 be closed.
     */
    synchronized void markForClose(Response resp)
    {
	if (RespHandlerList.getFirst() == null)	// no active request,
	{	    				// so close the socket
	    close(new IOException("Premature end of Keep-Alive"), false);
	    return;
	}

	if (Timer != null)
	{
	    Timer.kill();
	    Timer = null;
	}

	ResponseHandler resph;
	for (resph = (ResponseHandler) RespHandlerList.enumerate();
	     resph != null; resph = (ResponseHandler) RespHandlerList.next())
	{
	    if (resph.resp == resp)	// new resp precedes any others
	    {
		MarkedForClose = resph;

		if (DebugDemux)
		    System.err.println("Demux: stream " +
				       resp.inp_stream.hashCode() +
				       " marked for close (" +
				       Thread.currentThread() + ")");

		closeSocketIfAllStreamsClosed();
		return;
	    }

	    if (MarkedForClose == resph)
		return;	// already marked for closing after an earlier resp
	}

	MarkedForClose = resph;		// resp == null, so use last resph
	closeSocketIfAllStreamsClosed();

	if (DebugDemux)
	    System.err.println("Demux: stream " +
			       resph.resp.inp_stream.hashCode() +
			       " marked for close (" +
			       Thread.currentThread() + ")");
    }


    /**
     * Emergency stop. Closes the socket and notifies the responses that
     * the requests are aborted.
     *
     * @since V0.3
     */
    void abort()
    {
	if (DebugDemux)
	    System.err.println("Demux: Aborting socket (" +
				this.hashCode() + ")");


	// notify all responses of abort

	for (ResponseHandler resph =
				(ResponseHandler) RespHandlerList.enumerate();
	     resph != null; resph = (ResponseHandler) RespHandlerList.next())
	    resph.resp.http_resp.markAborted();


	/* Close the socket.
	 * Note: this duplicates most of close(IOException, boolean). We do
	 * *not* call close() because that is synchronized, but we want
	 * abort() to be asynch.
	 */
        if (Sock != null)
        {
	    try
	    {
		try
		    { Sock.setSoLinger(false, 0); }
		catch (Throwable t)
		    { }

		try
		    { Stream.close(); }
		catch (IOException ioe) { }
		try
		    { Sock.close(); }
		catch (IOException ioe) { }
		Sock = null;

		if (Timer != null)
		{
		    Timer.kill();
		    Timer = null;
		}
	    }
	    catch (NullPointerException npe)
		{ }

	    Connection.DemuxList.remove(this);
        }
    }


    /**
     * A safety net to close the connection.
     */
    protected void finalize()
    {
	close((IOException) null, false);
    }


    /**
     * produces a string.
     * @return a string containing the class name and protocol number
     */
    public String toString()
    {
	String prot;

	switch (Protocol)
	{
	    case HTTP:
		prot = "HTTP"; break;
	    case HTTPS:
		prot = "HTTPS"; break;
	    case SHTTP:
		prot = "SHTTP"; break;
	    case HTTP_NG:
		prot = "HTTP_NG"; break;
	    default:
		throw new Error("HTTPClient Internal Error: invalid protocol " +
				Protocol);
	}

	return getClass().getName() + "[Protocol=" + prot + "]";
    }
}


class SocketTimeout extends Thread implements GlobalConstants
{
    private static int t_num = 1;
    private long                 timeout;
    private StreamDemultiplexor  demux;
    private boolean              restart = false,
				 hyber = true,
				 die = false;


    SocketTimeout(long time, StreamDemultiplexor demux)
    {
	super("SocketTimeout-"+t_num+"-"+demux.hashCode());
	t_num++;

	try { setDaemon(true); }
	catch (SecurityException se) { }	// Oh well...
	setPriority(MAX_PRIORITY);

	timeout    = time;
	this.demux = demux;
    }


    public void run()
    {
	long num_sec = timeout / 1000,
	     one_sec = timeout / num_sec;

	if (DebugDemux)
	    System.err.println("Demux: Timeout " + this + " starting (" +
			       demux.hashCode() + ")");

	forever: while (true)
	{
	    while (hyber)
		{ try { sleep(one_sec); } catch (InterruptedException ie) { } }

	    if (die)
		break forever;

	    if (restart)
		restart = false;

	    // this loop is a hack to be able to restart the timer more
	    // precisely; if interrupt would work we could use that instead
	    for (long idx=num_sec; idx>0; idx--)
	    {
		try
		    { sleep(one_sec); }
		catch (InterruptedException ie)
		    { }

		if (die)
		    break forever;
		if (restart  ||  hyber)
		    continue forever;
	    }

	    synchronized(demux)
	    {
		if (die)
		    break forever;
		if (restart  ||  hyber)
		    continue forever;

		demux.markForClose(null);
	    }

	    break forever;
	}

	if (DebugDemux)
	    System.err.println("Demux: Timeout " + this + " ended (" +
			       demux.hashCode() + ")");
    }


    /**
     * Ideally this would just call interrupt(), but the Thread stuff is
     * not fully implemented (in JDK 1.0.2).
     */
    void reset()
    {
	restart = true;
	hyber   = false;
    }

    /**
     * Suspends the timer; suspend() ought to suffice, but Netscape seem to
     * be overtaxed when it comes to implementing this correctly (not that
     * it's trivial), so they've opted to possibly remove it instead...
     */
    void hyber()
    {
	restart = false;
	hyber   = true;
    }

    /**
     * Stops this timer if called by a different thread. Note, we just
     * let the run() method fall of the end instead of using stop() -
     * this is to circumvent a bug in the JDK 1.0.2 .
     */
    void kill()
    {
	die     = true;
	restart = false;
	hyber   = false;
    }
}


/**
 * This holds various information about an active response.
 */
final class ResponseHandler implements GlobalConstants
{
    /** the response stream */
    RespInputStream     stream;

    /** the response class */
    Response            resp;

    /** the response class */
    Request             request;

    /** signals that the demux has closed the response stream, and that
	therefore no more data can be read */
    boolean             eof = false;

    /** this is non-null if the stream has an exception pending */
    IOException         exception = null;


    /**
     * Creates a new handler. This also allocates the response input
     * stream.
     *
     * @param resp     the reponse
     * @param request  the request
     * @param demux    our stream demultiplexor.
     */
    ResponseHandler(Response resp, Request request, StreamDemultiplexor demux)
    {
	this.resp     = resp;
	this.request  = request;
	this.stream   = new RespInputStream(demux, this);

	if (DebugDemux)
	    System.err.println("Demux: Opening stream " +
				this.stream.hashCode() + " (" +
				Thread.currentThread() + ")");
    }


    /** holds the string that marks the end of this stream; used for
	multipart delimited responses. */
    private byte[] endbndry = null;

    /** holds the compilation of the above string */
    private int[]  end_cmp  = null;

    /**
     * return the boundary string for this response. Set's up the
     * InputStream buffer if neccessary.
     *
     * @param  MasterStream the input stream from which the stream demux
     *                      is reading.
     * @return the boundary string.
     */
    byte[] getEndBoundary(ExtBufferedInputStream MasterStream)
		throws IOException, ParseException
    {
	if (endbndry == null)
	    setupBoundary(MasterStream);

	return endbndry;
    }

    /**
     * return the compilation of the boundary string for this response.
     * Set's up the InputStream buffer if neccessary.
     *
     * @param  MasterStream the input stream from which the stream demux
     *                      is reading.
     * @return the compiled boundary string.
     */
    int[] getEndCompiled(ExtBufferedInputStream MasterStream)
		throws IOException, ParseException
    {
	if (end_cmp == null)
	    setupBoundary(MasterStream);

	return end_cmp;
    }

    /**
     * Gets the boundary string, compiles it for searching, and initializes
     * the buffered input stream.
     */
    void setupBoundary(ExtBufferedInputStream MasterStream)
		throws IOException, ParseException
    {
	String endstr = "--" + Util.getParameter("boundary",
			    resp.getHeader("Content-Type")) +
			"--\r\n";
	endbndry = new byte[endstr.length()];
	endstr.getBytes(0, endbndry.length, endbndry, 0);
	end_cmp = Util.compile_search(endbndry);
	MasterStream.initMark();
    }
}


/**
 * This is the InputStream that gets returned to the user. The extensions
 * consist of the capability to have the data pushed into a buffer if the
 * stream demux needs to.
 */
final class RespInputStream extends InputStream implements GlobalConstants
{
    /** the stream demultiplexor */
    private StreamDemultiplexor demux = null;

    /** our response handler */
    private ResponseHandler     resph;

    /** signals that the user has closed the stream and will therefore
	not read any further data */
	    boolean             closed = false;

    /** signals that the connection may not be closed prematurely */
    private boolean             dont_truncate = false;

    /** this buffer is used to buffer data that the demux has to get rid of */
    private byte[]              buffer = null;

    /** signals that we were interrupted and that the buffer is not complete */
    private boolean             interrupted = false;

    /** the offset at which the unread data starts in the buffer */
    private int                 offset = 0;

    /** the end of the data in the buffer */
    private int                 end = 0;

    /** the total number of bytes of entity data read from the demux so far */
            int                 count = 0;


    // Constructors

    RespInputStream(StreamDemultiplexor demux, ResponseHandler resph)
    {
	this.demux = demux;
	this.resph = resph;
    }


    // public Methods

    private byte[] ch = new byte[1];
    /**
     * Reads a single byte.
     *
     * @return the byte read, or -1 if EOF.
     * @exception IOException if any exception occured on the connection.
     */
    public synchronized int read() throws IOException
    {
	int rcvd = read(ch, 0, 1);
	if (rcvd == 1)
	    return ch[0] & 0xff;
	else
	    return -1;
    }


    /**
     * Reads <var>len</var> bytes into <var>b</var>, starting at offset
     * <var>off</var>.
     *
     * @return the number of bytes actually read, or -1 if EOF.
     * @exception IOException if any exception occured on the connection.
     */
    public synchronized int read(byte[] b, int off, int len) throws IOException
    {
	if (closed)
	    return -1;

	int left = end - offset;
	if (buffer != null  &&  !(left == 0  &&  interrupted))
	{
	    if (left == 0)  return -1;

	    len = (len > left ? left : len);
	    System.arraycopy(buffer, offset, b, off, len);
	    offset += len;

	    return len;
	}
	else
	{
	    if (DebugDemux)
	    {
		if (resph.resp.cl_type != CL_HDRS)
		    System.err.println("RspIS: Reading stream " +
				       this.hashCode() +
				       " (" + Thread.currentThread() + ")");
	    }

	    int rcvd;
	    if (resph.resp.cl_type == CL_HDRS)
		rcvd = demux.read(b, off, len, resph, resph.resp.timeout);
	    else
		rcvd = demux.read(b, off, len, resph, 0);
	    if (rcvd != -1  &&  resph.resp.got_headers)
		count += rcvd;

	    return rcvd;
	}
    }


    /**
     * skips <var>num</var> bytes.
     *
     * @return the number of bytes actually skipped.
     * @exception IOException if any exception occured on the connection.
     */
    public synchronized long skip(long num) throws IOException
    {
	if (closed)
	    return 0;

	int left = end - offset;
	if (buffer != null  &&  !(left == 0  &&  interrupted))
	{
	    num = (num > left ? left : num);
	    offset  += num;
	    return num;
	}
	else
	{
	    long skpd = demux.skip(num, resph);
	    if (resph.resp.got_headers)
		count += skpd;
	    return skpd;
	}
    }


    /**
     * gets the number of bytes available for reading without blocking.
     *
     * @return the number of bytes available.
     * @exception IOException if any exception occured on the connection.
     */
    public synchronized int available() throws IOException
    {
	if (closed)
	    return 0;

	if (buffer != null  &&  !(end-offset == 0  &&  interrupted))
	    return end-offset;
	else
	    return demux.available(resph);
    }


    /**
     * closes the stream.
     *
     * @exception if any exception occured on the connection before or
     *            during close.
     */
    public synchronized void close()  throws IOException
    {
	if (!closed)
	{
	    closed = true;

	    if (dont_truncate  &&  (buffer == null  ||  interrupted))
		readAll(resph.resp.timeout);

	    demux.closeSocketIfAllStreamsClosed();

	    if (dont_truncate)
	    {
		try
		    { resph.resp.http_resp.invokeTrailerHandlers(false); }
		catch (ModuleException me)
		    { throw new IOException(me.toString()); }
	    }
	}
    }


    /**
     * A safety net to clean up.
     */
    protected void finalize()
    {
	try
	    { close(); }
	catch (IOException ioe)
	    { }
    }


    // local Methods

    /**
     * Reads all remainings data into buffer. This is used to force a read
     * of upstream responses.
     *
     * <P>This is probably the most tricky and buggy method around. It's the
     * only one that really violates the strict top-down method invocation
     * from the Response through the ResponseStream to the StreamDemultiplexor.
     * This means we need to be awfully careful about what is synchronized
     * and what parameters are passed to whom.
     *
     * @param timeout the timeout to use for reading from the demux
     * @exception IOException If any exception occurs while reading stream.
     */
    void readAll(int timeout)  throws IOException
    {
	if (DebugDemux)
	    System.err.println("RspIS: Read-all on stream " + this.hashCode() +
			       " (" + Thread.currentThread() + ")");

	synchronized(resph.resp)
	{
	    if (!resph.resp.got_headers)	// force headers to be read
	    {
		int sav_to = resph.resp.timeout;
		resph.resp.timeout = timeout;
		resph.resp.getStatusCode();
		resph.resp.timeout = sav_to;
	    }
	}

	synchronized(this)
	{
	    if (buffer != null  &&  !interrupted)  return;

	    int rcvd = 0;
	    try
	    {
		if (closed)			// throw away
		{
		    buffer = new byte[10000];
		    do
		    {
			count += rcvd;
			rcvd   = demux.read(buffer, 0, buffer.length, resph,
					    timeout);
		    } while (rcvd != -1);
		    buffer = null;
		}
		else
		{
		    if (buffer == null)
		    {
			buffer = new byte[10000];
			offset = 0;
			end    = 0;
		    }

		    do
		    {
			rcvd = demux.read(buffer, end, buffer.length-end, resph,
					  timeout);
			if (rcvd < 0)  break;

			count  += rcvd;
			end    += rcvd;
			buffer  = Util.resizeArray(buffer, end+10000);
		    } while (true);
		}
	    }
	    catch (InterruptedIOException iioe)
	    {
		interrupted = true;
		throw iioe;
	    }
	    catch (IOException ioe)
	    {
		buffer = null;	// force a read on demux for exception
	    }

	    interrupted = false;
	}
    }


    /**
     * Sometime the full response body must be read, i.e. the connection may
     * not be closed prematurely (by us). Currently this is needed when the
     * chunked encoding with trailers is used in a response.
     */
    synchronized void dontTruncate()
    {
	dont_truncate = true;
    }
}

