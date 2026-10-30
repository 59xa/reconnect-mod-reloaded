import io.xa59.reconnect.utils.RetrySchedule;

class RetryScheduleTest {
    private static final long SECOND = 1_000_000_000L;

    public static void main(String[] args) {
        finiteAttemptsStop();
        manualReconnectDoesNotConsumeAutomaticAttempts();
        infiniteAttemptsContinue();
        togglingInfiniteResumesFiniteSchedule();
        screenInitialisationPreservesDeadline();
        countdownRoundsUp();
        connectingPreventsDuplicateAttempts();
        System.out.println("Reconnect schedule checks passed");
    }

    private static void finiteAttemptsStop() {
        RetrySchedule schedule = new RetrySchedule();
        long now = 100 * SECOND;
        for (int interval : new int[]{3, 10, 30, 60}) {
            schedule.disconnected(now);
            check(schedule.remainingSeconds(now) == interval, "Incorrect retry interval");
            check(!schedule.isDue(now + interval * SECOND - 1), "Retry started early");
            now += interval * SECOND;
            check(schedule.isDue(now), "Retry missed its deadline");
            check(schedule.beginAttempt(true), "Automatic retry did not start");
            now += 17 * SECOND;
        }
        schedule.disconnected(now);
        check(!schedule.isWaiting(), "More than four automatic retries were scheduled");
        check(!schedule.isDue(now + 3600 * SECOND), "Exhausted retries restarted");
        check(!schedule.beginAttempt(true), "Exhausted automatic retry was accepted");
        check(schedule.beginAttempt(false), "Manual reconnect was unavailable after exhaustion");
        schedule.disconnected(now + SECOND);
        check(!schedule.isWaiting(), "Manual reconnect restarted exhausted automatic retries");
        schedule.toggleInfinite(now);
        check(schedule.remainingSeconds(now) == 3, "Infinite mode did not restart exhausted retries");
        schedule.toggleInfinite(now);
        check(!schedule.isWaiting(), "Disabling infinite mode ignored exhausted retries");
    }

    private static void manualReconnectDoesNotConsumeAutomaticAttempts() {
        RetrySchedule schedule = new RetrySchedule();
        schedule.disconnected(0);
        check(schedule.beginAttempt(false), "Manual reconnect was rejected");
        schedule.disconnected(20 * SECOND);
        check(schedule.remainingSeconds(20 * SECOND) == 3, "Manual reconnect consumed an automatic attempt");
    }

    private static void infiniteAttemptsContinue() {
        RetrySchedule schedule = new RetrySchedule();
        schedule.disconnected(0);
        schedule.toggleInfinite(SECOND);
        long now = SECOND;
        for (int attempt = 0; attempt < 20; attempt++) {
            check(schedule.remainingSeconds(now) == 3, "Infinite retry interval changed");
            check(!schedule.isDue(now + 3 * SECOND - 1), "Infinite retry started early");
            now += 3 * SECOND;
            check(schedule.isDue(now), "Infinite retry stopped");
            check(schedule.beginAttempt(true), "Infinite retry was rejected");
            now += 5 * SECOND;
            schedule.disconnected(now);
        }
    }

    private static void togglingInfiniteResumesFiniteSchedule() {
        RetrySchedule schedule = new RetrySchedule();
        schedule.disconnected(0);
        check(schedule.beginAttempt(true), "First automatic retry did not start");
        schedule.disconnected(SECOND);
        schedule.toggleInfinite(2 * SECOND);
        check(schedule.remainingSeconds(2 * SECOND) == 3, "Infinite mode did not reset the countdown");
        check(schedule.beginAttempt(true), "Infinite retry did not start");
        schedule.disconnected(10 * SECOND);
        schedule.toggleInfinite(11 * SECOND);
        check(!schedule.isInfinite(), "Infinite mode remained enabled");
        check(schedule.remainingSeconds(11 * SECOND) == 10, "Finite retry progress was lost");
    }

    private static void screenInitialisationPreservesDeadline() {
        RetrySchedule schedule = new RetrySchedule();
        schedule.disconnected(0);
        schedule.disconnected(2 * SECOND);
        check(schedule.remainingSeconds(2 * SECOND) == 1, "Screen initialisation reset the countdown");
        check(schedule.isDue(3 * SECOND), "Screen initialisation postponed a retry");
    }

    private static void countdownRoundsUp() {
        RetrySchedule schedule = new RetrySchedule();
        schedule.disconnected(0);
        check(schedule.remainingSeconds(1) == 3, "Countdown rounded down");
        check(schedule.remainingSeconds(SECOND) == 2, "Countdown did not advance");
        check(schedule.remainingSeconds(3 * SECOND - 1) == 1, "Countdown reached zero early");
        check(schedule.remainingSeconds(3 * SECOND) == 0, "Countdown missed zero");
        check(schedule.remainingSeconds(300 * SECOND) == 0, "Countdown became negative");
    }

    private static void connectingPreventsDuplicateAttempts() {
        RetrySchedule schedule = new RetrySchedule();
        schedule.disconnected(0);
        check(schedule.beginAttempt(false), "Manual retry did not start");
        check(schedule.isConnecting(), "Connection state was lost");
        check(!schedule.beginAttempt(false), "Duplicate manual retry was accepted");
        check(!schedule.beginAttempt(true), "Overlapping automatic retry was accepted");
        check(!schedule.isDue(100 * SECOND), "Countdown ran during a connection attempt");
        schedule.disconnected(100 * SECOND);
        check(!schedule.isConnecting(), "Connection failure did not clear the connecting state");
        check(schedule.remainingSeconds(100 * SECOND) == 3, "Connection time consumed the next countdown");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
