package nl.myndocs.oauth2.javalin.integration

import io.javalin.Javalin
import nl.myndocs.oauth2.integration.BaseIntegrationTest
import nl.myndocs.oauth2.javalin.enableOauthServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

class JavalinIntegrationTest : BaseIntegrationTest() {

    private val server: Javalin = Javalin.create()
            .apply {
                enableOauthServer {
                    configBuilder(this)
                }
            }

    @BeforeEach
    fun before() {
        server.start(0)

        localPort = server.port()
    }

    @AfterEach
    fun after() {
        server.stop()
    }
}