package eello.elpring.di.context;

import eello.elpring.di.beans.factory.support.ClassPathBeanDefinitionScanner;

public class AnnotationConfigApplicationContext extends GenericApplicationContext implements AnnotationConfigRegistry {

    private final ClassPathBeanDefinitionScanner scanner;

    public AnnotationConfigApplicationContext(String... basePackages) {
        this.scanner = new ClassPathBeanDefinitionScanner(this);
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
}
