# elpring-di 테스트 전략 및 작성 가이드

이 문서는 `elpring-di` 라이브러리의 안정성과 다형성 확장을 보장하기 위해 적용된 테스트 전략과 작성 패턴을 설명합니다. 향후 새로운 구현체나 테스트를 추가할 때 이 규칙을 준수하여 작성해야 합니다.

---

## 1. 인터페이스 규약 테스트 패턴 (Contract Test / Abstract Test Class)

객체 지향 설계의 다형성을 보장하기 위해 인터페이스를 대상으로 하는 테스트는 **추상 테스트 클래스 패턴**을 사용합니다. 이는 구현체가 바뀌거나 추가되어도 기존 비즈니스 규약 검증 코드를 100% 재사용하기 위함입니다.

### 구조 및 작성법
인터페이스 `A`에 대한 테스트를 작성할 때:

1. **추상 테스트 클래스 작성 (`AbstractATest`)**
   * 인터페이스 `A`가 만족해야 하는 모든 기능적 요구사항과 예외 시나리오를 `@Test` 메서드로 구현합니다.
   * 실제 테스트 대상 객체는 생성하지 않고, 객체를 반환해 주는 팩토리 추상 메서드를 정의합니다.
     ```java
     public abstract class AbstractATest {
         protected A target;
         
         // 구현체를 제공받을 추상 팩토리 메서드
         protected abstract A createTarget();

         @BeforeEach
         void setUp() {
             this.target = createTarget();
         }

         @Test
         void testCoreBehavior() {
             // 인터페이스 규약 검증 로직 작성
         }
     }
     ```

2. **구현 테스트 클래스 작성 (`DefaultATest`)**
   * `AbstractATest`를 상속받습니다.
   * `createTarget()`을 오버라이드하여 검증하려는 특정 구현 클래스(예: `DefaultA`)의 인스턴스를 반환합니다.
     ```java
     public class DefaultATest extends AbstractATest {
         @Override
         protected A createTarget() {
             return new DefaultA();
         }
         
         // 필요시 DefaultA 구현체에만 특화된 테스트 추가 작성 가능
     }
     ```

이렇게 구성하면 향후 `NewSuperA` 구현체가 추가되었을 때, `AbstractATest`를 상속받는 `NewSuperATest` 클래스 하나만 생성하여 객체를 리턴해주면 기존의 모든 검증 로직이 자동으로 실행됩니다.

---

## 2. 테스트 피스처 격리 (Test Fixtures Isolation)

테스트에 쓰이는 더미 객체(Interface, Impl, Circular Dependency, Lazy Bean 등)들은 각 테스트 파일 내부에 정의하지 않고, **`src/test/java/eello/elpring/di/fixtures/`** 패키지 하위로 추출하여 관리합니다.

스캔용 통합 테스트 시 의존성 충돌(예: 예외 검증용으로 고의로 선언한 순환 참조 빈 스캔 등)을 피하기 위해 다음 두 서브 패키지로 물리적으로 분리합니다.

* **`fixtures.scanner` (해피 패스 스캔용)**
  * `@Component`, `@Lazy`, 커스텀 네임 빈 등 **정상 기동되어야 하는 빈들만 위치**시킵니다.
  * `ClassPathBeanDefinitionScannerTest` 및 `ApplicationContext` 통합 테스트는 오직 이 패키지 경로(`"eello.elpring.di.fixtures.scanner"`)만을 대상으로 스캔을 수행해야 합니다.
* **`fixtures.factory` (코어 로직 및 예외 검증용)**
  * 의존 관계 빈, 다형성 주입 테스트용 다중 구현체, 순환 참조 빈(`CircularA`/`CircularB`), 중복 `@Primary` 지정 빈 등 **수동 빈 등록 검증 및 예외 유도용 빈들을 위치**시킵니다.
  * **주의**: 이 패키지는 해피 패스 스캔 대상 패키지에 절대로 포함시켜서는 안 됩니다.

---

## 3. 새로운 기능/구현체 추가 시 가이드라인

### 새로운 BeanFactory 구현체를 개발할 때:
1. `AbstractBeanFactoryTest`를 상속받는 새로운 테스트 클래스를 `src/test/java`에 생성합니다.
2. `initBeanFactoryAndRegistry()`를 구현하여 새로 작성한 BeanFactory 인스턴스를 반환합니다.
3. 테스트를 돌려 인터페이스 규약(의존성 주입, 예외 처리 등)을 준수하는지 먼저 검증합니다.

### 예외 검증용 빈(Fixture)을 추가할 때:
1. 추가하려는 예외 상황에 맞는 피스처 클래스를 `eello.elpring.di.fixtures.factory` 패키지 하위에 생성합니다.
2. `AbstractBeanFactoryTest`에 해당 피스처를 수동으로 등록(`registry.registerBeanDefinition`)하고 예외가 정상 발생하는지 검증하는 테스트 케이스를 추가합니다.
