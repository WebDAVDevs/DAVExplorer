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

// This is the class containing the main() function, which merely
// instantiates the Main JFrame.
// The Main class creates the user interface and adds the appropriate
// listeners.
//
// Version: 0.3.1
// Author:  Robert Emmery
// Date:    4/2/98
////////////////////////////////////////////////////////////////
// The code has been modified to include povisions for the final
// WebDAV xml namespaces.  A small number of program errors have
// been corrected.
//
// Please use the following contact:
//
// dav-exp@ics.uci.edu
//
// Version: 0.4
// Changes by: Yuzo Kanomata and Joe Feise
// Date: 3/17/99
//
// Change List:
// 1. Changed treeView.fireSelectionEvent(); to treeView.initTree();
//    This is the same function, but with a better name.
// 2. Added Create Folder functionality
// 3. Added filename selection for export file

package WebDAV;

import com.sun.java.swing.*;
import com.sun.java.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import WebDAV.WebDAVManager;

public class Main extends JFrame
{
    JFrame WebDAVFrame;
    WebDAVFileView fileView;
    WebDAVTreeView treeView;
    WebDAVRequestGenerator requestGenerator;
    WebDAVResponseInterpreter responseInterpreter;
    WebDAVManager webdavManager;
    WebDAVMenu CommandMenu;
    WebDAVLoginDialog ld;
    Hashtable authTable;
    String authHost;
    public final static String VERSION = "0.4";
    public final static String UserAgent = "UCI DAV Explorer/" + VERSION;

    public Main(String frameName)
    {
        super (frameName);
//        Uncomment the following 8 lines if you want system's L&F
//        try
//        {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        }
//        catch (Exception except)
//        {
//            System.out.println("Error Loading L&F");
//        }
        WebDAVFrame = this;
        authTable = new Hashtable();
        authHost = null;
        treeView = new WebDAVTreeView(WebDAVFrame);
        fileView = new WebDAVFileView(WebDAVFrame);
        CommandMenu = new WebDAVMenu();
        setJMenuBar(CommandMenu);

        MenuListener_Gen menuListener = new MenuListener_Gen();
        WebDAVToolBar toolbar = new WebDAVToolBar();
        toolbar.addActionListener( menuListener );
        URIBox uribox = new URIBox();
        uribox.addActionListener(new URIBoxListener_Gen());

        CommandMenu.addWebDAVMenuListener( menuListener );
        fileView.addViewSelectionListener(new TableSelectListener_Gen());
        fileView.addViewSelectionListener(new TableSelectListener_Tree());

        treeView.addViewSelectionListener(new TreeSelectListener_Gen());
        treeView.addViewSelectionListener(new TreeSelectListener_Table());
        requestGenerator = new WebDAVRequestGenerator(WebDAVFrame);
        requestGenerator.setUserAgent( UserAgent );
        fileView.addRenameListener(new RenameListener());
        fileView.addDisplayLockListener(new DisplayLockListener());
        responseInterpreter = new WebDAVResponseInterpreter(WebDAVFrame);
        responseInterpreter.addInsertionListener(new InsertionListener());
        responseInterpreter.addMoveUpdateListener(new MoveUpdateListener());
        responseInterpreter.addLockListener(new LockListener());
        webdavManager = new WebDAVManager(WebDAVFrame);
        requestGenerator.addRequestListener(new RequestListener());
        webdavManager.addResponseListener(new ResponseListener());

        JScrollPane fileScrPane = fileView.getScrollPane();
        JScrollPane treeScrPane = treeView.getScrollPane();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,treeScrPane,fileScrPane);
        splitPane.setContinuousLayout(true);

        JPanel p = new JPanel();
        p.setSize(800,600);
        GridBagLayout gridbag = new GridBagLayout();
        p.setLayout(gridbag);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(toolbar,c);
        p.add(toolbar);

        c.gridy= GridBagConstraints.RELATIVE;
        c.gridheight = GridBagConstraints.RELATIVE;
        gridbag.setConstraints(uribox,c);
        p.add(uribox);

        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        c.weightx = 1.0;
        c.gridheight = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(splitPane,c);
        p.add(splitPane);

