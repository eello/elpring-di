package eello.elpring.di.fixtures.scanner;

import eello.elpring.di.annotation.Component;
import eello.elpring.di.annotation.Lazy;

@Component
@Lazy
public class LazyComponent {
    public static int constructorCount = 0;

    public LazyComponent() {
        constructorCount++;
    }
}
