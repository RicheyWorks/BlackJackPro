package com.richeyworks.blackjack.plugin;

/** Minimal plugin metadata. */
public record PluginManifest(
        String id,
        String name,
        String version,
        String author,
        String description) {

    public PluginManifest {
        if (id == null || id.isBlank())     throw new IllegalArgumentException("id required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name required");
        if (version == null)                version = "0.0.0";
        if (author == null)                 author  = "unknown";
        if (description == null)            description = "";
    }
}
