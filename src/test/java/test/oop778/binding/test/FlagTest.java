package test.oop778.binding.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.oop778.bindings.enums.BindableFlag;
import dev.oop778.bindings.type.Bindable;
import org.junit.jupiter.api.Test;

class FlagTest {

    @Test
    void testWhenEmptyClose() {
        final Bindable parent = Bindable.create();
        final Bindable child = Bindable.create();
        final Bindable child2 = Bindable.create();

        child.bindTo(parent);
        child2.bindTo(parent);

        parent.flag(BindableFlag.CLOSE_WHEN_EMPTY);

        child.close();
        assertFalse(parent.isClosed(), "Parent must not be closed");

        child2.close();
        assertTrue(parent.isClosed(), "Parent must be closed");
    }

    @Test
    void testOnFirstClose() {
        final Bindable parent = Bindable.create();
        final Bindable child = Bindable.create();
        final Bindable child2 = Bindable.create();

        child.bindTo(parent);
        child2.bindTo(parent);

        parent.flag(BindableFlag.CLOSE_ON_UNBIND);

        child.close();
        assertTrue(parent.isClosed(), "Parent must be closed");
    }
}
