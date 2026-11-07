package com.spiceroad.config;

import io.smallrye.config.ConfigMapping;

/** Single-table DynamoDB name, injected per environment (see template.yaml / application.properties). */
@ConfigMapping(prefix = "srt.data")
public interface SrtTables {

    String table();
}
