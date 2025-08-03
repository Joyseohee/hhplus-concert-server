package kr.hhplus.be.server.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class TestWebConfig : WebMvcConfigurer {

	override fun addInterceptors(registry: InterceptorRegistry) {
		// 인터셉터 생략
	}

	override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
		// 리졸버 생략
	}
}