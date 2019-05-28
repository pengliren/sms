package com.sms.server.util.timer;

public interface Timeout {

	/**
     * Returns the {@link Timer} that created this handle.
     */
    Timer getTimer();

    /**
     * Returns the {@link TimerTask} which is associated with this handle.
     */
    TimerTask getTask();

    /**
     * Returns {@code true} if and only if the {@link TimerTask} associated
     * with this handle has been expired.
     */
    boolean isExpired();

    /**
     * Returns {@code true} if and only if the {@link TimerTask} associated
     * with this handle has been cancelled.
     */
    boolean isCancelled();

    /**
     * Attempts to cancel the {@link TimerTask} associated with this handle.
     * If the task has been executed or cancelled already, it will return with
     * no side effect.
     *
     * @return True if the cancellation completed successfully, otherwise false
     */
    boolean cancel();
}
