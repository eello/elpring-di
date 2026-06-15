package eello.elpring.di.fixtures.factory;

import java.util.List;

@SuppressWarnings("rawtypes")
public class RawListConstructorBean {

    private final List list;

    public RawListConstructorBean(List list) {
        this.list = list;
    }

    public List getList() {
        return list;
    }
}
