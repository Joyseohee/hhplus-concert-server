package kr.hhplus.be.server.support.annotation


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RedisLock(
	/**
	 * SpEL 예: "seat:{#concertId}:{#seatId}"
	 */
	val key: String,
	// 락 대기 최대 시간(ms). 못 얻으면 재시도 등 실패 처리
	val waitTimeMs: Long = 3000,
	// 락 보유 시간(ms). 최악 수행시간보다 조금 크게
	val leaseTimeMs: Long = 5000,
	// 락 획득 실패 시 재시도 여부(true면 그냥 통과, false면 재시도)
	val failFast: Boolean = true,
	// 재시도 횟수
	val maxRetries: Int = 3,
	// 재시도 간격(ms)
	val retryDelayMs: Long = 1000,
)