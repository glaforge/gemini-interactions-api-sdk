# Gemini Interactions SDK for Java

A modern Java SDK for the Google Gemini Interactions API.

## Features
- **Modern Java**: Built with Java 17+, utilizing Records, Sealed Interfaces, and pattern matching.
- **Easy to Use**: Fluent Builder APIs for constructing requests.
- **Multimodal**: Native support for Text, Image, and Function Calling.
- **Lightweight**: Minimal dependencies (Jackson, Java Standard Library).

> [!WARNING]
> **⚠️ API Migration Notice (May 2026)**
> The Gemini Interactions API has undergone a major breaking change to a polymorphic `Step`-based architecture.
> - **Legacy:** `interaction.outputs()`
> - **New:** `interaction.steps()`
>
> Interactions now consist of a sequence of `Step` objects (e.g., `ModelOutputStep`, `FunctionCallStep`). All `Content` items
> are now nested within these steps. To retrieve the model's text response, you must extract the `ModelOutputStep` from the
> `steps()` list, and then retrieve the `TextContent` from that step's `content()` list. Furthermore, Server-Sent Events
> (SSE) now use `StepDelta` instead of `ContentDelta`.

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.glaforge</groupId>
    <artifactId>gemini-interactions-api-sdk</artifactId>
    <version>0.9.0</version>
</dependency>
```

## Usage

### Initialization
```java
import io.github.glaforge.gemini.interactions.GeminiInteractionsClient;
import io.github.glaforge.gemini.interactions.model.*;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;

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
Step.ModelOutputStep step = (Step.ModelOutputStep) response.steps().get(0);
System.out.println(step.content().get(0));
```

### Streaming Response
```java
import io.github.glaforge.gemini.interactions.model.Events.StepDelta;
import io.github.glaforge.gemini.interactions.model.Events.TextDelta;

ModelInteractionParams request = ModelInteractionParams.builder()
    .model("gemini-2.5-flash")
    .input("Why is the sky blue?")
    .stream(true)
    .build();

client.stream(request).forEach(event -> {
    if (event instanceof StepDelta delta) {
        if (delta.delta() instanceof TextDelta textPart) {
            System.out.print(textPart.text());
        }
    }
});
```

### Multi-turn Conversation
```java
import io.github.glaforge.gemini.interactions.model.Interaction.Turn;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import io.github.glaforge.gemini.interactions.model.Content.*;
import static io.github.glaforge.gemini.interactions.model.Interaction.Role.*;

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

### Multi-turn Conversation with Persistence

You can also continue a conversation by referencing the ID of a previous interaction. Ensure you set `store(true)` to persist the interaction context.

```java
// 1. First turn (must set store=true)
ModelInteractionParams turn1 = ModelInteractionParams.builder()
    .model("gemini-2.5-flash")
    .input("Hello!")
    .store(true)
    .build();

Interaction response1 = client.create(turn1);
String id = response1.id();
Step.ModelOutputStep step1 = (Step.ModelOutputStep) response1.steps().get(0);
System.out.println(step1.content().get(0));

// 2. Second turn (referencing previous ID)
ModelInteractionParams turn2 = ModelInteractionParams.builder()
    .model("gemini-2.5-flash")
    .input("Tell me a joke")
    .previousInteractionId(id)
    .store(true) // Optional if you want to extend further
    .build();

Interaction response2 = client.create(turn2);
Step.ModelOutputStep step2 = (Step.ModelOutputStep) response2.steps().get(0);
System.out.println(step2.content().get(0));
```

### Multimodal (Image)
```java
import io.github.glaforge.gemini.interactions.model.Content.*;

ModelInteractionParams request = ModelInteractionParams.builder()
    .model("gemini-2.5-flash")
    .input(
        new TextContent("Describe this image"),
        // Create an image from Base64 bytes
        new ImageContent(imageBytes, "image/png")
    )
    .build();

Interaction response = client.create(request);
```

