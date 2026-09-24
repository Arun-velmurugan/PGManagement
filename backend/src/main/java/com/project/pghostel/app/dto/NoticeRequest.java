package com.project.pghostel.app.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class NoticeRequest {

    private String noticeType;

    private String title;

    private String description;

    private LocalDate effectiveDate;

    private LocalTime fromTime;

    private LocalTime toTime;

    private boolean feedbackRequired;


    public String getNoticeType() {
        return noticeType;
    }

    public void setNoticeType(String noticeType) {
        this.noticeType = noticeType;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }


    public LocalTime getFromTime() {
        return fromTime;
    }

    public void setFromTime(LocalTime fromTime) {
        this.fromTime = fromTime;
    }


    public LocalTime getToTime() {
        return toTime;
    }

    public void setToTime(LocalTime toTime) {
        this.toTime = toTime;
    }


    public boolean isFeedbackRequired() {
        return feedbackRequired;
    }

    public void setFeedbackRequired(boolean feedbackRequired) {
        this.feedbackRequired = feedbackRequired;
    }
}