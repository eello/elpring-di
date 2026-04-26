package eello.container.boot;

import eello.container.context.AnnotationConfigApplicationContext;
import eello.container.context.ApplicationContext;

public class ElpringApplication {

    public static ApplicationContext run(Class<?> primarySource) {
        return run(primarySource.getPackageName());
    }

    public static ApplicationContext run(String basePackage) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(basePackage);
        try {
            ctx.refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ctx;
    }
}
