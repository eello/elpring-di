package eello.fixture.circular;

import eello.container.annotation.Component;

@Component
public class CircularB {

    private final CircularA circularA;

    public CircularB(CircularA circularA) {
        this.circularA = circularA;
    }
}
