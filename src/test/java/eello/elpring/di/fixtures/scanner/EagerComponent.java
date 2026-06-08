package eello.elpring.di.fixtures.scanner;

import eello.elpring.di.annotation.Component;

@Component
public class EagerComponent {
    public static int constructorCount = 0;

    public EagerComponent() {
        constructorCount++;
    }
}
