package dev.oop778.bindings;

public class BindingsOptions {
    public static long STACK_SIZE_LIMIT = Long.parseLong(System.getProperty("stackSizeLimit", "5"));
    public static boolean ENABLE_TRACING = Boolean.parseBoolean(System.getProperty("enableTracing", "false"));
    public static boolean TRACK_CLOSING = Boolean.parseBoolean(System.getProperty("trackClosing", "false"));
}
