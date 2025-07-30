package kr.hhplus.be.server.presentation

import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class CurrentUserResolver(
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(param: MethodParameter) =
        param.hasParameterAnnotation(CurrentUser::class.java)
                && param.parameterType == Long::class.java

    override fun resolveArgument(
        param: MethodParameter,
        mav: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        return (webRequest.getAttribute("currentUserId", SCOPE_REQUEST) as? Long)
            ?: throw IllegalArgumentException("인증 정보 없음")
    }
}