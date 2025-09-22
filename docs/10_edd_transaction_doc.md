# 🎤 콘서트 예약 서비스

## 📝 프로젝트 개요

이 프로젝트는 콘서트 예약 서비스를 위한 대기열 시스템을 설계하는 것입니다.
사용자는 대기열에 등록하고, 좌석을 점유하며, 결제를 통해 좌석 소유권을 획득할 수 있습니다.
이 시스템은 동시성 제어, 상태 기반 흐름 제어, 무결성 보장 등을 고려하여 설계하였습니다.

## 📚 설계 문서
- [요구사항 분석](1_requirements.md)
- [ERD](2_erd.md)
- [시퀀스 다이어그램](3_sequence_diagram.md)
- [상태 다이어그램](4_state_diagram.md)
- [API 명세](https://joyseohee.github.io/hhplus-concert-server)
- [DB 성능 보고서](5_db_report.md)
### 동시성 문제 보고서
- [동시성 이슈 : DB Lock 보고서](6_db_race_condition_report.md)
- [동시성 이슈 : 분산락 보고서](7_redis_distributed_lock_report.md)
### 캐시 전략 보고서
- [캐시 전략 보고서](8_redis_cash_report.md)
### 레디스 저장소 보고서
- [자료구조를 활용한 랭킹 시스템, 비동기 시스템 설계 및 구현 보고서](9_redis_datastructure.md)
- [도메인 분리 이후 트랜잭션 처리 전략 및 한계 분석](10_edd_transaction_doc.md)
- [이벤트 호출 로직 시퀀스 다이어그램](11_event_call_sequence_diagram.md)
### 카프카 설계 문서
- [카프카 기초 학습 및 활용](12_kafka.md)
### 부하테스트
- [카프카 부하 테스트](13_load_test.md)


---

# 📄 STEP 16: Transaction Diagnosis 문서

> **도메인 분리 이후의 트랜잭션 처리 전략 및 한계 분석**

---

## 🧩 배경

이전 단계(STEP 15)에서는 **Application Event**를 통해
메인 비즈니스 로직과 외부 전송 로직(mock API 호출)을 **논리적으로 분리**하였습니다.

```kotlin
// 주요 이벤트 처리 예시
@Async("eventExecutor")
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
fun handle(event: ConfirmReservationEvent) {
    println("SendReservationDataEventHandler called with event: $event")
    sendReservationDataUseCase.execute(
        reservationId = event.reservationId,
        userId = event.userId,
    )
}
```

하지만, 서비스 확장에 따라 다음과 같은 변화가 예상됩니다:

| 변화 항목   | 내용                                        |
| ------- | ----------------------------------------- |
| 서비스 분리  | 예약, 사용자, 결제, 정산 등 도메인이 독립 마이크로서비스로 나뉨     |
| DB 분리   | 각 도메인은 **별도의 DB**를 가지며 물리적으로 분산됨          |
| 트랜잭션 분리 | 기존의 단일 DB 트랜잭션이 **여러 DB로 분산**, 원자성 보장 불가능 |

---

## ⚠️ 분산 트랜잭션의 한계

### 1. **원자성(Atomicity) 보장 불가**

* 도메인 간 트랜잭션이 분리되어, **일부만 성공하고 일부는 실패**할 수 있음
* 예: `예약 저장 성공` 후 `포인트 차감 실패` → 데이터 정합성 깨짐

### 2. **Rollback 불가**

* 이벤트 기반 호출은 이미 **비동기적으로 외부 시스템으로 전파**되므로
* 원 트랜잭션에서 문제가 생겨도 **자동으로 되돌릴 수 없음**

---

## 🛠️ 해결 전략

### ✅ 1. **작업 단위 중심 설계**

> **하나의 트랜잭션** 대신 **하나의 유즈케이스 단위의 정합성 확보**로 전환

* 트랜잭션의 개념을 **DB의 rollback 범위**에서 → **비즈니스 단위로 확대**
* 이벤트 기반으로 상태 전이를 관리하고, 각 단계의 성공/실패를 **event-driven으로 보완**

📌 예:
예약 확정 → 잔액 차감 → 외부 데이터 전송 → 알림 전송
→ 각 단계별 이벤트로 발행하며 상태 전이 추적
→ 로깅 등을 남겨 각 단계의 성공/실패를 기록

---

### ✅ 2. **보상 트랜잭션**

> 실패 시 이전 단계 작업을 **취소하는 방식**으로 일관성 확보

* ex) 포인트 차감 실패 → 예약 상태를 `FAILED`로 변경하는 보상 흐름 설계

📌 구현 방식 예:

```kotlin
@TransactionalEventListener
fun onPointDeductionFailed(event: PointDeductFailedEvent) {
    reservationService.markAsFailed(event.reservationId)
}
```

---

### ✅ 3. **이벤트 큐 + 재처리 전략**

> 메시지 기반 비동기 처리에서 **오류/장애 발생 시 재시도 가능성 확보**

* Kafka, RabbitMQ, Redis Streams 등 메시지 큐 사용
* Dead Letter Queue (DLQ) 및 재처리 정책 설정

---

## 🧪 테스트 전략

* 분산 트랜잭션 상황을 Mock 또는 Testcontainers로 시뮬레이션
* `예약 성공 후 포인트 차감 실패`, `포인트 성공 후 예약 실패` 등의 케이스별 정합성 확인

---

## 🔍 한계와 고려사항

| 항목     | 고려사항                                     |
| ------ | ---------------------------------------- |
| 일관성 모델 | 최종적 일관성(Eventual Consistency)을 수용할 준비 필요 |
| 장애 처리  | 메시지 중복 처리, 재처리, 순서 보장 등 고려               |
| 모니터링   | 분산 환경에서는 상태 추적이 어려우므로, 이벤트 추적 시스템 필요     |
| 오퍼레이션  | 운영자가 보상 트랜잭션/재시도를 수동으로 제어할 수 있는 시스템 필요   |

---

## ✅ 결론

* 분산 환경에서 **"하나의 DB 트랜잭션"은 유효하지 않음**
* 트랜잭션의 책임은 **"DB"가 아닌 "서비스 흐름과 이벤트 설계"로 전가**되어야 할 것으로 보임
* Eventual Consistency(즉시 일관성을 보장하기보다 최종적으로 일관성을 보장)와 보상 트랜잭션, 작업 단위 중심 아키텍처는 실전적인 대안
* **도메인 간 느슨한 결합 + 이벤트 기반 상태 전이 설계**가 핵심