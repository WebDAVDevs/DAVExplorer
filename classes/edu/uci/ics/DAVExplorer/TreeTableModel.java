/*
 * TreeTableModel.java
 *
 * Based on the JTreeTable examples provided by Sun Microsystems, Inc.:
 * http://java.sun.com/products/jfc/tsc/articles/treetable1/index.html
 * http://java.sun.com/products/jfc/tsc/articles/treetable2/index.html
 */

package DAVExplorer;

import javax.swing.tree.TreeModel;

public interface TreeTableModel extends TreeModel
{
    public int getColumnCount();

    public String getColumnName( int column );

    public Class getColumnClass( int column );

    public Object getValueAt( Object node, int column );

    public boolean isCellEditable( Object node, int column );

    public void setValueAt( Object aValue, Object node, int column );
}
