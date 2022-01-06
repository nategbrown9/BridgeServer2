package org.sagebionetworks.bridge.models.schedules2.adherence.eventstream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;

import com.google.common.collect.ImmutableList;

public class EventStreamDay {
    private String sessionGuid;
    private String sessionName;
    private String sessionSymbol;
    private Integer week;
    private String studyBurstId;
    private Integer studyBurstNum;
    private Integer startDay;
    private LocalDate startDate;
    private Map<String,EventStreamWindow> timeWindows;
    
    public EventStreamDay() { 
        timeWindows = new HashMap<>();
    }
    public String getSessionGuid() {
        return sessionGuid;
    }
    public void setSessionGuid(String sessionGuid) {
        this.sessionGuid = sessionGuid;
    }
    public String getSessionName() {
        return sessionName;
    }
    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }
    public String getSessionSymbol() {
        return sessionSymbol;
    }
    public void setSessionSymbol(String sesionSymbol) {
        this.sessionSymbol = sesionSymbol;
    }
    public Integer getStartDay() {
        return startDay;
    }
    public void setStartDay(Integer startDay) {
        this.startDay = startDay;
    }
    public LocalDate getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    public Integer getWeek() {
        return week;
    }
    public void setWeek(Integer week) {
        this.week = week;
    }
    public String getStudyBurstId() {
        return studyBurstId;
    }
    public void setStudyBurstId(String studyBurstId) {
        this.studyBurstId = studyBurstId;
    }
    public Integer getStudyBurstNum() {
        return studyBurstNum;
    }
    public void setStudyBurstNum(Integer studyBurstNum) {
        this.studyBurstNum = studyBurstNum;
    }
    public List<EventStreamWindow> getTimeWindows() {
        return ImmutableList.copyOf(timeWindows.values());
    }
    public void setTimeWindows(List<EventStreamWindow> timeWindows) {
        if (timeWindows != null) {
            for (EventStreamWindow window : timeWindows) {
                addTimeWindow(window);
            }
        }
    }
    public void addTimeWindow(EventStreamWindow timeWindowEntry) {
        this.timeWindows.put(timeWindowEntry.getTimeWindowGuid(), timeWindowEntry);
    }
}