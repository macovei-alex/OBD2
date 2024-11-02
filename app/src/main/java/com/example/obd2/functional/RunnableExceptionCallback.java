package com.example.obd2.functional;

@FunctionalInterface
public interface RunnableExceptionCallback {
    void call(Throwable e);
}
