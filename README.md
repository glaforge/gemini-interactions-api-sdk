# Gemini Interactions SDK for Java

A modern Java SDK for the Google Gemini Interactions API.

## Features
- **Modern Java**: Built with Java 17+, utilizing Records, Sealed Interfaces, and pattern matching.
- **Easy to Use**: Fluent Builder APIs for constructing requests.
- **Multimodal**: Native support for Text, Image, and Function Calling.
- **Lightweight**: Minimal dependencies (Jackson, Java Standard Library).

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.glaforge</groupId>
    <artifactId>gemini-interactions-api-sdk</artifactId>
    <version>0.3.0</version>
</dependency>
```

## Usage

### Initialization
```java
import io.github.glaforge.gemini.interactions.GeminiInteractionsClient;
import io.github.glaforge.gemini.interactions.model.*;

GeminiInteractionsClient client = GeminiInteractionsClient.builder()
    .apiKey(System.getenv("GEMINI_API_KEY"))
    .build();
```

### Simple Text Interaction
```java
ModelInteractionParams request = ModelInteractionParams.builder()
    .model("gemini-2.5-flash")
    .input("Why is the sky blue?")
    .build();

Interaction response = client.create(request);
System.out.println(response.outputs().get(0));
```

### Multi-turn Conversation
```java
import io.github.glaforge.gemini.interactions.model.Interaction.*;
import io.github.glaforge.gemini.interactions.model.Interaction.Role;
import io.github.glaforge.gemini.interactions.model.Content.*;

ModelInteractionParams request = ModelInteractionParams.builder()
    .model("gemini-2.5-flash")
    .input(
        new Turn(USER, "Hello!"),
        new Turn(MODEL, "Hi! How can I help?"),
        new Turn(USER, "Tell me a joke")
    )
    .build();

Interaction response = client.create(request);
```

### Multimodal (Image)
```java
import io.github.glaforge.gemini.interactions.model.Content.*;

ModelInteractionParams request = ModelInteractionParams.builder()
    .model("gemini-2.5-flash")
    .input(
        new TextContent("Describe this image"),
        // Create an image from Base64 string
        new ImageContent("BASE64_STRING...", "image/png")
    )
    .build();

Interaction response = client.create(request);
```

### Image Generation (Nano Banana Pro)
```java
import io.github.glaforge.gemini.interactions.model.Content.*;
import io.github.glaforge.gemini.interactions.model.Interaction.Modality;

ModelInteractionParams request = ModelInteractionParams.builder()
    .model("gemini-3-pro-image-preview")
    .input("Create an infographic about blood, organs, and the circulatory system")
    .responseModalities(Modality.IMAGE)
    .build();

Interaction interaction = client.create(request);

interaction.outputs().forEach(content -> {
    if (content instanceof ImageContent image) {
        byte[] imageBytes = Base64.getDecoder().decode(image.data());
        // Save imageBytes to a file
    }
});
```

### Deep Research
```java
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.Interaction.Status;
import io.github.glaforge.gemini.interactions.model.Interaction.AgentInteractionParams;

AgentInteractionParams request = AgentInteractionParams.builder()
    .agent("deep-research-pro-preview-12-2025")
    .input("Research the history of the Google TPUs")
    .build();

Interaction interaction = client.create(request);

// Poll for completion
while (interaction.status() != Status.COMPLETED) {
    Thread.sleep(1000);
    interaction = client.get(interaction.id());
}

System.out.println(interaction.outputs());
```

### Function Calling
```java
import io.github.glaforge.gemini.interactions.model.Content;
import io.github.glaforge.gemini.interactions.model.Content.*;
import io.github.glaforge.gemini.interactions.model.Tool;
import io.github.glaforge.gemini.interactions.model.Tool.Function;

// 1. Define the tool
Function weatherTool = Function.builder()
    .name("get_weather")
    .description("Get the current weather")
    .parameters(
        Map.of(
            "type", "object",
            "properties", Map.of(
            "location", Map.of("type", "string")
        ),
        "required", List.of("location")
    )
    .build();

// 2. Initial Request with Tools
ModelInteractionParams request = ModelInteractionParams.builder()
    .model("gemini-2.5-flash")
    .input("What is the weather in London?")
    .tools(weatherTool)
    .build();

Interaction interaction = client.create(request);

// 3. Handle Function Call
Content lastOutput = interaction.outputs().getLast();
if (lastOutput instanceof FunctionCallContent call) {
    if ("get_weather".equals(call.name())) {
        String location = (String) call.arguments().get("location");
        // Execute local logic...
        String weather = "Rainy, 15°C"; // Simulated result

        // 4. Send Function Result
        ModelInteractionParams continuation = ModelInteractionParams.builder()
            .model("gemini-2.5-flash")
            .previousInteractionId(interaction.id())
            .input(new FunctionResultContent(
                "function_result",
                call.id(),
                call.name(),
                false,
                Map.of("weather", weather)
            ))
            .build();

        Interaction finalResponse = client.create(continuation);
        System.out.println(finalResponse.outputs().getLast());
    }
}
```

## License
Apache 2.0

## Disclaimer
This is not an official Google project.
