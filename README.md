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
   - 클래스 상에 명시된 어노테이션을 감지하고 메타-어노테이션 분석을 통해 빈 등록 대상을 추출합니다.
2. **생성자 기반 의존성 주입 (Constructor Injection)**:
   - 빈 생성 시점에 생성자 파라미터 타입을 분석하고, 필요한 의존 빈들을 컨테이너 내부에서 조회하여 결합합니다.
3. **Eager & Lazy 초기화 지원**:
   - 일반 빈은 애플리케이션 시작 단계(`refresh`)에서 즉시 사전 생성(Pre-instantiation)됩니다.
   - [@Lazy](file:///Users/jongseong/01.%20Projects/elpring/elpring-di/src/main/java/eello/elpring/di/annotation/Lazy.java)가 지정된 빈은 시작 시점에는 생성되지 않으며, 실제 빈이 요청되거나 참조되는 최초 시점에 지연 생성됩니다.
4. **다중 구현체 해결 및 우선순위 지정 (`@Primary`)**:
   - 인터페이스에 대한 구현체가 여러 개 존재할 때, [@Primary](file:///Users/jongseong/01.%20Projects/elpring/elpring-di/src/main/java/eello/elpring/di/annotation/Primary.java) 어노테이션이 붙은 빈을 최우선적으로 매핑 및 주입합니다.
5. **실시간 순환 참조 감지 (Circular Dependency Detection)**:
   - 깊이 우선 탐색(DFS) 방식의 빈 생성 과정에서 객체가 완전히 인스턴스화되기 전에 다시 본인에게 의존성 요청이 들어오는 현상을 실시간으로 모니터링하여, 무한 루프에 빠지기 전 즉시 예외를 발생시키고 안전하게 실행을 중단합니다.

---

## 📂 프로젝트 패키지 구조

```
src/main/java/
└── eello/
    └── elpring/
        └── di/
            ├── annotation/
            │   ├── Component.java                  # 빈 등록 마킹 어노테이션
            │   ├── Primary.java                    # 동일 타입 다중 빈 충돌 해결 우선순위 지정
            │   └── Lazy.java                       # 지연 초기화 지정
            ├── beans/
            │   ├── BeanDefinition.java             # 빈의 메타데이터 규약 인터페이스
            │   ├── DefaultBeanDefinition.java      # 리플렉션을 통해 메타데이터를 추출 및 저장하는 구현체
            │   ├── BeanDefinitionHolder.java       # 빈 이름과 BeanDefinition의 홀더
            │   ├── BeanScope.java                  # 빈의 스코프 규정 (싱글톤만 지원)
            │   └── factory/
            │       ├── BeanFactory.java            # 빈 조회를 위한 루트 인터페이스
            │       ├── ListableBeanFactory.java    # 타입별 빈 조회 기능을 포함하는 하위 인터페이스
            │       └── support/
            │           ├── BeanDefinitionRegistry.java # 빈 정보를 등록하기 위한 명세
            │           ├── DefaultListableBeanFactory.java # 빈의 보관, 매핑, DFS 인스턴스화 담당 핵심 엔진
            │           ├── ClassPathBeanDefinitionScanner.java # 리플렉션 기반 패키지 스캐너
            │           ├── BeanDefinitionReaderUtils.java # 레지스트리에 BeanDefinition 등록을 돕는 유틸
            │           └── registry/
            │               ├── SingletonBeanRegistry.java # 싱글톤 생명주기 관리 인터페이스
            │               └── DefaultSingletonBeanRegistry.java # 실시간 객체 캐시 저장 및 순환 참조 방지 상태 관리
            ├── boot/
            │   └── ElpringApplication.java         # 애플리케이션 시동부 부트스트랩 클래스
            ├── context/
            │   ├── ApplicationContext.java         # 최상위 컨텍스트 인터페이스
            │   ├── ConfigurableApplicationContext.java # 컨텍스트 생명주기 및 리프레시 명세
            │   ├── AbstractApplicationContext.java # refresh() 등의 초기화 템플릿 메서드 구현체
            │   ├── GenericApplicationContext.java  # BeanFactory 위임 및 설정 어댑터
            │   └── AnnotationConfigApplicationContext.java # 어노테이션 설정 기반 최상단 컨텍스트 구현체
            └── exception/
                ├── BeansException.java             # DI 전체 공통 예외 상위 클래스
                ├── NoSuchBeanDefinitionException.java # 요청한 빈 정의를 찾을 수 없을 때 발생
                ├── NoUniqueBeanDefinitionException.java # 다중 매핑 모호성 발생 시 발생
                ├── BeanCurrentlyInCreationException.java # 순환 참조 감지 시 발생
                └── BeanDefinitionStoreException.java # 동일 이름의 정의가 충돌하여 중복 등록 실패 시 발생
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
ClassPathBeanDefinitionScanner 패키지 스캔
  ├─ basePackage 하위의 모든 .class 파일 재귀 탐색
  ├─ @Component 어노테이션이 붙은 클래스를 추출
  └─ DefaultBeanDefinition.of(class)를 통해 메타데이터 생성 및 빈 레지스트리에 등록
  │
  ▼
AbstractApplicationContext.refresh() 수행
  ├─ 등록된 모든 BeanDefinition을 조회
  └─ @Lazy가 지정되지 않은 일반(Eager) 빈들에 대해 getBean(beanName) 호출하여 사전 인스턴스화
      │
      ▼ (instantiateBean - DFS 방식 재귀 인스턴스 생성)
   1. singletonCurrentlyInCreation 플래그에 현재 빈 등록 (순환 참조 방지 시작)
   2. 생성자 파라미터 타입을 분석해 getDependsOn() 의존 객체 파악
   3. 의존하는 각 타입에 대해 beanFactory.getBean(dependencyType)을 재귀적으로 호출
      └─ 만약 의존 대상이 아직 안 만들어졌다면 대상에 대해 getBean() 실행 (DFS 탐색)
      └─ 재귀 호출 도중 이미 singletonCurrentlyInCreation에 등록된 빈을 다시 참조하려고 시도하면,
         즉시 BeanCurrentlyInCreationException 던져 예외 차단
   4. 의존 객체들이 모두 정상 생성 완료되어 반환되면, 생성자 리플렉션 호출하여 인스턴스 생성
   5. 생성 완료된 객체를 DefaultSingletonBeanRegistry 캐시에 저장
   6. singletonCurrentlyInCreation 상태 해제
```

---

## 💻 사용 예제

### 1. 빈 등록 대상 정의
[@Component](file:///Users/jongseong/01.%20Projects/elpring/elpring-di/src/main/java/eello/elpring/di/annotation/Component.java) 어노테이션을 사용하여 빈으로 등록할 대상 클래스를 선언합니다.

```java
package eello.app.repository;

import eello.elpring.di.annotation.Component;

@Component
public class ARepository implements Repository {
    // 자동 스캔되어 빈으로 등록됩니다. 빈 이름: "aRepository"
}
```

### 2. 다중 구현체 처리 (`@Primary`)
동일한 인터페이스에 여러 구현체가 존재하는 경우, [@Primary](file:///Users/jongseong/01.%20Projects/elpring/elpring-di/src/main/java/eello/elpring/di/annotation/Primary.java)를 사용하여 주입 우선순위를 부여할 수 있습니다.

```java
package eello.app.service;

import eello.elpring.di.annotation.Component;
import eello.elpring.di.annotation.Primary;
import eello.app.repository.Repository;

@Component
@Primary
public class AService implements Service {
    private final Repository repository;

    // 생성자 주입
    public AService(Repository repository) {
        this.repository = repository;
    }
}
```

### 3. 애플리케이션 실행 및 사용
[ElpringApplication](file:///Users/jongseong/01.%20Projects/elpring/elpring-di/src/main/java/eello/elpring/di/boot/ElpringApplication.java)을 사용하여 컨테이너를 구동하고 필요한 빈을 꺼내어 사용할 수 있습니다.

```java
package eello.app;

import eello.elpring.di.boot.ElpringApplication;
import eello.elpring.di.context.ConfigurableApplicationContext;
import eello.app.service.Service;

public class SimpleDIContainer {
    public static void main(String[] args) {
        // 메인 애플리케이션 클래스의 패키지를 루트 삼아 컴포넌트 스캔 시작 및 기동
        ConfigurableApplicationContext context = ElpringApplication.run(SimpleDIContainer.class);

        // 인터페이스 타입을 기반으로 우선순위가 높은 (@Primary) 빈을 획득
        Service service = context.getBean(Service.class);
        System.out.println("조회된 빈: " + service.getClass().getSimpleName());
    }
}
```

---

## 🚫 제약 사항 및 예외 처리 규약

안정적인 경량 기동을 위해 다음과 같은 명확한 제약 사항과 예외를 가집니다:

### 1. 프레임워크 제약 사항
- **빈 정의 어노테이션**: 클래스 상에 선언된 [@Component](file:///Users/jongseong/01.%20Projects/elpring/elpring-di/src/main/java/eello/elpring/di/annotation/Component.java) 어노테이션 및 이를 메타-어노테이션으로 품은 커스텀 어노테이션만 수집합니다.
- **의존성 주입 방식**: 오직 **생성자 주입(Constructor Injection)**만 제공하며 필드나 세터 주입은 미지원합니다.
- **단일 생성자 가정**: 각 컴포넌트는 오직 1개의 public 생성자만 가지고 있다고 가정합니다.
- **다형성 상속 지원**: 단순 클래스 타입 매핑 외에 구현하는 인터페이스 계층 및 상위 슈퍼클래스 계층을 순회하여 상위 타입 명세로도 조회(`getBean`)가 가능합니다.
- **빈 스코프**: 항상 싱글톤(`SINGLETON`) 스코프만 작동합니다.

### 2. 주요 예외 정의 및 발생 조건
- **`BeanCurrentlyInCreationException`**: 재귀적 DFS 인스턴스 생성 루프 중 생성 대기 중 상태의 동일 빈 조회를 재시도 시(순환 참조 발생) 발생합니다.
- **`NoUniqueBeanDefinitionException`**: 
  - 특정 타입에 여러 빈이 등록되어 결정을 내려야 하는데 [@Primary](file:///Users/jongseong/01.%20Projects/elpring/elpring-di/src/main/java/eello/elpring/di/annotation/Primary.java) 지정 빈이 없을 때 발생합니다.
  - 하나의 타입에 `@Primary` 어노테이션이 중복 선언된 빈이 2개 이상일 때 발생합니다.
- **`NoSuchBeanDefinitionException`**: 존재하지 않는 이름 또는 타입의 빈을 컨테이너에 요청할 때 발생합니다.
- **`BeanDefinitionStoreException`**: 이미 등록된 고유 빈 이름과 충돌하는 다른 빈 메타데이터를 추가로 등록하려 할 때 발생합니다.

---

## 🧪 테스트 코드 및 검증 전략

`elpring-di`는 견고한 품질 유지를 위해 **인터페이스 규약 테스트(Contract Test)** 패턴을 채택하고 있으며, 테스트 전용 피스처를 완벽하게 격리해 운영 중입니다.

자세한 테스트 수행 방법과 아키텍처 전략에 대해서는 테스트 경로 아래의 **[TESTING.md](file:///Users/jongseong/01.%20Projects/elpring/elpring-di/src/test/TESTING.md)** 문서를 참조하십시오.
