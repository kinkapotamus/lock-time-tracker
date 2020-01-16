package com.locktimetracker.utils;

import android.os.CountDownTimer;

public abstract class CountUpTimer extends CountDownTimer {
    private static final long INTERVAL_MS = 1000;
    private final long duration;
    private long startFromInMs;

    protected CountUpTimer(long durationMs, long startFromInMs) {
        super(durationMs, INTERVAL_MS);
        this.duration = durationMs;
        this.startFromInMs = startFromInMs;
    }

    public abstract void onTick(int second);

    @Override
    public void onTick(long msUntilFinished) {
        int second = (int) ((duration - msUntilFinished) / 1000);
        onTick(second);
    }

    @Override
    public void onFinish() {
        onTick(duration / 1000);
    }

    public long getStartFromInMs() {
        return startFromInMs;
    }
    public void setStartFromInMs(long startFromInMs) {
        this.startFromInMs = startFromInMs;
    }
}
