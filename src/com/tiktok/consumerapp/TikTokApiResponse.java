//-----------------------------------------------------------------------------
// TikTokApiResponse
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class TikTokApiResponse
{
    @JsonCreator
    public TikTokApiResponse(
        @JsonProperty("status")        final String status,
        @JsonProperty("status_detail") final String detail,
        @JsonProperty("error")         final String error,
        @JsonProperty("results")       final Object results)
    {
        mStatus       = status;
        mStatusDetail = detail;
        mError        = error;
        mResults      = results;
    }

    //-------------------------------------------------------------------------
    // properties
    //-------------------------------------------------------------------------

    public String getStatus()
    {
        return mStatus;
    }

    public String getStatusDetail()
    {
        return mStatusDetail;
    }

    public String getError()
    {
        return mError;
    }

    public Object getResults()
    {
        return mResults;
    }

    //-------------------------------------------------------------------------
    // methods
    //-------------------------------------------------------------------------

    public boolean isOkay()
    {
        return getStatus().equals(TikTokApi.kTikTokApiStatusOkay);
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private final String mStatus;
    private final String mStatusDetail;
    private final String mError;
    private final Object mResults;
}

