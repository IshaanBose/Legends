package com.bose.legends;

public class RequestsFormat
{
    private String docID, requestID;

    public String getDocID()
    {
        return docID;
    }

    public void setDocID(String docID)
    {
        this.docID = docID;
    }

    public String getRequestID()
    {
        return requestID;
    }

    public void setRequestID(String requestID)
    {
        this.requestID = requestID;
    }

    @Override
    public String toString()
    {
        return "RequestsFormat{" +
                "docID='" + docID + '\'' +
                ", requestID='" + requestID + '\'' +
                '}';
    }
}
