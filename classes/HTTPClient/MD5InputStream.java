/*
 * @(#)MD5InputStream.java				0.3 30/01/1998
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
import java.io.InputStream;
import java.io.FilterInputStream;
import java.net.ProtocolException;


/**
 * This class calculates a running md5 digest of the data read. When the
 * stream is closed the calculated digest is passed to a HashVerifier which
 * is expected to verify this digest and to throw an Exception if it fails.
 *
 * @version	0.3  30/01/1998
 * @author	Ronald Tschal&auml;r
 */
class MD5InputStream extends FilterInputStream
{
    HashVerifier verifier;
    MD5 md5;
    long rcvd = 0;


    /**
     * @param is the input stream over which the md5 hash is to be calculated
     * @param verifier the HashVerifier to invoke when the stream is closed
     */
    MD5InputStream(InputStream is, HashVerifier verifier)
    {
	super(is);
	this.verifier = verifier;
	md5 = new MD5();
    }


    public synchronized int read() throws IOException
    {
	int b = in.read();
	if (b != -1)
	    md5.Update((byte) b);
	else
	    real_close();

	rcvd++;
	return b;
    }


    public synchronized int read(byte[] buf, int off, int len)
	    throws IOException
    {
	int num = in.read(buf, off, len);
	if (num > 0)
	    md5.Update(buf, off, num);
	else
	    real_close();

	rcvd += num;
	return num;
    }


    public synchronized long skip(long num)  throws IOException
    {
	byte[] tmp = new byte[(int) num];
	int got = read(tmp, 0, (int) num);

	if (got > 0)
	    return (long) got;
	else
	    return 0L;
    }


    /**
     * Close the stream and check the digest. If the stream has not been
     * fully read then the rest of the data will first be read (and discarded)
     * to complete the digest calculation.
     *
     * @exception IOException if the close()'ing the underlying stream throws
     *                        an IOException, or if the expected digest and
     *                        the calculated digest don't match.
     */
    public synchronized void close()  throws IOException
    {
	while (skip(10000) > 0) ;
	real_close();
    }


    /**
     * Close the stream and check the digest.
     *
     * @exception IOException if the close()'ing the underlying stream throws
     *                        an IOException, or if the expected digest and
     *                        the calculated digest don't match.
     */
    private void real_close()  throws IOException
    {
	in.close();
	verifier.verifyHash(md5.Final(), rcvd);
    }
}

