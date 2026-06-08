package eello.elpring.di.context;

import eello.elpring.di.fixtures.scanner.EagerComponent;
import eello.elpring.di.fixtures.scanner.LazyComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractApplicationContextTest {

    protected abstract ConfigurableApplicationContext createApplicationContext(String... basePackages);

    @BeforeEach
    void resetFixtures() {
        EagerComponent.constructorCount = 0;
        LazyComponent.constructorCount = 0;
    }

    @Test
    void testContextInitializationAndEagerVsLazyLoading() {
        assertEquals(0, EagerComponent.constructorCount);
        assertEquals(0, LazyComponent.constructorCount);

        // Initialize context and scan package
        ConfigurableApplicationContext context = createApplicationContext("eello.elpring.di.fixtures.scanner");

        // Eager component should be created immediately during refresh()
        assertEquals(1, EagerComponent.constructorCount);
        // Lazy component should NOT be created during refresh()
        assertEquals(0, LazyComponent.constructorCount);

        // Explicitly get lazy bean
        LazyComponent lazyBean = context.getBean(LazyComponent.class);
        assertNotNull(lazyBean);
        // Lazy component should be created now
        assertEquals(1, LazyComponent.constructorCount);

        // Retrieve again
        LazyComponent lazyBean2 = context.getBean(LazyComponent.class);
        assertSame(lazyBean, lazyBean2);
        assertEquals(1, LazyComponent.constructorCount);
    }
}
