package com.github.erosb.jsonsKema.examples;

import com.github.erosb.jsonsKema.FormatValidationFailure;
import com.github.erosb.jsonsKema.FormatValidationPolicy;
import com.github.erosb.jsonsKema.JsonParser;
import com.github.erosb.jsonsKema.JsonValue;
import com.github.erosb.jsonsKema.Schema;
import com.github.erosb.jsonsKema.SchemaLoader;
import com.github.erosb.jsonsKema.Validator;
import com.github.erosb.jsonsKema.ValidatorConfig;

public class CustomFormatValidator {

    public static void main(String[] args) {
        JsonValue schemaJson = new JsonParser("""
        {
            "$schema": "https://json-schema.org/draft/2020-12/schema",
            "type": "object",
            "properties": {
                "propWithMatchingParens": {
                    "type": "string",
                    "format": "parens"
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
        Validator validator = Validator.create(schema, ValidatorConfig.builder()
                        .validateFormat(FormatValidationPolicy.ALWAYS)
                        .additionalFormatValidator("parens", (instance, sch) ->
                            instance.maybeString(str -> {
                                var openCount = 0;
                                for (char ch : str.getValue().toCharArray()) {
                                    if (ch == '(') {
                                        ++openCount;
                                    } else if (ch == ')') {
                                        --openCount;
                                        if (openCount < 0) {
                                            return new FormatValidationFailure(sch, instance);
                                        }
                                    }
                                }
                                if (openCount == 0) {
                                    return null;
                                } else {
                                    return new FormatValidationFailure(sch, instance);
                                }
                            }))
                        .additionalFormatValidator("email", (inst, sch) ->
                                inst.maybeString(str -> str.getValue().endsWith("@example.com") ? null : new FormatValidationFailure(sch, str))
                        )
                .build());

        var actual = validator.validate(new JsonParser("""
                {
                    "propWithMatchingParens": "asd)()(asd)))",
                    "email": "me@example.io"
                }
                """).parse());
        // prints 2 errors:
        //  - parentheses are not matching in "propWithMatchingParens"
        //  - email format is overriden to accept only @example.com strings, so "email" also fails
        System.out.println(actual);
    }
}
