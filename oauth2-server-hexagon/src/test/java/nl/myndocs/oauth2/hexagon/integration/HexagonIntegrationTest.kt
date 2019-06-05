package nl.myndocs.oauth2.hexagon.integration

import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerPort
import com.hexagonkt.http.server.jetty.JettyServletAdapter
import com.hexagonkt.injection.InjectionManager.bindObject
import nl.myndocs.oauth2.hexagon.enableOauthServer
import nl.myndocs.oauth2.integration.BaseIntegrationTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

internal class HexagonIntegrationTest : BaseIntegrationTest() {
    val server: Server by lazy {
        Server {
            // @TODO: open random port?
            enableOauthServer { configBuilder(this) }
        }
    }

    @BeforeEach
    fun before() {
        bindObject<ServerPort>(JettyServletAdapter())
        server.start()

        localPort = server.bindPort
    }

    @AfterEach
    fun after() {
        server.stop()
    }
}
