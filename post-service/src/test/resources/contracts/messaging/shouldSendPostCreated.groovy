import org.springframework.cloud.contract.spec.Contract

Contract.make {

    description('should send post created event message')
    label('post.created')
    input {
        triggeredBy('sendPostCreatedEvent()')
    }
    outputMessage {
        sentTo 'posts'
        headers {
            header('amqp_receivedRoutingKey', 'post.created')
        }
        body ([
                postId: "post-id",
                authorId: "author-id",
                status: "CREATED"
        ])
    }
}