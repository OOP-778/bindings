package dev.oop778.bindings;

import dev.oop778.bindings.enums.BindingOrder;
import dev.oop778.bindings.type.Bindable;
import dev.oop778.bindings.type.BindableOnce;
import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.ZZ_Result;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;

@JCStressTest
@Outcome(id = {"true, false"}, expect = ACCEPTABLE, desc = "Yes")
@Outcome(id = {"false, true"}, expect = ACCEPTABLE, desc = "Yes")
public class ConcurrencyTest {

    @Actor
    public void actor1(TestState state, ZZ_Result result) {
        result.r1 = Bindings.getInstance().close(state.bindable1);
    }

    @Actor
    public void actor2(TestState state, ZZ_Result result) {
        result.r2 = Bindings.getInstance().bind(state.bindable2, state.bindable1, BindingOrder.NORMAL);
    }

    @State
    public static class TestState {
        private final BindableOnce bindable1;
        private final BindableOnce bindable2;

        public TestState() {
            this.bindable1 = new TestBindable();
            this.bindable2 = new TestBindable();
            this.bindable1.alive().bindTo(Bindable.create());
        }
    }
}
