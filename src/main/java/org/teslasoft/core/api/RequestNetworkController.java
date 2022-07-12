package org.teslasoft.core.api;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.teslasoft.core.api.logger.Log;
import org.teslasoft.experiments.ndev.KillServer;
import org.teslasoft.experiments.ndev.Main;
import org.teslasoft.experiments.ndev.ServerState;

import static org.teslasoft.experiments.ndev.Constants.DEFAULT_LOG_TAG;
import org.teslasoft.core.api.io.Colors;

@SuppressWarnings("unused")
public class RequestNetworkController {
    Log log;
    ServerState serverState;

    RequestNetworkController() {
        log = new Log();
        log.i(DEFAULT_LOG_TAG, "API Listener connected");
        serverState = Main.serverState;
    }
    public static final String GET      = "GET";
    public static final String POST     = "POST";
    public static final String PUT      = "PUT";
    public static final String DELETE   = "DELETE";

    public static final int REQUEST_PARAM = 0;
    public static final int REQUEST_BODY  = 1;

    private static final int SOCKET_TIMEOUT = 15000;
    private static final int READ_TIMEOUT   = 25000;

    protected OkHttpClient client;

    private static RequestNetworkController mInstance;

    public static synchronized RequestNetworkController getInstance() {
        if(mInstance == null) {
            mInstance = new RequestNetworkController();
        }
        return mInstance;
    }

    private OkHttpClient getClient() {
        if (client == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();

            try {
                final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
                };

                final SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
                builder.connectTimeout(SOCKET_TIMEOUT, TimeUnit.MILLISECONDS);
                builder.readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS);
                builder.writeTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS);
                builder.hostnameVerifier((hostname, session) -> true);
            } catch (Exception ignored) {}

            client = builder.build();
        }

        return client;
    }

    public void execute(final RequestNetwork requestNetwork, String method, String url, final String tag, final RequestNetwork.RequestListener requestListener) {
        Request.Builder reqBuilder = new Request.Builder();
        Headers.Builder headerBuilder = new Headers.Builder();

        if(requestNetwork.getHeaders().size() > 0) {
            HashMap<String, Object> headers = requestNetwork.getHeaders();

            for(HashMap.Entry<String, Object> header : headers.entrySet()) {
                headerBuilder.add(header.getKey(), String.valueOf(header.getValue()));
            }
        }

        try {
            if (requestNetwork.getRequestType() == REQUEST_PARAM) {
                if (method.equals(GET)) {
                    HttpUrl.Builder httpBuilder;

                    try {
                        httpBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
                    } catch (NullPointerException ne) {
                        throw new NullPointerException("unexpected url: " + url);
                    }

                    if (requestNetwork.getParams().size() > 0) {
                        HashMap<String, Object> params = requestNetwork.getParams();

                        for (HashMap.Entry<String, Object> param : params.entrySet()) {
                            httpBuilder.addQueryParameter(param.getKey(), String.valueOf(param.getValue()));
                        }
                    }

                    reqBuilder.url(httpBuilder.build()).headers(headerBuilder.build()).get();
                } else {
                    FormBody.Builder formBuilder = new FormBody.Builder();
                    if (requestNetwork.getParams().size() > 0) {
                        HashMap<String, Object> params = requestNetwork.getParams();

                        for (HashMap.Entry<String, Object> param : params.entrySet()) {
                            formBuilder.add(param.getKey(), String.valueOf(param.getValue()));
                        }
                    }

                    RequestBody reqBody = formBuilder.build();

                    reqBuilder.url(url).headers(headerBuilder.build()).method(method, reqBody);
                }
            } else {
                RequestBody reqBody = RequestBody.create(okhttp3.MediaType.parse("application/json"), new Gson().toJson(requestNetwork.getParams()));

                if (method.equals(GET)) {
                    reqBuilder.url(url).headers(headerBuilder.build()).get();
                } else {
                    reqBuilder.url(url).headers(headerBuilder.build()).method(method, reqBody);
                }
            }

            Request req = reqBuilder.build();

            getClient().newCall(req).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull final IOException e) {
                    log.e(DEFAULT_LOG_TAG, e.getMessage());
                    setResponse(e.getMessage(), 1);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                    if (serverState == null) {
                        throw new SecurityException("Access violation at org.teslasoft.core.api.logger.Log:  Methods of this class can not be accessed without class initialization.");
                    } else {
                        assert response.body() != null;
                        System.out.println("");
                        if (serverState.getState() == ServerState.State.DISCONNECTED) {
                            final String responseBody = Objects.requireNonNull(response.body()).string().trim();
                            log.v(DEFAULT_LOG_TAG, "Received auth key: ".concat(Colors.ANSI_PURPLE).concat(responseBody).concat(Colors.ANSI_RESET));
                            Main.setAuth_token(responseBody);
                            serverState.setState(ServerState.State.CONNECTED);
                        } else if (serverState.getState() == ServerState.State.CONNECTED || serverState.getState() == ServerState.State.AUTHENTICATION_FAILED) {
                            final String responseBody = Objects.requireNonNull(response.body()).string().trim();
                            log.v(DEFAULT_LOG_TAG, "ConsoleAuthenticator response: ".concat(Colors.ANSI_PURPLE).concat(responseBody).concat(Colors.ANSI_RESET));
                            if (responseBody.equals("{\"code\": 100, \"message\": \"Invalid session\"}")) {
                                log.e(DEFAULT_LOG_TAG, "Invalid session, exiting...");
                                setResponse("Invalid session, exiting...", 1);
                            } else if (responseBody.equals("{\"code\": 101, \"message\": \"Invalid password\"}")) {
                                System.out.println("Invalid password!");
                                serverState.setState(ServerState.State.AUTHENTICATION_FAILED);
                            } else {
                                serverState.setState(ServerState.State.AUTHENTICATED);
                                Main.setAuth_token(responseBody);
                                // setResponse(responseBody, 0);
                            }
                        }
                    }
                }
            });
        } catch (Exception e) {
            if (serverState == null) {
                throw new SecurityException("Access violation at org.teslasoft.core.api.logger.Log:  Methods of this class can not be accessed without class initialization.");
            } else {
                requestListener.onErrorResponse(tag, e.getMessage());
            }
        }
    }

    private void setResponse(String response, int exitCode) {
        /*Log log = new Log();
        if (exitCode == 0) {
            log.v(DEFAULT_LOG_TAG, response);
        } else {
            log.e(DEFAULT_LOG_TAG, response);
        }*/
        KillServer killServer = new KillServer(exitCode);
    }
}