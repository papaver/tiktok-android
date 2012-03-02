//-----------------------------------------------------------------------------
// TikTokApiMultiResponse
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public class TikTokApiMultiResponse
{
    @JsonCreator
    public TikTokApiMultiResponse(
        @JsonProperty("status")        final Map<String, String> status,
        @JsonProperty("status_detail") final Map<String, String> detail,
        @JsonProperty("error")         final Map<String, String> error,
        @JsonProperty("results")       final Map<String, Object> results)
    {
        mStatus       = status;
        mStatusDetail = detail;
        mError        = error;
        mResults      = results;
    }

    //-------------------------------------------------------------------------
    // properties
    //-------------------------------------------------------------------------

    public String getStatus(String key)
    {
        return mStatus != null ? mStatus.get(key) : null;
    }

    public String getStatusDetail(String key)
    {
        return mStatusDetail != null ? mStatusDetail.get(key) : null;
    }

    public String getError(String key)
    {
        return mError != null ? mError.get(key) : null;
    }

    public Object getResults(String key)
    {
        return mResults != null ? mResults.get(key) : null;
    }

    public TikTokApiResponse getResponse(String key)
    {
        TikTokApiResponse response = new TikTokApiResponse(
            getStatus(key), getStatusDetail(key), getError(key), getResults(key));
        return response;
    }

    //-------------------------------------------------------------------------
    // methods
    //-------------------------------------------------------------------------

    public boolean isOkay(String key)
    {
        return getStatus(key).equals(TikTokApi.kTikTokApiStatusOkay);
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private final Map<String, String> mStatus;
    private final Map<String, String> mStatusDetail;
    private final Map<String, String> mError;
    private final Map<String, Object> mResults;
}

