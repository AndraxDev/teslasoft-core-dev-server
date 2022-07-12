package org.teslasoft.core.api.logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.teslasoft.core.api.io.Colors;

public class Log {
    public Log() {}

    public void i(String tag, String message) {
        LocalDateTime timestamp = LocalDateTime.now();
        System.out.println("[".concat(timestamp.format(DateTimeFormatter.ofPattern("yyyy-MMM-dd hh:mm:ss:SSS"))).concat("/INFO] ").concat(tag).concat(": ").concat(Colors.ANSI_WHITE).concat(message).concat(Colors.ANSI_RESET));
    }

    public void w(String tag, String message) {
        LocalDateTime timestamp = LocalDateTime.now();
        System.out.println("[".concat(timestamp.format(DateTimeFormatter.ofPattern("yyyy-MMM-dd hh:mm:ss:SSS"))).concat("/WARN] ").concat(tag).concat(": ").concat(Colors.ANSI_YELLOW).concat(message).concat(Colors.ANSI_RESET));
    }

    public void v(String tag, String message) {
        LocalDateTime timestamp = LocalDateTime.now();
        System.out.println("[".concat(timestamp.format(DateTimeFormatter.ofPattern("yyyy-MMM-dd hh:mm:ss:SSS"))).concat("/VERBOSE] ").concat(tag).concat(": ").concat(Colors.ANSI_PURPLE).concat(message).concat(Colors.ANSI_RESET));
    }

    public void e(String tag, String message) {
        LocalDateTime timestamp = LocalDateTime.now();
        System.out.println("[".concat(timestamp.format(DateTimeFormatter.ofPattern("yyyy-MMM-dd hh:mm:ss:SSS"))).concat("/ERROR] ").concat(tag).concat(": ").concat(Colors.ANSI_RED).concat(message).concat(Colors.ANSI_RESET));
    }

    public void ah(String tag, String message) {
        LocalDateTime timestamp = LocalDateTime.now();
        System.out.println("[".concat(timestamp.format(DateTimeFormatter.ofPattern("yyyy-MMM-dd hh:mm:ss:SSS"))).concat("/AUTH] ").concat(tag).concat(": ").concat(Colors.ANSI_RED).concat(message).concat(Colors.ANSI_RESET));
    }
}
