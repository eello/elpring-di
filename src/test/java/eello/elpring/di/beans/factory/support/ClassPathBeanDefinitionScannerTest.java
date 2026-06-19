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

    @Test
    void testScanRegistersConfigurationAndBeans() throws ClassNotFoundException {
        int scannedCount = scanner.scan("eello.elpring.di.fixtures.config");

        // appConfig, dependencyBean, simpleBean, customName, dependentBean
        assertTrue(scannedCount >= 5);

        assertTrue(registry.containsBeanDefinition("appConfig"));
        assertTrue(registry.containsBeanDefinition("dependencyBean"));
        assertTrue(registry.containsBeanDefinition("simpleBean"));
        assertTrue(registry.containsBeanDefinition("customName"));
        assertTrue(registry.containsBeanDefinition("dependentBean"));

        // @Configuration Class 검증
        BeanDefinition configDef = registry.getBeanDefinition("appConfig");
        assertNotNull(configDef);
        assertTrue(configDef.isConfigurationClass());

        // @Bean Method 검증 - simpleBean
        BeanDefinition simpleBeanDef = registry.getBeanDefinition("simpleBean");
        assertNotNull(simpleBeanDef);
        assertTrue(simpleBeanDef.isFactoryBeanMethod());
        assertEquals("appConfig", simpleBeanDef.getFactoryBeanName());
        assertNotNull(simpleBeanDef.getInterfaceTypes());
        assertEquals(1, simpleBeanDef.getInterfaceTypes().length);
        assertEquals(java.io.Serializable.class, simpleBeanDef.getInterfaceTypes()[0]);

        // @Bean("customName") 검증
        BeanDefinition customNameDef = registry.getBeanDefinition("customName");
        assertNotNull(customNameDef);
        assertTrue(customNameDef.isFactoryBeanMethod());
        assertEquals("appConfig", customNameDef.getFactoryBeanName());
        assertNotNull(customNameDef.getInterfaceTypes());
        assertEquals(1, customNameDef.getInterfaceTypes().length);
        assertEquals(java.io.Serializable.class, customNameDef.getInterfaceTypes()[0]);
    }
}
