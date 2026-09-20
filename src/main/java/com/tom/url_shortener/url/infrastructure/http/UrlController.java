package com.tom.url_shortener.url.infrastructure.http;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/urls")
public class UrlController {

    @PostMapping
    public ResponseEntity<String> post(@RequestBody String url){
        return ResponseEntity.ok(url);
    }
}
