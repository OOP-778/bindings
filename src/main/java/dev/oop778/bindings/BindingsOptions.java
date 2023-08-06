package dev.oop778.bindings;

public class BindingsOptions {
    public static long TRACING_STACK_SIZE_LIMIT = Long.parseLong(System.getProperty("BindingsTracingStackSizeLimit", "5"));
    public static boolean ENABLE_TRACING = Boolean.parseBoolean(System.getProperty("BindingsTracing", "true"));
    public static boolean TRACING_TIME_STAMP = Boolean.parseBoolean(System.getProperty("BindingsTracingTimeStamp", "true"));
}
