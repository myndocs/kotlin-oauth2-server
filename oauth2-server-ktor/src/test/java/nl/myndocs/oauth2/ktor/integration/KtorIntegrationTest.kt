package nl.myndocs.oauth2.ktor.integration

import io.ktor.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import nl.myndocs.oauth2.integration.BaseIntegrationTest
import nl.myndocs.oauth2.ktor.feature.Oauth2ServerFeature
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.util.concurrent.TimeUnit


class KtorIntegrationTest : BaseIntegrationTest() {
    val server = embeddedServer(Netty, port = 50000) {
        install(Oauth2ServerFeature) {
            configBuilder(this)
        }
    }

    @BeforeEach
    fun before() {
        server.start(false)

        localPort = 50000
    }

    @AfterEach
    fun after() {
        server.stop(0, 10, TimeUnit.SECONDS)
    }

}