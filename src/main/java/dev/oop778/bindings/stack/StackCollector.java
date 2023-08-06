package dev.oop778.bindings.stack;

import dev.oop778.bindings.Bindings;
import dev.oop778.bindings.BindingsOptions;
import java.util.ArrayList;
import java.util.List;

public class StackCollector {

    public static List<String> collectStack() {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        final List<String> result = new ArrayList<>();

        for (int i = 2; i < stackTrace.length; i++) {
            final String stackTraceElementString = stackTrace[i].toString();
            if (stackTraceElementString.contains(Bindings.class.getPackage().getName())) {
                continue;
            }

            result.add(stackTraceElementString);
            if (result.size() == BindingsOptions.TRACING_STACK_SIZE_LIMIT) {
                break;
            }
        }

        return result;
    }
}
