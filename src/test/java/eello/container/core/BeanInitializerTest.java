package eello.container.core;

import eello.container.core.registry.DefaultSingletonBeanRegistry;
import eello.container.exception.NoUniqueBeanDefinitionException;
import eello.fixture.primary.PrimaryConsumer;
import eello.fixture.primary.PrimaryImplA;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BeanInitializerUsingTopologicalSorting")
class BeanInitializerTest {

    private DefaultBeanFactory beanFactory;
    private DefaultBeanInitializer initializer;

    @BeforeEach
    void setUp() {
        beanFactory = new DefaultBeanFactory(new DefaultSingletonBeanRegistry());
        initializer = new DefaultBeanInitializer(
                beanFactory, new ClassPathBeanDefinitionScanner());
    }

    @Test
    @DisplayName("선형 의존성 체인을 올바른 순서로 초기화한다")
    void initialize_simpleChain_success() throws Exception {
        initializer.initialize("eello.fixture.simple");

        assertTrue(beanFactory.isRegistered("simpleBean"));
        assertTrue(beanFactory.isRegistered("dependentBean"));
    }

    @Test
    @DisplayName("@Primary 빈이 의존성으로 선택된다")
    void initialize_primarySelected() throws Exception {
        initializer.initialize("eello.fixture.primary");

        PrimaryConsumer consumer = beanFactory.getBean("primaryConsumer", PrimaryConsumer.class);
        assertInstanceOf(PrimaryImplA.class, consumer.getDependency());
    }

    @Test
    @DisplayName("@Primary가 같은 타입에 2개 이상이면 NoUniqueBeanDefinitionException이 발생한다")
    void initialize_duplicatePrimary_throwsNoUniqueBeanDefinitionException() {
        assertThrows(NoUniqueBeanDefinitionException.class,
                () -> initializer.initialize("eello.fixture.duplicate_primary"));
    }

    @Test
    @DisplayName("후보 빈이 2개이고 @Primary가 없으면 IllegalStateException이 발생한다")
    void initialize_ambiguous_throwsIllegalStateException() {
        assertThrows(IllegalStateException.class,
                () -> initializer.initialize("eello.fixture.ambiguous"));
    }

    @Test
    @DisplayName("순환 참조가 있으면 IllegalStateException이 발생한다")
    void initialize_circularDependency_throwsIllegalStateException() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> initializer.initialize("eello.fixture.circular"));
        assertTrue(ex.getMessage().contains("순환참조"));
    }
}
