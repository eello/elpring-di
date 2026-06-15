package eello.elpring.di.beans.factory;

import eello.elpring.di.beans.DefaultBeanDefinition;
import eello.elpring.di.beans.factory.support.BeanDefinitionRegistry;
import eello.elpring.di.exception.BeanCurrentlyInCreationException;
import eello.elpring.di.exception.NoSuchBeanDefinitionException;
import eello.elpring.di.exception.NoUniqueBeanDefinitionException;
import eello.elpring.di.fixtures.factory.*;
import eello.elpring.di.fixtures.scanner.LazyComponent;
import java.util.Arrays;
import java.util.List;
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

    @Test
    void testApplicationContextAwareWithNullContext() {
        registry.registerBeanDefinition("awareBean", DefaultBeanDefinition.of(AwareBean.class));

        AwareBean awareBean = beanFactory.getBean("awareBean", AwareBean.class);
        assertNotNull(awareBean);
        assertNull(awareBean.getApplicationContext());
    }

    @Test
    void testGetBeanNamesAndBeansOfTypeExact() {
        registry.registerBeanDefinition("simpleBean1", DefaultBeanDefinition.of(SimpleBean.class));
        registry.registerBeanDefinition("simpleBean2", DefaultBeanDefinition.of(SimpleBean.class));

        String[] names = beanFactory.getBeanNamesForType(SimpleBean.class);
        assertEquals(2, names.length);
        List<String> nameList = Arrays.asList(names);
        assertTrue(nameList.contains("simpleBean1"));
        assertTrue(nameList.contains("simpleBean2"));

        java.util.Map<String, SimpleBean> beans = beanFactory.getBeansOfType(SimpleBean.class);
        assertEquals(2, beans.size());
        assertTrue(beans.containsKey("simpleBean1"));
        assertTrue(beans.containsKey("simpleBean2"));
        assertInstanceOf(SimpleBean.class, beans.get("simpleBean1"));
    }

    @Test
    void testGetBeanNamesAndBeansOfTypeHierarchy() {
        registry.registerBeanDefinition("implA1", DefaultBeanDefinition.of(ImplA1.class));
        registry.registerBeanDefinition("implA2", DefaultBeanDefinition.of(ImplA2.class));

        // InterfaceA 타입으로 조회
        String[] names = beanFactory.getBeanNamesForType(InterfaceA.class);
        assertEquals(2, names.length);
        List<String> nameList = Arrays.asList(names);
        assertTrue(nameList.contains("implA1"));
        assertTrue(nameList.contains("implA2"));

        java.util.Map<String, InterfaceA> beans = beanFactory.getBeansOfType(InterfaceA.class);
        assertEquals(2, beans.size());
        assertTrue(beans.containsKey("implA1"));
        assertTrue(beans.containsKey("implA2"));
    }

    @Test
    void testGetBeanNamesAndBeansOfTypeNonExistent() {
        String[] names = beanFactory.getBeanNamesForType(String.class);
        assertEquals(0, names.length);

        java.util.Map<String, String> beans = beanFactory.getBeansOfType(String.class);
        assertTrue(beans.isEmpty());
    }

    @Test
    void testGetBeanNamesAndBeansOfTypeLazyLifecycle() {
        LazyComponent.constructorCount = 0;
        eello.elpring.di.beans.BeanDefinition lazyBeanDef = DefaultBeanDefinition.of(LazyComponent.class);
        registry.registerBeanDefinition("lazyComponent", lazyBeanDef);

        // getBeanNamesForType은 빈을 생성하지 않고 이름만 반환해야 함
        String[] names = beanFactory.getBeanNamesForType(LazyComponent.class);
        assertEquals(1, names.length);
        assertEquals("lazyComponent", names[0]);
        assertEquals(0, LazyComponent.constructorCount); // 인스턴스 미생성 확인

        // getBeansOfType 호출 시에는 맵을 만들기 위해 빈이 생성되어야 함
        java.util.Map<String, LazyComponent> beans = beanFactory.getBeansOfType(LazyComponent.class);
        assertEquals(1, beans.size());
        assertTrue(beans.containsKey("lazyComponent"));
        assertEquals(1, LazyComponent.constructorCount); // 이 시점에 인스턴스가 생성됨
    }

    @Test
    void testConstructorInjectionWithList() {
        registry.registerBeanDefinition("implA1", DefaultBeanDefinition.of(ImplA1.class));
        registry.registerBeanDefinition("implA2", DefaultBeanDefinition.of(ImplA2.class));
        registry.registerBeanDefinition("listConstructorBean", DefaultBeanDefinition.of(ListConstructorBean.class));

        ListConstructorBean bean = beanFactory.getBean("listConstructorBean", ListConstructorBean.class);
        assertNotNull(bean);
        assertNotNull(bean.getList());
        assertEquals(2, bean.getList().size());
        
        List<Class<?>> classes = bean.getList().stream().map(Object::getClass).toList();
        assertTrue(classes.contains(ImplA1.class));
        assertTrue(classes.contains(ImplA2.class));
    }

    @Test
    void testConstructorInjectionWithSet() {
        registry.registerBeanDefinition("implA1", DefaultBeanDefinition.of(ImplA1.class));
        registry.registerBeanDefinition("implA2", DefaultBeanDefinition.of(ImplA2.class));
        registry.registerBeanDefinition("setConstructorBean", DefaultBeanDefinition.of(SetConstructorBean.class));

        SetConstructorBean bean = beanFactory.getBean("setConstructorBean", SetConstructorBean.class);
        assertNotNull(bean);
        assertNotNull(bean.getSet());
        assertEquals(2, bean.getSet().size());
        
        List<Class<?>> classes = bean.getSet().stream().map(Object::getClass).toList();
        assertTrue(classes.contains(ImplA1.class));
        assertTrue(classes.contains(ImplA2.class));
    }

    @Test
    void testConstructorInjectionWithArray() {
        registry.registerBeanDefinition("implA1", DefaultBeanDefinition.of(ImplA1.class));
        registry.registerBeanDefinition("implA2", DefaultBeanDefinition.of(ImplA2.class));
        registry.registerBeanDefinition("arrayConstructorBean", DefaultBeanDefinition.of(ArrayConstructorBean.class));

        ArrayConstructorBean bean = beanFactory.getBean("arrayConstructorBean", ArrayConstructorBean.class);
        assertNotNull(bean);
        assertNotNull(bean.getArray());
        assertEquals(2, bean.getArray().length);
    }

    @Test
    void testConstructorInjectionWithMap() {
        registry.registerBeanDefinition("implA1", DefaultBeanDefinition.of(ImplA1.class));
        registry.registerBeanDefinition("implA2", DefaultBeanDefinition.of(ImplA2.class));
        registry.registerBeanDefinition("mapConstructorBean", DefaultBeanDefinition.of(MapConstructorBean.class));

        MapConstructorBean bean = beanFactory.getBean("mapConstructorBean", MapConstructorBean.class);
        assertNotNull(bean);
        assertNotNull(bean.getMap());
        assertEquals(2, bean.getMap().size());
        assertTrue(bean.getMap().containsKey("implA1"));
        assertTrue(bean.getMap().containsKey("implA2"));
    }

    @Test
    void testConstructorInjectionWithEmptyCollections() {
        registry.registerBeanDefinition("listConstructorBean", DefaultBeanDefinition.of(ListConstructorBean.class));
        registry.registerBeanDefinition("setConstructorBean", DefaultBeanDefinition.of(SetConstructorBean.class));
        registry.registerBeanDefinition("arrayConstructorBean", DefaultBeanDefinition.of(ArrayConstructorBean.class));

        ListConstructorBean listBean = beanFactory.getBean("listConstructorBean", ListConstructorBean.class);
        assertNotNull(listBean);
        assertTrue(listBean.getList().isEmpty());

        SetConstructorBean setBean = beanFactory.getBean("setConstructorBean", SetConstructorBean.class);
        assertNotNull(setBean);
        assertTrue(setBean.getSet().isEmpty());

        ArrayConstructorBean arrayBean = beanFactory.getBean("arrayConstructorBean", ArrayConstructorBean.class);
        assertNotNull(arrayBean);
        assertEquals(0, arrayBean.getArray().length);
    }

    @Test
    void testConstructorInjectionWithInvalidMapKeyThrowsException() {
        registry.registerBeanDefinition("invalidMapKeyConstructorBean", DefaultBeanDefinition.of(InvalidMapKeyConstructorBean.class));

        assertThrows(IllegalArgumentException.class, () -> {
            beanFactory.getBean("invalidMapKeyConstructorBean");
        });
    }

    @Test
    void testConstructorInjectionWithNestedGenericThrowsException() {
        registry.registerBeanDefinition("nestedGenericConstructorBean", DefaultBeanDefinition.of(NestedGenericConstructorBean.class));

        assertThrows(IllegalStateException.class, () -> {
            beanFactory.getBean("nestedGenericConstructorBean");
        });
    }

    @Test
    void testConstructorInjectionWithRawList() {
        registry.registerBeanDefinition("implA1", DefaultBeanDefinition.of(ImplA1.class));
        registry.registerBeanDefinition("implA2", DefaultBeanDefinition.of(ImplA2.class));
        registry.registerBeanDefinition("rawListConstructorBean", DefaultBeanDefinition.of(RawListConstructorBean.class));

        RawListConstructorBean bean = beanFactory.getBean("rawListConstructorBean", RawListConstructorBean.class);
        assertNotNull(bean);
        assertNotNull(bean.getList());
        assertEquals(2, bean.getList().size());

        List<Class<?>> classes = ((List<?>) bean.getList()).stream().map(Object::getClass).toList();
        assertTrue(classes.contains(ImplA1.class));
        assertTrue(classes.contains(ImplA2.class));
    }

    @Test
    void testConstructorInjectionWithWildcardThrowsException() {
        registry.registerBeanDefinition("wildcardConstructorBean", DefaultBeanDefinition.of(WildcardConstructorBean.class));

        assertThrows(IllegalStateException.class, () -> {
            beanFactory.getBean("wildcardConstructorBean");
        });
    }
}
