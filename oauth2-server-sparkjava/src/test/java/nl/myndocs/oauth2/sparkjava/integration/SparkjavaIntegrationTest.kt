package nl.myndocs.oauth2.sparkjava.integration

import nl.myndocs.oauth2.integration.BaseIntegrationTest
import nl.myndocs.oauth2.sparkjava.Oauth2Server
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import spark.Spark

class SparkjavaIntegrationTest : BaseIntegrationTest() {
    @BeforeEach
    fun before() {
        Spark.port(0)

        Oauth2Server.configureOauth2Server{
            configBuilder(this)
        }

        Spark.awaitInitialization()

        localPort = Spark.port()
    }

    @AfterEach
    fun after() {
        Spark.stop()

        Spark.awaitStop()
    }
}