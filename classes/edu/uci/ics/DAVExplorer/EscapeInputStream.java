/*
 * Copyright (c) 1999-2001 Regents of the University of California.
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
 * Title:       EscapeInputStream
 * Description: This class is a filterstream that converts escaped characters to
 *              their normal equivalents, or escapes special characters, respectively,
 *              depending on the direction of the conversion.
 * Copyright:   Copyright (c) 1999-2001 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        1 April 1999
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        1 October 2001
 * Changes:     Change of package name
 */

package edu.uci.ics.DAVExplorer;

import java.io.InputStream;
import java.io.FilterInputStream;
import java.io.IOException;

public class EscapeInputStream
    extends FilterInputStream
{
    public EscapeInputStream( InputStream in, boolean remove )
    {
        super( in );
        m_in = in;
        m_remove = remove;
    }

    public int read()
        throws IOException
    {
        if( m_in == null )
            return -1;

        if( m_remove )
            return readRemove();
        else
            return readAdd();
    }

    public int read( byte[] b )
        throws IOException
    {
        return read( b, 0, b.length );
    }

    public int read( byte[] b, int off, int len )
        throws IOException
    {
        int count = 0;
        while( count < len )
        {
            int val = read();
            if( val == -1 )
            {
                // if we didn't read anything at all, return -1 as EOF marker
                // otherwise, return the number of chars read
                if( count > 0 )
                    return count;
                else
                    return -1;
            }
            b[off+count] = (byte)val;
            count++;
        }
        return count;
    }

    /**
     * read byte from input stream and remove any escaped
     * sequences and replace them with the unescaped equivalent
     * byte.
     */
    private int readRemove()
        throws IOException
    {
        int val = m_in.read();
        if( val == 37 ) // %
        {
            // found escape char, now combine the next two bytes
            // into the return char
            int high = m_in.read();
            if( high == -1 )
                throw new IOException( "Unexpected end of stream" );
            if( high > 96 )
                high -= 32;
            if( high > 64 )
                high -= 7;
            int low = m_in.read();
            if( low == -1 )
                throw new IOException( "Unexpected end of stream" );
            if( low > 96 )
                low -= 32;
            if( low > 64 )
                low -= 7;
            val = ((high-48) << 4) + (low-48);
        }
        return val;
    }

    /**
     * read byte from input stream and replace any byte that
     * requires escaping with the equivalent escape sequence.
     */
    private int readAdd()
        throws IOException
    {
        int val = -1;
        if( m_convert == null )
        {
            int i = 0;
            val = m_in.read();
            while( i<escape.length )
            {
                if( val == (int)(escape[i]) )
                {
                    m_convert = new int[2];
                    val = (int)(escape[i]);
                    m_convert[0] = (val>>4) + 48;
                    if( m_convert[0] > 57 )
                        m_convert[0] += 7;
                    m_convert[1] = (val&15) + 48;
                    if( m_convert[1] > 57 )
                        m_convert[1] += 7;
                    val = (int)'%';
                    break;
                }
                i++;
            }
        }
        else
        {
            val = m_convert[m_index++];
            if( m_index == m_convert.length )
            {
                m_convert = null;
                m_index = 0;
            }
        }
        return val;
    }

    private InputStream m_in = null;
    private boolean m_remove = false;
    private int[] m_convert = null;
    private int m_index = 0;
    private static char[] escape = { ' ', ';', '?', ':', '@', '&', '=', '+',
                                     '$', ',', '<', '>', '#', '%', '"', '{', '}',
                                     '|', '\\', '^', '[', ']', '`', '\'', '%' };
}
