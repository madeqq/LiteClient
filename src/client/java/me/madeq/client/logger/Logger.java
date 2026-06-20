package me.madeq.client.logger;

public class Logger {

    private Logger() {
    }

    public static void info(String message) {
        System.out.println("[INFO] " + message);
    }

    public static void error(String message) {
        System.err.println("[ERROR] " + message);
    }

    public static void info(String message, Object... args) {
        info(String.format(message, args));
    }

    public static void error(String message, Object... args) {
        error(String.format(message, args));
    }
}
