package com.bose.legends;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Objects;

public class ReportAction extends Report
{
    private String actionTaken, actionReasoning, duration;
    @JsonIgnore
    private Timestamp actionTime;

    public ReportAction()
    {
        super();
    }

    public ReportAction(Report report, String actionTaken, String actionReasoning, String duration)
    {
        super(report);

        this.actionTaken = actionTaken;
        this.actionReasoning = actionReasoning;
        this.duration = duration;
        this.actionTime = Timestamp.now();
    }

    public String getActionTaken()
    {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken)
    {
        this.actionTaken = actionTaken;
    }

    public String getActionReasoning()
    {
        return actionReasoning;
    }

    public void setActionReasoning(String actionReasoning)
    {
        this.actionReasoning = actionReasoning;
    }

    public String getDuration()
    {
        return duration;
    }

    public void setDuration(String duration)
    {
        this.duration = duration;
    }

    public Timestamp getActionTime()
    {
        return actionTime;
    }

    public void setActionTime(Timestamp actionTime)
    {
        this.actionTime = actionTime;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        ReportAction report = (ReportAction) o;

        return Objects.equals(super.getReportID(), report.getReportID());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.getAssignedTo(), super.getGroupID(), super.getMessage(),
                super.getReason(), super.getReportedBy(), super.getReportedUser(), super.getReportID(),
                super.getTime());
    }

    @Override
    public String toString()
    {
        return "ReportAction{" +
                "actionTaken='" + actionTaken + '\'' +
                ", actionReasoning='" + actionReasoning + '\'' +
                ", duration='" + duration + '\'' +
                ", actionTime=" + actionTime +
                " Report{" +
                "reportID='" + super.getReportID() + '\'' +
                "assignedTo='" + super.getAssignedTo() + '\'' +
                ", groupID='" + super.getGroupID() + '\'' +
                ", message='" + super.getMessage() + '\'' +
                ", reason='" + super.getReason() + '\'' +
                ", reportedBy='" + super.getReportedBy() + '\'' +
                ", reportedUser='" + super.getReportedUser() + '\'' +
                ", time=" + super.getTime() +
                '}' +
                '}';
    }
}
