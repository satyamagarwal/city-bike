import io.kotest.common.ExperimentalKotest
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.config.ProjectConfiguration

@ExperimentalKotest
object KotestProjectConfig : AbstractProjectConfig() {
    override val parallelism = 50

    override val concurrentSpecs: Int = ProjectConfiguration.MaxConcurrency
}
