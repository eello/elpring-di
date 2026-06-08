package eello.elpring.di.exception;

public class BeanDefinitionStoreException extends BeansException {

    public BeanDefinitionStoreException() {
    }

    public BeanDefinitionStoreException(String message) {
        super(message);
    }

    public BeanDefinitionStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeanDefinitionStoreException(Throwable cause) {
        super(cause);
    }

    public BeanDefinitionStoreException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
