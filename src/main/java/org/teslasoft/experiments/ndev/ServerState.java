package org.teslasoft.experiments.ndev;

import org.teslasoft.core.api.logger.Log;

import static org.teslasoft.experiments.ndev.Constants.DEFAULT_LOG_TAG;

public class ServerState {

    public enum State {
        DISCONNECTED,
        STARTED,
        AUTHENTICATED,
        CONNECTED,
        READY,
        AUTHENTICATION_FAILED,
        CONNECTION_FAILED,
        BUSY
    }

    private State serverState = State.DISCONNECTED;

    private final Log log;

    public ServerState() {
        log = new Log();
        log.i(DEFAULT_LOG_TAG, "StateListener connected");
    }

    public void setState(State state) {
        if (log == null) {
            throw new SecurityException("Access violation at org.teslasoft.core.api.logger.Log org.teslasoft.core.logger.Log: Methods of this class can not be accessed without class initialization.");
        } else {
            serverState = state;
            if (serverState != State.BUSY && serverState != State.READY) {
                log.i(DEFAULT_LOG_TAG, "Server state changed to ".concat(serverState.toString()));
            }
        }
    }

    public State getState() {
        if (log == null) {
            throw new SecurityException("Access violation at org.teslasoft.core.api.logger.Log org.teslasoft.core.logger.Log: Methods of this class can not be accessed without class initialization.");
        } else {
            return serverState;
        }
    }

    public void testConnection() throws SecurityException {
        if (log == null) {
            throw new SecurityException("Access violation at org.teslasoft.core.api.logger.Log org.teslasoft.core.logger.Log: Methods of this class can not be accessed without class initialization.");
        } else {
            log.i(DEFAULT_LOG_TAG, "Test connection successful");
        }
    }

    public interface ServerStateListener {
        void onServerStateChanged(ServerState serverState);
    }
}
