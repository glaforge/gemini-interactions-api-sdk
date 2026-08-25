---
name: gemini-interactions-java-api
description: Guides the usage of Gemini Interactions API Java SDK. Use when the user wants to use the stateful, server-managed Interactions API for multi-turn conversations, background execution, streaming, structured output, function calling, multimodal generation, and agent interactions using Java.
---

# Gemini Interactions API Java SDK Skill

This skill provides instructions for authenticating, connecting to, and utilizing the stateful, server-managed **Gemini Interactions API** using the Java SDK.

## 1. Installation

### Maven
Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.glaforge</groupId>
    <artifactId>gemini-interactions-api-sdk</artifactId>
    <version>1.1.0</version>
</dependency>
```

### Gradle
Add the dependency to your `build.gradle` or `build.gradle.kts`:

```gradle
implementation("io.github.glaforge:gemini-interactions-api-sdk:1.1.0")
```

> [!NOTE]
> Check [Maven Central](https://central.sonatype.com/artifact/io.github.glaforge/gemini-interactions-api-sdk) to find the latest available version of the SDK.


## 2. Client Initialization

### Option A: Google AI Studio (Default)
Ensure you have the `GEMINI_API_KEY` environment variable set. You can initialize the client using the builder:

```java
import io.github.glaforge.gemini.interactions.GeminiInteractionsClient;

GeminiInteractionsClient client = GeminiInteractionsClient.builder()
    .apiKey(System.getenv("GEMINI_API_KEY"))
    .build();
```

### Option B: Google Cloud Vertex AI
Ensure you are authenticated with Google Cloud Application Default Credentials (`gcloud auth application-default login`). Provide your Google Cloud Project ID and optionally the region:

```java
import io.github.glaforge.gemini.interactions.GeminiInteractionsClient;

GeminiInteractionsClient client = GeminiInteractionsClient.builder()
    .project("your-google-cloud-project-id")
    .location("global") // Defaults to "global"
    .build();
```

## 3. Core API Usage

### Simple Text Interaction

Submit a single prompt and read the text response from the model.

```java
import io.github.glaforge.gemini.interactions.GeminiInteractionsClient;
import io.github.glaforge.gemini.interactions.model.*;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;

GeminiInteractionsClient client = GeminiInteractionsClient.builder().apiKey(System.getenv("GEMINI_API_KEY")).build();

ModelInteractionParams request = ModelInteractionParams.builder()
    .model("gemini-2.5-flash")
    .input("Why is the sky blue?")
    .build();

Interaction response = client.create(request);
System.out.println(response.outputText());
```

### Multimodal Image Generation (Nano Banana Pro)

Generate images and extract the base64-encoded image data.

```java
import io.github.glaforge.gemini.interactions.GeminiInteractionsClient;
import io.github.glaforge.gemini.interactions.model.*;
import io.github.glaforge.gemini.interactions.model.Content.*;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import io.github.glaforge.gemini.interactions.model.Interaction.Modality;

GeminiInteractionsClient client = GeminiInteractionsClient.builder().apiKey(System.getenv("GEMINI_API_KEY")).build();

ModelInteractionParams request = ModelInteractionParams.builder()
    .model("gemini-3-pro-image-preview")
    .input("Create an infographic about blood, organs, and the circulatory system")
    .responseModalities(Modality.IMAGE)
    .build();

Interaction interaction = client.create(request);
Content.ImageContent image = interaction.outputImage();
if (image != null) {
    System.out.println("Image generated with " + image.data().length + " bytes.");
}
```

### Multimodal Video Generation (Gemini Omni Flash)

Generate videos and extract the video data.

```java
import io.github.glaforge.gemini.interactions.GeminiInteractionsClient;
import io.github.glaforge.gemini.interactions.model.*;
import io.github.glaforge.gemini.interactions.model.Content.*;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import io.github.glaforge.gemini.interactions.model.Interaction.Modality;

GeminiInteractionsClient client = GeminiInteractionsClient.builder().apiKey(System.getenv("GEMINI_API_KEY")).build();