        getContentPane().add(p);
        treeView.initTree();
        pack();
        setVisible(true);

        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent we_Event)
            {
                System.exit(0);
            }
        } );
    }

    public void checkAuth(String in)
    {
        int pos = in.indexOf("/");
        String host = null;
        String authInfo = null;

//        if (pos < 0)
//            pos = in.indexOf("/");
        if (pos < 0)
            host = in;
        else
            host = in.substring(0,pos);
        authHost = host;
        if (!authTable.containsKey(host))
        {
            ld = new WebDAVLoginDialog(WebDAVFrame, new LoginDialogListener(), "Auth Info For " + host + ":",true);
        }
        else
        {
            authInfo = (String) authTable.get(host);
            pos = authInfo.indexOf(":");
            requestGenerator.setUser(authInfo.substring(0,pos));
            requestGenerator.setPass(authInfo.substring(pos+1));
        }
    }

    public final static void main(String[] argv)
    {
        Main mFrame = new Main("WebDAV Explorer");
    }

    class LoginDialogListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            String user, pass;
            String str = e.getActionCommand();
            int pos = str.indexOf(":");
            user = str.substring(0,pos);
            pass = str.substring(pos+1);
            authTable.put(authHost, user + ":" + pass);
            requestGenerator.setUser(user);
            requestGenerator.setPass(pass);
        }
    }

    class URIBoxListener_Gen implements WebDAVURIBoxListener
    {
        public void actionPerformed(ActionEvent e)
        {
            String str = e.getActionCommand();
            checkAuth(str);
            if (!str.endsWith("/"))
                str += "/";
            requestGenerator.setExtraInfo("uribox");
            requestGenerator.GeneratePropFind(str,"allprop","one",null,null);
            requestGenerator.execute();
        }
    }

    class InsertionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            String str = e.getActionCommand();
            if (str == null)
            {
                treeView.refresh();
            }
            else
                treeView.addRowToRoot(str,false);
        }
    }

    class MoveUpdateListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            String str = e.getActionCommand();
            if (str == null)
                fileView.update();
            else
            {
                fileView.resetName();
            }
        }
    }

    class TableSelectListener_Gen implements ViewSelectionListener
    {
        public void selectionChanged(ViewSelectionEvent e)
        {
            requestGenerator.tableSelectionChanged(e);
        }
    }

    class TableSelectListener_Tree implements ViewSelectionListener
    {
        public void selectionChanged(ViewSelectionEvent e)
        {
            treeView.tableSelectionChanged(e);
        }
    }

    class TreeSelectListener_Gen implements ViewSelectionListener
    {
        public void selectionChanged(ViewSelectionEvent e)
        {
            requestGenerator.treeSelectionChanged(e);
        }
    }

    class LockListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            String token = e.getActionCommand();
            if ( e.getID() == 0 )
            {
                fileView.setLock();
                treeView.setLock( fileView.getName(), token );
            }
            else
            {
                fileView.resetLock();
                treeView.resetLock( fileView.getName() );
            }
        }
    }

    class DisplayLockListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            requestGenerator.DiscoverLock("display");
        }
    }

    class RenameListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            String str = e.getActionCommand();
            if (str != null)
                requestGenerator.GenerateRename( str, treeView.getCurrentPath() );
        }
    }

    class TreeSelectListener_Table implements ViewSelectionListener
    {
        public void selectionChanged(ViewSelectionEvent e)
        {
            fileView.treeSelectionChanged(e);
        }
    }

    class RequestListener implements WebDAVRequestListener
    {
        public void requestFormed(WebDAVRequestEvent e)
        {
            webdavManager.sendRequest(e);
        }
    }

    class ResponseListener implements WebDAVResponseListener
    {
        public void responseFormed(WebDAVResponseEvent e)
        {
            responseInterpreter.handleResponse(e);
        }
    }

    class MenuListener_Gen implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            String command = e.getActionCommand();
            if (command.equals("Exit"))
                System.exit(0);

            else if (command.equals("Get File"))
            {
                saveAsDocument();
            }
            else if (command.equals("Write File"))
            {
                FileDialog fd = new FileDialog(WebDAVFrame, "Write File" , FileDialog.LOAD);
                fd.setVisible(true);
                if( fd.getDirectory() != null )
                {
                    String fullPath = fd.getDirectory() + fd.getFile();
                    String token = treeView.getLockToken( fd.getFile() );
                    requestGenerator.GeneratePut( fullPath, treeView.getCurrentPath(), token );
                    requestGenerator.execute();
                }
            }
            else if (command.equals("Lock"))
            {
                lockDocument();
            }
            else if (command.equals("Unlock"))
            {
                unlockDocument();
            }
            else if (command.equals("Duplicate"))
            {
                requestGenerator.GenerateCopy( null, true, true );
                requestGenerator.execute();
            }
            else if (command.equals("Delete"))
            {
                deleteDocument();
            }
            else if (command.equals("Create Folder"))
            {
                String prompt = new String( "Enter directory name:" );
                String title = new String( "Directory Name" );
                String dirname = selectName( title, prompt );
                if( dirname != null )
                {
                    requestGenerator.GenerateMkCol( treeView.getCurrentPath(), dirname );
                    requestGenerator.execute();
                }
            }
            else if (command.equals("Clear Auth Buffer"))
            {
                authTable.clear();
            }
            else if (command.equals("Edit Lock Info"))
            {
                WebDAVLockInfo lockInfo = new WebDAVLockInfo(WebDAVFrame, "Lock Info", true);
            }
            else if (command.equals("HTTP Logging"))
            {
                boolean logging = false;
                String logFilename = null;
                if( CommandMenu.getLogging() )
                {
                    JOptionPane pane = new JOptionPane();
                    String message = new String( "WARNING: The logfile may get very large,\nsince all data is logged.\n" +
                                                 "Hit Cancel now if you don't want to log the data." );
                    int opt = pane.showConfirmDialog( WebDAVFrame, message, "HTTP Logging", JOptionPane.OK_CANCEL_OPTION );
                    if( opt == JOptionPane.OK_OPTION )
                    {
                        FileDialog fd = new FileDialog( WebDAVFrame, "Select Logging File" , FileDialog.SAVE );
                        fd.setVisible(true);
                        if( fd.getDirectory() != null )
                        {
                            logFilename = fd.getDirectory() + fd.getFile();
                            try
                            {
                                File f = new File( logFilename );
                                if( f.exists() )
                                    f.delete();
                                logging = true;
                            }
                            catch( Exception exception )
                            {
                                System.out.println( "File could not be deleted.\n" + exception );
                                logFilename = null;
                            }
                        }
                    }
                }
                    
                CommandMenu.setLogging( logging );
                webdavManager.setLogging( logging, logFilename );
            }
            else if (command.equals("View Properties"))
            {
                viewProperties();
            }
            else if (command.equals("View Lock Properties"))
            {
                requestGenerator.DiscoverLock("display");
            }
            else if (command.equals("Refresh"))
            {
                responseInterpreter.setRefresh();
                treeView.refresh();
            }
            else if (command.equals("About WebDAV..."))
            {
                JOptionPane pane = new JOptionPane();
                String message = new String("WebDAV Explorer Version: "+ VERSION + "\n\nUCI WebDAV Client Group is:\nUniversity of California, Irvine\n\tGerair Balian\n\tMirza Baig\n\tRobert Emmery\n\tThai Le\n\tTu Le\n" +
                                "Update for the final WebDAV Specification and Namespaces:\nYuzo Kanomata and Joachim Feise\n");
                Object [] options = { "OK" };
                pane.showOptionDialog(WebDAVFrame, message, "About WebDAV Client", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
            }
        }
    }

    protected void viewDocument()
    {
        requestGenerator.GenerateGet("view");
        requestGenerator.execute();
    }

    protected void saveDocument()
    {
        requestGenerator.GenerateGet("save");
        requestGenerator.execute();
    }

    protected void saveAsDocument()
    {
        requestGenerator.GenerateGet("saveas");
        requestGenerator.execute();
    }

    protected void deleteDocument()
    {
        JOptionPane pane = new JOptionPane();
        String str = new String("Are you sure?");
        int opt = pane.showConfirmDialog( WebDAVFrame, str, "Delete File", JOptionPane.YES_NO_OPTION );
        if (opt == JOptionPane.YES_OPTION)
            requestGenerator.DiscoverLock("delete");
    }

    protected void lockDocument()
    {
        requestGenerator.DiscoverLock("lock");
    }

    protected void unlockDocument()
    {
        requestGenerator.DiscoverLock("unlock");
    }

    protected void viewProperties()
    {
        requestGenerator.setExtraInfo("properties");
        requestGenerator.GeneratePropFind(null,"allprop","zero",null,null);
        requestGenerator.execute();
    }


    private String selectName( String title, String prompt )
    {
        JOptionPane pane = new JOptionPane();
        String ret = pane.showInputDialog( WebDAVFrame, prompt, title ,JOptionPane.QUESTION_MESSAGE );
        return ret;
    }


}
