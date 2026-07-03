package eello.elpring.di.context;

import eello.elpring.di.beans.factory.support.ClassPathBeanDefinitionScanner;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotationConfigApplicationContext extends GenericApplicationContext implements AnnotationConfigRegistry {

    private static final Logger log = LoggerFactory.getLogger(AnnotationConfigApplicationContext.class);

    private final ClassPathBeanDefinitionScanner scanner;

    public AnnotationConfigApplicationContext(String... basePackages) {
        this.scanner = new ClassPathBeanDefinitionScanner(getClassLoader(), this);
        scan(basePackages);
    }

    public AnnotationConfigApplicationContext(ClassPathBeanDefinitionScanner scanner, String... basePackages) {
        this.scanner = scanner;
        scan(basePackages);
    }

    @Override
    public void scan(String... basePackages) {
        try {
            log.info("Scanning {} base package(s) for application context", basePackages.length);
            this.scanner.scan(basePackages);
            log.info("Completed scanning {} base package(s)", basePackages.length);
        } catch (ClassNotFoundException e) {
            log.error("Failed to scan base packages: {}", (Object) basePackages, e);
        }
    }

    /**
     * ClassPathBeanDefinitionScanner에 @Configuration 클래스를 넘겨 추가로 빈 정의를 등록하는 메서드
     */
    public void registerCustomConfiguration(List<Class<?>> configs) {
        if (configs == null || configs.isEmpty()) {
            log.info("No custom auto-configuration classes to register");
            return;
        }
        log.info("Registering {} custom configuration class(es)", configs.size());
        if (log.isDebugEnabled()) {
            for (Class<?> config : configs) {
                log.debug("Registering custom configuration class [{}]", config.getName());
            }
        }
        this.scanner.registerConfigClass(configs);
    }
}
