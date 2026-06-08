package eello.elpring.di.beans.factory.support.registry;

public class DefaultSingletonBeanRegistryTest extends AbstractSingletonBeanRegistryTest {

    @Override
    protected SingletonBeanRegistry createRegistry() {
        return new DefaultSingletonBeanRegistry();
    }
}
