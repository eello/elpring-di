package eello.elpring.di.context;

import eello.elpring.di.annotation.Bean;
import eello.elpring.di.annotation.Configuration;
import eello.elpring.di.exception.BeanDefinitionStoreException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CustomConfigurationRegistrationTest {

    // 1. 단일 설정 클래스 수동 등록 검증
    @Test
    void testRegisterSingleConfiguration() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.registerCustomConfiguration(List.of(SingleConfig.class));
        context.refresh();

        assertNotNull(context.getBean(SingleConfig.class));
        assertEquals("Hello Single", context.getBean("simpleStringBean"));
    }

    // 2. 복수 설정 클래스 수동 등록 검증
    @Test
    void testRegisterMultipleConfigurations() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.registerCustomConfiguration(List.of(SingleConfig.class, AnotherConfig.class));
        context.refresh();

        assertNotNull(context.getBean(SingleConfig.class));
        assertNotNull(context.getBean(AnotherConfig.class));
        assertEquals("Hello Single", context.getBean("simpleStringBean"));
        assertEquals(42, context.getBean("simpleIntegerBean"));
    }

    // 3. 수동 등록 빈 간의 의존성 주입(DI) 검증
    @Test
    void testRegisterConfigurationWithDependency() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.registerCustomConfiguration(List.of(DependencyConfig.class));
        context.refresh();

        String message = context.getBean("messageBean", String.class);
        assertEquals("Injected: Hello Single", message);
    }

    // 4. 일반 클래스(비설정 클래스) 등록 시 예외 발생 검증
    @Test
    void testRegisterNonConfigurationClassThrowsException() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        
        assertThrows(IllegalArgumentException.class, () -> {
            context.registerCustomConfiguration(List.of(NonConfigClass.class));
        });
    }

    // 5-1. 동일 설정 클래스 중복 등록 시 예외 없이 멱등적으로 무시 검증
    @Test
    void testRegisterDuplicateConfigurationClassIsIdempotent() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.registerCustomConfiguration(List.of(SingleConfig.class));

        assertDoesNotThrow(() -> {
            context.registerCustomConfiguration(List.of(SingleConfig.class));
        });

        context.refresh();
        assertNotNull(context.getBean(SingleConfig.class));
    }

    // 5-2. 이름 충돌 빈 등록 시 예외 발생 검증
    @Test
    void testRegisterConflictingBeanNameThrowsException() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.registerCustomConfiguration(List.of(SingleConfig.class));

        assertThrows(BeanDefinitionStoreException.class, () -> {
            context.registerCustomConfiguration(List.of(ConflictingConfig.class));
        });
    }

    // 6. 컴포넌트 스캔 후 수동 등록 혼용 시 동작 검증
    @Test
    void testHybridScanningAndCustomConfiguration() {
        // 기존의 config 피스처 패키지 스캔으로 생성
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("eello.elpring.di.fixtures.config");
        
        // 추가로 외부 SingleConfig를 동적 등록
        context.registerCustomConfiguration(List.of(SingleConfig.class));
        context.refresh();

        // 컴포넌트 스캔 대상 빈 확인
        assertNotNull(context.getBean("simpleBean"));
        // 수동 등록 빈 확인
        assertNotNull(context.getBean("simpleStringBean"));
    }

    // --- Test Fixtures ---

    @Configuration
    public static class SingleConfig {
        public SingleConfig() {}
        @Bean
        public String simpleStringBean() {
            return "Hello Single";
        }
    }

    @Configuration
    public static class AnotherConfig {
        public AnotherConfig() {}
        @Bean
        public Integer simpleIntegerBean() {
            return 42;
        }
    }

    @Configuration
    public static class DependencyConfig {
        public DependencyConfig() {}
        @Bean
        public String sourceBean() {
            return "Hello Single";
        }

        @Bean
        public String messageBean(String sourceBean) {
            return "Injected: " + sourceBean;
        }
    }

    public static class NonConfigClass {
        public NonConfigClass() {}
        public String dummy() {
            return "No Configuration Annotation";
        }
    }

    @Configuration
    public static class ConflictingConfig {
        public ConflictingConfig() {}
        @Bean
        public Integer simpleStringBean() {
            return 100;
        }
    }
}
