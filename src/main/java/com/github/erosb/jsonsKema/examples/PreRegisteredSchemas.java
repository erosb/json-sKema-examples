package com.github.erosb.jsonsKema.examples;

import com.github.erosb.jsonsKema.JsonParser;
import com.github.erosb.jsonsKema.JsonValue;
import com.github.erosb.jsonsKema.Schema;
import com.github.erosb.jsonsKema.SchemaLoader;
import com.github.erosb.jsonsKema.SchemaLoaderConfig;
import com.github.erosb.jsonsKema.ValidationFailure;
import com.github.erosb.jsonsKema.Validator;

import java.net.URI;
import java.util.Map;

import static com.github.erosb.jsonsKema.SchemaLoaderConfig.createDefaultConfig;

public class PreRegisteredSchemas {

    public static void main(String[] args) throws Exception {
        // Creating a SchemaLoader config with a pre-registered schema by URI
        SchemaLoaderConfig config = createDefaultConfig(Map.of(
                    // When the loader sees this URI,
                    new URI("urn:uuid:d652a438-9897-4160-959c-bbdb690c3e0d"),

                    // then it will resolve it to this schema json
                    """
                    {
                        "$defs": {
                            "ItemType": {
                                "type": "integer",
                                "minimum": 0
                            }
                        }
                    }
                    """
            ));
        // parsing the schema json, with a $ref to the above pre-configured URI
        JsonValue schemaJson = new JsonParser("""
                {
                    "type": "array",
                    "items": {
                        "$ref": "urn:uuid:d652a438-9897-4160-959c-bbdb690c3e0d#/$defs/ItemType"
                    }
                }
                """).parse();
        // loading the schema json into a Schema object
        Schema schema = new SchemaLoader(schemaJson, config).load();

        // running the validation
        ValidationFailure result = Validator.forSchema(schema).validate(new JsonParser("[null]").parse());
        System.out.println(result.toJSON());
    }

}
