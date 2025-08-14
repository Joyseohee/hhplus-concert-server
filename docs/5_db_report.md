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


## 🚀 DB 성능 최적화 보고서

* **조회 성능 저하가 발생할 수 있는 기능을 식별**하고
* 원인을 분석하여 **쿼리 재설계 또는 인덱스 설계 등 최적화 방안을 제안**하고자 함
* 성능 비교를 위한 테스트 데이터는 아래와 같이 구성함

| 테이블명            | 삽입 건수      | 비고                               |
| --------------- | ---------- |----------------------------------|
| `concerts`      | 10건        | 단건 삽입, 루프                        |
| `user_balances` | 100,000건   | ID 기준 순차 삽입               |
| `seats`         | 100,000건   | ID 기준으로 순차 삽입                 |
| `seat_holds`    | 약 100,000건 | ID 기준 순차 삽입                      |
| `reservations`  | 약 100,000건 | `seat_id` + `user_id` 순차 조합으로 배치 |
| `tokens`        | 100,000건   | `user_id` 기반 순차 삽입, 상태 무작위       |


## 1. 병목 예상 쿼리 및 실행 계획 분석

### 1.1 대기열 토큰 순번 계산

```sql
SELECT COUNT(*) 
  FROM tokens 
 WHERE status = 'WAITING' 
   AND token_id < :myId;

SELECT COUNT(*) 
  FROM tokens 
 WHERE status = 'ACTIVE';
```

### 1.1.1 `EXPLAIN ANALYZE` 결과 요약

| 쿼리                                                                                   | 실제 시간          | 주요 원인                                 |
| ------------------------------------------------------------------------------------ | -------------- | ------------------------------------- |
| `COUNT(*) … WHERE status='WAITING' AND token_id<10153`                               | 0.743 ms       | `PRIMARY` 인덱스(range scan) 만으로도 충분히 빠름 |
| `COUNT(*) … WHERE status='ACTIVE'`                                                   | 2.30 ms        | 전체 테이블 스캔 → 높은 비용 발생                  |
| `SELECT * FROM tokens … WHERE status='WAITING' AND expires_at>… ORDER BY created_at` | 1.9 ms (정렬 포함) | 테이블 스캔 후 정렬, 복합 인덱스 적용해도 큰 개선 없음      |

* **총평**:

    * 대기 순번을 조회하는 쿼리는 `WAITING` 이미 `PRIMARY(token_id)` 인덱스로 충분히 빠른 편
    * `ACTIVE` 카운트는 단일 인덱스(`status`) 추가로 일부 개선 가능하나, 여전히 테이블 스캔 이슈가 남음
    * `ORDER BY created_at` 정렬 쿼리는 복합 인덱스(`status, expires_at, created_at` 혹은 `expires_at, status, created_at`)를 적용해 보았으나, **정렬 비용** 때문에 의미 있는 개선이 이루어지지 않음
    * **개선 여지**가 제한적이므로, SQL 최적화에 의존하기보다는 **Redis** 등의 인메모리 구조로 순번/정렬 로직을 이전하는 것이 실질적 대안


### 1.2 좌석 점유 조회

```sql
EXPLAIN ANALYZE
SELECT *
  FROM seat_holds sh
 WHERE sh.user_id    = :uid
   AND sh.seat_id    = :sid
   AND sh.expires_at > :now;
```

* **기존 인덱스(u­nq\_seat\_concert on `seat_id`)**:

    * 인덱스 스캔 후 추가 필터 → 2.66 ms
* **복합 인덱스** (`seat_id, user_id, expires_at` 또는 `user_id, seat_id, expires_at`):

    * 인덱스 range scan만으로 → 0.08 ms (≈33× 개선)
    * 카디널리티가 더 높은 user_id를 앞에 두는 것이 더 효율적이었음

```sql
-- (1) 복합 인덱스: (seat_id, user_id, expires_at)
CREATE INDEX idx_seat_user_expiry ON seat_holds (seat_id, user_id, expires_at);

EXPLAIN ANALYZE
SELECT * FROM seat_holds sh
 WHERE sh.seat_id    = 4
   AND sh.user_id    = 5
   AND sh.expires_at > '2025-07-31 14:59:25.370000';
```

