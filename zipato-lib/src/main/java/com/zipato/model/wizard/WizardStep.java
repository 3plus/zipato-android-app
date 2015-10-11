/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.wizard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;
import java.util.List;

/**
 * Created by murielK on 4/2/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WizardStep {

    private String transaction;
    private int step;
    private int stepCount;
    private String title;
    private String icon;
    private String body;
    private boolean next;
    private String nextLabel;
    private String nextUrl;
    private boolean repeat;
    private String repeatLabel;
    private String repeatUrl;
    private boolean cancel;
    private String cancelLabel;
    private String cancelUrl;
    private boolean poll;
    private int pollInterval;
    private String pollUrl;
    private boolean countdown;
    private Date countdownUntil;
    private Date now;
    private boolean finished;
    private boolean success;
    private List<WizardField> fields;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WizardStep)) return false;

        WizardStep that = (WizardStep) o;

        if (transaction != null ? !transaction.equals(that.transaction) : that.transaction != null)
            return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        return !(now != null ? !now.equals(that.now) : that.now != null);

    }

    @Override
    public int hashCode() {
        int result = transaction != null ? transaction.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (now != null ? now.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "WizardStep{" +
                "cancel=" + cancel +
                ", countdown=" + countdown +
                ", countdownUntil=" + countdownUntil +
                ", finished=" + finished +
                ", next=" + next +
                ", poll=" + poll +
                ", pollInterval=" + pollInterval +
                ", repeat=" + repeat +
                ", step=" + step +
                ", success=" + success +
                ", title='" + title + '\'' +
                ", transaction='" + transaction + '\'' +
                '}';
    }

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isNext() {
        return next;
    }

    public void setNext(boolean next) {
        this.next = next;
    }

    public String getNextLabel() {
        return nextLabel;
    }

    public void setNextLabel(String nextLabel) {
        this.nextLabel = nextLabel;
    }

    public String getNextUrl() {
        return nextUrl;
    }

    public void setNextUrl(String nextUrl) {
        this.nextUrl = nextUrl;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public String getRepeatLabel() {
        return repeatLabel;
    }

    public void setRepeatLabel(String repeatLabel) {
        this.repeatLabel = repeatLabel;
    }

    public String getRepeatUrl() {
        return repeatUrl;
    }

    public void setRepeatUrl(String repeatUrl) {
        this.repeatUrl = repeatUrl;
    }

    public boolean isCancel() {
        return cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }

    public String getCancelLabel() {
        return cancelLabel;
    }

    public void setCancelLabel(String cancelLabel) {
        this.cancelLabel = cancelLabel;
    }

    public String getCancelUrl() {
        return cancelUrl;
    }

    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }

    public boolean isPoll() {
        return poll;
    }

    public void setPoll(boolean poll) {
        this.poll = poll;
    }

    public int getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(int pollInterval) {
        this.pollInterval = pollInterval;
    }

    public String getPollUrl() {
        return pollUrl;
    }

    public void setPollUrl(String pollUrl) {
        this.pollUrl = pollUrl;
    }

    public boolean isCountdown() {
        return countdown;
    }

    public void setCountdown(boolean countdown) {
        this.countdown = countdown;
    }

    public Date getCountdownUntil() {
        return countdownUntil;
    }

    public void setCountdownUntil(Date countdownUntil) {
        this.countdownUntil = countdownUntil;
    }

    public Date getNow() {
        return now;
    }

    public void setNow(Date now) {
        this.now = now;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<WizardField> getFields() {
        return fields;
    }

    public void setFields(List<WizardField> fields) {
        this.fields = fields;
    }
}
