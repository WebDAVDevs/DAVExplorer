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

package edu.uci.ics.DAVExplorer;

import java.util.Date;
import java.util.Vector;

/**
 * Title:       DeltaVDataNode
 * Description: Node holding information about versions
 * Copyright:   Copyright (c) 2003 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        23 September 2003
 */
public class DeltaVDataNode extends DataNode
{

    /**
     * @param collection
     * @param locked
     * @param lockToken
     * @param name
     * @param display
     * @param type
     * @param size
     * @param date
     * @param subNodes
     */
    public DeltaVDataNode(
        boolean collection,
        boolean locked,
        String lockToken,
        String name,
        String display,
        String type,
        long size,
        Date date,
        Vector subNodes)
    {
        super(collection, locked, lockToken, name, display, type, size, date, subNodes);
    }

    /**
     * @param collection
     * @param locked
     * @param lockToken
     * @param name
     * @param display
     * @param type
     * @param size
     * @param date
     * @param subNodes
     */
    public DeltaVDataNode(
        boolean collection,
        boolean locked,
        String lockToken,
        String name,
        String display,
        String type,
        long size,
        String date,
        Vector subNodes)
    {
        super(collection, locked, lockToken, name, display, type, size, date, subNodes);
    }


    public DeltaVDataNode( DataNode node )
    {
        super(node.collection, node.locked, node.lockToken, node.name, node.display, node.type, node.size, node.getDate(), node.subNodes);
    }
    
    
    public DeltaVDataNode( DeltaVDataNode node )
    {
        super(node.collection, node.locked, node.lockToken, node.name, node.display, node.type, node.size, node.getDate(), node.subNodes);

        this.versions = node.versions;
        this.comment = node.comment;
        this.creatorDisplayName = node.creatorDisplayName;
        this.supportedMethodSet = node.supportedMethodSet;
        this.supportedLivePropertySet = node.supportedLivePropertySet;
        this.supportedReportSet = node.supportedReportSet;
        this.checkedIn = node.checkedIn;
        this.autoVersion = node.autoVersion;
        this.checkedOut = node.checkedOut;
        this.predecessorSet = node.predecessorSet;
        this.successorSet = node.successorSet;
        this.checkoutSet = node.checkoutSet;
        this.versionName =node.versionName;
        this.href = node.href;
        this.deltaV = node.deltaV;
        this.deltaVReports = node.deltaVReports;
    }
    

    public void copyFrom( DeltaVDataNode node )
    {
        if( node.getComment().length() > 0 )
            setComment( node.getComment() );
        if( node.getCreatorDisplayName().length() > 0 )
            setCreatorDisplayName( node.getCreatorDisplayName() );
        if( node.getSupportedMethodSet().length() > 0 )
            setSupportedMethodSet( node.getSupportedMethodSet() );
        if( node.getSupportedLivePropertySet().length() > 0 )
            setSupportedLivePropertySet( node.getSupportedLivePropertySet() );
        if( node.getSupportedReportSet().length() > 0 )
            setSupportedReportSet( node.getSupportedReportSet() );
        if( node.getCheckedIn().length() > 0 )
            setCheckedIn( node.getCheckedIn() );
        if( node.getAutoVersion().length() > 0 )
            setAutoVersion( node.getAutoVersion() );
        if( node.getCheckedOut().length() > 0 )
            setCheckedOut( node.getCheckedOut() );
        if( node.getPredecessorSet().length() > 0 )
            setPredecessorSet( node.getPredecessorSet() );
        if( node.getSuccessorSet().length() > 0 )
            setSuccessorSet( node.getSuccessorSet() );
        if( node.getCheckoutSet().length() > 0 )
            setCheckoutSet( node.getCheckoutSet() );
        if( node.getVersionName().length() > 0 )
            setVersionName( node.getVersionName() );
        if( node.getHref().length() > 0 )
            setHref( node.getHref() );
        if( node.getDeltaV() )
            setDeltaV( node.getDeltaV() );
        if( node.getDeltaVReports() )
            setDeltaVReports( node.getDeltaVReports() );
    }
    

    public void setVersions( Vector versions )
    {
        this.versions = versions;
    }


