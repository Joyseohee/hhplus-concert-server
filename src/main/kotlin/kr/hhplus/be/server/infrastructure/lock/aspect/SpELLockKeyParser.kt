package kr.hhplus.be.server.infrastructure.lock.aspect

import org.springframework.core.DefaultParameterNameDiscoverer
import org.springframework.expression.ExpressionParser
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.reflect.MethodSignature

class SpELLockKeyParser(
	private val parser: ExpressionParser = SpelExpressionParser(),
	private val nameDiscoverer: DefaultParameterNameDiscoverer = DefaultParameterNameDiscoverer()
) {
	fun parse(jp: JoinPoint, spel: String): String {
		val signature = jp.signature as MethodSignature
		val method = signature.method

		val paramNames = nameDiscoverer.getParameterNames(method) ?: emptyArray()
		val args = jp.args

		val context = StandardEvaluationContext()
		for (i in paramNames.indices) {
			context.setVariable(paramNames[i], args[i])
		}
		return parser.parseExpression(spel).getValue(context, String::class.java)
			?: throw IllegalArgumentException("락 획득을 위한 키 SpEL가 없습니다. : $spel")
	}
}
