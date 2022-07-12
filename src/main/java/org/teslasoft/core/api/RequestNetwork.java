package org.teslasoft.core.api;

import org.teslasoft.core.api.logger.Log;
import org.teslasoft.experiments.ndev.Constants;
import org.teslasoft.experiments.ndev.ServerState;

import java.util.HashMap;

@SuppressWarnings("unused")
public class RequestNetwork {
    private HashMap<String, Object> params = new HashMap<>();
    private HashMap<String, Object> headers = new HashMap<>();

    private int requestType = 0;

    public ServerState serverState;

    public RequestNetwork() {
        Log log = new Log();
        log.i(Constants.DEFAULT_LOG_TAG, "Teslasoft Core API connected successfully");
        serverState = new ServerState();
    }

    public void setHeaders(HashMap<String, Object> headers) {
        this.headers = headers;
    }

    public void setParams(HashMap<String, Object> params, int requestType) {
        this.params = params;
        this.requestType = requestType;
    }

    public HashMap<String, Object> getParams() {
        return params;
    }

    public HashMap<String, Object> getHeaders() {
        return headers;
    }

    public int getRequestType() {
        return requestType;
    }

    public void startRequestNetwork(String method, String url, String tag, RequestListener requestListener) {
        org.teslasoft.core.api.RequestNetworkController.getInstance().execute(this, method, url, tag, requestListener);
    }

    public interface RequestListener {
        void onResponse(String tag, String response);
        void onErrorResponse(String tag, String message);
    }
}