    public void addVersion( DataNode node )
    {
        if( versions == null )
            versions = new Vector();
        versions.add( node );
    }


    public Vector getVersions()
    {
        return versions;
    }


    public boolean hasVersions()
    {
        if( versions != null && versions.size() > 0 )
            return true;
        if( checkedIn != null && checkedIn.length() > 0 )
            return true;
        if( checkedOut != null && checkedOut.length() > 0 )
            return true;
        if( versionName != null && versionName.length() > 0 )
            return true;

        return false;
    }
    
    
    public void setComment( String comment )
    {
        this.comment = comment;
    }


    public String getComment()
    {
        return (comment==null)? "" : comment;
    }


    public void setCreatorDisplayName( String creatorDisplayName )
    {
        this.creatorDisplayName = creatorDisplayName;
    }


    public String getCreatorDisplayName()
    {
        return (creatorDisplayName==null)? "" : creatorDisplayName;
    }


    public void setSupportedMethodSet( String supportedMethodSet )
    {
        this.supportedMethodSet = supportedMethodSet;
    }


    public String getSupportedMethodSet()
    {
        return (supportedMethodSet==null)? "" : supportedMethodSet;
    }


    public void setSupportedLivePropertySet( String supportedLivePropertySet )
    {
        this.supportedLivePropertySet = supportedLivePropertySet;
    }


    public String getSupportedLivePropertySet()
    {
        return (supportedLivePropertySet==null)? "" : supportedLivePropertySet;
    }


    public void setSupportedReportSet( String supportedReportSet )
    {
        this.supportedReportSet = supportedReportSet;
    }


    public String getSupportedReportSet()
    {
        return (supportedReportSet==null)? "" : supportedReportSet;
    }


    public void setCheckedIn( String checkedIn )
    {
        this.checkedIn = checkedIn;
    }


    public String getCheckedIn()
    {
        return (checkedIn==null)? "" : checkedIn;
    }


    public void setAutoVersion( String autoVersion )
    {
        this.autoVersion = autoVersion;
    }


    public String getAutoVersion()
    {
        return (autoVersion==null)? "" : autoVersion;
    }


    public void setCheckedOut( String checkedOut )
    {
        this.checkedOut = checkedOut;
    }


    public String getCheckedOut()
    {
        return (checkedOut==null)? "" : checkedOut;
    }


    public void setPredecessorSet( String predecessorSet )
    {
        this.predecessorSet = predecessorSet;
    }


    public String getPredecessorSet()
    {
        return (predecessorSet==null)? "" : predecessorSet;
    }


    public void setSuccessorSet( String successorSet )
    {
        this.successorSet = successorSet;
    }


    public String getSuccessorSet()
    {
        return (successorSet==null)? "" : successorSet;
    }

    public void setCheckoutSet( String checkoutSet )
    {
        this.checkoutSet = checkoutSet;
    }


    public String getCheckoutSet()
    {
        return (checkoutSet==null)? "" : checkoutSet;
    }


    public void setVersionName( String versionName )
    {
        this.versionName = versionName;
    }


    public String getVersionName()
    {
        return (versionName==null)? "" : versionName;
    }


    public void setHref( String href )
    {
        this.href = href;
    }


    public String getHref()
    {
        return (href==null)? "" : href;
    }


    public boolean isDeltaV()
    {
        return getDeltaV();
    }
    
    
    public boolean getDeltaV()
    {
        return deltaV;
    }
    
    
    public void setDeltaV( boolean deltaV )
    {
        this.deltaV = deltaV;
    }
    
    
    public boolean getDeltaVReports()
    {
        return deltaVReports;
    }
    
    
    public void setDeltaVReports( boolean reports )
    {
        deltaVReports = reports;
    }


    protected Vector versions = null;

    protected String comment;
    protected String creatorDisplayName;
    protected String supportedMethodSet;
    protected String supportedLivePropertySet;
    protected String supportedReportSet;
    protected String checkedIn;
    protected String autoVersion;
    protected String checkedOut;
    protected String predecessorSet;
    protected String successorSet;
    protected String checkoutSet;
    protected String versionName;
    protected String href;
    
    protected boolean deltaV = false;
    protected boolean deltaVReports = false;


}
