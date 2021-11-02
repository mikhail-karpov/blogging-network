import org.springframework.cloud.contract.spec.Contract

Contract.make {

    description('should send follow event message')
    label('user.follows.event')
    input {
        triggeredBy('sendFollowingEvent()')
    }
    outputMessage {
        sentTo 'users'
        headers {
            header('contentType', applicationJson())
            header('amqp_receivedRoutingKey', 'user.follow')
        }
        body ([
                followerId: "followerId",
                followingId: "followingId",
                status: "FOLLOWED"
        ])
    }
}