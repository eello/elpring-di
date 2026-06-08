package eello.elpring.di.beans.factory.support;

import eello.elpring.di.beans.BeanDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClassPathBeanDefinitionScannerTest {

    private DefaultListableBeanFactory registry;
    private ClassPathBeanDefinitionScanner scanner;

    @BeforeEach
    void setUp() {
        this.registry = new DefaultListableBeanFactory();
        this.scanner = new ClassPathBeanDefinitionScanner(registry);
    }

    @Test
    void testScanRegistersCandidateComponents() throws ClassNotFoundException {
        int scannedCount = scanner.scan("eello.elpring.di.fixtures.scanner");

        // We should have scanned at least EagerComponent, LazyComponent, and CustomNamedComponent
        assertTrue(scannedCount >= 3);

        assertTrue(registry.containsBeanDefinition("eagerComponent"));
        assertTrue(registry.containsBeanDefinition("lazyComponent"));
        assertTrue(registry.containsBeanDefinition("customBeanName"));

        BeanDefinition eagerDef = registry.getBeanDefinition("eagerComponent");
        assertNotNull(eagerDef);
        assertEquals("EagerComponent", eagerDef.getBeanClassName());
        assertFalse(eagerDef.isLazyInit());

        BeanDefinition lazyDef = registry.getBeanDefinition("lazyComponent");
        assertNotNull(lazyDef);
        assertEquals("LazyComponent", lazyDef.getBeanClassName());
        assertTrue(lazyDef.isLazyInit());

        BeanDefinition customDef = registry.getBeanDefinition("customBeanName");
        assertNotNull(customDef);
        assertEquals("CustomNamedComponent", customDef.getBeanClassName());
    }
}
