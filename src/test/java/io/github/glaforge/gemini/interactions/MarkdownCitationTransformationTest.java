package io.github.glaforge.gemini.interactions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MarkdownCitationTransformationTest {

    @Test
    void testTransformCitations() {
        String input = """
            Some text with a citation [cite: 1] and another [cite: 2, 3].

            **Sources:**
            1. [google.com](https://google.com)
            2. [example.com](https://example.com)
            3. [test.com](https://test.com)
            """;

        String expected = """
            Some text with a citation <sup>[1](https://google.com)</sup> and another <sup>[2](https://example.com), [3](https://test.com)</sup>.

            **Sources:**
            1. [google.com](https://google.com)
            2. [example.com](https://example.com)
            3. [test.com](https://test.com)
            """;

        String result = ResearchFrontend.transformCitations(input);

        assertEquals(expected, result);
    }

    @Test
    void testTransformCitations_SingleCitation() {
        String inputStrict = """
            Text [cite: 1].

            **Sources:**
            1. [Start](http://start.com)
            """;

        String expected = """
            Text <sup>[1](http://start.com)</sup>.

            **Sources:**
            1. [Start](http://start.com)
            """;

        assertEquals(expected, ResearchFrontend.transformCitations(inputStrict));
    }
}
