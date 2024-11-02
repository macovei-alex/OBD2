package com.example.obd2.model;

import android.util.Log;

import com.example.obd2.Utils;
import com.example.obd2.exceptions.OBDSessionException;
import com.example.obd2.functional.RunnableExceptionCallback;
import com.example.obd2.view.OBDCommandView;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unused")
public class OBDCommunicationRunnable implements Runnable {

    private static final String TAG = OBDCommunicationRunnable.class.getSimpleName();
    private static final int PAUSED_CHECK_SLEEP_INTERVAL = 500;

    private final OBDSession session;
    private final Map<OBDCommand, OBDCommandView> commandToViewMap;
    private final OBDParser parser;
    private final int sleepTimeAfterCommands;

    private final AtomicReference<RunnableState> state = new AtomicReference<>(RunnableState.PAUSED);
    private final Object lock = new Object();

    private final RunnableExceptionCallback exceptionHandler;


    public OBDCommunicationRunnable(OBDSession session, Map<OBDCommand, OBDCommandView> commandToViewMap,
                                    OBDParser parser, RunnableExceptionCallback handler, int sleepTimeAfterCommands) {
        this.session = session;
        this.commandToViewMap = commandToViewMap;
        this.parser = parser;
        this.exceptionHandler = handler;
        this.sleepTimeAfterCommands = sleepTimeAfterCommands;
    }


    public RunnableState getState() {
        return state.get();
    }


    public synchronized void pause() {
        if (state.get() != RunnableState.DEAD) {
            state.set(RunnableState.PAUSED);
        } else {
            Log.e(TAG, "Thread is dead");
        }
    }


    public synchronized void resume() {
        if (state.get() != RunnableState.DEAD) {
            this.state.set(RunnableState.RUNNING);
        } else {
            Log.e(TAG, "Thread is dead");
        }
    }


    public synchronized void stop() {
        state.set(RunnableState.DEAD);

        Log.d(TAG, "Stopping thread");
        synchronized (lock) {
            Log.d(TAG, "Thread stopped");
        }
    }


    @Override
    public void run() {
        state.set(RunnableState.RUNNING);

        if (!session.isOpen() && !session.tryOpen()) {
            Log.e(TAG, "Failed to open session");
            state.set(RunnableState.DEAD);
            return;
        }

        synchronized (lock) {

            while (state.get() != RunnableState.DEAD) {
                while (state.get() == RunnableState.RUNNING) {

                    try {

                        commandToViewMap.forEach((command, view) -> {
                            String response;

                            session.write(command.getCommandString());
                            response = session.read();

                            OBDData data = parser.parseMessage(response);
                            Log.d(TAG, data.toString());
                            view.setValue(command.applyFormula(data));
                        });

                    } catch (OBDSessionException e) {
                        stop();
                        Log.e(TAG, "Failed to write command. Thread stopped", e);
                        exceptionHandler.call(e);
                    }

                    if (sleepTimeAfterCommands > 0) {
                        Utils.sleep(sleepTimeAfterCommands);
                    }
                }

                while (state.get() == RunnableState.PAUSED) {
                    Utils.sleep(PAUSED_CHECK_SLEEP_INTERVAL);
                }
            }
        }
    }
}
