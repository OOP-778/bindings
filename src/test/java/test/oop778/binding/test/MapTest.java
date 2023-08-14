package test.oop778.binding.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.oop778.bindings.collection.BindableMap;
import dev.oop778.bindings.collection.implementation.map.ValueBindableMap;
import dev.oop778.bindings.type.Bindable;
import java.util.HashMap;
import org.junit.jupiter.api.Test;

class MapTest {

    @Test
    void test() {
        final ValueBindableMap<String, Object> valueBindableMap = BindableMap.wrapValueBindable(new HashMap<>());
        final Bindable bindable = Bindable.create();
        valueBindableMap.put("key", 1).bindAllTo(bindable);

        bindable.close();
        assertEquals(0, valueBindableMap.size(), "BindableMap should be empty after closing bindable");
    }
}
