package kr.hhplus.be.server.config

import kr.hhplus.be.server.presentation.CurrentUserResolver
import kr.hhplus.be.server.presentation.ValidateInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private final val validateInterceptor: ValidateInterceptor,
    private final val currentUserResolver: CurrentUserResolver
) : WebMvcConfigurer {

    override fun addInterceptors(reg: InterceptorRegistry) {
        reg.addInterceptor(validateInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns(
                "/static/**",
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html"
            )
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers += currentUserResolver
    }
}