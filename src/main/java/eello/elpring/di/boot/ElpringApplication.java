package eello.elpring.di.boot;

import eello.elpring.di.context.AnnotationConfigApplicationContext;
import eello.elpring.di.context.ConfigurableApplicationContext;

public class ElpringApplication {

    public static ConfigurableApplicationContext run(Class<?> primarySource) {
        return run(primarySource.getPackageName());
    }

    public static ConfigurableApplicationContext run(String... basePackage) {
        ConfigurableApplicationContext ctx = new AnnotationConfigApplicationContext(basePackage);
        ctx.refresh();
        return ctx;
    }
}
