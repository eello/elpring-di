package eello.container.core;

import eello.container.annotation.Component;
import eello.container.annotation.Primary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DefaultBeanDefinition")
class DefaultBeanDefinitionTest {

    @Component
    public static class NoDepBean {
    }

    public interface SomeInterface {
    }

    @Primary
    @Component
    public static class PrimaryBean implements SomeInterface {
    }

    @Component
    public static class DependentBean {
        public DependentBean(NoDepBean dep) {
        }
    }

    @Nested
    @DisplayName("빈 이름")
    class BeanName {

        @Test
        @DisplayName("클래스명 첫 글자를 소문자로 변환한다")
        void firstCharLowercase() {
            BeanDefinition def = DefaultBeanDefinition.of(NoDepBean.class);
            assertEquals("noDepBean", def.getBeanName());
        }

        @Test
        @DisplayName("커스텀 이름이 지정되면 해당 이름을 사용한다")
        void customNameOverride() {
            BeanDefinition def = DefaultBeanDefinition.of(NoDepBean.class, "myCustomName");
            assertEquals("myCustomName", def.getBeanName());
        }
    }

    @Nested
    @DisplayName("빈 타입")
    class BeanType {

        @Test
        @DisplayName("getBeanType()은 대상 클래스 자체를 반환한다")
        void beanType_isClass() {
            BeanDefinition def = DefaultBeanDefinition.of(NoDepBean.class);
            assertEquals(NoDepBean.class, def.getBeanType());
        }

        @Test
        @DisplayName("getBeanTypeName()은 클래스 전체 이름을 반환한다")
        void beanTypeName_isFullyQualified() {
            BeanDefinition def = DefaultBeanDefinition.of(NoDepBean.class);
            assertEquals(NoDepBean.class.getName(), def.getBeanTypeName());
        }
    }

    @Nested
    @DisplayName("인터페이스 추출")
    class Interfaces {

        @Test
        @DisplayName("구현한 인터페이스 목록이 정확히 추출된다")
        void interfaces_extracted() {
            BeanDefinition def = DefaultBeanDefinition.of(PrimaryBean.class);
            assertArrayEquals(new Class<?>[]{SomeInterface.class}, def.getInterfaceTypes());
        }

        @Test
        @DisplayName("인터페이스가 없는 클래스는 빈 배열을 반환한다")
        void interfaces_emptyWhenNone() {
            BeanDefinition def = DefaultBeanDefinition.of(NoDepBean.class);
            assertEquals(0, def.getInterfaceTypes().length);
        }
    }

    @Nested
    @DisplayName("의존성(dependsOn)")
    class DependsOn {

        @Test
        @DisplayName("기본 생성자 클래스는 의존성이 없다")
        void dependsOn_emptyWhenNoArgs() {
            BeanDefinition def = DefaultBeanDefinition.of(NoDepBean.class);
            assertEquals(0, def.getDependsOn().length);
        }

        @Test
        @DisplayName("생성자 파라미터 타입이 dependsOn으로 추출된다")
        void dependsOn_extractedFromConstructor() {
            BeanDefinition def = DefaultBeanDefinition.of(DependentBean.class);
            assertArrayEquals(new Class<?>[]{NoDepBean.class}, def.getDependsOn());
        }
    }

    @Nested
    @DisplayName("@Primary")
    class PrimaryAnnotation {

        @Test
        @DisplayName("@Primary 없는 클래스는 isPrimary() = false")
        void primary_falseByDefault() {
            BeanDefinition def = DefaultBeanDefinition.of(NoDepBean.class);
            assertFalse(def.isPrimary());
        }

        @Test
        @DisplayName("@Primary 있는 클래스는 isPrimary() = true")
        void primary_trueWhenAnnotated() {
            BeanDefinition def = DefaultBeanDefinition.of(PrimaryBean.class);
            assertTrue(def.isPrimary());
        }
    }

    @Test
    @DisplayName("스코프는 항상 SINGLETON이다")
    void scope_alwaysSingleton() {
        BeanDefinition def = DefaultBeanDefinition.of(NoDepBean.class);
        assertEquals(BeanScope.SINGLETON, def.getScope());
    }
}
