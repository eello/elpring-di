package eello.elpring.di.context;

import eello.elpring.di.beans.factory.support.ClassPathBeanDefinitionScanner;

import java.util.List;

public class AnnotationConfigApplicationContext extends GenericApplicationContext implements AnnotationConfigRegistry {

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
            System.out.println("Scanning " + basePackages.length + " application context");
            this.scanner.scan(basePackages);
            System.out.println("Completed Scanning " + basePackages.length + " application context");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * ClassPathBeanDefinitionScanner에 @Configuration 클래스를 넘겨 추가로 빈 정의를 등록하는 메서드
     */
    public void registerCustomConfiguration(List<Class<?>> configs) {
        this.scanner.registerConfigClass(configs);
    }
}
