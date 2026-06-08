package eello.elpring.di.beans.factory;

import eello.elpring.di.beans.DefaultBeanDefinition;
import eello.elpring.di.beans.factory.support.BeanDefinitionRegistry;
import eello.elpring.di.exception.BeanCurrentlyInCreationException;
import eello.elpring.di.exception.NoSuchBeanDefinitionException;
import eello.elpring.di.exception.NoUniqueBeanDefinitionException;
import eello.elpring.di.fixtures.factory.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractBeanFactoryTest {

    protected ListableBeanFactory beanFactory;
    protected BeanDefinitionRegistry registry;

    protected abstract void initBeanFactoryAndRegistry();

    @BeforeEach
    void setUp() {
        initBeanFactoryAndRegistry();
    }

    // --- Tests ---

    @Test
    void testGetBeanByName() {
        registry.registerBeanDefinition("simpleBean", DefaultBeanDefinition.of(SimpleBean.class));

        Object bean = beanFactory.getBean("simpleBean");
        assertNotNull(bean);
        assertInstanceOf(SimpleBean.class, bean);

        // Singleton check
        assertSame(bean, beanFactory.getBean("simpleBean"));
    }

    @Test
    void testGetBeanByNameAndType() {
        registry.registerBeanDefinition("simpleBean", DefaultBeanDefinition.of(SimpleBean.class));

        SimpleBean bean = beanFactory.getBean("simpleBean", SimpleBean.class);
        assertNotNull(bean);
    }

    @Test
    void testGetBeanByType() {
        registry.registerBeanDefinition("simpleBean", DefaultBeanDefinition.of(SimpleBean.class));

        SimpleBean bean = beanFactory.getBean(SimpleBean.class);
        assertNotNull(bean);
    }

    @Test
    void testGetNonExistentBeanThrowsException() {
        assertThrows(NoSuchBeanDefinitionException.class, () -> {
            beanFactory.getBean("nonExistent");
        });

        assertThrows(NoSuchBeanDefinitionException.class, () -> {
            beanFactory.getBean(SimpleBean.class);
        });
    }

    @Test
    void testConstructorDependencyInjection() {
        registry.registerBeanDefinition("simpleBean", DefaultBeanDefinition.of(SimpleBean.class));
        registry.registerBeanDefinition("dependencyBean", DefaultBeanDefinition.of(DependencyBean.class));

        DependencyBean dependencyBean = beanFactory.getBean(DependencyBean.class);
        assertNotNull(dependencyBean);
        assertNotNull(dependencyBean.getSimpleBean());
        assertSame(beanFactory.getBean(SimpleBean.class), dependencyBean.getSimpleBean());
    }

    @Test
    void testCircularDependencyThrowsException() {
        registry.registerBeanDefinition("circularA", DefaultBeanDefinition.of(CircularA.class));
        registry.registerBeanDefinition("circularB", DefaultBeanDefinition.of(CircularB.class));

        assertThrows(BeanCurrentlyInCreationException.class, () -> {
            beanFactory.getBean(CircularA.class);
        });
    }

    @Test
    void testGetBeanByInterfaceSingleImplementation() {
        registry.registerBeanDefinition("implA1", DefaultBeanDefinition.of(ImplA1.class));

        InterfaceA bean = beanFactory.getBean(InterfaceA.class);
        assertNotNull(bean);
        assertInstanceOf(ImplA1.class, bean);
    }

    @Test
    void testGetBeanByInterfaceMultipleImplementationsWithoutPrimaryThrowsException() {
        registry.registerBeanDefinition("implA1", DefaultBeanDefinition.of(ImplA1.class));
        registry.registerBeanDefinition("implA2", DefaultBeanDefinition.of(ImplA2.class));

        assertThrows(NoUniqueBeanDefinitionException.class, () -> {
            beanFactory.getBean(InterfaceA.class);
        });
    }

    @Test
    void testGetBeanByInterfaceMultipleImplementationsWithSinglePrimary() {
        registry.registerBeanDefinition("implA1", DefaultBeanDefinition.of(ImplA1.class));
        registry.registerBeanDefinition("implA2", DefaultBeanDefinition.of(ImplA2.class));
        registry.registerBeanDefinition("implAPrimary", DefaultBeanDefinition.of(ImplAPrimary.class));

        InterfaceA bean = beanFactory.getBean(InterfaceA.class);
        assertNotNull(bean);
        assertInstanceOf(ImplAPrimary.class, bean);
    }

    @Test
    void testGetBeanByInterfaceMultipleImplementationsWithMultiplePrimaryThrowsException() {
        registry.registerBeanDefinition("implAPrimary", DefaultBeanDefinition.of(ImplAPrimary.class));
        registry.registerBeanDefinition("implAPrimary2", DefaultBeanDefinition.of(ImplAPrimary2.class));

        assertThrows(NoUniqueBeanDefinitionException.class, () -> {
            beanFactory.getBean(InterfaceA.class);
        });
    }

    @Test
    void testGetBeansWithAnnotationNormal() {
        registry.registerBeanDefinition("annotatedBean", DefaultBeanDefinition.of(AnnotatedBean.class));
        registry.registerBeanDefinition("simpleBean", DefaultBeanDefinition.of(SimpleBean.class));

        java.util.Map<String, Object> beans = beanFactory.getBeansWithAnnotation(CustomAnnotation.class);
        assertEquals(1, beans.size());
        assertTrue(beans.containsKey("annotatedBean"));
        assertInstanceOf(AnnotatedBean.class, beans.get("annotatedBean"));
    }

    @Test
    void testGetBeansWithAnnotationMeta() {
        registry.registerBeanDefinition("metaAnnotatedBean", DefaultBeanDefinition.of(MetaAnnotatedBean.class));

        java.util.Map<String, Object> beans = beanFactory.getBeansWithAnnotation(CustomAnnotation.class);
        assertEquals(1, beans.size());
        assertTrue(beans.containsKey("metaAnnotatedBean"));
        assertInstanceOf(MetaAnnotatedBean.class, beans.get("metaAnnotatedBean"));
    }

    @Test
    void testGetBeansWithAnnotationSystemFiltering() {
        registry.registerBeanDefinition("unrelatedAnnotatedBean", DefaultBeanDefinition.of(UnrelatedAnnotatedBean.class));

        java.util.Map<String, Object> beans = beanFactory.getBeansWithAnnotation(MetaOfUnrelated.class);
        // MetaOfUnrelated is a custom meta-annotation, so it should be collected!
        assertEquals(1, beans.size());
        assertTrue(beans.containsKey("unrelatedAnnotatedBean"));

        // java.lang.annotation.Target should be filtered out!
        java.util.Map<String, Object> targetBeans = beanFactory.getBeansWithAnnotation(java.lang.annotation.Target.class);
        assertEquals(0, targetBeans.size());
    }

    @Test
    void testGetBeansWithAnnotationNonExistent() {
        registry.registerBeanDefinition("simpleBean", DefaultBeanDefinition.of(SimpleBean.class));

        java.util.Map<String, Object> beans = beanFactory.getBeansWithAnnotation(org.junit.jupiter.api.Test.class);
        assertTrue(beans.isEmpty());
    }
}
