package eello.container.core;

import eello.container.annotation.Component;
import eello.container.annotation.Primary;
import eello.container.core.registry.DefaultSingletonRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DefaultBeanFactory")
class DefaultBeanFactoryTest {

    // 모든 내부 클래스는 public으로 선언해야 getConstructors()가 공개 생성자를 인식함

    @Component
    public static class Alpha {
    }

    public interface Greet {
    }

    @Component
    public static class NoDepGreetA implements Greet {
    }

    @Component
    public static class NoDepGreetB implements Greet {
    }

    @Primary
    @Component
    public static class PrimaryGreet implements Greet {
    }

    @Component
    public static class GreetConsumer {
        private final Greet greet;

        public GreetConsumer(Greet greet) {
            this.greet = greet;
        }

        public Greet getGreet() {
            return greet;
        }
    }

    @Component
    public static class AlphaDepBean {
        private final Alpha alpha;

        public AlphaDepBean(Alpha alpha) {
            this.alpha = alpha;
        }

        public Alpha getAlpha() {
            return alpha;
        }
    }

    private DefaultBeanFactory factory;

    @BeforeEach
    void setUp() {
        factory = new DefaultBeanFactory(new DefaultSingletonRegistry());
    }

    @Nested
    @DisplayName("registerBean")
    class RegisterBean {

        @Test
        @DisplayName("의존성 없는 빈을 정상 등록한다")
        void success() throws Exception {
            factory.registerBean(DefaultBeanDefinition.of(Alpha.class));
            assertTrue(factory.isRegistered("alpha"));
        }

        @Test
        @DisplayName("같은 이름의 빈을 중복 등록하면 IllegalArgumentException이 발생한다")
        void duplicate_throwsIllegalArgumentException() throws Exception {
            BeanDefinition def = DefaultBeanDefinition.of(Alpha.class);
            factory.registerBean(def);

            assertThrows(IllegalArgumentException.class,
                    () -> factory.registerBean(def));
        }

        @Test
        @DisplayName("의존 빈이 먼저 등록되지 않은 경우 IllegalStateException이 발생한다")
        void missingDependency_throwsIllegalStateException() {
            BeanDefinition def = DefaultBeanDefinition.of(AlphaDepBean.class);

            assertThrows(IllegalStateException.class,
                    () -> factory.registerBean(def));
        }

        @Test
        @DisplayName("후보 빈이 2개이고 @Primary가 없으면 IllegalStateException이 발생한다")
        void ambiguous_throwsIllegalStateException() throws Exception {
            factory.registerBean(DefaultBeanDefinition.of(NoDepGreetA.class));
            factory.registerBean(DefaultBeanDefinition.of(NoDepGreetB.class));

            assertThrows(IllegalStateException.class,
                    () -> factory.registerBean(DefaultBeanDefinition.of(GreetConsumer.class)));
        }

        @Test
        @DisplayName("후보 빈이 2개일 때 @Primary 빈이 주입된다")
        void primarySelected() throws Exception {
            factory.registerBean(DefaultBeanDefinition.of(NoDepGreetA.class));
            factory.registerBean(DefaultBeanDefinition.of(PrimaryGreet.class));
            factory.registerBean(DefaultBeanDefinition.of(GreetConsumer.class));

            GreetConsumer consumer = factory.getBean("greetConsumer", GreetConsumer.class);
            assertInstanceOf(PrimaryGreet.class, consumer.getGreet());
        }
    }

    @Nested
    @DisplayName("getBean")
    class GetBean {

        @BeforeEach
        void registerAlpha() throws Exception {
            factory.registerBean(DefaultBeanDefinition.of(Alpha.class));
        }

        @Test
        @DisplayName("이름으로 빈을 조회한다")
        void byName() {
            assertNotNull(factory.getBean("alpha"));
        }

        @Test
        @DisplayName("이름과 타입으로 빈을 조회한다")
        void byNameAndType() {
            Alpha bean = factory.getBean("alpha", Alpha.class);
            assertNotNull(bean);
            assertInstanceOf(Alpha.class, bean);
        }

        @Test
        @DisplayName("타입으로 해당 타입의 모든 빈을 배열로 반환한다")
        void byType_returnsAll() throws Exception {
            factory.registerBean(DefaultBeanDefinition.of(Alpha.class, "alpha2"));

            Object[] beans = factory.getBean(Alpha.class);
            assertEquals(2, beans.length);
        }

        @Test
        @DisplayName("인터페이스 타입으로 구현체 빈을 조회한다")
        void byInterfaceType() throws Exception {
            factory.registerBean(DefaultBeanDefinition.of(NoDepGreetA.class));

            Object[] beans = factory.getBean(Greet.class);
            assertEquals(1, beans.length);
            assertInstanceOf(NoDepGreetA.class, beans[0]);
        }
    }

    @Nested
    @DisplayName("isRegistered")
    class IsRegistered {

        @Test
        @DisplayName("등록된 빈 이름은 true를 반환한다")
        void byName_true() throws Exception {
            factory.registerBean(DefaultBeanDefinition.of(Alpha.class));
            assertTrue(factory.isRegistered("alpha"));
        }

        @Test
        @DisplayName("미등록 이름은 false를 반환한다")
        void byName_false() {
            assertFalse(factory.isRegistered("notExists"));
        }

        @Test
        @DisplayName("BeanDefinition으로 등록 여부를 확인한다")
        void byDefinition() throws Exception {
            BeanDefinition def = DefaultBeanDefinition.of(Alpha.class);
            assertFalse(factory.isRegistered(def));

            factory.registerBean(def);
            assertTrue(factory.isRegistered(def));
        }
    }

    @Test
    @DisplayName("의존성 있는 빈 등록 시 의존 빈이 정상 주입된다")
    void dependency_injectedCorrectly() throws Exception {
        factory.registerBean(DefaultBeanDefinition.of(Alpha.class));
        factory.registerBean(DefaultBeanDefinition.of(AlphaDepBean.class));

        AlphaDepBean depBean = factory.getBean("alphaDepBean", AlphaDepBean.class);
        Alpha alpha = factory.getBean("alpha", Alpha.class);

        assertSame(alpha, depBean.getAlpha());
    }
}
