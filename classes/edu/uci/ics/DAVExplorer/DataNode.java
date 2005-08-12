/*
 * Copyright (c) 1998-2004 Regents of the University of California.
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
 * Title:       DataNode
 * Description: Node holding information about resources and collections
 * Copyright:   Copyright (c) 1998-2004 Regents of the University of California. All rights reserved.
 * @author      Undergraduate project team ICS 126B 1998
 * @date        1998
 * @author      Yuzo Kanomata, Joachim Feise (dav-exp@ics.uci.edu)
 * @date        17 March 1999
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        1 October 2001
 * Changes:     Change of package name
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        13 May 2003
 * Changes:     Changed date conversion for column sorting.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        23 September 2003
 * Changes:     Changed the formatting of the parameter lists.
 * @author      Jason McIntosh/Joachim Feise (dav-exp@ics.uci.edu)
 * @date        29 October 2003
 * Changes:     Integrated Jason's patch to handle cases where
 *              lastModified is null or a funky string.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        04 February 2004
 * Changes:     Added workaround for Documentum Modified-Date bug
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        08 February 2004
 * Changes:     Added Javadoc templates
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        09 February 2004
 * Changes:     Added workaround for MS Exchange Server Date format
 */

package edu.uci.ics.DAVExplorer;

import java.util.Vector;
import java.util.Date;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.text.DateFormat;
import java.text.ParseException;


/**
 * Node holding information about resources and collections
 */
public class DataNode
{
    protected String name;
    protected String display;
    protected String type;
    protected long size;
    protected String lastModified;
    protected boolean locked;
    protected String lockToken;
    protected boolean collection;
    protected boolean UTF;
    protected Vector subNodes = null;


    /**
     * Constructor
     * @param collection
     * @param locked
     * @param lockToken
     * @param name
     * @param display
     * @param type
     * @param size
     * @param date
     * @param UTF
     * @param subNodes
     */
    public DataNode( boolean collection,
                     boolean locked,
                     String lockToken,
                     String name,
                     String display,
                     String type,
                     long size,
                     Date date,
                     boolean UTF,
                     Vector subNodes )
    {
        DateFormat df = DateFormat.getDateTimeInstance( DateFormat.FULL, DateFormat.FULL );
        df.setLenient( true );
        String strDate = date==null? "" : df.format( date );
        init( collection, locked, lockToken, name, display, type, size, strDate, UTF, subNodes );
    }


    /**
     * Constructor
     * @param collection
     * @param locked
     * @param lockToken
     * @param name
     * @param display
     * @param type
     * @param size
     * @param date
     * @param UTF
     * @param subNodes
     */
    public DataNode( boolean collection,
                     boolean locked,
                     String lockToken,
                     String name,
                     String display,
                     String type,
                     long size,
                     String date,
                     boolean UTF,
                     Vector subNodes )
    {
        init( collection, locked, lockToken, name, display, type, size, date, UTF, subNodes );
    }


    /**
     * 
     * @param collection
     * @param locked
     * @param lockToken
     * @param name
     * @param display
     * @param type
     * @param size
     * @param date
     * @param UTF
     * @param subNodes
     */
    private void init( boolean _collection, boolean _locked, String _lockToken,
                       String _name, String _display, String _type, long _size,
                       String date, boolean _UTF, Vector _subNodes )
    {
        this.name = _name;
        this.display = _display;
        this.type = _type;
        this.size = _size;
        this.lastModified = date;
        this.locked = _locked;
        this.lockToken = _lockToken;
        this.collection = _collection;
        this.UTF = _UTF;
        this.subNodes = _subNodes;
    }


    /**
     *
     * @param subNodes 
     */
    public void setSubNodes(Vector subNodes)
    {
        this.subNodes = subNodes;
    }


    /**
     * 
     * @return
     */
    public Vector getSubNodes()
    {
        return subNodes;
    }


    /**
     * Set the name of this node.
     * @param newName       The new name to set 
     */
    public void setName(String newName)
    {
        name = newName;
    }


    /**
     * Set the display name of this node.
     * @param newDisplay        The new display name to set
     */
    public void setDisplay(String newDisplay)
    {
        display = newDisplay;
    }


