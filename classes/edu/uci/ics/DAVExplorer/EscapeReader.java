/*
 * Copyright (c) 1999-2005 Regents of the University of California.
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
 * Title:       EscapeInputReader
 * Description: This class is a filterreader that converts escaped characters to
 *              their normal equivalents, or escapes special characters, respectively,
 *              depending on the direction of the conversion.
 * Copyright:   Copyright (c) 1999-2005 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        29 April 1999
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        1 October 2001
 * Changes:     Change of package name
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        08 February 2004
 * Changes:     Added Javadoc templates
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         12 August 2005
 * Changes:     Escaping for UTF-8 
 */

package edu.uci.ics.DAVExplorer;

import java.io.Reader;
import java.io.FilterReader;
import java.io.IOException;


/**
 * 
 */
public class EscapeReader
    extends FilterReader
{
    /**
     * 
     * @param _in
     * @param _remove
     */
    public EscapeReader( Reader _in, boolean _remove, boolean _UTF )
    {
        super( _in );
        in = _in;
        remove = _remove;
        utf = _UTF;
    }


    /**
     * 
     * @return
     */
    public int read()
        throws IOException
    {
        if( in == null )
            return -1;

        if( remove )
            return readRemove();
        else
            return readAdd();
    }


    /**
     * 
     * @param b
     * 
     * @return
     */
    public int read( char[] b )
        throws IOException
    {
        return read( b, 0, b.length );
    }


    /**
     * 
     * @param b
     * @param off
     * @param len
     * 
     * @return
     */
    public int read( char[] b, int off, int len )
        throws IOException
    {
        int count = 0;
        while( count < len )
        {
            int val = read();
            if( val == -1 )
                return count;
            b[off+count] = (char)val;
            count++;
        }
        return count;
    }


    /**
     * read byte from input stream and remove any escaped
     * sequences and replace them with the unescaped equivalent
     * byte.
     *
     * @return
     */
    private int readRemove()
        throws IOException
    {
        int val = in.read();
        if( val == 37 ) // %
        {
            // found escape char, now combine the next two bytes
            // into the return char
            int high = in.read();
            if( high == -1 )
                throw new IOException( "Unexpected end of stream" );
            if( high > 96 )
                high -= 32;
            if( high > 64 )
                high -= 7;
            int low = in.read();
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
     * 
     * @return
     */
    private int readAdd()
        throws IOException
    {
        int val = -1;
        if( convert == null )
        {
            int i = 0;
            val = in.read();
            while( i<escape.length )
            {
                if( val == (int)(escape[i]) )
                {
                    convert = new int[2];
                    val = (int)(escape[i]);
                    convert[0] = (val>>4) + 48;
                    if( convert[0] > 57 )
                        convert[0] += 7;
                    convert[1] = (val&15) + 48;
                    if( convert[1] > 57 )
                        convert[1] += 7;
                    val = (int)'%';
                    break;
                }
                i++;
            }
            if( convert == null && utf )
            {
                if( val > 127)
                {
                    convert = new int[getUnicodeLength(val)*3];
                    setUTFEscape( val );
                    val = convert[0];
                    index = 1;
                }
            }
        }
        else
        {
            val = convert[index++];
            if( index == convert.length )
            {
                convert = null;
                index = 0;
            }
        }
        return val;
    }


    /**
     * Get the length in bytes needed to encode in UTF-8 and escape a character.
     * Example: 
     * 
     * @param i     The character.
     * @return      The length required to escape the character
     */
    private int getUnicodeLength( int i )
    {
        if( i < 128 )                   // 00..7F
            return 1;
        else if( i >= 128 && i <= 2047 ) // 80..7FF
        {
            return 2;
        }
        else                            // 800..FFFF
            return 3;
    }

    
    /**
     * Determine if the current character in a stream represents an allowed
     * Unicode character.
     * 
     * @param i         The character to check
     * @param stream    The stream to read additional characters from to
     *                  make the determination
     * @return          True if the current character is a Unicode character
     * 
     * @see <a href"http://www.unicode.org/versions/Unicode4.0.0/ch03.pdf">The
     * Unicode Standard, Section 3.10, Table 3.6</a>
     */
    private void setUTFEscape( int i )
    {
        if( i >= 128 && i <= 2047 ) // 80..7FF
        {
            int high = 192 + ((i & 1984) >> 6);
            int low = 128 + (i & 63);
            getAscii(high, 0);
            getAscii(low, 3);
        }
        else if( i >= 2048 && i <= 4095 )   // 800..FFF
        {
            int high = 192 + ((i & 1984) >> 6);
            int low = 128 + (i & 63);
            getAscii( 224, 0 );    // E0
            getAscii( high, 3 );
            getAscii( low, 6 );
        }
        else if( i >= 4096 && i <= 53247 )   // 1000..CFFF
        {
            int high2 = 224 + ((i & 4096) >> 12);
            int high = 192 + ((i & 1984) >> 6);
            int low = 128 + (i & 63);
            getAscii( high2, 0 );    // E0
            getAscii( high, 3 );
            getAscii( low, 6 );
        }
        else if( i >= 53248 && i <= 55295 )   // D000..D7FF
        {
            int high = 192 + ((i & 1984) >> 6);
            int low = 128 + (i & 63);
            getAscii( 237, 0 );    // ED
            getAscii( high, 3 );
            getAscii( low, 6 );
        }
        else if( i >= 57344 && i <= 65535 )   // E000..FFFF
        {
            int high2 = 224 + ((i & 4096) >> 12);
            int high = 192 + ((i & 1984) >> 6);
            int low = 128 + (i & 63);
            getAscii( high2, 0 );
            getAscii( high, 3 );
            getAscii( low, 6 );
        }
    }


    private void getAscii( int val, int i )
    {
        if( convert == null )
            return;
        
        convert[i++] = (int)'%';
        int high = ((val & 240) >> 4) + 48;
        int low = (val & 15) + 48;
        if( high > 57 )
            high += 7;
        if( low > 57 )
            low += 7;
        convert[i++] = high;
        convert[i++] = low;
    }


    private Reader in = null;
    private boolean remove = false;
    private boolean utf = false;
    private int[] convert = null;
    private int index = 0;
    private static char[] escape = { ' ', ';', '?', ':', '@', '&', '=', '+',
                                     '$', ',', '<', '>', '#', '%', '"', '{', '}',
                                     '|', '\\', '^', '[', ']', '`', '\''};
}
