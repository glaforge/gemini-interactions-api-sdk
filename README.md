# Gemini Interactions SDK for Java

A modern Java SDK for the [Google Gemini Interactions API](https://ai.google.dev/gemini-api/docs/interactions).

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
> are now nested within these steps. However, the SDK provides convenience getters like `interaction.outputText()`, `interaction.outputImage()`, `interaction.outputAudio()`, and `interaction.outputVideo()` to dynamically extract the final output from the steps sequence for you. Furthermore, Server-Sent Events
> (SSE) now use `StepDelta` instead of `ContentDelta`.

## Installation

To use the core client SDK, add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.glaforge</groupId>
    <artifactId>gemini-interactions-api-sdk</artifactId>
    <version>2.0.1</version>
</dependency>
```

If you are implementing the server-side webhook handler (`InteractionsHandler`), add the server dependency:

```xml
<dependency>
    <groupId>io.github.glaforge</groupId>
    <artifactId>gemini-interactions-server</artifactId>
    <version>2.0.1</version>
</dependency>
```

> [!NOTE]
> Check [Maven Central](https://central.sonatype.com/artifact/io.github.glaforge/gemini-interactions-api-sdk) to find the latest available version of these artifacts.

## AI Agent Skill
[![skills.sh](https://skills.sh/b/glaforge/gemini-interactions-api-sdk)](https://skills.sh/glaforge/gemini-interactions-api-sdk)

This project provides an officially maintained [Agent Skill](skills/gemini-interactions-java-api/SKILL.md) to help your AI coding assistants write code using the SDK.
If you use an agentic IDE or AI coding assistant that supports the [Agent Skills specification](https://agentskills.io/home), it should automatically discover and use this skill when you are working with the SDK in your projects. 

You can also install this skill across your projects globally using the [Vercel skills CLI](https://skills.sh/):

```bash
npx skills add glaforge/gemini-interactions-api-sdk
```

This will automatically install the skill into the `.agents/skills/gemini-interactions-java-api` directory at the root of your workspace, allowing compatible AI coding assistants to load it instantly.

## Usage

### Initialization

#### Option A: Google AI Studio (Default)
```java
import io.github.glaforge.gemini.interactions.GeminiInteractionsClient;

GeminiInteractionsClient client = GeminiInteractionsClient.builder()
    .apiKey(System.getenv("GEMINI_API_KEY"))
    .build();
```

#### Option B: Google Cloud Vertex AI
```java
import io.github.glaforge.gemini.interactions.GeminiInteractionsClient;

// When using Vertex AI, the SDK automatically uses Google Cloud Application Default Credentials (ADC)
// and dynamically constructs the appropriate enterprise endpoints.
GeminiInteractionsClient client = GeminiInteractionsClient.builder()
    .project("your-google-cloud-project-id")
    .location("global") // Defaults to "global", can also be specific regions
    .build();
```

> [!NOTE]
> Currently, standard Gemini models are not supported directly on Google Cloud via the Gemini Interactions API endpoint. However, you can generate an API key from your Google Cloud Project and use it with the usual API key approach (Option A) to interact with Gemini models.

### Simple Text Interaction
```java
ModelInteractionParams request = ModelInteractionParams.builder()
    .model("gemini-2.5-flash")
    .input("Why is the sky blue?")
    .build();

Interaction response = client.create(request);
System.out.println(response.outputText());
```

### Configuration (GenerationConfig)

You can customize the model's generation behavior using the fluent `GenerationConfig` builder:

```java
import io.github.glaforge.gemini.interactions.model.Config.GenerationConfig;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import io.github.glaforge.gemini.interactions.model.Interaction;

GenerationConfig config = GenerationConfig.builder()
    .temperature(0.7)
    .topP(0.9)
    .maxOutputTokens(1024)
    .build();

ModelInteractionParams request = ModelInteractionParams.builder()
    .model("gemini-2.5-flash")
    .input("Write a short story about a brave knight.")
    .generationConfig(config)
    .build();

Interaction response = client.create(request);
```

### Safety Settings

You can customize the safety settings for an interaction to control the likelihood of the model generating harmful content.

```java
import io.github.glaforge.gemini.interactions.model.SafetySetting;
import io.github.glaforge.gemini.interactions.model.HarmCategory;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import io.github.glaforge.gemini.interactions.model.Interaction;
import java.util.List;

ModelInteractionParams request = ModelInteractionParams.builder()
    .model("gemini-2.5-flash")
    .input("How do I make gunpowder?")
    .safetySettings(List.of(
        new SafetySetting(HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT, "BLOCK_LOW_AND_ABOVE", null)
    ))
    .build();

Interaction response = client.create(request);
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

### Speech Recognition & Transcription (ASR)
```java
import io.github.glaforge.gemini.interactions.model.Config.GenerationConfig;
import io.github.glaforge.gemini.interactions.model.Config.TranscriptionConfig;
import io.github.glaforge.gemini.interactions.model.ModelOption;
import io.github.glaforge.gemini.interactions.model.Content.*;
import java.util.List;

TranscriptionConfig transcriptionConfig = TranscriptionConfig.builder()
    .languageHints(List.of("en-US", "auto"))
    .mode(new Config.VerbatimTranscriptionMode("speaker", List.of("word")))
    .build();

GenerationConfig generationConfig = GenerationConfig.builder()
    .transcriptionConfig(transcriptionConfig)
    .build();

ModelInteractionParams request = ModelInteractionParams.builder()
    .model(ModelOption.GEMINI_3_6_FLASH)
    .input(
        new TextContent("Transcribe and annotate speaker labels"),
        new AudioContent(audioBytes, "audio/wav")
    )
    .generationConfig(generationConfig)
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

### Audio Output & Multi-Speaker Synthesis
```java
import io.github.glaforge.gemini.interactions.model.Content.*;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import io.github.glaforge.gemini.interactions.model.Interaction.Modality;
import io.github.glaforge.gemini.interactions.model.Config.GenerationConfig;
import io.github.glaforge.gemini.interactions.model.Config.SpeechConfig;
import io.github.glaforge.gemini.interactions.model.Config.SpeakerConfig;
import io.github.glaforge.gemini.interactions.model.SpeechConfiguration;
import java.util.List;

// Single speaker configuration
GenerationConfig genConfig = GenerationConfig.builder()
    .speechConfig(List.of(new SpeechConfig("Puck", "en-US")))
    .build();

// Or multi-speaker configuration
SpeakerConfig multiSpeaker = new SpeakerConfig(List.of(
    new SpeechConfig("Algenib", "en-US", "speaker1"),
    new SpeechConfig("Kore", "en-US", "speaker2")
));
GenerationConfig multiSpeakerGenConfig = GenerationConfig.builder()
    .speechConfig(multiSpeaker)
    .build();

ModelInteractionParams request = ModelInteractionParams.builder()
    .model("gemini-2.5-flash-preview-tts")
    .input("Hey, we can generate audio too!")
    .responseModalities(Modality.AUDIO, Modality.TEXT)
    .generationConfig(genConfig)
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

### Agentic Video Understanding
Enable model-driven dynamic video navigation (`"processing": "agentic"`) to let Gemini models actively search, zoom, and inspect video segments rather than relying purely on fixed-interval frame sampling:

```java
import io.github.glaforge.gemini.interactions.model.*;
import io.github.glaforge.gemini.interactions.model.Content.*;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;

Content.VideoContent videoInput = new Content.VideoContent(
    "video",
    null,
    "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
    "video/mp4",
    "sample_clip",
    Content.Resolution.HIGH,
    MediaProcessingConfiguration.of("agentic")
);

ModelInteractionParams request = ModelInteractionParams.builder()
    .model(ModelOption.GEMINI_3_7_FLASH)
    .input(
        new TextContent("What happens at the key moment of this video?"),
        videoInput
    )
    .build();

Interaction response = client.create(request);
System.out.println(response.outputText());

// Inspect server-initiated processing steps
for (Step step : response.steps()) {
    if (step instanceof Step.ProcessingCallStep procCall) {
        System.out.println("Processing Call: " + procCall.id());
    } else if (step instanceof Step.ProcessingResultStep procResult) {
        System.out.println("Processing Result for Call: " + procResult.callId());
    }
}
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
while (!interaction.status().isFinished()) {
    Thread.sleep(1000);
    interaction = client.get(interaction.id());
}

System.out.println(interaction.steps());
```

### Custom Agents (CRUD & Remote Sandboxing)

The SDK provides first-class support for creating, managing, and running **Custom Agents** in secure remote or local sandboxed environments.

#### 1. Defining and Provisioning a Custom Agent
You can easily provision custom agents configured with system instructions, specific base models/agents, mounted sources, custom tools, and fine-grained network egress rules:

```java
import io.github.glaforge.gemini.interactions.model.*;
import java.util.List;

Agent customAgent = Agent.builder()
    .id("my-concise-coder-agent")
    .description("A custom agent built for secure coding tasks.")
    .baseAgent("antigravity-preview-05-2026")
    .baseEnvironment("remote") // Run inside a secure, remote Linux sandbox
    .systemInstruction("You are a helpful coding assistant. Always respond concisely.")
    .tools(List.of(
        new AgentTool.GoogleSearch(), // Enable grounded search
        new AgentTool.CodeExecution() // Enable secure code execution in the sandbox
    ))
    .environmentConfig(EnvironmentConfig.builder()
        .network(EnvironmentNetworkEgressAllowlist.builder()
            .allowlist(List.of(
                new AllowlistEntry("github.com", List.of("*:443")) // Safely allow GitHub traffic
            ))
            .build()
        )
        .build()
    )
    .build();

// Create the custom agent
Agent provisioned = client.createAgent(customAgent);
```

#### 2. Running an Interaction with the Custom Agent
To run interactions against your custom agent, use `AgentInteractionParams` specifying your agent's unique ID:

```java
import io.github.glaforge.gemini.interactions.model.InteractionParams.AgentInteractionParams;

AgentInteractionParams params = AgentInteractionParams.builder()
    .agent("my-concise-coder-agent")
    .input("Explain the difference between HSL and RGB color systems.")
    .environment("remote") // Run in remote sandboxed workspace
    .build();

Interaction interaction = client.create(params);

// Poll remote sandbox until completion
while (!interaction.status().isFinished()) {
    Thread.sleep(2000);
    interaction = client.get(interaction.id());
}

System.out.println(interaction.steps());
```

#### 3. Inspecting & Reading Files from an Agent's Environment Workspace

The SDK provides two complementary ways to work with an agent's remote environment files:

> [!TIP]
> **Choosing the right approach:**
> - **`EnvironmentWorkspace` (Local TAR Cache)**: Best when you need to **read, extract, or download actual file contents** (text, images, data). Downloads the workspace archive once into a temporary local cache so subsequent file reads (`readTextFile`, `readBinaryFile`, `downloadFile`) incur zero extra network calls.
> - **`client.getEnvironmentFiles(...)` (REST API Metadata Inspection)**: Best when you want to **browse directory trees, check file existence, or inspect file sizes & timestamps** directly via the API with pagination, without downloading the entire TAR archive.

##### Option A: Bulk File Content Reading & Extraction (`EnvironmentWorkspace`)
```java
import io.github.glaforge.gemini.interactions.EnvironmentWorkspace;
import java.nio.file.Path;

// Download workspace snapshot once and inspect file contents locally
try (EnvironmentWorkspace workspace = client.getWorkspace(interaction.environmentId()).refresh()) {
    if (workspace.fileExists("output.json")) {
        // Read file contents directly into a String
        String content = workspace.readTextFile("output.json");
        System.out.println(content);
        
        // Save binary files (e.g. charts, PDFs) to your local disk
        workspace.downloadFile("chart.png", Path.of("/local/path/chart.png"));
    }

    // Extract all environment files to a local directory
    workspace.extractAll(Path.of("./local/extracted_snapshot"));
}

// Or download the complete raw TAR snapshot directly to a file:
client.downloadEnvironment(interaction.environmentId(), Path.of("./snapshot.tar"));
```

##### Option B: Direct Single File & Directory Downloads (`downloadEnvironmentFile`)
Download individual files or subdirectories directly without fetching the full environment archive:
```java
import java.io.InputStream;
import java.nio.file.Path;

// Download single file content directly as bytes or save to disk
byte[] codeBytes = client.downloadEnvironmentFileBytes(envId, "src/main.py");
client.downloadEnvironmentFile(envId, "src/main.py", Path.of("./main.py"));

// Stream raw content
try (InputStream in = client.downloadEnvironmentFile(envId, "workspace/report.md")) {
    // Process stream...
}

// Download a subdirectory as a TAR archive (with recursive=true)
client.downloadEnvironmentFile(envId, "src", true, Path.of("./src.tar"));
```

##### Option C: Uploading Files & Archives into Sandbox (`uploadEnvironmentFile` & `uploadEnvironmentArchive`)
Upload files or seed entire directory structures dynamically into an active sandbox:
```java
import java.nio.file.Path;

// Upload a single text file (with overwrite protection)
client.uploadEnvironmentFile(envId, "workspace/config.json", "{\"debug\": true}", true);

// Upload a local binary file
client.uploadEnvironmentFile(envId, "workspace/data.bin", Path.of("./local/data.bin"), true);

// Upload and automatically extract a .tar or .tar.gz directory archive into destination path
client.uploadEnvironmentArchive(envId, "workspace/src/", Path.of("./source.tar.gz"), true);
```

##### Option D: Remote Metadata Inspection & Directory Browsing (`getEnvironmentFiles`)
```java
import io.github.glaforge.gemini.interactions.model.GetEnvironmentFilesResponse;
import io.github.glaforge.gemini.interactions.model.EnvironmentFile;
import java.util.Optional;

// Fetch metadata for a specific file
Optional<EnvironmentFile> file = client.getEnvironmentFile(envId, "src/main.py");
file.ifPresent(f -> System.out.println(f.name() + ": " + f.sizeBytes() + " bytes"));

// Inspect directory tree metadata via API
GetEnvironmentFilesResponse filesResponse = client.getEnvironmentFiles(
    interaction.environmentId(), 
    "workspace", // path
    50,          // pageSize
    null,        // pageToken
    true         // recursive
);

filesResponse.files().forEach(f -> {
    System.out.println(f.path() + " (" + f.type() + ", " + f.sizeBytes() + " bytes)");
});
```

#### 4. Managing Standalone Environments
The SDK supports full CRUD operations for standalone execution environments (`Environment` resources):

```java
import io.github.glaforge.gemini.interactions.model.Environment;
import io.github.glaforge.gemini.interactions.model.EnvironmentNetworkEgressAllowlist;
import io.github.glaforge.gemini.interactions.model.AllowlistEntry;
import io.github.glaforge.gemini.interactions.model.ListEnvironmentsResponse;
import java.util.List;

// Create a standalone execution environment with egress allowlists
Environment env = client.createEnvironment(
    new EnvironmentNetworkEgressAllowlist(List.of(new AllowlistEntry("github.com"))),
    null
);

// Retrieve environment metadata
Environment retrieved = client.getEnvironment(env.id());
System.out.println("Status: " + retrieved.status() + ", files: " + retrieved.fileCount());

// List environments with pagination
ListEnvironmentsResponse response = client.listEnvironments(10, null);
response.environments().forEach(e -> System.out.println(e.id() + ": " + e.status()));

// Clone / fork an existing environment directly from a snapshot
Environment cloned = client.createEnvironmentFrom(env.id());

// Delete environment
client.deleteEnvironment(env.id());
```

#### 5. Server-Managed Credentials (Provisioning, Injection & Rotation)
The SDK supports Google's server-managed Credentials API, allowing developers to securely provision API keys, OAuth tokens, and environment variables into agent environments and tools without exposing secrets in client logs or model context:

```java
import io.github.glaforge.gemini.interactions.model.Credential;
import io.github.glaforge.gemini.interactions.model.CredentialType;
import io.github.glaforge.gemini.interactions.model.BearerTokenConfig;
import io.github.glaforge.gemini.interactions.model.OAuth2Config;
import io.github.glaforge.gemini.interactions.model.EnvironmentVariableConfig;
import io.github.glaforge.gemini.interactions.model.CredentialUpdate;
import io.github.glaforge.gemini.interactions.model.ListCredentialsResponse;
import io.github.glaforge.gemini.interactions.model.Tool;
import io.github.glaforge.gemini.interactions.model.EnvironmentConfig;

// 1. Provision a Bearer token credential (secret is write-only, never returned by server)
Credential jiraCred = client.createCredential(Credential.builder()
    .id("jira-api-token")
    .bearerToken(BearerTokenConfig.builder()
        .token("secret-token-12345")
        .headerName("Authorization")
        .prefix("Bearer")
        .build())
    .build());

// 2. Provision an OAuth2 credential
Credential oauthCred = client.createCredential(Credential.oauth2("github-oauth", OAuth2Config.builder()
    .clientId("client-id-xyz")
    .clientSecret("client-secret-xyz")
    .refreshToken("refresh-token-xyz")
    .tokenUrl("https://github.com/login/oauth/access_token")
    .scopes("repo", "read:user")
    .build()));

// 3. Provision an environment variable credential with trusted domains
Credential slackCred = client.createCredential(Credential.environmentVariable("slack-token", EnvironmentVariableConfig.builder()
    .value("xoxb-secret-slack-token")
    .injectionLocation("header")
    .trustedDomains("*.slack.com", "slack.com")
    .build()));

// 4. Reference credentials in MCP Tools
Tool.McpServer mcpTool = Tool.McpServer.builder()
    .name("jira-tool")
    .url("https://jira.internal.net/sse")
    .credential("jira-api-token")
    .build();

// 5. Inject credentials into Environment sandbox containers
EnvironmentConfig envConfig = EnvironmentConfig.builder()
    .env("DEBUG", "true")
    .envCredential("SLACK_BOT_TOKEN", "slack-token")
    .build();

// 6. Inspect metadata, list, rotate secrets, and delete
Credential meta = client.getCredential("jira-api-token");
ListCredentialsResponse list = client.listCredentials(10, null);

// Rotate secret without changing referencing agents
client.updateCredential("jira-api-token", CredentialUpdate.ofBearerToken("rotated-secret-token"));

// Delete credential
client.deleteCredential("jira-api-token");
```

#### 6. Refreshing Credentials on Existing Environments
When tokens expire during multi-step tasks, you can update network credentials on an active sandbox while preserving all installed packages, repositories, and workspace files:

```java
import io.github.glaforge.gemini.interactions.model.AllowlistEntry;
import io.github.glaforge.gemini.interactions.model.EnvironmentConfig;
import io.github.glaforge.gemini.interactions.model.EnvironmentNetworkEgressAllowlist;
import io.github.glaforge.gemini.interactions.model.InteractionParams.AgentInteractionParams;
import java.util.List;
import java.util.Map;

// Configure updated network allowlist with refreshed token (egress proxy header injection)
EnvironmentNetworkEgressAllowlist refreshedNetwork = new EnvironmentNetworkEgressAllowlist(List.of(
    AllowlistEntry.of("storage.googleapis.com", "Authorization", "Bearer " + refreshedGcsToken),
    AllowlistEntry.of("api.github.com", Map.of("Authorization", "Bearer " + refreshedGithubPat)),
    AllowlistEntry.of("*") // Allow all other outbound traffic without transformation
));

// Update the existing environment during interaction creation
AgentInteractionParams refreshParams = AgentInteractionParams.builder()
    .agent("antigravity-preview-05-2026")
    .input("Fetch the latest reports from gs://my-bucket/data/ and create a PR on GitHub.")
    .environment(EnvironmentConfig.forExisting(env.id(), refreshedNetwork))
    .build();

Interaction refreshedInteraction = client.create(refreshParams);
```

#### 7. Listing, Retrieving, and Deleting Agents
The SDK supports standard management CRUD endpoints:

```java
// List agents with pagination
ListAgentsResponse listResponse = client.listAgents(10, null);
listResponse.agents().forEach(System.out::println);

// Retrieve agent details
Agent retrieved = client.getAgent("my-concise-coder-agent");

// Delete custom agent
client.deleteAgent("my-concise-coder-agent");
```

#### 8. Server-Side Handling (InteractionsHandler)

> [!NOTE]
> To use `InteractionsHandler`, make sure you have added the `gemini-interactions-server` dependency to your project.

If you are exposing interactions endpoints or webhooks using `InteractionsHandler`, you can seamlessly plug in agent management support. Simply extend the handler and override the default concrete agent methods:

```java
import io.github.glaforge.gemini.interactions.server.InteractionsHandler;

public class MyInteractionsServer extends InteractionsHandler {
    @Override
    public Agent createAgent(Agent agent) {
        // Expose custom agent creation logic (e.g. database persistence)
        return agent;
    }

    @Override
    public Agent getAgent(String id) {
        // Expose agent retrieval logic
        return fetchAgentFromDB(id);
    }
}
```

### Antigravity Agent & Budget Controls

The **Antigravity agent** (`antigravity-preview-09-2026`) is a general-purpose managed agent powered by Gemini 3.8 Flash that can reason, run code (Bash, Python, Node.js), manage files, and search the web inside an isolated remote Linux sandbox.

You can configure its underlying model (e.g. `gemini-3.8-flash`, `gemini-3.5-flash-lite`), set token budget caps with `AntigravityAgentConfig.builder()`, and seamlessly continue executions when the token budget is reached (`status: "incomplete"`):

```java
import io.github.glaforge.gemini.interactions.model.AgentOption;
import io.github.glaforge.gemini.interactions.model.ModelOption;
import io.github.glaforge.gemini.interactions.model.Config.AntigravityAgentConfig;
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.InteractionParams.AgentInteractionParams;

// 1. Run Antigravity with a token budget
AgentInteractionParams params = AgentInteractionParams.builder()
    .agent(AgentOption.ANTIGRAVITY_PREVIEW_09_2026)
    .input("Review recent commits and run test suite.")
    .environment("remote")
    .agentConfig(AntigravityAgentConfig.builder()
        .model(ModelOption.GEMINI_3_8_FLASH)
        .maxTotalTokens(50000L) // Budget cap
        .build())
    .build();

Interaction response = client.create(params);

// 2. If status is incomplete, continue in the same environment:
if (response.status().isIncomplete()) {
    Interaction continuation = client.create(
        AgentInteractionParams.builder()
            .agent(AgentOption.ANTIGRAVITY_PREVIEW_09_2026)
            .input("continue")
            .previousInteractionId(response.id())
            .environment(response.environmentId())
            .agentConfig(AntigravityAgentConfig.builder().maxTotalTokens(50000L).build())
            .build()
    );
}
```

### Triggers (Scheduling & Automation)

You can set up CRON-like schedules to automatically run agents in the background. This is useful for periodic auditing, continuous integration, or recurring tasks.

```java
import io.github.glaforge.gemini.interactions.model.AgentOption;
import io.github.glaforge.gemini.interactions.model.Trigger;
import io.github.glaforge.gemini.interactions.model.TriggerCreateParams;
import io.github.glaforge.gemini.interactions.model.InteractionParams.AgentInteractionParams;
import io.github.glaforge.gemini.interactions.model.Config.AntigravityAgentConfig;

TriggerCreateParams params = TriggerCreateParams.builder()
    .displayName("Daily Security Audit")
    .schedule("0 0 * * *") // Run daily at midnight
    .interaction(AgentInteractionParams.builder()
        .agent(AgentOption.ANTIGRAVITY_PREVIEW_09_2026)
        .input("Audit the codebase for hardcoded secrets.")
        .agentConfig(AntigravityAgentConfig.builder().maxTotalTokens(50000L).build()) // Budget cap: 50K tokens
        .build())
    .build();

Trigger trigger = client.createTrigger(params);
System.out.println("Created Trigger ID: " + trigger.id());

// Pause and resume triggers
client.pauseTrigger(trigger.id());
client.resumeTrigger(trigger.id());

// Run immediately on demand
TriggerExecution execution = client.runTrigger(trigger.id());

// Query and list triggers by filter/status
ListTriggersResponse activeTriggers = client.listTriggers("status=active", 10, null);

// Inspect the trigger's scheduled interaction template or resource in a type-safe way
TriggerInteraction interaction = trigger.interaction();
if (interaction.isRequest()) {
    InteractionParams.Request req = interaction.request();
} else if (interaction.isResource()) {
    Interaction res = interaction.resource();
}

// List trigger executions
ListTriggerExecutionsResponse executions = client.listTriggerExecutions(trigger.id());
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

### Built-in Tools (Retrieval)

The SDK provides support for Retrieval tools like Google Search and Vertex AI Search using the fluent `Retrieval` builder:

```java
import io.github.glaforge.gemini.interactions.model.Tool.Retrieval;
import io.github.glaforge.gemini.interactions.model.Tool.GoogleSearchRetrieval;
import io.github.glaforge.gemini.interactions.model.Tool.DynamicRetrievalConfig;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;

Retrieval retrievalTool = Retrieval.builder()
    .googleSearchRetrieval(new GoogleSearchRetrieval(
        new DynamicRetrievalConfig("unspecified", 0.7)
    ))
    .build();

ModelInteractionParams request = ModelInteractionParams.builder()
    .model("gemini-2.5-pro")
    .input("What are the latest advancements in quantum computing?")
    .tools(retrievalTool)
    .build();
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

## Type-Safe Union Records

To avoid untyped `Object` fields and provide compile-time safety when working with polymorphic payloads, the SDK provides dedicated union records with helper predicates:

- **`InteractionInput`**: Represents the polymorphic input to an interaction (`String text`, `List<Content> contents`, `List<Turn> turns`, `List<Step> steps`). Inspect with `input.isText()`, `input.isContents()`, `input.isTurns()`, `input.isSteps()`, or extract with `input.text()`, `input.contents()`, `input.turns()`, `input.steps()`.
- **`TurnContent`**: Represents multi-turn conversation turn content (`String text` or `List<Content> parts`). Inspect with `content.isText()`, `content.isParts()`, or use convenience methods directly on `Turn`: `turn.text()` and `turn.parts()`.
- **`BaseEnvironment`**: Configures an agent's sandbox environment as either a preset string (e.g. `"default"`, `"remote"`) or custom `EnvironmentConfig` (`isPreset()`, `isCustom()`, `preset()`, `config()`).
- **`TriggerInteraction`**: Encapsulates scheduled trigger interactions as either an interaction template `InteractionParams.Request` or a hydrated `Interaction` resource (`isRequest()`, `isResource()`, `request()`, `resource()`).
- **`ToolChoiceConfiguration`**: Configures tool choice mode as either a preset string (`"auto"`, `"any"`, `"none"`, `"validated"`) or detailed `Tool.ToolChoiceConfig` (`isMode()`, `isConfig()`).
- **`SpeechConfiguration`**: Encapsulates speech config as single-speaker `List<SpeechConfig>` or multi-speaker `SpeakerConfig` (`isSingleSpeaker()`, `isMultiSpeaker()`).
- **`NetworkConfiguration`**: Configures environment networking as a preset string (e.g. `"disabled"`, `"allow_all"`) or custom allowlist `EnvironmentNetworkEgressAllowlist` (`isPreset()`, `isCustom()`).
- **`MediaProcessingConfiguration`**: Configures media mode as a preset string (e.g. `"static"`, `"agentic"`) or custom `Content.MediaProcessing` (`isPreset()`, `isCustom()`).
- **`TranscriptionModeConfiguration`**: Configures audio transcription mode as a preset string or custom config (`isPreset()`, `isCustom()`).

## License
Apache 2.0

## Disclaimer
This is not an official Google project.

