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

import java.awt.Font;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultMutableTreeNode;
import com.ms.xml.om.Element;


public class PropDialog extends JDialog
    implements ActionListener, ChangeListener, ListSelectionListener, WebDAVCompletionListener
{
    public PropDialog( Element properties, String resource, String hostname, boolean changeable )
    {
        super( GlobalData.getGlobalData().getMainFrame() );
        this.changeable = changeable;
        if( changeable )
            setTitle("View/Modify Properties");
        else
            setTitle("View Properties");
        this.resource = hostname + resource;
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
        JPanel buttonPanel = new JPanel();
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

        model = new PropModel( properties );
        model.addChangeListener(this);
        treeTable = new JTreeTable( model );
        treeTable.getSelectionModel().addListSelectionListener(this);

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

        pack();
        setSize( getPreferredSize() );
        center();
        show();
    }

    public void stateChanged( ChangeEvent e )
    {
        setChanged( true );
    }

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

    public void completion( WebDAVCompletionEvent e )
    {
        if( waiting && e.isSuccessful() )
            setChanged( false );  // disable save button
        waiting = false;
    }

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
            if( parentNode.getValue().length() != 0 )
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
            model.addNode( parentNode, newNode );
            setChanged( true );
        }
    }

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

    public void save()
    {
        Element add = model.getModified(false);
        Element remove = model.getModified(true);
        WebDAVRequestGenerator generator = WebDAVResponseInterpreter.getGenerator();
        generator.GeneratePropPatch( resource, add, remove );
        // TODO: some kind of visual indication
        waiting = true;
        generator.execute();
        // TODO: check for error
        //setChanged( false );  // disable save button
    }

    public void cancel()
    {
        setVisible(false);
        dispose();
    }

    protected void center()
    {
        Rectangle recthDimensions = getParent().getBounds();
        Rectangle bounds = getBounds();
        setBounds(recthDimensions.x + (recthDimensions.width-bounds.width)/2,
             recthDimensions.y + (recthDimensions.height - bounds.height)/2, bounds.width, bounds.height );
    }


    protected boolean ConfirmationDialog( String title, String text )
    {
        JOptionPane pane = new JOptionPane();
        int opt = pane.showConfirmDialog( GlobalData.getGlobalData().getMainFrame(), text, title, JOptionPane.YES_NO_OPTION );
        if (opt == JOptionPane.YES_OPTION)
            return true;
        return false;
    }

    private JTreeTable treeTable;
    private PropModel model;
    private JButton addButton;
    private JButton deleteButton;
    private JButton saveButton;
    private JButton closeButton;
    private boolean changeable;
    private boolean changed = false;
    private String resource;
    private boolean waiting;
}
