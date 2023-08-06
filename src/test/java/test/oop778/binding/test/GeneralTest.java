package test.oop778.binding.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.oop778.bindings.Bindings;
import dev.oop778.bindings.type.Bindable;
import org.junit.jupiter.api.Test;

class GeneralTest {

    @Test
    void testChildrenClosingRemovedFromParent() {
        final Bindable parent = Bindable.create();
        final Bindable child = Bindable.create();

        child.bindTo(parent);

        child.close();
        assertFalse(parent.isBinded(child), "Child is still binded to parent");
    }

    @Test
    void testParentCloseRemovedFromMemory() {
        final Bindable parent = Bindable.create();
        final Bindable child = Bindable.create();

        child.bindTo(parent);

        parent.close();
        assertFalse(Bindings.getInstance().contains(parent), "Parent is still binded to child");
    }

    @Test
    void testMultipleTimesBind() {
        final Bindable parent = Bindable.create();
        final Bindable child = Bindable.create();

        child.bindTo(parent);
        child.bindTo(parent);
        child.bindTo(parent);
    }

    @Test
    void testCircularBindings() {
        final Bindable parent = Bindable.create();
        final Bindable child = Bindable.create();

        child.bindTo(parent);

        Throwable throwable = null;
        try {
            parent.bindTo(child);
        } catch (Throwable error) {
            throwable = error;
        }

        assertNotNull(throwable, "Circular binding is not detected");
    }
}
