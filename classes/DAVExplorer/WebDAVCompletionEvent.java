
/**
 * Title:        DAV Explorer<p>
 * Description:  <p>
 * Copyright:    Copyright (c) 1999-2001 U.C. Regents<p>
 * Company:      University of California, Irvine<p>
 * @author Joachim Feise
 * @version
 */
package DAVExplorer;

import java.util.EventObject;

public class WebDAVCompletionEvent extends EventObject
{

    public WebDAVCompletionEvent( Object source, boolean success )
    {
        super( source );
        this.success = success;
    }

    public boolean isSuccessful()
    {
        return success;
    }

    private boolean success;
}
