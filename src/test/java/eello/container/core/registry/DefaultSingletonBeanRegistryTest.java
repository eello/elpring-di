package eello.container.core.registry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DefaultSingletonRegistry")
class DefaultSingletonBeanRegistryTest {

    private DefaultSingletonBeanRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new DefaultSingletonBeanRegistry();
    }

    @Test
    @DisplayName("저장한 객체를 이름으로 조회하면 동일 인스턴스를 반환한다")
    void addAndGet_returnsSameInstance() {
        Object bean = new Object();
        registry.addSingleton("myBean", bean);

        assertSame(bean, registry.getSingleton("myBean"));
    }

    @Test
    @DisplayName("같은 이름으로 두 번 addSingleton하면 IllegalStateException이 발생한다")
    void duplicateName_throwsIllegalStateException() {
        registry.addSingleton("myBean", new Object());

        assertThrows(IllegalStateException.class,
                () -> registry.addSingleton("myBean", new Object()));
    }

    @Test
    @DisplayName("등록되지 않은 이름으로 getSingleton하면 null을 반환한다")
    void getUnregistered_returnsNull() {
        assertNull(registry.getSingleton("notExists"));
    }
}
