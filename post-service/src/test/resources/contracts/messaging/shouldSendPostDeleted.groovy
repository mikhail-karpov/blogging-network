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
                authorId: "author-id",
                postId: "35cacb21-dc2a-4912-afe7-51d693e8f208",
                postContent: null,
                status: "DELETED"
        ])
    }
}