package kr.hhplus.be.server.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaAuditing
@EntityScan("kr.hhplus.be.server.domain.model")
@EnableJpaRepositories("kr.hhplus.be.server.infrastructure.persistence.jpa")
class JpaConfig {

}
