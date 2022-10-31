package city.bike.status

import city.bike.status.app.Environment
import io.ktor.server.engine.addShutdownHook
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.stop
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

val MAIN_LOGGER: Logger = LoggerFactory.getLogger("Main")
val ENV: Environment = System
    .getProperty("env")
    ?.takeIf { it == "prod" }
    ?.let { Environment.Prod }
    ?: Environment.Local

fun main() {
    val server: NettyApplicationEngine = embeddedServer(
        factory = Netty,
        port = 8080,
        host = "0.0.0.0",
        watchPaths = emptyList(),
        configure = {},
        module = {
            appModule()
        }
    )
    server.addShutdownHook {
        MAIN_LOGGER.info("Starting graceful shutdown")
        server.stop(gracePeriod = 3, timeout = 5, timeUnit = TimeUnit.SECONDS)
        MAIN_LOGGER.info("Graceful shutdown done")
    }

    server.start(wait = true)
}
