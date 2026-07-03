[![](https://jitpack.io/v/eello/elpring-di.svg)](https://jitpack.io/#eello/elpring-di)

# elpring-di (경량 DI 컨테이너)

`elpring-di`는 Spring의 핵심 기술인 **Dependency Injection (DI) 컨테이너**의 동작 원리를 깊이 있게 학습하고 직접 이해하기 위해 순수 Java로 구현된 경량 DI 프레임워크 라이브러리입니다. ClassPath 스캔, Reflection API, 그리고 재귀적 의존성 탐색 알고리즘을 사용해 빈(Bean)의 생명주기와 의존 관계 주입을 완전 자동화 처리합니다.

---

## 🛠 기술 스택

- **Core**: Java 21 (JDK 21)
- **외부 의존성 없음**: 라이브러리 코어는 JDK 표준 API만 사용
- **빌드 도구**: Gradle
- **테스트 프레임워크**: JUnit 5

---

## ✨ 핵심 기능

1. **ClassPath 재귀 스캔**:
   - `ClassLoader`를 활용해 특정 패키지 하위의 모든 `.class` 파일을 탐색합니다.
   - 클래스 상에 명시된 `@Component` 및 스테레오타입 어노테이션(`@Service`, `@Repository` 등)을 감지하고 메타-어노테이션 분석을 통해 빈 등록 대상을 추출합니다.
2. **수동 빈 등록 기능 (`@Configuration` & `@Bean`)**:
   - `@Configuration` 어노테이션이 적용된 클래스 내부에서 `@Bean` 팩토리 메서드를 분석하여 수동으로 빈을 등록하고 의존 관계를 형성할 수 있습니다.
3. **생성자 및 팩토리 메서드 기반 의존성 주입**:
   - 생성자 파라미터 분석 혹은 `@Bean` 팩토리 메서드의 매개변수 분석을 통해 필요한 의존 빈들을 컨테이너 내부에서 조회하여 결합합니다.
4. **다형성 기반 다중 의존성 주입 (Collection/Array Injection)**:
   - 동일한 상위 타입 또는 인터페이스를 구현한 여러 빈이 존재할 때, `List<T>`, `Set<T>`, `Map<String, T>`, `T[]` 형태로 자동 조립하여 주입받을 수 있습니다. (`ResolvableType` 활용)
5. **다중 구현체 매칭 우선순위 및 fallback 전략**:
   - 동일 타입 빈이 여러 개 존재할 때, `@Primary` 어노테이션이 지정된 빈을 최우선으로 주입합니다.
   - `@Primary`가 없는 경우, 파라미터 변수명과 등록된 빈의 이름을 비교하여 일치하는 빈을 자동으로 주입합니다.
6. **Eager & Lazy 초기화 지원**:
   - 일반 빈은 애플리케이션 시작 단계(`refresh`)에서 즉시 사전 생성(Pre-instantiation)됩니다.
   - `@Lazy`가 지정된 빈은 시작 시점에는 생성되지 않으며, 실제 빈이 요청되거나 참조되는 최초 시점에 지연 생성됩니다.
7. **`ApplicationContextAware` 생명주기 인터페이스 지원**:
   - 컨테이너 초기화 완료 후, 해당 인터페이스를 구현한 빈에 애플리케이션 컨텍스트를 주입하는 라이프사이클 콜백을 제공합니다.
8. **실시간 순환 참조 감지 (Circular Dependency Detection)**:
   - 깊이 우선 탐색(DFS) 방식의 빈 생성 과정에서 순환 참조를 감지하면 즉시 `BeanCurrentlyInCreationException` 예외를 발생시키고 실행을 중단합니다.

---

## 📂 프로젝트 패키지 구조

```
src/main/java/
└── eello/
    └── elpring/
        └── di/
            ├── annotation/
            │   ├── Bean.java                       # 수동 빈 팩토리 메서드 등록 마킹
            │   ├── Component.java                  # 자동 빈 등록 기본 마킹
            │   ├── Service.java                    # 비즈니스 서비스 계층 스테레오타입 마킹 (@Component 포함)
            │   ├── Repository.java                 # 데이터 영속성 계층 스테레오타입 마킹 (@Component 포함)
            │   ├── Configuration.java              # 설정 클래스 지정 마킹
            │   ├── Lazy.java                       # 지연 초기화 지정
            │   └── Primary.java                    # 동일 타입 다중 빈 주입 시 우선순위 지정
            ├── beans/
            │   ├── BeanDefinition.java             # 빈 메타데이터 규약 인터페이스
            │   ├── BeanDefinitionHolder.java       # 빈 이름과 BeanDefinition의 홀더
            │   ├── BeanScope.java                  # 빈의 스코프 규정 (싱글톤만 지원)
            │   ├── DefaultBeanDefinition.java      # 리플렉션 및 ResolvableType 기반 메타데이터 추출 구현체
            │   └── factory/
            │       ├── BeanFactory.java            # 빈 조회를 위한 최상위 인터페이스
            │       ├── DefaultBeanFactory.java      # 기본 빈 팩토리 구현체
            │       ├── ListableBeanFactory.java    # 타입별 빈 다중 조회를 위한 확장 인터페이스
            │       └── support/
            │           ├── BeanDefinitionRegistry.java # 빈 메타데이터 등록 명세 인터페이스
            │           ├── BeanDefinitionReaderUtils.java # 빈 등록 헬퍼 유틸
            │           ├── ClassPathBeanDefinitionScanner.java # 자동 컴포넌트 스캔 및 수동 구성 클래스 파싱/등록
            │           ├── DefaultListableBeanFactory.java # 빈의 매핑, DFS 인스턴스화, 다중/제네릭 컬렉션 주입을 담당하는 핵심 엔진
            │           └── registry/
            │               ├── SingletonBeanRegistry.java # 싱글톤 생명주기 관리 인터페이스
            │               └── DefaultSingletonBeanRegistry.java # 싱글톤 인스턴스 캐시 및 순환 참조 방지 상태 관리
            ├── boot/
            │   └── ElpringApplication.java         # 애플리케이션 시동부 부트스트랩 클래스
            ├── context/
            │   ├── ApplicationContext.java         # 최상위 애플리케이션 컨텍스트 인터페이스
            │   ├── ApplicationContextAware.java    # 컨텍스트 자동 주입 라이프사이클 콜백 인터페이스
            │   ├── ConfigurableApplicationContext.java # 컨텍스트 초기화 및 리프레시 명세
            │   ├── AbstractApplicationContext.java # refresh() 등의 초기화 템플릿 메서드 구현체
            │   ├── GenericApplicationContext.java  # BeanFactory 위임 및 설정 어댑터
            │   ├── AnnotationConfigRegistry.java   # 설정 클래스 스캔/등록 제어 인터페이스
            │   └── AnnotationConfigApplicationContext.java # 어노테이션 기반 최상단 컨텍스트 구현체
            ├── exception/
            │   ├── BeansException.java             # DI 공통 최상위 예외
            │   ├── BeanCreationException.java      # 빈 인스턴스화/생성 실패 시 발생
            │   ├── BeanCurrentlyInCreationException.java # 순환 참조 감지 시 발생
            │   ├── BeanDefinitionStoreException.java # 동일 빈 이름 중복 등록 충돌 시 발생
            │   ├── NoSuchBeanDefinitionException.java # 요청 또는 의존하는 빈 정의가 없을 때 발생
            │   └── NoUniqueBeanDefinitionException.java # 다중 구현체 매칭 실패(우선순위 모호) 시 발생
            ├── inbox/
            │   ├── ConfigurationClassParser.java   # @Configuration 클래스 파서
            │   ├── ResolvableType.java             # 제네릭 타입 파싱 및 계층 매칭 코어
            │   └── ResourceLoader.java             # 리소스 로더
            └── util/
                └── GenericTypeResolver.java        # 제네릭 타입 해결 유틸리티
```

---

## 🔄 DI 라이프사이클 및 의존성 주입 프로세스

`elpring-di` 컨테이너의 핵심 라이프사이클과 의존성 주입 흐름은 다음과 같습니다:

```
ElpringApplication.run(App.class)
  │
  ▼
AnnotationConfigApplicationContext 초기화
  │
  ▼
ClassPathBeanDefinitionScanner 패키지 스캔 & 수동 설정 클래스 등록
  ├─ basePackage 하위의 모든 .class 파일 재귀 탐색
  ├─ @Component, @Service, @Repository 등 어노테이션이 붙은 클래스 추출 후 BeanDefinition 등록
  ├─ @Configuration 어노테이션이 붙은 설정 클래스 및 내부 @Bean 메서드 파싱 후 BeanDefinition 등록
  └─ registerCustomConfiguration(List<Class<?>>) 호출 시 전달된 설정 클래스들의 BeanDefinition 등록
  │
  ▼
AbstractApplicationContext.refresh() 수행
  ├─ 등록된 모든 BeanDefinition을 조회
  └─ @Lazy가 지정되지 않은 일반(Eager) 빈들에 대해 getBean(beanName) 호출하여 사전 인스턴스화
      │
      ▼ (instantiateBean - DFS 방식 재귀 인스턴스 생성)
   1. singletonCurrentlyInCreation 플래그에 현재 빈 등록 (순환 참조 방지 시작)
   2. 의존 객체 파악:
      ├─ 일반 컴포넌트: 생성자 파라미터 타입 분석 (`getDependsOn()`)
      └─ 팩토리 메서드: `@Bean` 메서드의 파라미터 타입 분석 (`getDependsOn()`)
   3. 의존하는 각 타입/제네릭에 대해 재귀적으로 의존성 조회 및 해결
      ├─ 단일 빈 주입: 타입 매칭 -> @Primary -> 변수명 매칭 순으로 대상 결정 후 getBean(beanName) 호출
      └─ 컬렉션/다중 빈 주입: ResolvableType 기반으로 매칭되는 모든 빈을 찾아 Array, List, Set, Map으로 조립
      └─ 재귀 호출 도중 이미 singletonCurrentlyInCreation에 등록된 빈을 다시 참조하려고 시도하면,
         즉시 BeanCurrentlyInCreationException 던져 예외 차단
   4. 인스턴스 생성:
      ├─ 일반 컴포넌트: 생성자 리플렉션 호출 (`newInstance`)
      └─ 팩토리 메서드: 설정 클래스 싱글톤 빈 획득 후 팩토리 메서드 리플렉션 호출 (`invoke`)
   5. 생성 완료된 객체에 대해 ApplicationContextAware 인터페이스 구현 여부를 판단하여 ApplicationContext 주입
   6. 완성된 인스턴스를 DefaultSingletonBeanRegistry 캐시에 저장
   7. singletonCurrentlyInCreation 상태 해제
```

---

## 💻 사용 예제

### 1. 빈 등록 대상 정의 (자동 컴포넌트 스캔 및 계층형 어노테이션)
비즈니스 성격에 맞게 `@Service` 및 `@Repository` 어노테이션을 사용하여 빈으로 등록할 대상 클래스를 선언합니다. 이 어노테이션들은 내부적으로 메타-어노테이션 `@Component`를 포함하고 있어 컴포넌트 스캔 대상에 자동으로 잡힙니다.

* **Repository 레이어**:
```java
package eello.app.repository;

import eello.elpring.di.annotation.Repository;

@Repository
public class MemoryTodoRepository implements TodoRepository {
    // 자동 스캔되어 빈으로 등록됩니다. 빈 이름: "memoryTodoRepository"
}
```

* **Service 레이어**:
```java
package eello.app.service;

import eello.elpring.di.annotation.Service;
import eello.app.repository.TodoRepository;

@Service
public class TodoService {
    private final TodoRepository todoRepository;

    // 생성자 주입 방식으로 의존성이 자동 조립됩니다.
    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }
}
```

### 2. 수동 빈 등록 (`@Configuration` & `@Bean`)
외부 라이브러리 객체나 정교한 빈 생성 설정이 필요한 경우 `@Configuration` 클래스와 `@Bean` 메서드를 사용하여 빈을 수동 등록할 수 있습니다.

```java
package eello.app.config;

import eello.elpring.di.annotation.Bean;
import eello.elpring.di.annotation.Configuration;
import eello.app.repository.TodoRepository;
import eello.app.repository.MemoryTodoRepository;

@Configuration
public class AppConfig {

    @Bean
    public TodoRepository customTodoRepository() {
        return new MemoryTodoRepository();
    }
}
```

### 3. 동적 설정 클래스 등록 (`registerCustomConfiguration`)
컴포넌트 스캔 대상 이외의 경로에 존재하는 설정 클래스나 프레임워크 확장 클래스를 컨텍스트 구동 시점에 동적으로 추가할 수 있습니다.

```java
AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("eello.app");
context.registerCustomConfiguration(List.of(ExternalConfig.class));
context.refresh();
```

---

## 🚫 제약 사항 및 예외 처리 규약

### 1. 프레임워크 제약 사항
- **빈 정의 수집**: 클래스 상에 명시된 `@Component` 및 이를 내부적으로 메타-어노테이션으로 가지고 있는 `@Service`, `@Repository`, `@Controller` 등과 `@Configuration` 클래스의 `@Bean` 메서드를 수집합니다.
- **의존성 주입 방식**: 오직 **생성자 주입(Constructor Injection)** 및 **팩토리 메서드 파라미터 주입**만 제공합니다. 필드 주입이나 세터 주입은 지원하지 않습니다.
- **단일 생성자 가정**: 컴포넌트 스캔 대상 클래스는 오직 1개의 public 생성자만 가지고 있다고 가정합니다.
- **다형성 상속 지원**: 단순 클래스 타입 매핑 외에 구현하는 인터페이스 계층 및 상위 슈퍼클래스 계층을 순회하여 상위 타입 명세로도 조회(`getBean`)가 가능합니다.
- **다중 빈 주입 제약 (Map)**: Map 타입으로 다중 빈을 주입받을 경우, Key의 타입은 반드시 `String`(빈 이름)이어야 합니다.
- **빈 스코프**: 항상 싱글톤(`SINGLETON`) 스코프만 작동합니다.

### 2. 주요 예외 정의 및 발생 조건
- **`BeanCurrentlyInCreationException`**: 재귀적 DFS 인스턴스 생성 루프 중 생성 대기 중 상태의 동일 빈 조회를 재시도 시(순환 참조 발생) 발생합니다.
- **`NoUniqueBeanDefinitionException`**: 
  - 특정 타입에 여러 빈이 등록되어 결정을 내려야 하는데 `@Primary` 지정 빈이 없고, 생성자 파라미터 변수명과 일치하는 이름을 가진 빈도 없을 때 발생합니다.
  - 하나의 타입에 `@Primary` 어노테이션이 중복 선언된 빈이 2개 이상일 때 발생합니다.
- **`NoSuchBeanDefinitionException`**: 존재하지 않는 이름 또는 타입의 빈을 컨테이너에 요청하거나, 의존성 관계에서 필요한 빈 정의를 찾을 수 없을 때 발생합니다.
- **`BeanDefinitionStoreException`**: 동일한 이름의 다른 빈 메타데이터를 추가로 등록하려 하거나, 동일한 이름으로 여러 개가 충돌할 때 발생합니다.
- **`BeanCreationException`**: 리플렉션을 통한 인스턴스화 및 팩토리 메서드 호출 실패, 또는 다중 빈 컬렉션의 자료구조 생성 실패 시 발생합니다.

---

## 🔍 주요 API 레퍼런스

`elpring-di` 컨테이너의 최상위 컨텍스트 구현체(`ApplicationContext`) 및 하위 빈 팩토리(`ListableBeanFactory`)는 다음과 같은 향상된 빈 쿼리 API들을 표준 인터페이스로 제공합니다.

### 1. 타입 기반 다중 빈 조회
등록된 빈들 중 특정 인터페이스 혹은 상위 타입을 상속/구현한 모든 빈의 목록과 이름을 조회할 수 있습니다.

- **`String[] getBeanNamesForType(Class<?> type)`**: 특정 타입의 모든 빈 이름 목록을 반환합니다.
- **`String[] getBeanNamesForType(ResolvableType type)`**: 제네릭 정보(ResolvableType)를 고려하여 완벽히 일치하는 빈 이름 목록을 반환합니다.
- **`<T> Map<String, T> getBeansOfType(Class<T> type)`**: 특정 타입을 구현한 빈들의 이름을 Key, 싱글톤 객체 인스턴스를 Value로 하는 Map을 반환합니다.

### 2. 어노테이션 기반 빈 조회
특정 어노테이션(메타-어노테이션 포함)을 가진 모든 빈 목록을 한 번에 조회합니다.

- **`Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType)`**: 주어진 어노테이션을 직접 가지고 있거나 메타-어노테이션으로 가진 모든 빈들을 Map 형태로 반환합니다.

---

## 🧪 테스트 코드 및 검증 전략

`elpring-di`는 견고한 품질 유지를 위해 **인터페이스 규약 테스트(Contract Test)** 패턴을 채택하고 있으며, 테스트 전용 피스처를 완벽하게 격리해 운영 중입니다.

자세한 테스트 수행 방법과 아키텍처 전략에 대해서는 테스트 경로 아래의 **[TESTING.md](file:///Users/jongseong/01.%20Projects/elpring-framework/elpring-di/src/test/TESTING.md)** 문서를 참조하십시오.
