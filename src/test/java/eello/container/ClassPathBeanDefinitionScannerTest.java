package eello.container;

import eello.container.core.BeanDefinition;
import eello.fixture.simple.DependentBean;
import eello.fixture.simple.NonComponentBean;
import eello.fixture.simple.SimpleBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ClassPathBeanDefinitionScanner")
class ClassPathBeanDefinitionScannerTest {

    private ClassPathBeanDefinitionScanner scanner;

    @BeforeEach
    void setUp() {
        scanner = new ClassPathBeanDefinitionScanner();
    }

    @Test
    @DisplayName("@Component 클래스만 BeanDefinition으로 반환한다")
    void scan_findsOnlyComponentAnnotatedClasses() throws ClassNotFoundException {
        Set<BeanDefinition> result = scanner.doScan("eello.fixture.simple");

        Set<Class<?>> types = result.stream()
                .map(BeanDefinition::getBeanType)
                .collect(Collectors.toSet());

        assertTrue(types.contains(SimpleBean.class));
        assertTrue(types.contains(DependentBean.class));
    }

    @Test
    @DisplayName("@Component 없는 클래스는 결과에 포함되지 않는다")
    void scan_excludesNonAnnotatedClasses() throws ClassNotFoundException {
        Set<BeanDefinition> result = scanner.doScan("eello.fixture.simple");

        Set<Class<?>> types = result.stream()
                .map(BeanDefinition::getBeanType)
                .collect(Collectors.toSet());

        assertFalse(types.contains(NonComponentBean.class));
    }

    @Test
    @DisplayName("스캔 결과 BeanDefinition의 이름과 타입이 정확하다")
    void scan_beanDefinitionMetadata_isCorrect() throws ClassNotFoundException {
        Set<BeanDefinition> result = scanner.doScan("eello.fixture.simple");

        BeanDefinition simpleDef = result.stream()
                .filter(bd -> bd.getBeanType() == SimpleBean.class)
                .findFirst()
                .orElseThrow();

        assertEquals("simpleBean", simpleDef.getBeanName());
        assertEquals(SimpleBean.class, simpleDef.getBeanType());
    }

    @Test
    @DisplayName("하위 패키지까지 재귀적으로 탐색한다")
    void scan_recursivelyScansSubpackages() throws ClassNotFoundException {
        Set<BeanDefinition> result = scanner.doScan("eello.fixture");

        Set<Class<?>> types = result.stream()
                .map(BeanDefinition::getBeanType)
                .collect(Collectors.toSet());

        assertTrue(types.contains(SimpleBean.class));
        assertTrue(types.contains(eello.fixture.primary.PrimaryImplA.class));
        assertTrue(types.contains(eello.fixture.ambiguous.AmbiguousImplA.class));
    }
}
