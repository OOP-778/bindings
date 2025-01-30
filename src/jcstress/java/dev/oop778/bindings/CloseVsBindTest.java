package dev.oop778.bindings;

import dev.oop778.bindings.enums.BindingOrder;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.L_Result;

@JCStressTest
@Outcome(
        id = "CLOSED_OR_ABSENT",
        expect = Expect.ACCEPTABLE,
        desc = "Node ended up closed or absent. No rebind"
)
@Outcome(
        id = "BOUND_PRESENT",
        expect = Expect.ACCEPTABLE_INTERESTING,
        desc = "We see the node re-created"
)
@State
public class CloseVsBindTest {
    private final Bindings bindings = Bindings.getInstance();
    private final TestBindable A = new TestBindable();
    private final TestBindable B = new TestBindable();

    @Actor
    public void closer() {
        bindings.close(A);
    }

    @Actor
    public void binder() {
        bindings.bind(A, B, BindingOrder.FIRST);
    }

    @Arbiter
    public void arbiter(L_Result r) {
        if (bindings.isClosed(A)) {
            r.r1 = "CLOSED_OR_ABSENT";
            return;
        }

        int d = bindings.getDirection(A, B);
        r.r1 = (d == -1 ? "PRESENT_NO_BIND" : "BOUND_PRESENT");
    }
}