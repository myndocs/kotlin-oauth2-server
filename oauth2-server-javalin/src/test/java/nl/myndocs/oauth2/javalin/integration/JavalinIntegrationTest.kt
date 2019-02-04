package nl.myndocs.oauth2.javalin.integration

import io.javalin.Javalin
import nl.myndocs.oauth2.integration.BaseIntegrationTest
import nl.myndocs.oauth2.javalin.enableOauthServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

class JavalinIntegrationTest : BaseIntegrationTest() {

    val server = Javalin.create()
            .apply {
                enableOauthServer {
                    configBuilder(this)
                }
            }
            .port(50000)

    @BeforeEach
    fun before() {
        server.start()
    }

    @AfterEach
    fun after() {
        server.stop()
    }
}