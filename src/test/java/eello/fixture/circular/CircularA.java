package eello.fixture.circular;

import eello.container.annotation.Component;

@Component
public class CircularA {

    private final CircularB circularB;

    public CircularA(CircularB circularB) {
        this.circularB = circularB;
    }
}
