package nl.myndocs.oauth2.sparkjava.integration

import nl.myndocs.oauth2.integration.BaseIntegrationTest
import nl.myndocs.oauth2.sparkjava.Oauth2Server
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import spark.Spark

class SparkjavaIntegrationTest : BaseIntegrationTest() {
    @BeforeEach
    fun before() {
        Spark.port(50000)
        Oauth2Server.configureOauth2Server {
            configBuilder(this)
        }

        Spark.awaitInitialization()
    }

    @AfterEach
    fun after() {
        Spark.stop()

        Spark.awaitStop()
    }
}