    /**
     * Set the Mime-type information of this node.
     * @param newType       The Mime-type to set.
     */
    public void setType(String newType)
    {
        type = newType;
    }


    /**
     * Set the size information of this node.
     * @param newSize       The size information to set.
     */
    public void setSize(long newSize)
    {
        size = newSize;
    }


    /**
     * Set the last-modified date of this node.
     * @param newDate   The last-modified date to set. Should be
     *                  in the format specified in section 13.1 of
     *                  RFC 2518, but we can handle several formats.
     * @see             <a href="http://www.ietf.org/rfc/rfc2518">RFC 2518, Section 13.1</a>
     */
    public void setDate(String newDate)
    {
        lastModified = newDate;
    }


    /**
     * Set the last-modified date of this node.
     * @param newDate   The last-modified date to set
     */
    public void setDate(Date newDate)
    {
        DateFormat df = DateFormat.getDateTimeInstance();
        lastModified = df.format( newDate );
    }


    /**
     * Indicate if the name was originally encoded in UTF-8
     * @param _UTF      true if the name was encoded in UTF-8, false else.
     *                  A parameter of false implies an ISO-8859-1 encoding.
     */
    public void setUTF(boolean _UTF)
    {
        UTF = _UTF;
    }


    /**
     * Set the locktoken of this node.
     * @param lockToken     The locktoken to set
     */
    public void lock( String _lockToken )
    {
        locked = true;
        this.lockToken = _lockToken;
    }


    /**
     * 
     */
    public void unlock()
    {
        locked = false;
        lockToken = null;
    }


    /**
     * Indicate that this node is a collection resource. 
     */
    public void makeCollection()
    {
        collection = true;
    }


    /**
     * Indicate that this node is a non-collection resource. 
     */
    public void makeNonCollection()
    {
        collection = false;
    }


    /**
     * 
     * @return
     */
    public String getName()
    {
        return new String(name);
    }


    /**
     * 
     * @return
     */
    public String getDisplay()
    {
        return new String(display);
    }


    /**
     * 
     * @return
     */
    public String getType()
    {
        return new String(type);
    }


    /**
     * 
     * @return
     */
    public String getLockToken()
    {
        return lockToken;
    }


    /**
     * 
     * @return
     */
    public long getSize()
    {
        return size;
    }


    /**
     * 
     * @return
     */
    public Date getDate()
    {
        if( lastModified == null || lastModified.length() == 0 )
            return null;

        /* documentum workaround hack: they apparently use localized
         * weekday abbreviations, which violates RFC 3518 and RFC 2616
         * This hack checks for valid weekdays and removes anything that
         * does not follow the RFCs
         * Allowed formats:
         * wkday "," ...
         * wkday SP ...
         * weekday "," ...
         * with (see RFC 2616):
         * wkday    = "Mon" | "Tue" | "Wed"
         *		    | "Thu" | "Fri" | "Sat" | "Sun"
         * weekday  = "Monday" | "Tuesday" | "Wednesday"
         *			| "Thursday" | "Friday" | "Saturday" | "Sunday"
         */
		String[] wkday = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
		String[] weekday = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };

		boolean found = false;
		for( int i=0; i<weekday.length; i++ )
		{
			if( lastModified.startsWith(weekday[i]) )
			{
				found = true;
				break;
			}
		}
		for( int i=0; !found && i<wkday.length; i++ )
		{
			if( lastModified.startsWith(wkday[i]) )
			{
				found = true;
				break;
			}
		}

        // ignore if string starts with digit, may be year
        char c = lastModified.charAt(0);
        if( Character.isDigit(c) )
            found = true;

        // ignore known good weekdays and local files
		if( !found && !display.equals("Local File") )
		{
			// remove the unknown data
			int pos = lastModified.indexOf(",");
			if( pos == -1 )
				pos = lastModified.indexOf(" ");
			if( pos > -1 )
			{
				String newModified = lastModified.substring( pos+1 );
                newModified = newModified.trim();
                lastModified = newModified;                
				if( lastModified == null || lastModified.length() == 0 )
					return null;
			}
		}

