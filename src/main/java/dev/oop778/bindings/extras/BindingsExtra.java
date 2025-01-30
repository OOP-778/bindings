package dev.oop778.bindings.extras;

import dev.oop778.bindings.BindableNode;
import dev.oop778.bindings.Bindings;
import dev.oop778.bindings.listener.BindingsListener;
import dev.oop778.bindings.type.Bindable;
import dev.oop778.bindings.util.map.IdentityMap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class BindingsExtra implements BindingsListener {
    private final IdentityMap<Bindable, Map<String, Object>> extras = new IdentityMap<>();
    private final AtomicBoolean enabled = new AtomicBoolean(false);
    private static final BindingsExtra INSTANCE = new BindingsExtra();

    public static void install() {
        INSTANCE.enable();
    }

    public static Map<String, Object> getExtras(Bindable bindable) {
        return INSTANCE.extras.get(bindable);
    }

    @Override
    public void onBindableClose(Bindable bindable, BindableNode node) {
        this.extras.remove(bindable);
    }

    @Override
    public void onNodeCreate(Bindable bindable, BindableNode node) {
        this.extras.put(bindable, new LinkedHashMap<>());
    }

    private void enable() {
        if (!this.enabled.compareAndSet(false, true)) {
            return;
        }

        Bindings.getInstance().addListener(this);
    }
}
