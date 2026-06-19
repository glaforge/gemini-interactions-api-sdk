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
    <version>0.11.0</version>
</dependency>
```

### Gradle
Add the dependency to your `build.gradle` or `build.gradle.kts`:

```gradle
implementation("io.github.glaforge:gemini-interactions-api-sdk:0.11.0")
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
Step.ModelOutputStep step = (Step.ModelOutputStep) response.steps().getLast();
System.out.println(step.content().get(0));
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

interaction.steps().forEach(step -> {
    if (step instanceof Step.ModelOutputStep modelOutputStep) {
        modelOutputStep.content().forEach(content -> {
            if (content instanceof ImageContent image) {
                byte[] imageBytes = image.data();
                System.out.println("Image generated with " + imageBytes.length + " bytes.");
            }
        });
    }
});
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
Step.ModelOutputStep step1 = (Step.ModelOutputStep) response1.steps().getLast();
System.out.println(step1.content().get(0));

// 2. Second turn (referencing previous ID)
ModelInteractionParams turn2 = ModelInteractionParams.builder()
    .model("gemini-2.5-flash")
    .input("Tell me a joke")
    .previousInteractionId(id)
    .store(true)
    .build();

Interaction response2 = client.create(turn2);
Step.ModelOutputStep step2 = (Step.ModelOutputStep) response2.steps().getLast();
System.out.println(step2.content().get(0));
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
Step.ModelOutputStep step = (Step.ModelOutputStep) response.steps().getLast();
System.out.println(step.content().get(0));
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
while (interaction.status() != Status.COMPLETED && interaction.status() != Status.FAILED) {
    try {
        Thread.sleep(2000);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
    interaction = client.get(interaction.id());
}

System.out.println(interaction.steps());
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