### Multimodal (Audio)
```java
import io.github.glaforge.gemini.interactions.model.Content.*;
import io.github.glaforge.gemini.interactions.model.Config.SpeechConfig;

ModelInteractionParams request = ModelInteractionParams.builder()
    .model("gemini-2.5-flash")
    .input(
        new TextContent("Transcribe this audio"),
        new AudioContent(audioBytes, "audio/mp3")
    )
    .build();

Interaction response = client.create(request);
```

### Image Generation (Nano Banana Pro)
```java
import io.github.glaforge.gemini.interactions.model.Content.*;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import io.github.glaforge.gemini.interactions.model.Interaction.Modality;

ModelInteractionParams request = ModelInteractionParams.builder()
    .model("gemini-3-pro-image-preview")
    .input("Create an infographic about blood, organs, and the circulatory system")
    .responseModalities(Modality.IMAGE)
    .build();

Interaction interaction = client.create(request);

interaction.steps().forEach(step -> {
    if (step instanceof Step.ModelOutputStep modelOutputStep) {
        modelOutputStep.content().forEach(content -> {
            if (content instanceof ImageContent image) {
                byte[] imageBytes = Base64.getDecoder().decode(image.data());
                // Save imageBytes to a file
            }
        });
    }
});
```

### Audio Output
```java
import io.github.glaforge.gemini.interactions.model.Content.*;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import io.github.glaforge.gemini.interactions.model.Interaction.Modality;
import io.github.glaforge.gemini.interactions.model.Config.SpeechConfig;

ModelInteractionParams request = ModelInteractionParams.builder()
    .model("gemini-2.5-flash-preview-tts")
    .input("Hey, we can generate audio too!")
    .responseModalities(Modality.AUDIO, Modality.TEXT)
    .speechConfig(new SpeechConfig("Puck", "en-US"))
    .build();

Interaction interaction = client.create(request);

interaction.steps().forEach(step -> {
    if (step instanceof Step.ModelOutputStep modelOutputStep) {
        modelOutputStep.content().forEach(content -> {
            if (content instanceof AudioContent audio) {
                byte[] audioBytes = audio.data();
                // Save audioBytes to a raw PCM file (16-bit little-endian, 24kHz, mono)
            }
        });
    }
});
```

### Lyria Music Generation
```java
import io.github.glaforge.gemini.interactions.model.Content.*;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import io.github.glaforge.gemini.interactions.model.Interaction.Modality;
import java.nio.file.Files;
import java.nio.file.Paths;

ModelInteractionParams request = ModelInteractionParams.builder()
    .model("models/lyria-3-clip-preview")
    .input("An epic song with opera voices about a quest. Deep synths and a speeding up tempo.")
    .responseModalities(Modality.AUDIO, Modality.TEXT)
    .build();

Interaction interaction = client.create(request);

interaction.steps().forEach(step -> {
    if (step instanceof Step.ModelOutputStep modelOutputStep) {
        modelOutputStep.content().forEach(content -> {
            if (content instanceof TextContent text) {
                System.out.println("Lyrics / Structure Generated:\\n" + text.text());
            }
            if (content instanceof AudioContent audio) {
                // Lyria directly returns an encoded MP3 byte stream!
                try {
                    Files.write(Paths.get("quest-song.mp3"), audio.data());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
});
```

### Deep Research
```java
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.Interaction.Status;
import io.github.glaforge.gemini.interactions.model.InteractionParams.AgentInteractionParams;

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

System.out.println(interaction.steps());
```

### Function Calling
```java
import io.github.glaforge.gemini.interactions.model.Content;
import io.github.glaforge.gemini.interactions.model.Content.*;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
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
Step.FunctionCallStep callStep = (Step.FunctionCallStep) interaction.steps().getLast();
if ("get_weather".equals(callStep.name())) {
    String location = (String) callStep.arguments().get("location");
    // Execute local logic...
    String weather = "Rainy, 15°C"; // Simulated result

    // 4. Send Function Result
    ModelInteractionParams continuation = ModelInteractionParams.builder()
        .model("gemini-2.5-flash")
        .previousInteractionId(interaction.id())
        .input(new Step.FunctionResultStep(
            "step-result-id",
            callStep.name(),
            false,
            Map.of("weather", weather)
        ))
        .build();

    Interaction finalResponse = client.create(continuation);
    Step.ModelOutputStep finalStep = (Step.ModelOutputStep) finalResponse.steps().getLast();
    System.out.println(finalStep.content().getLast());
}
```

