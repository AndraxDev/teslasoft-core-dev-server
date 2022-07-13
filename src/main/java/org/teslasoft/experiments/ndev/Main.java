package org.teslasoft.experiments.ndev;

import org.teslasoft.core.api.logger.Log;
import org.teslasoft.core.api.RequestNetwork;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Console;
import org.teslasoft.core.api.io.Colors;

public class Main {

    public static RequestNetwork api_connector;
    public static RequestNetwork.RequestListener api_listener = new RequestNetwork.RequestListener() {
        @Override
        public void onErrorResponse(String tag, String response) {}

        @Override
        public void onResponse(String tag, String response) {}
    };

    public static Log log;
    public static ServerState serverState;

    public static String AUTH_TOKEN = "";
    public static String USERNAME = "";
    public static String HOSTNAME = "id.teslasoft.org";
    public static String PS = "$";
    public static String PATH = "~";


    public static void main(String[] args) throws IOException {
        SplashLogo splashLogo = new SplashLogo();
        splashLogo.print();
        serverState = new ServerState();
        log = new Log();
        log.i(Constants.DEFAULT_LOG_TAG, "Starting server...");
        log.i(Constants.DEFAULT_LOG_TAG, "Connecting to Teslasoft Core...");
        api_connector = new RequestNetwork();
        log.ah(Constants.DEFAULT_LOG_TAG, "==========[ AUTHENTICATION REQUIRED for org.teslasoft.core.api ]==========");
        auth();
    }

    public static void auth() throws IOException {
        serverState.setState(ServerState.State.DISCONNECTED);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(Colors.ANSI_CYAN.concat("Username: ").concat(Colors.ANSI_RESET));
        String username = reader.readLine();

        if (username == null) {
            System.out.println("Please enter username!");
            auth();
        } else {
            if (username.trim().equals("")) {
                System.out.println("Please enter username!");
                auth();
            } else {
                USERNAME = username;
                pass(username);
            }
        }
    }

    public static void pass(String username) throws IOException {
        Console console = System.console();
        System.out.print(Colors.ANSI_CYAN.concat("Password: ").concat(Colors.ANSI_RESET));

        StringBuilder password = new StringBuilder();

        char[] p = console.readPassword();

        for (char c : p) {
            password.append(c);
        }

        if (password.toString().trim().equals("")) {
            System.out.println("Please enter password!");
            pass(username);
        } else {
            log.i(Constants.DEFAULT_LOG_TAG, "Generating auth credentials...");
            api_connector.startRequestNetwork("GET", "https://id.teslasoft.org/protected/AuthTokenGenerator.php?username=".concat(username).concat("&password=").concat(password.toString()).concat("&xpr=7200"), "A", api_listener);

            System.out.print("Connecting");
            while (serverState.getState() == ServerState.State.DISCONNECTED) {
                System.out.print(".");
                try {
                    Thread.sleep(50);
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }

            System.out.println("");

            System.out.print("Authenticating");

            api_connector.startRequestNetwork("GET", "https://id.teslasoft.org/protected/ConsoleAuthenticator.php?auth_token=".concat(AUTH_TOKEN), "A", api_listener);

            while (serverState.getState() == ServerState.State.CONNECTED) {
                System.out.print(".");
                try {
                    Thread.sleep(50);
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }

            if (serverState.getState() == ServerState.State.AUTHENTICATION_FAILED) {
                auth();
            } else {
                if (serverState.getState() == ServerState.State.AUTHENTICATED) {
                    log.ah(Constants.DEFAULT_LOG_TAG, "==========[ AUTHENTICATION COMPLETED for org.teslasoft.core.api ]==========");
                    lifeCycle();
                } else {
                    throw new IllegalStateException("Illegal server state detected, exiting...");
                }
            }
        }
    }

    public static void lifeCycle() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("[CONSOLE] ".concat(Colors.ANSI_BLUE).concat(USERNAME).concat(Colors.ANSI_GREEN).concat("@").concat(HOSTNAME).concat(" ").concat(PATH).concat(" ").concat(Colors.ANSI_YELLOW).concat(PS).concat(" ").concat(Colors.ANSI_RESET));
        String command = reader.readLine();
        try {
            if (command.trim().equals("logout") || command.trim().equals("exit")) {
                System.out.println("logout");
                System.out.println("Stopping server...");
                System.exit(0);
            } if (command.trim().equals("clear")) {
                System.out.print("\033[H\033[2J");
                System.out.flush();
                lifeCycle();
            } if (command.trim().equals("")) {
                lifeCycle();
            } else {
                serverState.setState(ServerState.State.BUSY);
                System.out.print("Please wait");
                api_connector.startRequestNetwork("GET", "https://id.teslasoft.org/protected/Console.php?auth_token=".concat(AUTH_TOKEN).concat("&command=").concat(command), "A", api_listener);

                while (serverState.getState() == ServerState.State.BUSY) {
                    System.out.print(".");
                    try {
                        Thread.sleep(50);
                    } catch(InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
                System.out.println("");
                lifeCycle();
            }
        } catch (Exception e) {
            System.out.println("logout");
            System.out.println("Stopping server...");
            System.exit(0);
        }
    }

    public static void setAuth_token(String auth_token) {
        AUTH_TOKEN = auth_token;
    }
}