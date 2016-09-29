package cloud.cave.service;

import cloud.cave.common.Inspector;
import cloud.cave.server.common.RealTimeStrategy;
import cloud.cave.server.common.TimeStrategy;

/**
 * Created by krest on 21-09-2016.
 */
public class WeatherCircuitBreaker implements CircuitBreaker {

    private CircuitBreakerState state;
    private int failureCount;
    private long timeout;
    private Inspector inspector;
    private static final int TWENTY_SECONDS = 1000 * 20;
    private double timeToWait;
    private TimeStrategy timeStrategy;

    public WeatherCircuitBreaker() {
        state = CircuitBreakerState.CLOSED;
        failureCount = 0;
        timeStrategy = new RealTimeStrategy();
        timeToWait = TWENTY_SECONDS;
    }

    public WeatherCircuitBreaker(TimeStrategy timeStrategy, double timeToWait) {
        state = CircuitBreakerState.CLOSED;
        failureCount = 0;
        this.timeStrategy = timeStrategy;
        this.timeStrategy = timeStrategy;
        this.timeToWait = timeToWait;
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
        if (failureCount >= 3) {
            trip();
        }
    }

    @Override
    public void trip() {
        if (state.equals(CircuitBreakerState.CLOSED)) {
            inspector.write(Inspector.WEATHER_CIRCUIT_BREAKER_TOPIC, "Closed -> Open");
        } else if (state.equals(CircuitBreakerState.HALF_OPEN)) {
            inspector.write(Inspector.WEATHER_CIRCUIT_BREAKER_TOPIC, "HalfOpen -> Open");
        }
        open();
        startTimeout();
    }

    @Override
    public void reset() {
        failureCount = 0;
        if (state.equals(CircuitBreakerState.OPEN)){
            inspector.write(Inspector.WEATHER_CIRCUIT_BREAKER_TOPIC, "Open -> Closed");
        }
        else if (state.equals(CircuitBreakerState.HALF_OPEN)){
            inspector.write(Inspector.WEATHER_CIRCUIT_BREAKER_TOPIC, "HalfOpen -> Closed");
        }
        close();
    }

    private void startTimeout() {
        timeout = timeStrategy.getTime();
    }

    @Override
    public boolean hasTimeOutPassed() {
        long theirTime = timeStrategy.getTime();
        boolean hasTimeOutPassed = theirTime - timeout > timeToWait;
        if (hasTimeOutPassed) {
            inspector.write(Inspector.WEATHER_CIRCUIT_BREAKER_TOPIC, "Open -> HalfOpen");
            halfOpen();
        }
        return hasTimeOutPassed;
    }

    @Override
    public void setInspector(Inspector inspector) {
        this.inspector = inspector;
    }
}