ModelInteractionParams request = ModelInteractionParams.builder()
    .model("gemini-omni-flash-preview")
    .input("A highly detailed cinematic shot of a futuristic banana.")
    .responseModalities(Modality.VIDEO)
    .build();

Interaction interaction = client.create(request);
Content.VideoContent video = interaction.outputVideo();
if (video != null) {
    System.out.println("Video generated with " + video.data().length + " bytes.");
}
```

### Speech Recognition & Transcription (ASR)

Enable speech-to-text recognition with custom vocabulary, diarization, or word-level timestamps.

```java
import io.github.glaforge.gemini.interactions.GeminiInteractionsClient;
import io.github.glaforge.gemini.interactions.model.*;
import io.github.glaforge.gemini.interactions.model.Config.GenerationConfig;
import io.github.glaforge.gemini.interactions.model.Config.TranscriptionConfig;
import io.github.glaforge.gemini.interactions.model.Content.*;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import java.util.List;

GeminiInteractionsClient client = GeminiInteractionsClient.builder().apiKey(System.getenv("GEMINI_API_KEY")).build();

TranscriptionConfig transcriptionConfig = TranscriptionConfig.builder()
    .languageHints(List.of("en-US", "auto"))
    .diarizationMode("speaker")
    .timestampGranularities(List.of("word"))
    .build();

GenerationConfig generationConfig = GenerationConfig.builder()
    .transcriptionConfig(transcriptionConfig)
    .build();

ModelInteractionParams request = ModelInteractionParams.builder()
    .model(ModelOption.GEMINI_3_7_FLASH)
    .input(
        new TextContent("Transcribe audio with speaker labels"),
        new AudioContent(audioBytes, "audio/wav")
    )
    .generationConfig(generationConfig)
    .build();

Interaction response = client.create(request);
```

### Stateful Conversation (Multi-Turn)

Use `store(true)` to persist the conversation in the cloud, and `previousInteractionId` to continue the thread.

```java
import io.github.glaforge.gemini.interactions.GeminiInteractionsClient;
import io.github.glaforge.gemini.interactions.model.*;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;

GeminiInteractionsClient client = GeminiInteractionsClient.builder().apiKey(System.getenv("GEMINI_API_KEY")).build();

// 1. First turn (must set store=true)
ModelInteractionParams turn1 = ModelInteractionParams.builder()
    .model("gemini-2.5-flash")
    .input("Hello!")
    .store(true)
    .build();

Interaction response1 = client.create(turn1);
String id = response1.id();
System.out.println(response1.outputText());

// 2. Second turn (referencing previous ID)
ModelInteractionParams turn2 = ModelInteractionParams.builder()
    .model("gemini-2.5-flash")
    .input("Tell me a joke")
    .previousInteractionId(id)
    .store(true)
    .build();

Interaction response2 = client.create(turn2);
System.out.println(response2.outputText());
```

### Structured Output (JSON)

Use the built-in fluent `GSchema` builder to enforce JSON structures.

```java
import io.github.glaforge.gemini.interactions.GeminiInteractionsClient;
import io.github.glaforge.gemini.interactions.model.*;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import static io.github.glaforge.gemini.schema.GSchema.*;

GeminiInteractionsClient client = GeminiInteractionsClient.builder().apiKey(System.getenv("GEMINI_API_KEY")).build();

ModelInteractionParams params = ModelInteractionParams.builder()
    .model("gemini-2.5-flash")
    .input("List 2 popular cookie recipes")
    .responseFormat(
        arr().items(
            obj()
                .prop("recipe_name", str())
                .prop("ingredients", arr().items(str()))
        )
    )
    .build();

Interaction response = client.create(params);
System.out.println(response.outputText());
```

### Deep Research Agent Invocation

Run the Deep Research agent and poll until completion.

```java
import io.github.glaforge.gemini.interactions.GeminiInteractionsClient;
import io.github.glaforge.gemini.interactions.model.*;
import io.github.glaforge.gemini.interactions.model.Interaction.Status;
import io.github.glaforge.gemini.interactions.model.InteractionParams.AgentInteractionParams;

