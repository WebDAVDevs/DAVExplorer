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

// This is the interpreter module that parses WebDAV responses.
// Some of the methods are not parsed, and the functions are left
// empty intentinally.
//
// Version: 0.4
// Author:  Joachim Feise
// Date:    4/1/99
////////////////////////////////////////////////////////////////
//
// Please use the following contact:
//
// dav-exp@ics.uci.edu
//

package WebDAV;

import java.io.*;

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
                return count;
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
            int low = m_in.read();
            if( low == -1 )
                throw new IOException( "Unexpected end of stream" );
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
                    m_convert[1] = (val&15) + 48;
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
