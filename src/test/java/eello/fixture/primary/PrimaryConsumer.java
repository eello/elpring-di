package eello.fixture.primary;

import eello.container.annotation.Component;

@Component
public class PrimaryConsumer {

    private final PrimaryInterface dependency;

    public PrimaryConsumer(PrimaryInterface dependency) {
        this.dependency = dependency;
    }

    public PrimaryInterface getDependency() {
        return dependency;
    }
}
