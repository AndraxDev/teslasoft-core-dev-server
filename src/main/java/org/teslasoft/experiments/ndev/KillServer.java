package org.teslasoft.experiments.ndev;

import org.teslasoft.core.api.logger.Log;

public class KillServer {
    public KillServer(int code) {
        Log log = new Log();
        log.i(Constants.DEFAULT_LOG_TAG, "Stopping server...");
        System.exit(code);
    }
}