GeminiInteractionsClient client = GeminiInteractionsClient.builder().apiKey(System.getenv("GEMINI_API_KEY")).build();

AgentInteractionParams request = AgentInteractionParams.builder()
    .agent("deep-research-pro-preview-12-2025")
    .input("Research the history of the Google TPUs")
    .background(true)
    .build();

Interaction interaction = client.create(request);

// Poll for completion
while (!interaction.status().isFinished()) {
    try {
        Thread.sleep(2000);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
    interaction = client.get(interaction.id());
}

System.out.println(interaction.steps());
```

### Safety Settings

Customize safety settings to control harmful content generation.

```java
import io.github.glaforge.gemini.interactions.GeminiInteractionsClient;
import io.github.glaforge.gemini.interactions.model.SafetySetting;
import io.github.glaforge.gemini.interactions.model.HarmCategory;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import java.util.List;

GeminiInteractionsClient client = GeminiInteractionsClient.builder().apiKey(System.getenv("GEMINI_API_KEY")).build();

ModelInteractionParams params = ModelInteractionParams.builder()
    .model("gemini-2.5-flash")
    .input("Tell me a dangerous secret.")
    .safetySettings(List.of(
        new SafetySetting(HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT, "BLOCK_LOW_AND_ABOVE", null)
    ))
    .build();

client.create(params);
```

### Streaming Interactions

For long-running tasks or to see real-time updates (thoughts, code execution, text output), you can use the streaming API.

```java
import io.github.glaforge.gemini.interactions.GeminiInteractionsClient;
import io.github.glaforge.gemini.interactions.model.*;
import io.github.glaforge.gemini.interactions.model.InteractionParams.AgentInteractionParams;

GeminiInteractionsClient client = GeminiInteractionsClient.builder().apiKey(System.getenv("GEMINI_API_KEY")).build();

AgentInteractionParams params = AgentInteractionParams.builder()
    .agent("deep-research-pro-preview-12-2025")
    .input("Write a Python script for Conway's Game of Life")
    .stream(true)
    .build();

try (var eventStream = client.stream(params)) {
    eventStream.forEach(event -> {
        if (event instanceof Events.InteractionCreated created) {
            System.out.println("Interaction created: " + created.interaction().id());
        } else if (event instanceof Events.ContentDelta contentDelta) {
            if (contentDelta.delta() instanceof Events.TextDelta textDelta) {
                System.out.print(textDelta.text());
            }
        } else if (event instanceof Events.StepDelta stepDelta) {
            if (stepDelta.delta() instanceof Events.ThoughtSummaryDelta thought) {
                System.out.println("\n[Thinking...] " + thought.content());
            } else if (stepDelta.delta() instanceof Events.CodeExecutionCallDelta codeCall) {
                System.out.println("\n[Executing Code...] " + codeCall.arguments());
            }
        }
    });
}
```

### Custom Agents (Sandboxing & Environment Egress)

Create an isolated agent securely using remote sandboxing with specific network allowlists.

```java
import io.github.glaforge.gemini.interactions.GeminiInteractionsClient;
import io.github.glaforge.gemini.interactions.model.*;
import io.github.glaforge.gemini.interactions.model.InteractionParams.AgentInteractionParams;
import java.util.List;

GeminiInteractionsClient client = GeminiInteractionsClient.builder().apiKey(System.getenv("GEMINI_API_KEY")).build();

Agent customAgent = Agent.builder()
    .id("my-concise-coder-agent-" + System.currentTimeMillis())
    .description("A custom agent built for secure coding tasks.")
    .baseAgent("antigravity-preview-05-2026")
    .baseEnvironment("remote")
    .systemInstruction("You are a helpful coding assistant. Always respond concisely.")
    .tools(List.of(
        new AgentTool.GoogleSearch(),
        new AgentTool.CodeExecution()
    ))
    .baseEnvironment(new EnvironmentConfig(
        new EnvironmentNetworkEgressAllowlist(List.of(
            new AllowlistEntry("github.com")
        )),
        null
    ))
    .build();

Agent provisioned = client.createAgent(customAgent);
System.out.println("Created custom agent: " + provisioned.id());

// You can interact with it using AgentInteractionParams...
AgentInteractionParams params = AgentInteractionParams.builder()
    .agent(provisioned.id())
    .input("Explain the difference between HSL and RGB color systems.")
    .environment("remote")
    .build();

Interaction interaction = client.create(params);

// Delete custom agent when done
client.deleteAgent(provisioned.id());
```

### Budget & Token Controls

For specialized agents like `antigravity` or `deep-research`, set a strict token budget using `agentConfig`:

```java
import io.github.glaforge.gemini.interactions.model.InteractionParams.AgentInteractionParams;
import io.github.glaforge.gemini.interactions.model.Config.AntigravityAgentConfig;

AgentInteractionParams params = AgentInteractionParams.builder()
    .agent("antigravity-preview-05-2026")
    .input("Review recent commits.")
    .agentConfig(new AntigravityAgentConfig(10000L))
    .build();
```

### Environment Workspace (Reading & Extracting Sandbox Files)

After an agent interaction completes, inspect its remote sandbox files using `EnvironmentWorkspace`:

```java
import io.github.glaforge.gemini.interactions.EnvironmentWorkspace;
import java.nio.file.Path;

try (EnvironmentWorkspace workspace = client.getWorkspace(interaction.environmentId()).refresh()) {
    if (workspace.fileExists("output.json")) {
        String content = workspace.readTextFile("output.json");
        System.out.println("Output JSON: " + content);
        
        workspace.downloadFile("chart.png", Path.of("./chart.png"));
    }
}
```

### Standalone Environments Management

Create, retrieve, list, and delete standalone execution environments:

```java
import io.github.glaforge.gemini.interactions.model.Environment;
import io.github.glaforge.gemini.interactions.model.EnvironmentNetworkEgressAllowlist;
import io.github.glaforge.gemini.interactions.model.AllowlistEntry;
import io.github.glaforge.gemini.interactions.model.ListEnvironmentsResponse;
import java.util.List;

// Provision environment with egress allowlist
Environment env = client.createEnvironment(
    new EnvironmentNetworkEgressAllowlist(List.of(new AllowlistEntry("github.com"))),
    null
);

// Get metadata
Environment retrieved = client.getEnvironment(env.id());
System.out.println("Environment status: " + retrieved.status() + ", files: " + retrieved.fileCount());

// List environments
ListEnvironmentsResponse response = client.listEnvironments();

// Delete environment
client.deleteEnvironment(env.id());
```

### Triggers (Scheduling & Automation)

Set up CRON schedules to automatically run agents in the background:

```java
import io.github.glaforge.gemini.interactions.model.TriggerCreateParams;
import io.github.glaforge.gemini.interactions.model.InteractionParams.AgentInteractionParams;

TriggerCreateParams params = TriggerCreateParams.builder()
    .displayName("Daily Audit")
    .schedule("0 0 * * *")
    .interaction(AgentInteractionParams.builder()
        .agent("antigravity-preview-05-2026")
        .input("Audit the codebase.")
        .build())
    .build();

client.createTrigger(params);
```

### Type-Safe Union Configuration Records

The SDK uses specialized union records instead of untyped `Object` fields to handle polymorphic API configuration payloads:

- **`SpeechConfiguration`**: Wraps either single-speaker `List<SpeechConfig>` or multi-speaker `SpeakerConfig` (`isSingleSpeaker()`, `isMultiSpeaker()`).
- **`NetworkConfiguration`**: Wraps either a preset network string (e.g. `"disabled"`, `"allow_all"`) or custom `EnvironmentNetworkEgressAllowlist` (`isPreset()`, `isCustom()`).
- **`BaseEnvironment`**: Wraps either a preset base environment string (e.g. `"default"`, `"remote"`) or custom `EnvironmentConfig` (`isPreset()`, `isCustom()`).
- **`MediaProcessingConfiguration`**: Wraps either a preset media mode string (e.g. `"static"`, `"agentic"`) or custom `Content.MediaProcessing` (`isPreset()`, `isCustom()`).

