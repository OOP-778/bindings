package dev.oop778.bindings.extras;

import dev.oop778.bindings.BindableNode;
import dev.oop778.bindings.Bindings;
import dev.oop778.bindings.listener.BindingsListener;
import dev.oop778.bindings.type.Bindable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class BindingsStackTracker implements BindingsListener {
    private final AtomicBoolean enabled = new AtomicBoolean(false);
    private volatile int depth;
    private static final BindingsStackTracker INSTANCE = new BindingsStackTracker(5);

    public BindingsStackTracker(int depth) {
        this.depth = depth;
    }

    public static void install(int stackDepth) {
        INSTANCE.enable(stackDepth);
    }

    @Override
    public void onNodeCreate(Bindable bindable, BindableNode node) {
        final Map<String, Object> extras = BindingsExtra.getExtras(bindable);
        if (extras == null) {
            return;
        }

        final List<StackTraceElement> stack = this.collectStack();
        extras.put("stack", stack.toArray(new StackTraceElement[0]));
    }

    @Override
    public void onDump(BindableNode node, Map<String, Object> params) {
        final StackTraceElement[] stackTraceElements = (StackTraceElement[]) BindingsExtra.getExtras(node.getBindable()).get("stack");
        if (stackTraceElements == null) {
            return;
        }

        params.put("Stack", "\n" + Arrays.stream(stackTraceElements)
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n")));
    }

    public void enable(int stackDepth) {
        INSTANCE.depth = stackDepth;

        if (!this.enabled.compareAndSet(false, true)) {
            return;
        }

        BindingsExtra.install();
        Bindings.getInstance().addListener(new BindingsStackTracker(stackDepth));
    }

    public List<StackTraceElement> collectStack() {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        final List<StackTraceElement> result = new ArrayList<>();

        for (int i = 6; i < stackTrace.length; i++) {
            final StackTraceElement stackTraceElement = stackTrace[i];
            if (stackTraceElement.getClassName().contains(Bindings.class.getPackage().getName())) {
                continue;
            }

            result.add(stackTraceElement);
            if (result.size() == this.depth) {
                break;
            }
        }

        return result;
    }
}
