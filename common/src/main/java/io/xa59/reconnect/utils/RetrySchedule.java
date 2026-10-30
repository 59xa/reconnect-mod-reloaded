package io.xa59.reconnect.utils;

public final class RetrySchedule {
    private static final int[] INTERVALS = {3, 10, 30, 60};
    private int attempts;
    private boolean infinite;
    private boolean waiting;
    private boolean connecting;
    private long deadline;

    public void disconnected(long now) {
        connecting = false;
        if (!waiting) {
            schedule(now);
        }
    }

    public boolean beginAttempt(boolean automatic) {
        if (connecting || automatic && !waiting) {
            return false;
        }
        connecting = true;
        waiting = false;
        if (automatic && !infinite) {
            attempts++;
        }
        return true;
    }

    public void toggleInfinite(long now) {
        infinite = !infinite;
        if (!connecting) {
            schedule(now);
        }
    }

    private void schedule(long now) {
        waiting = infinite || attempts < INTERVALS.length;
        if (waiting) {
            int seconds = infinite ? 3 : INTERVALS[attempts];
            deadline = now + seconds * 1_000_000_000L;
        }
    }

    public boolean isDue(long now) {
        return waiting && now - deadline >= 0;
    }

    public int remainingSeconds(long now) {
        return waiting ? (int) Math.max(0, (deadline - now + 999_999_999L) / 1_000_000_000L) : 0;
    }

    public boolean isWaiting() {
        return waiting;
    }

    public boolean isConnecting() {
        return connecting;
    }

    public boolean isInfinite() {
        return infinite;
    }
}
