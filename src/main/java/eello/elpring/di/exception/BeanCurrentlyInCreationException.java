package eello.elpring.di.exception;

public class BeanCurrentlyInCreationException extends BeansException {

    public BeanCurrentlyInCreationException() {
    }

    public BeanCurrentlyInCreationException(String message) {
        super(message);
    }

    public BeanCurrentlyInCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeanCurrentlyInCreationException(Throwable cause) {
        super(cause);
    }

    public BeanCurrentlyInCreationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
