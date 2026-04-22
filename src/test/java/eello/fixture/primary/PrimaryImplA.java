package eello.fixture.primary;

import eello.container.annotation.Component;
import eello.container.annotation.Primary;

@Primary
@Component
public class PrimaryImplA implements PrimaryInterface {

    @Override
    public String name() {
        return "A";
    }
}
