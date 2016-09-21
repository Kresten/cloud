package cloud.cave.service;

import cloud.cave.common.Inspector;

/**
 * Created by krest on 21-09-2016.
 */
public class WeatherCircuitBreaker implements CircuitBreaker {

    private CircuitBreakerState state;
    private int failureCount;
    private long timeout;
    private Inspector inspector;


    public WeatherCircuitBreaker() {
        state = CircuitBreakerState.CLOSED;
        failureCount = 0;
    }

    @Override
    public CircuitBreakerState getState() {
        return state;
    }

    @Override
    public void open() {
        state = CircuitBreakerState.OPEN;
    }

    @Override
    public void close() {
        state = CircuitBreakerState.CLOSED;
    }

    @Override
    public void halfOpen() {
        state = CircuitBreakerState.HALF_OPEN;
    }

    @Override
    public void incrementFailureCount() {
        failureCount++;
        if (failureCount >= 3 && state.equals(CircuitBreakerState.CLOSED)) {
            trip();
        }
    }

    @Override
    public void trip() {
        if (state.equals(CircuitBreakerState.CLOSED)) {
            inspector.write("weather-circuit-breaker", "Closed -> Open");
        } else if (state.equals(CircuitBreakerState.HALF_OPEN)) {
            inspector.write("weather-circuit-breaker", "HalfOpen -> Open");
        }
        open();
        startTimeout();
    }

    @Override
    public void reset() {
        failureCount = 0;
        inspector.write("weather-circuit-breaker", "Open -> Closed");
        close();
    }

    private void startTimeout() {
        timeout = System.currentTimeMillis();
    }

    private static final int TWENTY_SECONDS = 1000 * 20;

    @Override
    public boolean hasTimeOutPassed(long theirTime) {
        boolean hasTimeOutPassed = theirTime - timeout > TWENTY_SECONDS;
        if (hasTimeOutPassed) {
            inspector.write("weather-circuit-breaker", "Open -> HalfOpen");
            halfOpen();
        }
        return hasTimeOutPassed;
    }

    @Override
    public void setInspector(Inspector inspector) {
        this.inspector = inspector;
    }
}
