/*
 * Copyright (c) 2005 Regents of the University of California.
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


package edu.uci.ics.DAVExplorer;

import com.ms.xml.om.Element;


/**
 * Title:       ACL OwnerProperty Dialog
 * Description: Dialog for viewing/modifying ACL owner and group properties
 * Copyright:   Copyright (c) 2005 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        14 Feb 2005
 */
public class ACLOwnerDialog extends PropDialog
{
    /**
     * Constructor
     * 
     * @param properties
     * @param resource
     * @param hostname
     * @param locktoken
     * @param changeable
     */
    public ACLOwnerDialog( Element properties, String resource, String hostname, boolean owner, boolean changeable )
    {
        String title;
        if( changeable )
            title = "View/Modify ACL ";
        else
        {
            title = "View ACL ";
            buttonPanel.remove(saveButton);
        }
        if( owner )
            title += "Owner";
        else
            title += "Group";
        init( new ACLPropModel(properties), resource, hostname, title, null, false );
        buttonPanel.remove(addButton);
        buttonPanel.remove(deleteButton);
        pack();
        setSize( getPreferredSize() );
        GlobalData.getGlobalData().center( this );
        show();
    }


    /**
     * 
     */
    public void save()
    {
        Element modified = model.getModified(false);
        ACLRequestGenerator generator = (ACLRequestGenerator)WebDAVResponseInterpreter.getGenerator();
        if( owner )
            generator.SetOwner( modified, resource );
        else
            generator.SetGroup( modified, resource );
        waiting = true;
        generator.execute();
    }


    private boolean owner;
}