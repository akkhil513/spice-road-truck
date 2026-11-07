package com.spiceroad.config;

import io.smallrye.config.ConfigMapping;

/** DynamoDB table names, injected per environment (see template.yaml / application.properties). */
@ConfigMapping(prefix = "srt.tables")
public interface SrtTables {

    String dishes();

    String orders();
}
