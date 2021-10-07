package com.mikhailkarpov.bloggingnetwork.posts.dto;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class PagedResultTest {

    @Autowired
    private JacksonTester<PagedResult<String>> jsonTester;

    @Test
    void serialize() throws IOException {
        //given
        Page<String> page = new PageImpl<>(Arrays.asList("user", "admin"), PageRequest.of(1, 3), 5L);
        PagedResult<String> pagedResult = new PagedResult<>(page);

        //when
        JsonContent<PagedResult<String>> jsonContent = jsonTester.write(pagedResult);

        //then
        assertThat(jsonContent).extractingJsonPathArrayValue("$.result").contains("user", "admin");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.page").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathNumberValue("$.totalPages").isEqualTo(2);
        assertThat(jsonContent).extractingJsonPathNumberValue("$.totalResults").isEqualTo(5);
    }

    @Test
    void deserialize() throws IOException {
        //given
        String json = "{\"page\":1, \"totalPages\":2, \"totalResults\":5, \"result\": [\"user\", \"admin\"]}";

        //when
        PagedResult<String> pagedResult = jsonTester.parse(json).getObject();

        //then
        Assertions.assertThat(pagedResult.getPage()).isEqualTo(1);
        Assertions.assertThat(pagedResult.getTotalPages()).isEqualTo(2);
        Assertions.assertThat(pagedResult.getTotalResults()).isEqualTo(5L);
        Assertions.assertThat(pagedResult.getResult()).contains("user", "admin");
    }
}