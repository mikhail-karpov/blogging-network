import org.springframework.cloud.contract.spec.Contract

Contract.make {

    description('should send post deleted event message')
    label('post.deleted')
    input {
        triggeredBy('sendPostDeletedEvent()')
    }
    outputMessage {
        sentTo 'posts'
        headers {
            header('contentType', applicationJson())
            header('amqp_receivedRoutingKey', 'post.deleted')
        }
        body ([
                postId: "post-id",
                authorId: "author-id",
                status: "DELETED"
        ])
    }
}