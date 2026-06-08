package eello.elpring.di.boot;

import eello.elpring.di.context.ConfigurableApplicationContext;
import eello.elpring.di.fixtures.scanner.EagerComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ElpringApplicationTest {

    @BeforeEach
    void resetFixtures() {
        EagerComponent.constructorCount = 0;
    }

    @Test
    void testBootstrapWithPackages() {
        assertEquals(0, EagerComponent.constructorCount);

        ConfigurableApplicationContext ctx = ElpringApplication.run("eello.elpring.di.fixtures.scanner");
        assertNotNull(ctx);

        // Verify that it scanned and refreshed (instantiated eager component)
        assertEquals(1, EagerComponent.constructorCount);
        assertNotNull(ctx.getBean(EagerComponent.class));
    }

    @Test
    void testBootstrapWithPrimarySourceClass() {
        assertEquals(0, EagerComponent.constructorCount);

        // Should use the package name of EagerComponent ("eello.elpring.di.context.testfixtures")
        ConfigurableApplicationContext ctx = ElpringApplication.run(EagerComponent.class);
        assertNotNull(ctx);

        // Verify that it scanned and refreshed
        assertEquals(1, EagerComponent.constructorCount);
        assertNotNull(ctx.getBean(EagerComponent.class));
    }
}
