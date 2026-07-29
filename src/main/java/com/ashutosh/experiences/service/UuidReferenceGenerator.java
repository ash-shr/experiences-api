package com.ashutosh.experiences.service;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.UUID;

@Component
public class UuidReferenceGenerator implements ReferenceGenerator {

    @Override
    public String generate() {
        String raw = UUID.randomUUID().toString().replace("-", "");
        return "VTR-" + raw.substring(0, 8).toUpperCase(Locale.ROOT);
    }
}
