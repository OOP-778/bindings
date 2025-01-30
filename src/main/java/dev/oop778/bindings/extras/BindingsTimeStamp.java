package dev.oop778.bindings.extras;

import dev.oop778.bindings.BindableNode;
import dev.oop778.bindings.Bindings;
import dev.oop778.bindings.listener.BindingsListener;
import dev.oop778.bindings.type.Bindable;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class BindingsTimeStamp implements BindingsListener {
    private static final AtomicBoolean ENABLED = new AtomicBoolean(false);

    public static void install() {
        if (!ENABLED.compareAndSet(false, true)) {
            return;
        }

        BindingsExtra.install();
        Bindings.getInstance().addListener(new BindingsTimeStamp());
    }

    @Override
    public void onNodeCreate(Bindable bindable, BindableNode node) {
        BindingsExtra.getExtras(bindable).put("timestamp", System.currentTimeMillis());
    }

    @Override
    public void onDump(BindableNode node, Map<String, Object> params) {
        final Map<String, Object> extras = BindingsExtra.getExtras(node.getBindable());
        final long createdAgo = (long) extras.getOrDefault("timestamp", 0L);
        params.put("Created Ago", createdAgo == 0 ? "-1" : (System.currentTimeMillis() - createdAgo) + "ms");
    }
}
