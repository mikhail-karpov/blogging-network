package com.mikhailkarpov.bloggingnetwork.feed.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class TestController {

    @GetMapping("/feed")
    public String hello(@RequestParam String name) {
        return String.format("Hello, %s!", name);
    }
}
