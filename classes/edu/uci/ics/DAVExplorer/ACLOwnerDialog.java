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

/**
 * Title:       ACL OwnerProperty Dialog
 * Description: Dialog for viewing/modifying ACL owner and group properties
 * Copyright:   Copyright (c) 2005 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        18 January 2005
 */

package edu.uci.ics.DAVExplorer;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import com.ms.xml.om.Element;


/**
 * 
 */
public class ACLOwnerDialog extends PropDialog
{
    /**
     * Constructor
     * @param properties
     * @param resource
     * @param hostname
     * @param locktoken
     * @param changeable
     */
    public ACLOwnerDialog( Element properties, String resource, String hostname, boolean owner, boolean changeable )
    {
        init( new ACLPropModel(properties), properties, resource, hostname, null, false );
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
        setTitle( title );
        buttonPanel.remove(addButton);
        buttonPanel.remove(deleteButton);
        pack();
        setSize( getPreferredSize() );
        center();
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