        DateFormat df = DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT );
        df.setLenient( true );
        int dateStyle = DateFormat.SHORT;
        int timeStyle = DateFormat.SHORT;
        while( true )
        {
            try
            {
                df = DateFormat.getDateTimeInstance( dateStyle, timeStyle );
                return df.parse(lastModified);
            }
            catch( ParseException e )
            {
                switch( timeStyle )
                {
                    case DateFormat.SHORT:
                        timeStyle = DateFormat.MEDIUM;
                        break;
                    case DateFormat.MEDIUM:
                        timeStyle = DateFormat.LONG;
                        break;
                    case DateFormat.LONG:
                        timeStyle = DateFormat.FULL;
                        break;
                    case DateFormat.FULL:
                        timeStyle = DateFormat.SHORT;
                        switch( dateStyle )
                        {
                            case DateFormat.SHORT:
                                dateStyle = DateFormat.MEDIUM;
                                break;
                            case DateFormat.MEDIUM:
                                dateStyle = DateFormat.LONG;
                                break;
                            case DateFormat.LONG:
                                dateStyle = DateFormat.FULL;
                                break;
                            case DateFormat.FULL:
                                // all combinations tried, fallback to
                                // old Date(String) ctor
                                // Reason: the old Date ctor recognizes
                                // even strange date strings.
                                try
                                {
                                    // some date strings we get can't be decoded
                                    // even by the old Date ctor.
                                    // Example: Microsoft Exchange Server
                                    // returns an ISO 8601-format date for
                                    // getlastmodified: 2004-02-08T13:12:00.593Z
                                    return new Date(lastModified);
                                }
                                catch( Exception e2 )
                                {
                                    return MSExchangeDateDecoding(lastModified);
                                }
                        }
                        break;
                }
            }
        }
    }


    /**
     * 
     * @return
     */
    public boolean isUTF()
    {
        return UTF;
    }


    /**
     *
     * @return 
     */
    public boolean isLocked()
    {
        return locked;
    }


    /**
     * 
     * @return 
     */
    public boolean isCollection()
    {
        return collection;
    }

    /**
     * Microsoft Exchange Server gives us an ISO 8601 Date for getlastmodified:
     * yyyy-mm-ddThh:mm:ss.sssZ<br>
     * i.e., 4-digit year, '-', 2-digit month, '-', 2-digit day, 'T',
     * 2-digit hour, ':', 2-digit minutes, ':', 2-digit seconds, '.',
     * 3-digit thousands of a second, timezone<br> 
     * Example: 2004-02-08T13:12:00.593Z<br>
     * This violates RFC 2518.<br>
     * We are decoding it here.
     * @param strDate       The date as a string
     * @see <a href="http://www.ietf.org/rfc/rfc2518.txt">RFC 2518, Section 13.1</a>
     * @see <a href="http://www.ietf.org/rfc/rfc2518.txt">RFC 2518, Appendix 2</a>
     *  
     * @return              The Date object derived from the input string,
     *                      or null if the string could not be decoded.
     */
    private Date MSExchangeDateDecoding( String strDate )
    {
        int pos = 0;
        int year = 0;
        int month = 0;
        int day = 0;
        int hour = 0;
        int minute = 0;
        int second = 0;
        String tzone = "";

        try
        {
            StringTokenizer t = new StringTokenizer( strDate, "-T:." );
            while( t.hasMoreTokens() )
            {
                String token = t.nextToken();
                switch( pos++ )
                {
                    case 0:
                        year = Integer.parseInt(token);
                        break;

                    case 1:
                        month = Integer.parseInt(token) - 1;
                        break;

                    case 2:
                        day = Integer.parseInt(token);
                        break;

                    case 3:
                        hour = Integer.parseInt(token);
                        break;

                    case 4:
                        minute = Integer.parseInt(token);
                        break;

                    case 5:
                        second = Integer.parseInt(token);
                        break;

                    case 6:
                        // ignore subseconds, get timezone
                        for( int i=0; i<token.length(); i++ )
                        {
                            char c = token.charAt(i);
                            if( !Character.isDigit(c) )
                            {
                                tzone = token.substring(i);
                                if( tzone.equals("Z") )
                                    tzone = "GMT";
                                break;
                            }
                        }
                        break;
                }
            }
            Calendar c = Calendar.getInstance( TimeZone.getTimeZone(tzone) );
            c.set( year, month, day, hour, minute, second );
            return c.getTime();
        }
        catch( Exception e )
        {
            return null;
        }
    }
}