### Gemma Open Models

You can use the open Gemma models through the interactions API just like the standard Gemini models.

#### Simple Interaction (Gemma 4 26B)
```java
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import io.github.glaforge.gemini.interactions.model.Interaction;

ModelInteractionParams request = ModelInteractionParams.builder()
    .model("models/gemma-4-26b-a4b-it")
    .input("What is the capital of France? Answer in one word.")
    .build();

Interaction interaction = client.create(request);
Step.ModelOutputStep step = (Step.ModelOutputStep) interaction.steps().getLast();
System.out.println(step.content().getLast());
```

#### Grounded Search (Gemma 4 31B)
Gemma 4 models fully support the Google Search grounding tool.

```java
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.Tool;

ModelInteractionParams request = ModelInteractionParams.builder()
    .model("models/gemma-4-31b-it")
    .input("Who is the actress who invented the MemPalace agent memory project?")
    .tools(new Tool.GoogleSearch())
    .build();

Interaction interaction = client.create(request);
Step.ModelOutputStep step = (Step.ModelOutputStep) interaction.steps().getLast();
System.out.println(step.content().getLast());
```

### Built-in Tools (Google Maps)
```java
import io.github.glaforge.gemini.interactions.model.Content;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import io.github.glaforge.gemini.interactions.model.Tool;

// 1. Define the Google Maps tool
Tool googleMaps = new Tool.GoogleMaps();

// 2. Initial Request with the Tool
ModelInteractionParams request = ModelInteractionParams.builder()
    .model("gemini-2.5-flash")
    .input("Can you recommend some good restaurants near the Eiffel tower in Paris?")
    .tools(googleMaps)
    .build();

Interaction interaction = client.create(request);

// 3. Handle Result
Step.ModelOutputStep step = (Step.ModelOutputStep) interaction.steps().getLast();
System.out.println(step.content().getLast());
```

### JSON Output (Structured Output)
You can enforce the model to output a specific JSON structure using the `responseFormat` parameter.

#### Map-based Approach
You can pass a `Map` representing the JSON Schema directly.

```java
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import java.util.Map;
import java.util.List;

ModelInteractionParams params = ModelInteractionParams.builder()
    .model("gemini-2.5-flash")
    .input("List 5 popular cookie recipes")
    .responseMimeType("application/json")
    .responseFormat(Map.of(
        "type", "array",
        "items", Map.of(
            "type", "object",
            "properties", Map.of(
                "recipe_name", Map.of("type", "string")
            )
        )
    ))
    .build();
```

#### Schema Builder Approach
You can use the fluent Schema builder API provided by the SDK.

```java
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import io.github.glaforge.gemini.schema.GSchema;
import static io.github.glaforge.gemini.schema.GSchema.*;

ModelInteractionParams params = ModelInteractionParams.builder()
    .model("gemini-2.5-flash")
    .input("List 5 popular cookie recipes")
    .responseMimeType("application/json")
    .responseFormat(
        arr().items(
            obj()
                .prop("recipe_name", str())
                .prop("ingredients", arr().items(str()))
        )
    )
    .build();
```

#### From Java Class
You can generate the schema directly from a Java class (Records or POJOs).

```java
public record Recipe(String name, List<String> ingredients) {}

ModelInteractionParams params = ModelInteractionParams.builder()
    .model("gemini-2.5-flash")
    .input("List 5 popular cookie recipes")
    .responseMimeType("application/json")
    .responseFormat(GSchema.fromClass(Recipe.class))
    .build();
```

#### From JSON Schema String
You can also parse an existing JSON Schema string.

```java
String jsonSchema = """
    {
      "type": "array",
      "items": { "type": "string" }
    }
    """;

ModelInteractionParams params = ModelInteractionParams.builder()
    .model("gemini-2.5-flash")
    .input("List 5 popular cookie recipes")
    .responseMimeType("application/json")
    .responseFormat(GSchema.fromJson(jsonSchema))
    .build();
```

## License
Apache 2.0

## Disclaimer
This is not an official Google project.
