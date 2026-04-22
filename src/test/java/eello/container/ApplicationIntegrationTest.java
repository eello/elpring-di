package eello.container;

import eello.app.contoller.AController;
import eello.app.repository.ARepository;
import eello.app.service.AService;
import eello.app.service.BService;
import eello.app.service.Service;
import eello.container.core.BeanFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Application 통합 테스트")
class ApplicationIntegrationTest {

    private static BeanFactory context;

    @BeforeAll
    static void setUp() throws Exception {
        context = Application.run("eello.app");
    }

    @Test
    @DisplayName("Application.run()이 BeanFactory를 반환한다")
    void run_returnsNonNullBeanFactory() {
        assertNotNull(context);
    }

    @Test
    @DisplayName("이름으로 aController 빈을 조회한다")
    void getBean_aController_notNull() {
        AController aController = context.getBean("aController", AController.class);
        assertNotNull(aController);
    }

    @Test
    @DisplayName("같은 이름으로 두 번 조회하면 동일한 싱글톤 인스턴스를 반환한다")
    void singleton_sameInstance_fromMultipleGetBean() {
        AService first = context.getBean("aService", AService.class);
        AService second = context.getBean("aService", AService.class);
        assertSame(first, second);
    }

    @Test
    @DisplayName("@Primary인 AService가 AController에 주입된다")
    void primary_aServiceInjectedIntoController() {
        AController aController = context.getBean("aController", AController.class);
        AService aService = context.getBean("aService", AService.class);
        assertSame(aService, aController.getService());
    }

    @Test
    @DisplayName("@Primary가 아닌 BService는 AController에 주입되지 않는다")
    void primary_bServiceNotInjectedIntoController() {
        AController aController = context.getBean("aController", AController.class);
        BService bService = context.getBean("bService", BService.class);
        assertNotSame(bService, aController.getService());
    }

    @Test
    @DisplayName("AService에 ARepository가 주입된다")
    void dependency_aServiceHasARepository() {
        AService aService = context.getBean("aService", AService.class);
        ARepository aRepository = context.getBean("aRepository", ARepository.class);
        assertSame(aRepository, aService.getRepository());
    }

    @Test
    @DisplayName("Service 타입으로 조회하면 AService, BService 모두 반환된다")
    void getBean_byInterface_returnsAllImplementations() {
        Object[] services = context.getBean(Service.class);
        assertEquals(2, services.length);
    }
}
