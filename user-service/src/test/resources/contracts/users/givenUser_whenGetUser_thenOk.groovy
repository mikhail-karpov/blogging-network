package contracts.users

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("get user by id when user not found responds 404")

    request {
        method(GET())
        url('/users/1/profile')
        headers {
            header(accept(), applicationJson())
        }
    }
    response {
        status(OK())
        headers {
            header(contentType(), applicationJson())
        }
        body(
                userId: anyNonBlankString(),
                username: anyNonBlankString()
        )
    }
}