```
-- 실행 계획 (idx_seat_user_expiry)
Index range scan on idx_seat_user_expiry  (cost=0.71 rows=1) (actual time=0.089..0.094 rows=1 loops=1)
```

```sql
-- (2) 복합 인덱스: (user_id, seat_id, expires_at)
CREATE INDEX idx_seat_user_expiry ON seat_holds (user_id, seat_id, expires_at);

EXPLAIN ANALYZE
SELECT * FROM seat_holds sh
 WHERE sh.user_id    = 5
   AND sh.seat_id    = 4
   AND sh.expires_at > '2025-07-31 14:59:25.370000';
```

```
-- 실행 계획 (idx_seat_user_expiry)
Index range scan on idx_seat_user_expiry  (cost=0.71 rows=1) (actual time=0.0717..0.0766 rows=1 loops=1)
```


### 1.3 전체 테이블 스캔 확인

```sql
EXPLAIN ANALYZE
SELECT * FROM seat_holds;
```

* **결과**: 테이블 스캔으로 110 ms 소요
* **원인**: 대용량 데이터 처리 시, 풀 스캔은 메모리·I/O 비용 폭증
* **대안**:

    * **범위 한정**: 필요한 열만 조회하도록 비즈니스 로직 수정(concert_id, seat_id로 조회 범위 제한)
    * **레디스 전환**: 추후 좌석 점유 상태를 Redis로 캐싱하여 조회 비용 절감
      * 좌석 점유 데이터는 TTL 조회가 중요하고
      * 잦은 조회가 발생하는 반면
      * 반대로 히스토리는 중요하지 않은 데이터이다.
      * 관계형 DB에 저장하기 적합하지 않은 데이터로
      * 추후 redis로 전환이 필요하다

---

## 2. 최적화 방안 제시

### 2.1 대기열 토큰 순번 최적화

1. **Redis  전환**

    * **장점**: 순번 계산에 많은 비용이 들지 않으며, TTL 기반으로 만료 관리가 용이


### 2.2 좌석 점유 조회 최적화

1. **복합 인덱스 적용**

   ```sql
   CREATE INDEX idx_seat_user_expiry 
     ON seat_holds(user_id, seat_id, expires_at);
   ```
   카디널리티가 높은 user_id를 가장 앞으로

2. **반정규화**

    * `seats.is_available` 컬럼 추가 + 이벤트 트리거로 유지하는 방법도 있으나
    * 데이터 정합성 문제가 발생할 수 있어 적용하지는 않음

4. **캐싱 / Materialized View**

    * MV 주기 리프레시 또는 Redis 활용

---

## 3. 인덱스 적용 전후 성능 비교

| 항목                                    | 기존 실행 시간 | 최적화 실행 시간   | 개선 배율 |
| ------------------------------------- | -------- | ----------- | ----- |
| SeatHold 조회 (단일 인덱스)                  | 2.66 ms  | —           | —     |
| SeatHold 조회 (복합 인덱스)                  | —        | 0.08 ms     | ×33   |
| 전체 SeatHold 테이블 스캔                    | 110 ms   | —           | —     |
| Token COUNT(Waiting) (`token_id` idx) | 0.74 ms  | —           | —     |
| Token COUNT(Active) (테이블 스캔)          | 2.30 ms  | —           | —     |
| Token 정렬 쿼리(`ORDER BY created_at`)    | 1.9 ms   | ≈1.8–4.0 ms | ×1    |

> **※** 토큰 정렬·카운트 쿼리는 인덱스 튜닝만으로는 근본적 개선 한계가 있으므로, **Redis** 전환이 권장됩니다.


---

# 4. 결론

* **SeatHold 조회**: `(user_id, seat_id, expires_at)` 복합 인덱스로 33배 성능 개선
* **전체 스캔 쿼리**: 부분 조회 등으로 개선
* **토큰 순번 계산**: SQL 최적화만으론 한계 → **Redis** or **카운터 테이블**
* **다음 단계**: 제안된 인덱스 적용 후 운영 환경에서 `EXPLAIN ANALYZE` 검증 및 부하 테스트 수행