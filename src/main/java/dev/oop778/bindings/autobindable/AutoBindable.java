package dev.oop778.bindings.autobindable;

import dev.oop778.bindings.type.Bindable;
import lombok.NonNull;
import lombok.SneakyThrows;

public interface AutoBindable {

    default void autoBind() {
        this.autoBind(false);
    }

    default void autoBind(boolean withHierarchy) {
        if (!(this instanceof Bindable)) {
            throw new IllegalStateException("AutoBindable must be also Bindable");
        }

        this.autoBind((Bindable) this, withHierarchy);
    }

    @SneakyThrows
    default void autoBind(@NonNull Bindable to, boolean withHierarchy) {
        final Class<? extends AutoBindable> currentClass = this.getClass();

        AutoBindableHelper
            .collectMethods(currentClass, withHierarchy)
            .forEach((method) -> {
                try {
                    ((Bindable) method.invoke(this)).bindTo(to);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });
    }

    default void autoBind(@NonNull Bindable to) {
        this.autoBind(to, false);
    }
}
