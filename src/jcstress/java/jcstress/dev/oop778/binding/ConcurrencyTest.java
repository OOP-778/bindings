package jcstress.dev.oop778.binding;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;

import dev.oop778.bindings.Bindings;
import dev.oop778.bindings.type.Bindable;
import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.ZZ_Result;

@JCStressTest
@Outcome(id = {"true, false"}, expect = ACCEPTABLE, desc = "Yes")
@Outcome(id = {"false, true"}, expect = ACCEPTABLE, desc = "Yes")
public class ConcurrencyTest {

    @State
    public static class TestState {
        private final Bindable bindable1;
        private final Bindable bindable2;

        public TestState() {
            this.bindable1 = Bindable.create();
            this.bindable2 = Bindable.create();
            bindable2.bindTo(bindable1);
        }
    }

    @Actor
    public void actor1(TestState state, ZZ_Result result) {
        result.r1 = Bindings.getInstance().close(state.bindable1);
    }

    @Actor
    public void actor2(TestState state, ZZ_Result result) {
        state.bindable2.bindTo(state.bindable1);
        result.r2 = state.bindable1.isBinded(state.bindable2);
    }
}
