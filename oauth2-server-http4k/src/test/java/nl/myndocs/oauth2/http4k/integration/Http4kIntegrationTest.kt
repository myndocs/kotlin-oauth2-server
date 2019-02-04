package nl.myndocs.oauth2.http4k.integration

import nl.myndocs.oauth2.http4k.`enable oauth2`
import nl.myndocs.oauth2.integration.BaseIntegrationTest
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.routes
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

class Http4kIntegrationTest : BaseIntegrationTest() {
    val server = routes(*emptyArray<RoutingHttpHandler>())
            .let { it `enable oauth2` { configBuilder(this) } }
            .let { it.asServer(Jetty(50000)) }

    @BeforeEach
    fun before() {
        server.start()
    }

    @AfterEach
    fun after() {
        server.stop()
    }
}