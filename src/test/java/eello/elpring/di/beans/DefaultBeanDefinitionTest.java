package eello.elpring.di.beans;

import eello.elpring.di.fixtures.factory.*;
import eello.elpring.di.fixtures.scanner.LazyComponent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultBeanDefinitionTest {

    @Test
    void testSimpleClassParsing() {
        BeanDefinition def = DefaultBeanDefinition.of(SimpleBean.class);

        assertEquals("SimpleBean", def.getBeanClassName());
        assertEquals(SimpleBean.class, def.getBeanType());
        assertEquals(SimpleBean.class.getName(), def.getBeanTypeName());
        assertEquals(0, def.getInterfaceTypes().length);
        assertEquals(0, def.getDependsOn().length);
        assertFalse(def.isPrimary());
        assertFalse(def.isLazyInit());
        assertEquals(BeanScope.SINGLETON, def.getScope());
    }

    @Test
    void testClassWithInterfaceParsing() {
        BeanDefinition def = DefaultBeanDefinition.of(ImplA1.class);

        assertEquals(1, def.getInterfaceTypes().length);
        assertEquals(InterfaceA.class, def.getInterfaceTypes()[0]);
    }

    @Test
    void testClassWithConstructorDependenciesParsing() {
        BeanDefinition def = DefaultBeanDefinition.of(DependencyBean.class);

        assertEquals(1, def.getDependsOn().length);
        assertEquals(SimpleBean.class, def.getDependsOn()[0].getType());
    }

    @Test
    void testPrimaryAnnotationParsing() {
        BeanDefinition def = DefaultBeanDefinition.of(ImplAPrimary.class);

        assertTrue(def.isPrimary());
    }

    @Test
    void testLazyAnnotationParsing() {
        BeanDefinition def = DefaultBeanDefinition.of(LazyComponent.class);

        assertTrue(def.isLazyInit());
    }
}
