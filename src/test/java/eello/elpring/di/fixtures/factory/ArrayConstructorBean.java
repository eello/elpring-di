package eello.elpring.di.fixtures.factory;

public class ArrayConstructorBean {

    private final InterfaceA[] array;

    public ArrayConstructorBean(InterfaceA[] array) {
        this.array = array;
    }

    public InterfaceA[] getArray() {
        return array;
    }
}
