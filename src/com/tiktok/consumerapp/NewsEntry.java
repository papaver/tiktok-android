//-----------------------------------------------------------------------------
// NewsEntry
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.Date;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public final class NewsEntry 
{
    
    /**
     * Called when the activity is first created. 
     */
    public NewsEntry(final String title, final String author, 
                     final Date postDate, final int icon)
    {
        mTitle    = title;
        mAuthor   = author;
        mPostDate = postDate;
        mIcon     = icon;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Title of news entry.
     */
    public String getTitle()
    {
        return mTitle;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Author of the news.
     */
    public String getAuthor()
    {
        return mAuthor;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Post date of the news entry.
     */
    public Date getPostDate()
    {
        return mPostDate;
    }

    //-------------------------------------------------------------------------

    /**
     * @return Icon of this news entry.
     */
    public int getIcon() 
    {
        return mIcon;
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private final String mTitle;
    private final String mAuthor;
    private final Date   mPostDate;
    private final int    mIcon;

}
