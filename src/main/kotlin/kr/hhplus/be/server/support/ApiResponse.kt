package kr.hhplus.be.server.support

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "API 응답 형식")
data class ApiResponse<T> (
    @Schema(description = "응답 코드", example = "SUCCESS")
    val code: String,

    @Schema(description = "응답 메시지", example = "요청이 성공했습니다.")
    val message: String,

    @Schema(description = "응답 데이터")
    val data: T? = null
)