/*
 * Copyright (c) 2003 Regents of the University of California.
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
 * Title:       Version Info Dialog
 * Description: Dialog for viewing DeltaV version histories
 * Copyright:   Copyright (c) 2003 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        16 September 2003
 */

package edu.uci.ics.DAVExplorer;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.DefaultTableCellRenderer;


public class VersionInfoDialog extends JDialog
    implements ActionListener
{
    String[] colNames = {"Version",
                         "Author", 
                         "Date",
                         "Size",
                         "Checked-In",
                         "Comment",
                         "href"};

    
    public VersionInfoDialog( Vector nodes, String resource, String hostname )
    {
        super( GlobalData.getGlobalData().getMainFrame() );
        setTitle("View Version Information");
        this.resource = hostname + resource;
        JLabel label = new JLabel( this.resource, JLabel.CENTER );
        label.setForeground(Color.black);
        getContentPane().add( "North", label );

        closeButton  = new JButton("Close");
        closeButton.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        getRootPane().setDefaultButton( closeButton );
        closeButton.grabFocus();

        getContentPane().add( "South", buttonPanel );
        setBackground(Color.lightGray);

        CHECKEDIN_ICON = GlobalData.getGlobalData().getImageIcon("version.gif", "");
        CHECKEDOUT_ICON = GlobalData.getGlobalData().getImageIcon("noversion.gif", "");

        dataModel = new AbstractTableModel()
        {
            public int getColumnCount()
            {
                return colNames.length;
            }


            public int getRowCount()
            {
                return data.size();
            }


            public Object getValueAt(int row, int col)
            {
                if (data.size() == 0)
                    return null;
                return ((Vector)data.elementAt(row)).elementAt(col);
            }


            public String getColumnName(int column)
            {
                return colNames[column];
            }


            public Class getColumnClass( int col )
            {
                if (data.size() == 0)
                    return null;
                // try to find a cell in the column that has a value
                // using that value as representative for the column
                Object cell = null;
                int count = 0;
                while( cell == null && count < data.size() )
                {
                    cell = getValueAt( count++, col);
                    if( cell != null )
                        break;
                }

                if( cell == null )
                    return null;

                return cell.getClass();
            }

            public  boolean isCellEditable( int row, int col )
            {
                return false;
            }


            public void setValueAt( Object value, int row, int col )
            {
                try
                {
                    ((Vector)data.elementAt(row)).setElementAt( value, col );
                }
                catch (Exception exc)
                {
                    System.out.println(exc);
                }
            }
        };
        
        table = new JTable( dataModel );

        JScrollPane scrollpane = new JScrollPane();
        scrollpane.setViewportView( table );
        getContentPane().add( "Center", scrollpane );

        setupTable( table );
        addRows( nodes );
        
        addWindowListener(
            new WindowAdapter()
            {
                public void windowClosing(WindowEvent we_Event)
                {
                    cancel();
                }
            }
        );

        MouseListener ml = new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                if(e.getClickCount() == 2)
                {
                    handleDoubleClick(e);
                }
            }
        };
        table.addMouseListener(ml);

        pack();
        center();
    }


    protected void addRows( Vector nodes )
    {
        if( nodes != null )
        {
            for( int i=0; i<nodes.size(); i++ )
            {
                Object[] rowObj = new Object[7];
                rowObj[0] = ((DeltaVDataNode)nodes.elementAt(i)).getVersionName();
                rowObj[1] = ((DeltaVDataNode)nodes.elementAt(i)).getCreatorDisplayName();
                rowObj[2] = ((DeltaVDataNode)nodes.elementAt(i)).getDate();
                rowObj[3] = new Long( ((DeltaVDataNode)nodes.elementAt(i)).getSize() );
                rowObj[4] = new Boolean( ((DeltaVDataNode)nodes.elementAt(i)).getCheckedIn() );
                rowObj[5] = ((DeltaVDataNode)nodes.elementAt(i)).getComment();
                rowObj[6] = ((DeltaVDataNode)nodes.elementAt(i)).getHref();     // hidden
                addRow(rowObj);
                addRows( ((DeltaVDataNode)nodes.elementAt(i)).getVersions());
            }
        }
    }


    private void addRow(Object[] rowData)
    {
        Vector newRow = new Vector();

        int numOfCols = table.getColumnCount();
        for (int i=0;i<numOfCols;i++)
            newRow.addElement(rowData[i]);

        data.addElement(newRow);
    }


    public void actionPerformed(ActionEvent e)
    {
        if( e.getActionCommand().equals("Close") )
        {
            cancel();
        }
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


    public Dimension getPreferredSize()
    {
        return new Dimension( 700, 200 );
    }
    
    
    public void setupTable(JTable table)
    {
        if( GlobalData.getGlobalData().getDebugFileView() )
        {
            System.err.println( "WebDAVFileView::setupTable" );
        }

        table.clearSelection();
        table.setIntercellSpacing(new Dimension(0,0));
        table.setCellSelectionEnabled(false);
        table.setColumnSelectionAllowed(false);
        table.setShowGrid( false );
        DefaultTableCellRenderer ren = new DefaultTableCellRenderer();
        ren.setHorizontalAlignment(JLabel.CENTER);

        // Version name
        TableColumn resizeCol = table.getColumn(colNames[0]);
        resizeCol.setMinWidth(50);
        resizeCol.setCellRenderer(ren);

        // Author
        resizeCol = table.getColumn(colNames[1]);
        resizeCol.setMinWidth(50);
        resizeCol.setCellRenderer(ren);

        // last modified date
        resizeCol = table.getColumn(colNames[2]);
        resizeCol.setMinWidth(150);
        resizeCol.setCellRenderer(ren);

        // size
        resizeCol = table.getColumn(colNames[3]);
        resizeCol.setMinWidth(50);
        resizeCol.setCellRenderer(ren);

        // checked-in
        resizeCol = table.getColumn(colNames[4]);
        resizeCol.setMinWidth(70);
        resizeCol.setMaxWidth(70);
        DefaultTableCellRenderer checkedInRenderer = new DefaultTableCellRenderer()
        {
            public void setValue(Object value)
            {
                try
                {
                    boolean isCheckedIn = new Boolean(value.toString()).booleanValue();
                    if (isCheckedIn)
                        setIcon(CHECKEDIN_ICON);
                    else
                        setIcon(CHECKEDOUT_ICON);
                    setHorizontalAlignment( SwingConstants.CENTER );
                }
                catch (Exception e)
                {
                    // ignore
                }
            }
        };
        resizeCol.setCellRenderer( checkedInRenderer );

        // comment
        resizeCol = table.getColumn(colNames[5]);
        resizeCol.setMinWidth(100);
        resizeCol.setCellRenderer(ren);

        // href
        resizeCol = table.getColumn(colNames[6]);
        resizeCol.setMinWidth(0);
        resizeCol.setMaxWidth(0);
        resizeCol.setCellRenderer(ren);
    }


    public void handleDoubleClick(MouseEvent e)
    {
        Point pt = e.getPoint();
        int col = table.columnAtPoint(pt);
        int row = table.rowAtPoint(pt);

        int column = table.convertColumnIndexToView(0); // version name
        if( column == -1 )
            column = 0;     // use default
        if( (col == column) && (row != -1) )
        {
            Vector ls;
            synchronized( this )
            {
                ls = (Vector)getVersionListeners.clone();
            }
            ActionEvent event = new ActionEvent( this, 0, (String)table.getValueAt(row, 6) );
            for( int i=0; i<ls.size(); i++ )
            {
                ActionListener l = (ActionListener)ls.elementAt(i);
                l.actionPerformed( event );
            }
        }
    }


    public synchronized void addGetVersionListener(ActionListener l)
    {
        getVersionListeners.addElement(l);
    }


    public synchronized void removeGetVersionListener(ActionListener l)
    {
        getVersionListeners.removeElement(l);
    }


    private Vector data = new Vector();
    private TableModel dataModel;
    private JTable table;
    private JButton closeButton;
    private String resource;
    private Vector getVersionListeners = new Vector();
    private ImageIcon CHECKEDIN_ICON;
    private ImageIcon CHECKEDOUT_ICON;
}
