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
    <groupId>com.google.ai</groupId>
    <artifactId>gemini-interactions-sdk</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## Usage

### Initialization
```java
GeminiInteractionsClient client = GeminiInteractionsClient.builder()
    .apiKey(System.getenv("GEMINI_API_KEY"))
    .build();
```

### Simple Text Interaction
```java
InteractionRequest request = InteractionRequest.builder()
    .model("gemini-2.5-flash")
    .input("Why is the sky blue?")
    .build();

Interaction response = client.create(request);
System.out.println(response.outputs().get(0));
```

### Multi-turn Conversation
```java
InteractionRequest request = InteractionRequest.builder()
    .model("gemini-2.5-flash")
    .input(List.of(
        Message.user("Hello!"),
        Message.model("Hi! How can I help?"),
        Message.user("Tell me a joke")
    ))
    .build();

Interaction response = client.create(request);
```

### Multimodal (Image)
```java
InteractionRequest request = InteractionRequest.builder()
    .model("gemini-2.5-flash")
    .input(List.of(
        new ContentPart.Text("text", "Describe this image"),
        new ContentPart.Image("BASE64_STRING...", "image/png")
    ))
    .build();

Interaction response = client.create(request);
```

### Image Generation (Nano Banana Pro)
```java
InteractionRequest request = InteractionRequest.builder()
    .model("gemini-3-pro-image-preview")
    .input("Create an infographic about blood, organs, and the circulatory system")
    .responseModalities(List.of("image"))
    .build();

Interaction interaction = client.create(request);

interaction.outputs().forEach(content -> {
    if (content instanceof Content.ImageContent image) {
        byte[] imageBytes = java.util.Base64.getDecoder().decode(image.data());
        // Save imageBytes to a file
    }
});
```

### Deep Research
```java
InteractionRequest request = InteractionRequest.builder()
    .agent("deep-research-pro-preview-12-2025")
    .input("Research the history of the Google TPUs")
    .build();

Interaction interaction = client.create(request);

// Poll for completion
while (interaction.status() != Interaction.Status.COMPLETED) {
    Thread.sleep(1000);
    interaction = client.get(interaction.id());
}

System.out.println(interaction.outputs());
```

### Function Calling
Define tools and handle responses using the `Tool` and `InteractionRequest` builders.

## License
Apache 2.0

## Disclaimer
This is not an official Google project.
