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

package DAVExplorer;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.border.*;
import java.util.*;
import com.ms.xml.om.Element;


public class PropDialog extends JDialog implements ActionListener, ChangeListener
{
    public PropDialog( Element properties, String resource, String hostname, boolean changeable )
    {
        super( GlobalData.getGlobalData().getMainFrame() );
        this.changeable = changeable;
        if( changeable )
            setTitle("View/Modify Properties");
        else
            setTitle("View Properties");
        JLabel label = new JLabel( resource + " (" + hostname + ")", JLabel.CENTER );
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(Color.black);
        getContentPane().add( "North", label );

        addButton = new JButton("Add");
        addButton.addActionListener(this);
        saveButton = new JButton("Save");
        saveButton.addActionListener(this);
        closeButton  = new JButton("Close");
        closeButton.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);
        getRootPane().setDefaultButton( closeButton );
        closeButton.grabFocus();
        if( !changeable )
            addButton.setEnabled( false );
        saveButton.setEnabled( false );

        getContentPane().add( "South", buttonPanel );
        setBackground(Color.lightGray);

        model = new PropModel( properties );
        model.addChangeListener(this);
        treeTable = new JTreeTable( model );

        JScrollPane scrollpane = new JScrollPane();
        scrollpane.setViewportView( treeTable );
        getContentPane().add( "Center", scrollpane );

        addWindowListener(
            new WindowAdapter()
            {
                public void windowClosing(WindowEvent we_Event)
                {
                    cancel();
                }
            });

        pack();
        setSize( getPreferredSize() );
        center();
        show();
    }

    public void stateChanged( ChangeEvent e )
    {
        if( changeable )
        {
            changed = true;
            if( saveButton != null )
                saveButton.setEnabled( true );
        }
    }

    public void actionPerformed(ActionEvent e)
    {
        if( e.getActionCommand().equals("Add") )
        {
            add();
        }
        else if( e.getActionCommand().equals("Save") )
        {
            save();
        }
        else if( e.getActionCommand().equals("Close") )
        {
            cancel();
        }
    }

    public void add()
    {
        // TODO
        setVisible(false);
    }

    public void save()
    {
        // TODO
        setVisible(false);
    }

    public void cancel()
    {
        setVisible(false);
    }

    protected void center()
    {
        Rectangle recthDimensions = getParent().getBounds();
        Rectangle bounds = getBounds();
        setBounds(recthDimensions.x + (recthDimensions.width-bounds.width)/2,
             recthDimensions.y + (recthDimensions.height - bounds.height)/2, bounds.width, bounds.height );
    }

    private PropModel model;
    private JTreeTable treeTable;
    private JButton addButton;
    private JButton saveButton;
    private JButton closeButton;
    private boolean changeable;
    private boolean changed = false;
}
