# Reservation Service 아키텍처

## 개요

본 프로젝트는 콘서트 좌석 예약 시스템의 백엔드 서비스입니다.
Kotlin, Spring Boot, Gradle을 기반으로 개발되었으며, 주요 도메인은 대기열 토큰, 유저 잔고, 콘서트, 좌석 등입니다.
**단기간 개발과 점진적 확장을 전제로, 실용성과 명확한 책임 분리를 동시에 고려한 설계**를 따릅니다.

---

## 1. 계층 구조

### Controller (API Layer)

* HTTP 요청을 수신하고 적절한 서비스에 위임합니다.
* 토큰(`Queue-Token` 헤더)을 추출하고, 검증 결과인 `userId`를 서비스로 전달합니다.
* 흐름 제어 책임만 갖고 비즈니스 로직은 포함하지 않습니다.

### Service (Usecase Layer)

* 각 유즈케이스(콘서트 목록 조회, 좌석 조회, 좌석 점유, 예약 확정 등)에 대응하는 **단일 책임의 서비스 클래스**로 구성됩니다.
* 각 서비스는 **자신의 책임만 수행**하며, **서비스 간 직접 참조를 하지 않습니다**.
* 서비스 내부에 `Input`, `Output` 클래스를 정의하여 **DTO와 도메인 간 완충지대 역할을 수행**합니다.

### Validation Service (Cross-cutting Concern)

* `ValidateQueueTokenService`는 공통 인증 로직을 분리한 서비스로,

  * 토큰의 유효성/만료 여부 확인
  * 토큰 상태(`ACTIVE`) 확인
  * `userId` 추출
    등의 책임을 가집니다.
* 컨트롤러나 각 유즈케이스 서비스에서 의존성 주입을 통해 사용됩니다.

### Repository (Persistence Abstraction Layer)

* 각 도메인 객체의 저장/조회 책임을 갖습니다.
* **인터페이스 + 구현체 구조**로 설계되어 있으며,

  * 초기에는 InMemory
  * 추후 JPA, Redis 등으로 확장 가능한 구조입니다.
* 메서드 네이밍은 JPA 스타일(`findById`, `save` 등)을 따르되,
  **예외 처리는 하지 않고**, 비즈니스 레이어에서 처리되도록 위임합니다.

---

## 2. 토큰 검증 흐름

```plaintext
HTTP 요청 → Controller
              └─> ValidateQueueTokenService
                     └─> 유효성 확인 + userId 추출
                           └─> Service (userId와 함께 비즈니스 로직 수행)
```

* 모든 요청은 **토큰 검증 → 사용자 식별(userId) → 유즈케이스 수행**의 흐름을 따릅니다.

---

## 3. 패키지 구조 예시

```
kr.hhplus.be.server
├── controller
│   └── ReservationController.kt
├── service
│   ├── validation
│   │   └── ValidateQueueTokenService.kt
│   ├── ListConcertService.kt
│   ├── ListSeatService.kt
│   ├── HoldSeatService.kt
│   └── ConfirmReservationService.kt
├── domain
│   ├── QueueToken.kt
│   ├── UserBalance.kt
│   ├── UserBalanceTable.kt // repository 구현체
│   └── ...
├── repository
│   ├── QueueTokenRepository.kt
│   ├── SeatRepository.kt
│   └── ...
├── support
│   └── (예외 처리, 시간 헬퍼 등)
```

---

## 4. 설계 원칙 및 의도

### ✅ 단일 책임 & 유즈케이스 중심

* 유즈케이스별로 서비스를 분리하여 책임을 명확히 구분
* API 수는 많지 않지만 하나의 유즈케이스에서 여러 개의 도메인과 리포지토리를 참조하는 일이 많아 서비스 레이어를 유즈케이스별로 나눔
* 공통 관심사(토큰 검증 등)는 별도 서비스로 추출하여 중복 제거 및 재사용성 확보
* 각 서비스는 **자신의 유즈케이스에만 집중**하고, 다른 서비스에 의존하지 않도록 설계
* 프레젠테이션 레이어 -> 서비스 레이어 -> 도메인 레이어로의 명확한 책임 분리

### ✅ 레이어 침투 방지

* 서비스 내부에서 정의한 `Input`, `Output` 객체를 통해 DTO와 도메인 간 계층 간섭 최소화
* 외부 요청/응답 객체가 도메인이나 내부 서비스 로직까지 침투하지 않도록 설계

### ✅ 점진적 확장 고려

* Repository는 인터페이스 기반으로 정의되어 있어, InMemory → JPA → Redis 등으로 확장 용이
* Repository에서는 예외를 발생시키지 않고, 비즈니스 로직을 담당하는 서비스에서 명시적으로 처리
* Repository 메서드는 JPA 스타일의 네이밍을 따르고 예외 처리는 하지 않음

### ✅ 실용 중심, 과도한 추상화 지양

* 현재 프로젝트 규모에 비해 과한 계층 분리(예: 파사드 도입, 도메인 서비스 분리 등)는 지양
* 단기간의 개발 효율성과 테스트 용이성을 우선 고려

---

## 5. 테스트 전략

* 도메인 객체는 주요한 도메인 객체에 한해서 **단위 테스트** 로 검증
* 유즈케이스 서비스는 **실제 객체를 활용한 경량 통합 테스트** 로 도메인 간의 상호작용 및 비즈니스 로직 검증
* 컨트롤러는 **MockMvc를 활용한 API 테스트**로 HTTP 요청/응답 흐름 검증
* 테스트 도구: **Kotest + Mockk + Spring Boot Test**

---

## 6. 기술 스택

* Kotlin / Java
* Spring Boot
* Gradle (Kotlin DSL)
* Kotest
* (추후) JPA / Redis / Kafka 등으로 확장 고려

---

## 7. 향후 개선 고려 사항

* 공통 예외 처리(`@ControllerAdvice`, `ErrorResponse`) 적용
* 예약 확정 및 좌석 점유의 동시성 이슈 개선
* 인메모리 적용으로 별도의 트랜잭션 처리는 이후 DB 접목 후 처리할 예정
