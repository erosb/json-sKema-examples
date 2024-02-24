package com.github.erosb.jsonsKema.examples;

import com.github.erosb.jsonsKema.JsonParser;
import com.github.erosb.jsonsKema.JsonValue;
import com.github.erosb.jsonsKema.ReadWriteContext;
import com.github.erosb.jsonsKema.Schema;
import com.github.erosb.jsonsKema.SchemaLoader;
import com.github.erosb.jsonsKema.Validator;
import com.github.erosb.jsonsKema.ValidatorConfig;

public class ReadWriteContextValidation {

    public static void main(String[] args) {
        JsonValue schemaJson = new JsonParser("""
        {
            "type": "object",
            "properties": {
                "id": {
                    "readOnly": true,
                    "type": "number",
                    "minimum": 0
                },
                "name": {
                    "type": "string"
                },
                "password": {
                    "type": "string",
                    "writeOnly": true
                }
            }
        }
        """).parse();
        // map the raw json to a reusable Schema instance
        Schema schema = new SchemaLoader(schemaJson).load();

        // creating write-context validator, it will report validation failures
        // for read-only properties that are included in the instance
        var writeContextValidator = Validator.create(schema, ValidatorConfig.builder()
                .readWriteContext(ReadWriteContext.WRITE)
                .build()
        );

        // creating the json document which will be validated (first in write context, then in read context)
        JsonValue instance = new JsonParser("""
                {
                    "id": 1,
                    "name": "John Doe",
                    "password": "v3ry_s3cur3"
                }
                """).parse();
        var writeContextFailure = writeContextValidator.validate(instance);

        // prints failure because the read-only property "id" is present in write context
        System.out.println(writeContextFailure);

        // creating read-context validator, it will report validation failures
        // for write-only properties that are included in the instance
        var readContextValidator = Validator.create(schema, ValidatorConfig.builder()
                .readWriteContext(ReadWriteContext.READ)
                .build()
        );

        var readContextFailure = readContextValidator.validate(instance);

        // prints failure because the write-only property "password" is present in read context
        System.out.println(readContextFailure);
    }
}
