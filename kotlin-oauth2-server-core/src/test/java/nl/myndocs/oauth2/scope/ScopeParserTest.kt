package nl.myndocs.oauth2.scope

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test

internal class ScopeParserTest {
    @Test
    fun nullShouldResultInEmptySet() {
        assertThat(
                ScopeParser.parseScopes(null),
                `is`(empty())
        )
    }

    @Test
    fun emptyStringShouldResultInEmptySet() {
        assertThat(
                ScopeParser.parseScopes(""),
                `is`(empty())
        )
    }

    @Test
    fun setShouldBeSeparatedBySpace() {
        assertThat(
                ScopeParser.parseScopes("foo bar"),
                `is`(equalTo(setOf("foo", "bar")))
        )
    }
}