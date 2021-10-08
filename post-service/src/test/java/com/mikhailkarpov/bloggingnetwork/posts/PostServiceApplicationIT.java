package com.mikhailkarpov.bloggingnetwork.posts;

import com.mikhailkarpov.bloggingnetwork.posts.config.AbstractIT;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PostServiceApplicationIT extends AbstractIT {

    @Test
    void contextLoads() {
    }

}
