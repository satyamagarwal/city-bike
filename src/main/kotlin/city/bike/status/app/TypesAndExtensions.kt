package city.bike.status.app

import arrow.core.Either

typealias ErrorOr<A> = Either<Throwable, A>
