
/**
 * Title:        DAV Explorer<p>
 * Description:  <p>
 * Copyright:    Copyright (c) 1999-2001 U.C. Regents<p>
 * Company:      University of California, Irvine<p>
 * @author Joachim Feise
 * @version
 */
package DAVExplorer;

import java.util.EventListener;

public interface WebDAVCompletionListener extends EventListener
{
    public void completion( WebDAVCompletionEvent e );
}
