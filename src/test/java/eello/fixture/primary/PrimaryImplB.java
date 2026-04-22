package eello.fixture.primary;

import eello.container.annotation.Component;

@Component
public class PrimaryImplB implements PrimaryInterface {

    @Override
    public String name() {
        return "B";
    }
}
