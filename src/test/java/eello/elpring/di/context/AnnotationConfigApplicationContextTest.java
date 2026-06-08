package eello.elpring.di.context;

public class AnnotationConfigApplicationContextTest extends AbstractApplicationContextTest {

    @Override
    protected ConfigurableApplicationContext createApplicationContext(String... basePackages) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(basePackages);
        context.refresh();
        return context;
    }
}
