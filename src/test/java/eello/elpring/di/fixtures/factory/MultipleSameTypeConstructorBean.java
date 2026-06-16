package eello.elpring.di.fixtures.factory;

public class MultipleSameTypeConstructorBean {

    private final InterfaceA implA1;
    private final InterfaceA implA2;

    public MultipleSameTypeConstructorBean(InterfaceA implA1, InterfaceA implA2) {
        this.implA1 = implA1;
        this.implA2 = implA2;
    }

    public InterfaceA getImplA1() {
        return implA1;
    }

    public InterfaceA getImplA2() {
        return implA2;
    }
}
