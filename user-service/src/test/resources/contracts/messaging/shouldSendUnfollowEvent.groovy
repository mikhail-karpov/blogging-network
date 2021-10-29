import org.springframework.cloud.contract.spec.Contract

Contract.make {

    description('should send unfollow event message')
    label('user.unfollows.event')
    input {
        triggeredBy('sendUnfollowingEvent()')
    }
    outputMessage {
        sentTo 'users'
        headers {
            header('amqp_receivedRoutingKey', 'user.unfollow')
        }
        body ([
                followerId: "followerId",
                followingId: "followingId",
                status: "UNFOLLOWED"
        ])
    }
}