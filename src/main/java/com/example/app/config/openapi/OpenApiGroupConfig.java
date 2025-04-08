package com.example.app.config.openapi;

import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class OpenApiGroupConfig {

    @Bean
    public OpenApiCustomizer sortTagsAlphabetically() {
        return openApi -> {
            List<Tag> tags = openApi.getTags() != null ? openApi.getTags() : new ArrayList<>();
            // Sắp xếp các tag theo thứ tự bảng chữ cái
            List<Tag> sortedTags = tags.stream()
                    .sorted(Comparator.comparing(Tag::getName))
                    .collect(Collectors.toList());
            openApi.setTags(sortedTags);
        };
    }
} 