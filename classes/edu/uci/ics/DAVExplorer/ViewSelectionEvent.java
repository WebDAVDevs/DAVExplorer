/*
 * Copyright (c) 1998-2001 Regents of the University of California.
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
 * Title:       ViewSelection Event
 * Description: Event to be sent when the selection changes
 * Copyright:   Copyright (c) 1998-2001 Regents of the University of California. All rights reserved.
 * @author      Undergraduate project team ICS 126B 1998
 * @date        1998
 * @author      Yuzo Kanomata, Joachim Feise (dav-exp@ics.uci.edu)
 * @date        17 March 1999
 */

package DAVExplorer;

import java.util.EventObject;
import javax.swing.tree.TreePath;

public class ViewSelectionEvent extends EventObject
{
    TreePath strPath;
    Object Node;

    public ViewSelectionEvent(Object module, Object Node, TreePath Path)
    {
        super(module);
        this.strPath = Path;
        this.Node = Node;
    }

    public ViewSelectionEvent(Object module, TreePath Path)
    {
        super(module);
        this.strPath = Path;
    }

    public TreePath getPath()
    {
        return strPath;
    }

    public Object getNode()
    {
        return Node;
    }
}
