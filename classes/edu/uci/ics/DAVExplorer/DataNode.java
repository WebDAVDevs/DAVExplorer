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
 * Copyright:   Copyright (c) 1998-2003 Regents of the University of California. All rights reserved.
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
 */

package edu.uci.ics.DAVExplorer;

import java.util.Vector;
import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;


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
    protected Vector subNodes = null;


    public DataNode( boolean collection,
                     boolean locked,
                     String lockToken,
                     String name,
                     String display,
                     String type,
                     long size,
                     Date date,
                     Vector subNodes )
    {
        DateFormat df = DateFormat.getDateTimeInstance( DateFormat.FULL, DateFormat.FULL );
        df.setLenient( true );
        String strDate = date==null? "" : df.format( date );
        init( collection, locked, lockToken, name, display, type, size, strDate, subNodes );
    }


    public DataNode( boolean collection,
                     boolean locked,
                     String lockToken,
                     String name,
                     String display,
                     String type,
                     long size,
                     String date,
                     Vector subNodes )
    {
        init( collection, locked, lockToken, name, display, type, size, date, subNodes );
    }


    private void init( boolean collection,
                       boolean locked,
                       String lockToken,
                       String name,
                       String display,
                       String type,
                       long size,
                       String date,
                       Vector subNodes )
    {
        this.name = name;
        this.display = display;
        this.type = type;
        this.size = size;
        this.lastModified = date;
        this.locked = locked;
        this.lockToken = lockToken;
        this.collection = collection;
        this.subNodes = subNodes;
    }


    public void setSubNodes(Vector subNodes)
    {
        this.subNodes = subNodes;
    }


    public Vector getSubNodes()
    {
        return subNodes;
    }


    public void setName(String newName)
    {
        name = newName;
    }


    public void setDisplay(String newDisplay)
    {
        display = newDisplay;
    }


    public void setType(String newType)
    {
        type = newType;
    }


    public void setSize(long newSize)
    {
        size = newSize;
    }


    public void setDate(String newDate)
    {
        lastModified = newDate;
    }


    public void setDate(Date newDate)
    {
        DateFormat df = DateFormat.getDateTimeInstance();
        lastModified = df.format( newDate );
    }


    public void lock( String lockToken )
    {
        locked = true;
        this.lockToken = lockToken;
    }


    public void unlock()
    {
        locked = false;
        lockToken = null;
    }


    public void makeCollection()
    {
        collection = true;
    }


    public void makeNonCollection()
    {
        collection = false;
    }


    public String getName()
    {
        return new String(name);
    }


    public String getDisplay()
    {
        return new String(display);
    }


    public String getType()
    {
        return new String(type);
    }


    public String getLockToken()
    {
        return lockToken;
    }


    public long getSize()
    {
        return size;
    }


    public Date getDate()
    {
        if( lastModified == null || lastModified.length() == 0 )
            return null;

		// documentum workaround hack: they apparently use localized
		// weekday abbreviations, which violates RFC 3518 and RFC 2616
		// This hack checks for valid weekdays and removes anything that
		// does not follow the RFCs
		// Allowed formats:
		// wkday "," ...
		// wkday SP ...
		// weekday "," ...
		// with (see RFC 2616):
		// wkday    = "Mon" | "Tue" | "Wed"
		//		    | "Thu" | "Fri" | "Sat" | "Sun"
		// weekday  = "Monday" | "Tuesday" | "Wednesday"
		//			| "Thursday" | "Friday" | "Saturday" | "Sunday"
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
            catch( ParseException e)
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
                                return new Date(lastModified);
                        }
                        break;
                }
            }
        }
    }


    public boolean isLocked()
    {
        return locked;
    }


    public boolean isCollection()
    {
        return collection;
    }
}
