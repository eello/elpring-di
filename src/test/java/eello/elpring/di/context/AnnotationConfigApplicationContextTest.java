package eello.elpring.di.context;

public class AnnotationConfigApplicationContextTest extends AbstractApplicationContextTest {

    @Override
    protected ConfigurableApplicationContext createApplicationContext(String... basePackages) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(basePackages);
        context.refresh();
        return context;
    }

    @org.junit.jupiter.api.Test
    void testConfigurationAndBeansInstantiation() {
        ConfigurableApplicationContext context = createApplicationContext("eello.elpring.di.fixtures.config");

        // 1. @Configuration 빈이 로드되었는지 확인
        eello.elpring.di.fixtures.config.AppConfig appConfig = context.getBean(eello.elpring.di.fixtures.config.AppConfig.class);
        org.junit.jupiter.api.Assertions.assertNotNull(appConfig);

        // 2. @Bean 빈들이 로드되었는지 확인 (기본 이름)
        eello.elpring.di.fixtures.config.TestBean simpleBean = (eello.elpring.di.fixtures.config.TestBean) context.getBean("simpleBean");
        org.junit.jupiter.api.Assertions.assertNotNull(simpleBean);

        // 3. @Bean 빈들이 로드되었는지 확인 (커스텀 이름)
        eello.elpring.di.fixtures.config.TestBean customName = (eello.elpring.di.fixtures.config.TestBean) context.getBean("customName");
        org.junit.jupiter.api.Assertions.assertNotNull(customName);

        // 4. 싱글톤 관리 확인
        eello.elpring.di.fixtures.config.TestBean simpleBean2 = (eello.elpring.di.fixtures.config.TestBean) context.getBean("simpleBean");
        org.junit.jupiter.api.Assertions.assertSame(simpleBean, simpleBean2);

        // 5. @Bean 파라미터를 통한 의존성 주입 테스트
        eello.elpring.di.fixtures.config.DependencyBean dependencyBean = context.getBean(eello.elpring.di.fixtures.config.DependencyBean.class);
        org.junit.jupiter.api.Assertions.assertNotNull(dependencyBean);

        eello.elpring.di.fixtures.config.TestBean dependentBean = (eello.elpring.di.fixtures.config.TestBean) context.getBean("dependentBean");
        org.junit.jupiter.api.Assertions.assertNotNull(dependentBean);
        org.junit.jupiter.api.Assertions.assertNotNull(dependentBean.getDependencyBean(), "dependencyBean should be injected into dependentBean");
        org.junit.jupiter.api.Assertions.assertSame(dependencyBean, dependentBean.getDependencyBean(), "Injected dependencyBean should be the same as the bean in context");

        // 6. 다중 파라미터 주입 테스트
        eello.elpring.di.fixtures.config.AnotherDependencyBean anotherDependencyBean = context.getBean(eello.elpring.di.fixtures.config.AnotherDependencyBean.class);
        org.junit.jupiter.api.Assertions.assertNotNull(anotherDependencyBean);

        eello.elpring.di.fixtures.config.TestBean multiDependentBean = (eello.elpring.di.fixtures.config.TestBean) context.getBean("multiDependentBean");
        org.junit.jupiter.api.Assertions.assertNotNull(multiDependentBean);
        org.junit.jupiter.api.Assertions.assertSame(dependencyBean, multiDependentBean.getDependencyBean());
        org.junit.jupiter.api.Assertions.assertSame(anotherDependencyBean, multiDependentBean.getAnotherDependencyBean());

        // 7. Array, List, Set 컬렉션 타입 주입 테스트
        eello.elpring.di.fixtures.config.TestBean collectionDependentBean = (eello.elpring.di.fixtures.config.TestBean) context.getBean("collectionDependentBean");
        org.junit.jupiter.api.Assertions.assertNotNull(collectionDependentBean);

        org.junit.jupiter.api.Assertions.assertNotNull(collectionDependentBean.getDependencyBeans());
        org.junit.jupiter.api.Assertions.assertEquals(1, collectionDependentBean.getDependencyBeans().size());
        org.junit.jupiter.api.Assertions.assertSame(dependencyBean, collectionDependentBean.getDependencyBeans().get(0));

        org.junit.jupiter.api.Assertions.assertNotNull(collectionDependentBean.getDependencyBeanArray());
        org.junit.jupiter.api.Assertions.assertEquals(1, collectionDependentBean.getDependencyBeanArray().length);
        org.junit.jupiter.api.Assertions.assertSame(dependencyBean, collectionDependentBean.getDependencyBeanArray()[0]);

        org.junit.jupiter.api.Assertions.assertNotNull(collectionDependentBean.getDependencyBeanSet());
        org.junit.jupiter.api.Assertions.assertEquals(1, collectionDependentBean.getDependencyBeanSet().size());
        org.junit.jupiter.api.Assertions.assertTrue(collectionDependentBean.getDependencyBeanSet().contains(dependencyBean));

        // 8. 인터페이스 기반 조회 테스트
        java.util.Map<String, java.io.Serializable> serializableBeans = context.getBeansOfType(java.io.Serializable.class);
        org.junit.jupiter.api.Assertions.assertTrue(serializableBeans.containsKey("simpleBean"));
        org.junit.jupiter.api.Assertions.assertTrue(serializableBeans.containsKey("customName"));
        org.junit.jupiter.api.Assertions.assertTrue(serializableBeans.containsKey("dependentBean"));
        org.junit.jupiter.api.Assertions.assertTrue(serializableBeans.containsKey("multiDependentBean"));
        org.junit.jupiter.api.Assertions.assertTrue(serializableBeans.containsKey("collectionDependentBean"));

        // 9. 2단계 상속 계층 구조의 ApplicationContextAware 주입 검증 테스트
        eello.elpring.di.fixtures.config.ChildAwareBean childAwareBean = (eello.elpring.di.fixtures.config.ChildAwareBean) context.getBean("childAwareBean");
        org.junit.jupiter.api.Assertions.assertNotNull(childAwareBean);
        org.junit.jupiter.api.Assertions.assertNotNull(childAwareBean.getApplicationContext());
        org.junit.jupiter.api.Assertions.assertSame(context, childAwareBean.getApplicationContext());

        // 10. List<Converter<?>> 와일드카드 제네릭 인터페이스 리스트 주입 검증 테스트
        eello.elpring.di.fixtures.config.TestBean converterTargetBean = (eello.elpring.di.fixtures.config.TestBean) context.getBean("converterTargetBean");
        org.junit.jupiter.api.Assertions.assertNotNull(converterTargetBean);
        org.junit.jupiter.api.Assertions.assertNotNull(converterTargetBean.getConverters());
        org.junit.jupiter.api.Assertions.assertEquals(2, converterTargetBean.getConverters().size());
    }
}
