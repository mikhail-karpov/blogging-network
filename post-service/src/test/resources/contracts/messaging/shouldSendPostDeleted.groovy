import org.springframework.cloud.contract.spec.Contract

Contract.make {

    description('should send post created event message')
    label('post.created')
    input {
        triggeredBy('sendPostDeletedEvent()')
    }
    outputMessage {
        sentTo 'posts'
        headers {
            header('amqp_receivedRoutingKey', 'post.deleted')
        }
        body ([
                postId: "post-id",
                authorId: "author-id",
                status: "DELETED"
        ])
    }
}