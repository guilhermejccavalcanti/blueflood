package com.rackspacecloud.blueflood.service;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

// Context of execution for a single shard, representing many rollups of a given granularity.
public class RollupExecutionContext {
    private final AtomicLong readCounter;
    private final Thread owner;
    private final AtomicLong writeCounter;
    private final AtomicBoolean successful = new AtomicBoolean(true);

    public RollupExecutionContext(Thread owner) {
        this.owner = owner;
        this.readCounter = new AtomicLong(0L);
        this.writeCounter = new AtomicLong(0L);
    }

    public String toString() {
        return "Pending reads: " + readCounter.get() + " writes: " + writeCounter.get() + " -- Success: " + successful.get();
    }

    void decrementReadCounter() {
        readCounter.decrementAndGet();
        owner.interrupt(); // this is what interrupts that long sleep in LocatorFetchRunnable.
    }

    public void decrementReadCounter(long count) {
        readCounter.addAndGet(-count);
        owner.interrupt();
    }

    void decrementWriteCounter(long count) {
        writeCounter.addAndGet(-count);
        owner.interrupt();
    }

    void incrementReadCounter() {
        readCounter.incrementAndGet();
    }

    void incrementWriteCounter() {
        writeCounter.incrementAndGet();
    }

    boolean doneReading() {
        return readCounter.get() == 0;
    }

    boolean doneWriting() {
        return writeCounter.get() == 0;
    }

    boolean done() {
        return doneReading() && doneWriting();
    }

    boolean wasSuccessful() {
        return successful.get();
    }

    void markUnsuccessful(Throwable t) {
        successful.set(false);
    }
}
