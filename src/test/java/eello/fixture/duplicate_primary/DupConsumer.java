package eello.fixture.duplicate_primary;

import eello.container.annotation.Component;

@Component
public class DupConsumer {

    private final DupInterface dependency;

    public DupConsumer(DupInterface dependency) {
        this.dependency = dependency;
    }
}
