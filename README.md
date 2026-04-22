# Lpring 개발 문서

Spring의 DI 컨테이너 동작 원리를 학습하기 위해 ClassPath Scan과 Reflection을 사용해 직접 구현한 경량 DI 컨테이너입니다.

## 스택

- Java 21
- 외부 의존성 없음 (JDK 표준 API만 사용)
- 빌드 도구: IntelliJ IDEA 네이티브

## 프로젝트 구조

```
src/main/java/
├── eello/
│   ├── container/                          # DI 컨테이너 본체
│   │   ├── Application.java                # 컨테이너 부트스트랩 진입점
│   │   ├── ClassPathBeanDefinitionScanner.java  # @Component 클래스 탐색기
│   │   ├── annotation/
│   │   │   ├── Component.java              # 빈 등록 대상 마킹 어노테이션
│   │   │   └── Primary.java               # 다중 빈 우선 주입 어노테이션
│   │   ├── core/
│   │   │   ├── BeanDefinition.java         # 빈 메타데이터 인터페이스
│   │   │   ├── DefaultBeanDefinition.java  # BeanDefinition 구현체
│   │   │   ├── BeanFactory.java            # 빈 조회/등록 인터페이스
│   │   │   ├── DefaultBeanFactory.java     # BeanFactory 구현체
│   │   │   ├── BeanInitializer.java        # 빈 초기화 인터페이스
│   │   │   ├── BeanInitializerUsingTopologicalSorting.java  # 위상정렬 기반 초기화 구현체
│   │   │   ├── BeanScope.java             # 빈 스코프 enum (SINGLETON)
│   │   │   └── registry/
│   │   │       ├── SingletonBeanRegistry.java       # 싱글톤 저장소 인터페이스
│   │   │       └── DefaultSingletonRegistry.java    # 싱글톤 저장소 구현체
│   │   └── exception/
│   │       └── NoUniqueBeanDefinitionException.java # 빈 중복 예외
│   └── app/                               # 컨테이너 동작 검증용 샘플 앱
│       ├── SimpleDIContainer.java         # main 메서드
│       ├── contoller/AController.java
│       ├── service/
│       │   ├── Service.java (interface)
│       │   ├── AService.java (@Primary)
│       │   └── BService.java
│       └── repository/
│           ├── Repository.java (interface)
│           ├── ARepository.java
│           └── BRepository.java
```

## 동작 흐름

```
Application.run(SimpleDIContainer.class)
    │
    ▼
ClassPathBeanDefinitionScanner.doScan(basePackage)
    - ClassLoader로 basePackage 하위 .class 파일 탐색 (재귀)
    - Reflection으로 @Component 어노테이션 여부 확인
    - 대상 클래스 → DefaultBeanDefinition.of(clazz) 변환
    │
    ▼
BeanInitializerUsingTopologicalSorting.initialize(basePackage)
    - BeanDefinition 목록으로 의존성 그래프 구성
    - @Primary 유효성 검증 (동일 타입에 2개 이상이면 예외)
    - Kahn 알고리즘으로 위상정렬 → 의존 순서 결정
    - 순서대로 BeanFactory.registerBean(bd) 호출
    │
    ▼
DefaultBeanFactory.registerBean(bd)
    - createBean(): 생성자 파라미터 타입으로 이미 등록된 빈 조회
      - 후보 1개: 그대로 주입
      - 후보 2개 이상: @Primary 적용된 빈 주입 / 없으면 예외
    - Reflection으로 생성자 호출 → 인스턴스 생성
    - SingletonBeanRegistry에 등록
    - beanDefinitionMapByName / beanDefinitionMapByType 갱신
    │
    ▼
BeanFactory 반환 → getBean()으로 빈 조회 가능
```

## 주요 클래스 상세

### `ClassPathBeanDefinitionScanner`

basePackage 경로를 `ClassLoader.getResource()`로 URL을 얻어 파일 시스템의 실제 디렉토리로 변환한 뒤, 하위 `.class` 파일을 재귀적으로 탐색합니다.

```java
// URL 인코딩(%20 등) 문제를 방지하기 위해 toURI() 사용
File directory = new File(resource.toURI());
```

