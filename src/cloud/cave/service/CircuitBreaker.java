package cloud.cave.service;

import cloud.cave.common.Inspector;

/**
 * Created by krest on 21-09-2016.
 * Interface for the circuit breaker. A circuit breaker helps
 * the integration points in trouble from overloading by
 * limiting requests.
 */
public interface CircuitBreaker {

    /**
     * Get the current circuit breaker state
     * @return the state of the circuit breaker
     */
    CircuitBreakerState getState();

    /**
     * Change the state of the circuit breaker to open
     */
    void open();

    /**
     * Change the state of the circuit breaker to closed
     */
    void close();

    /**
     * Change the state of the circuit breaker to half-open
     */
    void halfOpen();

    /**
     * Increments the failure count.
     */
    void incrementFailureCount();

    /**
     * Trips the circuit breaker.
     */
    void trip();

    /**
     * Resets the circuit breaker to closed state.
     */
    void reset();

    boolean hasTimeOutPassed(long theirTime);

    void setInspector(Inspector inspector);
}

