package com.github.erosb.jsonsKema.examples;

import com.github.erosb.jsonsKema.AggregateSchemaLoadingException;
import com.github.erosb.jsonsKema.JsonDocumentLoadingException;
import com.github.erosb.jsonsKema.JsonParser;
import com.github.erosb.jsonsKema.JsonTypeMismatchException;
import com.github.erosb.jsonsKema.RefResolutionException;
import com.github.erosb.jsonsKema.SchemaLoader;
import com.github.erosb.jsonsKema.SchemaLoadingException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static com.github.erosb.jsonsKema.SchemaLoaderConfig.createDefaultConfig;

public class LoadingFailureDetails {

    public static void main(String[] args)
            throws URISyntaxException {
        var loader = new SchemaLoader(
                new JsonParser(
                """
                {
                    "title": null,
                    "description": 2,
                    "properties": {
                        "wrongType": {
                            "type": "float",
                            "minimum": "maybe 2 or so"
                        },
                        "remoteNotFound": {
                            "$ref": "classpath://not-found.file"
                        },
                        "remotePointerFailure": {
                            "$ref": "http://example.org/schema#/$defs/X"
                        },
                        "remoteParsingFailure": {
                            "$ref": "classpath://xml"
                        }
                    }
                }
            """).parse(),
                createDefaultConfig(Map.of(
                        new URI("http://example.org/schema"), """
                    {
                        "$defs": {}
                    }
                """,
                new URI("classpath://xml"), """
                    <?xml version="1.0">
                    <project>
                    </project>
                """
            ))
        );

        try {
            loader.load();
        } catch (SchemaLoadingException e) {
            printDetails(e);
        }
    }

    private static void printDetails(SchemaLoadingException e) {
        switch (e) {
            case JsonTypeMismatchException typeEx -> {
                System.out.println("Unexpected json type found at: " + typeEx.getLocation());
                System.out.println("was looking for a value of type " + typeEx.getExpectedType());
                System.out.println("but found value of type " + typeEx.getActualType());
            }
            case RefResolutionException refEx -> {
                System.out.println("Could not resolve reference " + refEx.getRef().getRef());
                System.out.println("Which was found at " + refEx.getRef().getLocation());
                System.out.println("Was looking for property " + refEx.getMissingProperty());
                System.out.println("But it was not found at " + refEx.getResolutionFailureLocation());
            }
            case AggregateSchemaLoadingException aggEx -> {
                System.out.println("Multiple schema loading failures found: ");
                aggEx.getCauses().forEach(LoadingFailureDetails::printDetails);
            }
            case JsonDocumentLoadingException loadEx -> {
                System.out.println("could not load schema from URI " + loadEx.getUri());
                System.out.println("Reason: " + loadEx.getCause());
            }
            default -> throw new IllegalStateException("Unexpected value: " + e);
        }
    }
}
