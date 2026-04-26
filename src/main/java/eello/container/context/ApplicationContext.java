package eello.container.context;

import java.lang.reflect.InvocationTargetException;

public interface ApplicationContext {

    void refresh() throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException; // 컨텍스트 초기화 함수
    Object getBean(String beanName);
    <T> T[] getBean(Class<T> requiredType);
    <T> T getBean(String beanName, Class<T> requiredType);
}
