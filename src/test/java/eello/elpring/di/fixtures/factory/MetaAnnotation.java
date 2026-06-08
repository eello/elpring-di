package eello.elpring.di.fixtures.factory;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@CustomAnnotation
public @interface MetaAnnotation {
}
