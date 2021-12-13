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
            header('contentType', applicationJson())
            header('amqp_receivedRoutingKey', 'post.created')
        }
        body ([
                authorId: "author-id",
                postId: "37e4e4d0-c615-4c5c-85b5-adab30093def",
                postContent: "Post content",
                status: "CREATED"
        ])
    }
}