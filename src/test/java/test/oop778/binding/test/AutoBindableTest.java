package test.oop778.binding.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.oop778.bindings.autobindable.AutoBindable;
import dev.oop778.bindings.type.Bindable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import lombok.Getter;
import org.junit.jupiter.api.Test;

class AutoBindableTest {

    @Test
    void testAutoBindingWithoutHierarchy() {
        final AutoBindableObject autoBindableObject = new AutoBindableObject(false);
        final int count = autoBindableObject.counter.get();

        assertEquals(1, count, "Expected 1, got " + count);
    }

    @Test
    void testAutoBindingWithHierarchy() {
        final AutoBindableObject autoBindableObject = new AutoBindableObject(true);
        final int count = autoBindableObject.counter.get();

        assertEquals(2, count, "Expected 2, got " + count);
    }

    @Test
    void testAutoBindingWithInterfaceHierarchy() {
        final AutoBindableObjectWithParentInterface autoBindableObject = new AutoBindableObjectWithParentInterface(true);
        final int count = autoBindableObject.counter.get();

        assertEquals(3, count, "Expected 3, got " + count);
    }

    @Test
    void testAutoBindingWithOverriddenHierarchy() {
        final AutoBindableObjectWithOverridden autoBindableObjectWithOverridden = new AutoBindableObjectWithOverridden(true);
        final int count = autoBindableObjectWithOverridden.counter.get();

        assertEquals(2, count, "Expected 2, got " + count);
    }

    private interface AutoBindableParentInterface {
        default Bindable interfaceTestBinding() {
            return this.increment(() -> Bindable.create(() -> System.out.println("interfaceTestClosed")));
        }

        Bindable increment(Supplier<Bindable> supplier);
    }

    private static class AutoBindableObject extends AutoBindableParent implements AutoBindable, Bindable {

        public AutoBindableObject(boolean withHierarchy) {
            this.autoBind(this, withHierarchy);
        }

        protected Bindable testBinding() {
            return this.increment(() -> Bindable.create(() -> System.out.println("testBindingClosed")));
        }
    }

    private static class AutoBindableObjectWithParentInterface extends AutoBindableObject implements AutoBindableParentInterface {

        public AutoBindableObjectWithParentInterface(boolean withHierarchy) {
            super(withHierarchy);
        }

        @Override
        public Bindable increment(Supplier<Bindable> supplier) {
            return super.increment(supplier);
        }
    }

    private static class AutoBindableObjectWithOverridden extends AutoBindableObject {

        public AutoBindableObjectWithOverridden(boolean withHierarchy) {
            super(withHierarchy);
        }

        @Override
        protected Bindable testBinding() {
            return super.testBinding();
        }
    }

    @Getter
    protected abstract static class AutoBindableParent {
        protected AtomicInteger counter = new AtomicInteger(0);

        private Bindable parentTestBinding() {
            return this.increment(() -> Bindable.create(() -> System.out.println("ParentTestClosed")));
        }

        protected Bindable increment(Supplier<Bindable> supplier) {
            this.counter.incrementAndGet();
            return supplier.get();
        }
    }
}
