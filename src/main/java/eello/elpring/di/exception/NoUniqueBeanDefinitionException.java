package eello.elpring.di.exception;

public class NoUniqueBeanDefinitionException extends BeansException {

    public NoUniqueBeanDefinitionException(String message) {
        super(message);
    }
}
