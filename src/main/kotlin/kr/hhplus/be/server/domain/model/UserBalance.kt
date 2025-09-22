package kr.hhplus.be.server.domain.model

import jakarta.persistence.*
import kr.hhplus.be.server.infrastructure.persistence.jpa.BaseEntity

@Entity
@Table(name = "users")
class UserBalance private constructor(
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val userId: Long?,
	@Column(name = "balance", nullable = false)
	var balance: Long? = 0L
) : BaseEntity() {

	init {
		require(balance in MIN_POINT..MAX_POINT) {
			"잔고는 $MIN_POINT 이상 $MAX_POINT 이하여야 합니다. 현재: $balance"
		}
	}
	companion object {
		const val MIN_POINT = 0L
		const val MAX_POINT = 500_000L
		const val MIN_TRANSACTION_AMOUNT = 1L

		fun create(
			userId: Long? = null,
			balance: Long? = 0L
		): UserBalance {
			return UserBalance(userId = userId, balance = balance)
		}
	}

	// 충전 정책(외부에서 알 필요 없음)
	private fun validateChargePolicy(chargingBalance: Long) {
		require(chargingBalance >= MIN_TRANSACTION_AMOUNT) {  "충전 금액은 $MIN_TRANSACTION_AMOUNT 이상이어야 합니다. 현재: $chargingBalance" }
		require(balance!! + chargingBalance <= MAX_POINT) {"잔고는 ${MAX_POINT}를 초과할 수 없습니다. 현재 잔고: $balance, 충전 금액: $chargingBalance"}
	}

	// 사용 정책(외부에서 알 필요 없음)
	private fun validateUsePolicy(usingBalance: Long) {
		require(usingBalance >= MIN_TRANSACTION_AMOUNT) {  "사용 금액은 $MIN_TRANSACTION_AMOUNT 이상이어야 합니다. 사용 금액: $usingBalance" }
		require(balance!! >= usingBalance){ "잔고가 부족합니다. 현재 잔고 : $balance , 사용 금액 : $usingBalance" }
	}

	// balance 충전
	fun charge(amount: Long) {
		validateChargePolicy(amount)
		balance = balance!! + amount
	}

	// balance 사용
	fun use(amount: Long) {
		validateUsePolicy(amount)
		balance = balance!! - amount
	}
}