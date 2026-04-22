package eello.fixture.ambiguous;

import eello.container.annotation.Component;

@Component
public class AmbiguousConsumer {

    private final AmbiguousInterface dependency;

    public AmbiguousConsumer(AmbiguousInterface dependency) {
        this.dependency = dependency;
    }
}
