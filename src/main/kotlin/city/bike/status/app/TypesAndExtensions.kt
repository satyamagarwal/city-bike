package city.bike.status.app

import arrow.core.Either

typealias ErrorOr<A> = Either<Throwable, A>

sealed interface Environment {
    data object Local : Environment
    data object Prod : Environment
}
