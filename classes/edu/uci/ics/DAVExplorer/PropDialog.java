/*
 * Copyright (c) 2001-2004 Regents of the University of California.
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
 * Title:       Property Dialog
 * Description: Dialog for viewing/modifying DAV properties
 * Copyright:   Copyright (c) 2001-2004 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        29 September 2001
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        1 October 2001
 * Changes:     Change of package name
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        08 February 2004
 * Changes:     Added Javadoc templates
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
import javax.swing.tree.TreePath;
import com.ms.xml.om.Element;


/**
 * 
 */
public class PropDialog extends JDialog
    implements ActionListener, ChangeListener, ListSelectionListener, WebDAVCompletionListener
{
    /**
     * Constructor
     * @param properties
     * @param resource
     * @param hostname
     * @param locktoken
     * @param changeable
     */
    public PropDialog( Element properties, String resource, String hostname, String locktoken, boolean changeable )
    {
        super( GlobalData.getGlobalData().getMainFrame() );
        model = new PropModel( properties );
        init( model, properties, resource, hostname, locktoken, changeable );
        pack();
        setSize( getPreferredSize() );
        center();
        show();
    }


    public PropDialog( PropModel model, Element properties, String resource, String hostname, String locktoken, boolean changeable )
    {
        super( GlobalData.getGlobalData().getMainFrame() );
        init( model, properties, resource, hostname, locktoken, changeable );
        pack();
        setSize( getPreferredSize() );
        center();
        show();
    }


    protected PropDialog()
    {
        super( GlobalData.getGlobalData().getMainFrame() );
    }

    
    protected void init( PropModel model, Element properties, String resource, String hostname, String locktoken, boolean changeable )
    {
        this.changeable = changeable;
        if( changeable )
            setTitle("View/Modify Properties");
        else
            setTitle("View Properties");
        this.resource = hostname + resource;
        this.locktoken = locktoken;
        JLabel label = new JLabel( this.resource, JLabel.CENTER );
        label.setForeground(Color.black);
        getContentPane().add( "North", label );

        addButton = new JButton("Add");
        addButton.addActionListener(this);
        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(this);
        deleteButton.setEnabled( false );
        saveButton = new JButton("Save");
        saveButton.addActionListener(this);
        closeButton  = new JButton("Close");
        closeButton.addActionListener(this);
        buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);
        getRootPane().setDefaultButton( closeButton );
        closeButton.grabFocus();
        if( !changeable )
        {
            addButton.setEnabled( false );
            deleteButton.setEnabled( false );
        }
        saveButton.setEnabled( false );

        getContentPane().add( "South", buttonPanel );
        setBackground(Color.lightGray);

        model.addChangeListener(this);
        treeTable = new JTreeTable( model );
        treeTable.getSelectionModel().addListSelectionListener(this);
        // the minimum number of rows to see, expand as necessary
        JTree tree = treeTable.getTree();
        tree.expandRow( 0 );
        if( tree.getRowCount() < 5 )
            tree.expandRow( 1 );

        JScrollPane scrollpane = new JScrollPane();
        scrollpane.setViewportView( treeTable );
        getContentPane().add( "Center", scrollpane );

        ((Main)GlobalData.getGlobalData().getMainFrame()).addWebDAVCompletionListener(this);
        addWindowListener(
            new WindowAdapter()
            {
                public void windowClosing(WindowEvent we_Event)
                {
                    cancel();
                }
            });

    }


    /**
     * 
     * @param e
     */
    public void stateChanged( ChangeEvent e )
    {
        setChanged( true );
    }


    /**
     * 
     * @param enable
     */
    public void setChanged( boolean enable )
    {
        if( changeable )
        {
            changed = enable;
            saveButton.setEnabled( changed );
            if( !changed )
                model.clear();
        }
    }


    /**
     * 
     * @param e
     */
    public void actionPerformed(ActionEvent e)
    {
        if( e.getActionCommand().equals("Add") )
        {
            add();
        }
        else if( e.getActionCommand().equals("Delete") )
        {
            remove();
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


    /**
     * 
     * @param e
     */
    public void valueChanged(ListSelectionEvent e)
    {
        if( changeable )
        {
            TreePath path = treeTable.getTree().getPathForRow( treeTable.getSelectedRow() );
            if( path == null )
            {
                deleteButton.setEnabled( false );
                return;
            }

            PropNode node = (PropNode)path.getLastPathComponent();
            deleteButton.setEnabled( model.isNodeRemovable(node) );
        }
    }


    /**
     * 
     * @param e
     */
    public void completion( WebDAVCompletionEvent e )
    {
        if( waiting && e.isSuccessful() )
            setChanged( false );  // disable save button
        waiting = false;
    }


    /**
     * 
     */
    public void add()
    {
        boolean selected = true;
        TreePath path = treeTable.getTree().getPathForRow( treeTable.getSelectedRow() );
        if( path == null )
            selected = false;
        else
        {
            PropNode parentNode = (PropNode)path.getLastPathComponent();
            // can't have child nodes if value is not empty
            if( (parentNode.getValue().length() != 0) || parentNode.isDAVProp() )
                selected = false;
        }

        PropAddDialog add = new PropAddDialog( resource, selected );
        if( !add.isCanceled() )
        {
            PropNode parentNode = null;
            PropNode newNode = new PropNode( add.getTag(), add.getNamespace(), add.getValue(), true );
            if( add.isAddToRoot() )
                parentNode = (PropNode)model.getRoot();
            else
                parentNode = (PropNode)path.getLastPathComponent();
            model.addNode( parentNode, newNode, add.isAddToRoot() );
            setChanged( true );
        }
    }


    /**
     *
     */
    public void remove()
    {
        String title = "Delete Property";
        String text = "Do you really want to delete the selected property?";
        if( ConfirmationDialog( title, text ) )
        {
            TreePath path = treeTable.getTree().getPathForRow( treeTable.getSelectedRow() );
            model.removeNode( path );
            treeTable.updateUI();
            setChanged( true );
        }
    }


    /**
     * 
     */
    public void save()
    {
        Element add = model.getModified(false);
        Element remove = model.getModified(true);
        WebDAVRequestGenerator generator = WebDAVResponseInterpreter.getGenerator();
        generator.GeneratePropPatch( resource, add, remove, locktoken );
        waiting = true;
        generator.execute();
    }


    /**
     *
     */
    public void cancel()
    {
        setVisible(false);
        dispose();
    }


    /**
     *
     */
    protected void center()
    {
        Rectangle recthDimensions = getParent().getBounds();
        Rectangle bounds = getBounds();
        setBounds(recthDimensions.x + (recthDimensions.width-bounds.width)/2,
             recthDimensions.y + (recthDimensions.height - bounds.height)/2, bounds.width, bounds.height );
    }


    /**
     * 
     * @param title
     * @param text
     * @return
     */
    protected boolean ConfirmationDialog( String title, String text )
    {
        int opt = JOptionPane.showConfirmDialog( GlobalData.getGlobalData().getMainFrame(), text, title, JOptionPane.YES_NO_OPTION );
        if (opt == JOptionPane.YES_OPTION)
            return true;
        return false;
    }


    protected JTreeTable treeTable;
    protected PropModel model;
    protected JPanel buttonPanel;
    protected JButton addButton;
    protected JButton deleteButton;
    protected JButton saveButton;
    protected JButton closeButton;
    protected boolean changeable;
    protected boolean changed = false;
    protected String resource;
    protected String locktoken;
    protected boolean waiting;
}
