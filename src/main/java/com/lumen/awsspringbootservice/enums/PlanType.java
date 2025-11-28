package com.lumen.awsspringbootservice.enums;

public enum PlanType {
    WEEK(7),
    MONTH(30),
    FULL(Integer.MAX_VALUE),
    PREMIERE(0);

    private final int duration;

    PlanType(int duration) {
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }
}

