package se.ams.dcatprocessor.util;

import java.util.List;

public final class CliFlags {
    public static final List<String> SUPPORTED_FLAGS = List.of(
        "-f",   // Convert a single API specification file (.raml, .yaml, .json)
        "-d"    // Convert all API specification files in a directory
    );
}
