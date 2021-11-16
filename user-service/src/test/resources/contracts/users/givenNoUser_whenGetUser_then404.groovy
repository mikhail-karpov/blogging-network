package contracts.users

import org.springframework.cloud.contract.spec.Contract

Contract.make {

    priority(1)
    description("get user by id when user not found responds 404")

    request {
        method(GET())
        url('/users/0/profile')
    }
    response {
        status(NOT_FOUND())
    }
}