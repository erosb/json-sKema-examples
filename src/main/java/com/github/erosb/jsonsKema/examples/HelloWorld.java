package com.github.erosb.jsonsKema.examples;

import com.github.erosb.jsonsKema.FormatValidationPolicy;
import com.github.erosb.jsonsKema.JsonParser;
import com.github.erosb.jsonsKema.JsonValue;
import com.github.erosb.jsonsKema.Schema;
import com.github.erosb.jsonsKema.SchemaLoader;
import com.github.erosb.jsonsKema.ValidationFailure;
import com.github.erosb.jsonsKema.Validator;
import com.github.erosb.jsonsKema.ValidatorConfig;

public class HelloWorld {

    public static void main(String[] args) {
        // parse the schema JSON as string
        JsonValue schemaJson = new JsonParser("""
        {
            "$schema": "https://json-schema.org/draft/2020-12/schema",
            "type": "object",
            "properties": {
                "age": {
                    "type": "number",
                    "minimum": 0
                },
                "name": {
                    "type": "string"
                },
                "email": {
                    "type": "string",
                    "format": "email"
                }
            }
        }
        """).parse();
        // map the raw json to a reusable Schema instance
        Schema schema = new SchemaLoader(schemaJson).load();

        // create a validator instance for each validation (one-time use object)
        Validator validator = Validator.create(schema, new ValidatorConfig(FormatValidationPolicy.ALWAYS));

        // parse the input instance to validate against the schema
        JsonValue instance = new JsonParser("""
        {
            "age": -5,
            "name": null,
            "email": "invalid"
        }
        """).parse();

        // run the validation
        ValidationFailure failure = validator.validate(instance);

        // print the validation failures (if any)
        System.out.println(failure);
    }
}