`@Component`가 붙은 클래스를 `DefaultBeanDefinition.of(clazz)`로 변환해 반환합니다.

---

### `DefaultBeanDefinition`

클래스의 메타데이터를 Reflection으로 추출해 저장합니다.

| 필드 | 설명 |
|---|---|
| `beanName` | 클래스 단순명의 첫 글자를 소문자로 변환 (e.g. `AService` → `aService`) |
| `beanType` | 해당 클래스 타입 |
| `interfaces` | `clazz.getInterfaces()` — 구현한 인터페이스 목록 |
| `constructor` | `clazz.getConstructors()[0]` — 첫 번째(유일한) 생성자 |
| `dependsOn` | 생성자 파라미터 타입 목록 (의존하는 빈 타입) |
| `primary` | `@Primary` 어노테이션 적용 여부 |
| `scope` | 항상 `BeanScope.SINGLETON` |

---

### `BeanInitializerUsingTopologicalSorting`

**Kahn 알고리즘**으로 의존성 순서를 결정합니다.

- `indegree`: 각 클래스가 의존하는 빈의 수 (진입 차수)
- `dependsOnMe`: 특정 타입을 의존하는 클래스 목록 (역방향 그래프)
- `beanDefinitionMap`: 타입 → BeanDefinition 목록

진입 차수가 0인 클래스(의존성 없음)부터 큐에 넣어 처리하며, 처리 후 해당 클래스를 의존하던 클래스들의 진입 차수를 1씩 감소시킵니다. 최종적으로 생성된 빈 수가 전체 BeanDefinition 수와 다르면 순환 참조로 판단해 예외를 던집니다.

**`@Primary` 유효성 검증**: 동일 타입을 의존하는 클래스가 존재하는 상황에서 해당 타입에 `@Primary`가 2개 이상 붙어있으면 `NoUniqueBeanDefinitionException`을 던집니다.

---

### `DefaultBeanFactory`

빈 인스턴스 생성과 `getBean()` API를 담당합니다.

- **빈 등록**: `registerBean(BeanDefinition)` — 인스턴스 생성 후 `SingletonBeanRegistry`에 저장
- **빈 조회**:
    - `getBean(String beanName)` — 이름으로 조회
    - `getBean(String beanName, Class<T> type)` — 이름 + 타입으로 조회
    - `getBean(Class<T> beanType)` — 타입으로 전체 조회 (배열 반환)

내부적으로 두 개의 인덱스를 관리합니다.
- `beanDefinitionMapByName`: 빈 이름 → BeanDefinition
- `beanDefinitionMapByType`: 클래스 타입 및 인터페이스 타입 → BeanDefinition 목록

---

### `DefaultSingletonRegistry`

`Map<String, Object>` 기반의 싱글톤 저장소입니다. 동일 이름의 빈이 중복 등록되면 예외를 던집니다. `BeanFactory`에서 빈 저장 책임을 분리하기 위해 별도 클래스로 추출했습니다.

## 제약 사항

| 항목 | 지원 범위 |
|---|---|
| 빈 등록 어노테이션 | `@Component`만 지원 (`@Service`, `@Repository` 등 미지원) |
| 의존성 주입 방식 | 생성자 주입만 지원 (필드/세터 주입 미지원) |
| 생성자 개수 | 빈 클래스당 생성자 1개 가정 |
| 상속 | 클래스 상속 미지원, 인터페이스 구현만 지원 |
| 인터페이스 계층 | 1단계 위 인터페이스까지만 탐색 |
| 빈 스코프 | SINGLETON만 지원 |

## 예외 처리

| 예외 | 발생 조건 |
|---|---|
| `NoUniqueBeanDefinitionException` | 동일 타입을 의존하는 클래스가 있는데 해당 타입에 `@Primary`가 2개 이상 |
| `IllegalStateException` (주입 모호) | 후보 빈이 2개 이상이고 `@Primary`도 없는 경우 |
| `IllegalStateException` (순환 참조) | 위상정렬 완료 후 생성된 빈 수 != 전체 BeanDefinition 수 |
| `IllegalArgumentException` | 동일 이름의 빈이 이미 등록된 경우 |
| `IllegalStateException` (미등록 의존) | 생성에 필요한 빈이 아직 등록되지 않은 경우 |
