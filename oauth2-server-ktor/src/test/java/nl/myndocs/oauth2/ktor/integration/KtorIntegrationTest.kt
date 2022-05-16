package nl.myndocs.oauth2.ktor.integration

import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import nl.myndocs.oauth2.integration.BaseIntegrationTest
import nl.myndocs.oauth2.ktor.feature.Oauth2ServerFeature
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.net.BindException
import java.util.concurrent.TimeUnit


class KtorIntegrationTest : BaseIntegrationTest() {

    var server: NettyApplicationEngine? = null

    @BeforeEach
    fun before() {
        for (port in 49152..65535) {
            localPort = port
            try {
                server = embeddedServer(Netty, port = localPort!!) {
                    install(Oauth2ServerFeature) {
                        configBuilder(this)
                    }
                }

                server!!.start(false)
                break
            } catch (e: BindException) {
                e.printStackTrace()
            }
        }
    }

    @AfterEach
    fun after() {
        server!!.stop(0, 10000)
    }

}