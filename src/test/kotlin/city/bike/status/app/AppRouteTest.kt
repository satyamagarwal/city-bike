package city.bike.status.app

import city.bike.status.registerPlugins
import city.bike.status.testHttpClient
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.testing.testApplication
import java.util.Base64

class AppRouteTest : FunSpec({

    test("sendIndexHtml returns InternalServerError if server returns a fatal error") {
        testApplication {
            registerPlugins()

            routing { sendIndexHtml("/some/wrong/path/index.html") }

            val response: HttpResponse = testHttpClient().get("/")
            val content: ErrorMessage = response.body()

            response.status shouldBe InternalServerError
            content shouldBe ErrorMessage("Internal Server Error", errors = emptyList(), null)
        }
    }

    test("sendIndexHtml returns Ok response with html content and encoded config") {
        val expectedEncodedConfig: String = Base64.getEncoder().encodeToString("{ isProd: false }".toByteArray())

        testApplication {
            registerPlugins()

            routing { sendIndexHtml("/index.html") }

            val response: HttpResponse = testHttpClient().get("/")
            val content: String = response.bodyAsText()

            response.status shouldBe OK
            content shouldContain expectedEncodedConfig
        }
    }
})
