package test.oop778.binding.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.oop778.bindings.collection.BindableCollection;
import dev.oop778.bindings.collection.implementation.BindableCollectionImpl;
import dev.oop778.bindings.type.Bindable;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class BindableCollectionTest {

    @Test
    void testClosing() {
        final Bindable bindable = Bindable.create();

        final BindableCollection<Object> wrap = BindableCollection.wrap(new ArrayList<>());
        wrap.add(1).bindAllTo(bindable);

        bindable.close();
        assertTrue(wrap.isEmpty(), "BindableCollection should be empty after closing bindable");
    }

    @Test
    void testCleanupOnRemove() {
        final Bindable bindable = Bindable.create();

        final BindableCollection<Object> wrap = BindableCollection.wrap(new ArrayList<>());
        wrap.add(1).bindAllTo(bindable);

        wrap.remove(1);
        assertTrue(
            ((BindableCollectionImpl) wrap).getValueToBindable().isEmpty(),
            "BindableCollection should be empty after closing bindable"
        );
    }

    @Test
    void removeIf() {
        final Bindable bindable = Bindable.create();

        final BindableCollection<Object> wrap = BindableCollection.wrap(new ArrayList<>());
        wrap.addAll(1, 2, 3).bindAllTo(bindable);

        wrap.removeIf(o -> (int) o == 2);
        assertEquals(2, wrap.size(), "Collection did not change it's size after removal");
    }
}
