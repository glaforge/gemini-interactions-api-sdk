/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.glaforge.gemini.schema;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FromClassTest {

    @Test
    public void testFromClass() {
        Schema schema = GSchema.fromClass(Person.class);
        String json = GSchema.toJson(schema);
        System.out.println(json);
        assertTrue(json.contains("\"type\" : \"integer\""), "Missing integer type");
        assertTrue(json.contains("\"enum\" : [ \"ACTIVE\", \"INACTIVE\" ]"), "Missing enum");
        assertTrue(json.contains("\"street\" : {"), "Missing nested address street");
    }

    @Test
    public void testArrayAndCollection() {
        Schema schema = GSchema.fromClass(Container.class);
        String json = GSchema.toJson(schema);
        System.out.println(json);

        // Check for List
        assertTrue(json.contains("\"list\" : {"), "Missing list field");

        // Check for Array
        assertTrue(json.contains("\"array\" : {"), "Missing array field");
        // We can't easily check inner types with simple contains, but "items" should be there for both
    }

    static class Container {
        public List<String> list;
        public String[] array;
    }

    @Test
    public void testInheritance() {
        Schema schema = GSchema.fromClass(Employee.class);
        String json = GSchema.toJson(schema);
        System.out.println(json);

        // Check for subclass field
        assertTrue(json.contains("\"employeeId\" : {"), "Missing subclass field employeeId");
        // Check for superclass field
        assertTrue(json.contains("\"name\" : {"), "Missing superclass field name");
    }

    enum Status {
        ACTIVE, INACTIVE
    }

    record Person(
            String name,
            int age,
            Status status,
            List<String> hobbies,
            Address address
    ) {}

    record Address(
            String street,
            int number
    ) {}

    static class BaseEntity {
        public String name;
    }

    static class Employee extends BaseEntity {
        public String employeeId;
    }
}
