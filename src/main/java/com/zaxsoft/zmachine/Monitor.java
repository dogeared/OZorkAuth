package com.zaxsoft.zmachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Monitor {

    private static final Logger log = LoggerFactory.getLogger(Monitor.class);

    private final Object myMonitorObject = new Object();
    private boolean wasSignalled = false;

    public void doWait() {
        synchronized (myMonitorObject) {
            while (!wasSignalled) {
                try {
                    myMonitorObject.wait();
                } catch(InterruptedException e){
                    log.error("Interrupted while waiting", e);
                }
            }
            //clear signal and continue running.
            wasSignalled = false;
        }
    }

    public void doNotify() {
        synchronized (myMonitorObject) {
            wasSignalled = true;
            myMonitorObject.notify();
        }
    }
}