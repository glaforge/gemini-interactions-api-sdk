package io.github.glaforge.gemini.interactions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import io.github.glaforge.gemini.interactions.model.Config.DeepResearchAgentConfig;
import io.github.glaforge.gemini.interactions.model.Config.ThinkingSummaries;
import io.github.glaforge.gemini.interactions.model.Content.ImageContent;
import io.github.glaforge.gemini.interactions.model.Content.TextContent;
import io.github.glaforge.gemini.interactions.model.Events;
import io.github.glaforge.gemini.interactions.model.Events.ContentDelta;
import io.github.glaforge.gemini.interactions.model.Events.InteractionEvent;
import io.github.glaforge.gemini.interactions.model.Events.TextDelta;
import io.github.glaforge.gemini.interactions.model.Events.ThoughtSummaryDelta;
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.InteractionParams.AgentInteractionParams;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import io.github.glaforge.gemini.interactions.model.Tool.GoogleSearch;
import io.github.glaforge.gemini.schema.GSchema;
import io.javelit.core.Jt;
import io.javelit.core.Server;

public class ResearchFrontend {

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    private static class ResearchState {
        public boolean exploringTopics = false;
        public boolean researching = false;
        public String subject = "";
        public List<String> topics = new ArrayList<>();
        public List<String> selectedTopics = new ArrayList<>();
        public String report = "";
        public String summary = "";
        public byte[] imageBytes;
    }

    public static void main(String[] args) {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null) {
            Server.builder(() -> Jt.error("GEMINI_API_KEY environment variable not set").use(), 8080).build().start();
            return;
        }

        GeminiInteractionsClient client = GeminiInteractionsClient.builder()
                .apiKey(apiKey)
                .build();

        Server.builder(() -> {
            ResearchState state = (ResearchState) Jt.sessionState().computeIfAbsent("state", k -> new ResearchState());

            Jt.title("🔎 Deep Research Agent").use();

            Jt.header("Subject").use();
            var formSubject = Jt.form().key("form_subject").use();
            state.subject = Jt.textArea("Subject")
                    .placeholder("Enter the subject you want to research...")
                    .value(state.subject != null ? state.subject : "")
                    .use(formSubject);

            var columns = Jt.columns(2).widths(List.of(0.9, 0.1)).use(formSubject);

            if (Jt.formSubmitButton("Clear All").use(columns.col(1))) {
                Jt.sessionState().remove("state");
                Jt.rerun();
            }

            if (Jt.formSubmitButton("Explore Topics").type("primary").use(columns.col(0)) || state.exploringTopics) {

                Jt.header("Topics").use();
                var formTopics = Jt.form().key("form_topics").use();

                var topicsContainer = Jt.empty().key("topicsInfoBubble").use(formTopics);
                Jt.info("Preparing topics...").icon(":hourglass:").use(topicsContainer);

                if (!state.exploringTopics) {
                    ModelInteractionParams planParams = ModelInteractionParams.builder()
                            .model("gemini-3-flash-preview")
                            .input(String.format("""
                                    Find a list of topics to research on the following subject:
                                    %s
                                    """, state.subject))
                            .responseFormat(GSchema.fromClass(String[].class))
                            .tools(new GoogleSearch())
                            .store(true)
                            .build();

                    Interaction planInteraction = client.create(planParams);
                    state.topics = getTopics(planInteraction);
                }
                state.exploringTopics = true;

                var topicsHolder = Jt.container().key("topics").use(topicsContainer);
                state.selectedTopics = state.topics
                        .stream()
                        .filter(topic -> Jt.checkbox(topic).use(topicsHolder))
                        .toList();

                if (Jt.formSubmitButton("Launch Research").type("primary").use(formTopics) || state.researching) {
                    state.researching = true;

                    Jt.header("Report").use();

                    var tabLabels = List.of("Full Report", "Summary", "Infographic");
                    var tabs = Jt.tabs(tabLabels).use();

                    var reportPlaceholder = Jt.empty().key("fullReport").use(tabs.tab(tabLabels.get(0)));
                    var summaryPlaceholder = Jt.empty().key("summary").use(tabs.tab(tabLabels.get(1)));
                    var infographicPlaceholder = Jt.empty().key("infographic").use(tabs.tab(tabLabels.get(2)));

                    Jt.info("Preparing full report...").icon(":hourglass:").use(reportPlaceholder);
                    Jt.info("Preparing summary...").icon(":hourglass:").use(summaryPlaceholder);
                    Jt.info("Preparing infographic...").icon(":hourglass:").use(infographicPlaceholder);

                    var topicsList = state.selectedTopics.stream().map(t -> "- " + t).collect(Collectors.joining("\n"));
                    AgentInteractionParams researchParams = AgentInteractionParams.builder()
                            .agent("deep-research-pro-preview-12-2025")
                            .input(String.format("""
                                    Write a concise research report on the following subject:
                                    <subject>
                                    %s
                                    </subject>

                                    By focusing on the following topics:
                                    <topics>
                                    %s
                                    </topics>
                                    """,
                                    state.subject, topicsList))
                            .background(true)
                            .stream(true)
                            .agentConfig(new DeepResearchAgentConfig(ThinkingSummaries.AUTO))
                            .store(true)
                            .build();

                    StringBuilder reportBuilder = new StringBuilder();

                    client.stream(researchParams).forEach(event -> {
                        if (event instanceof ContentDelta delta) {
                            if (delta.delta() instanceof ThoughtSummaryDelta thought) {
                                if (thought.content() instanceof TextContent textContent) {
                                    Jt.markdown(textContent.text()).use(reportPlaceholder);
                                }
                            } else if (delta.delta() instanceof TextDelta textPart) {
                                reportBuilder.append(textPart.text());
                                Jt.markdown(reportBuilder.toString()).use(reportPlaceholder);
                            }
                        } else if (event instanceof InteractionEvent interactionEvent &&
                                Events.EventType.INTERACTION_COMPLETE == interactionEvent.eventType()) {
                            state.researching = false;
                            state.report = reportBuilder.toString();
                            Jt.markdown(state.report).use(reportPlaceholder);

                            ModelInteractionParams synthesisParams = ModelInteractionParams.builder()
                                    .model("gemini-3-pro-preview")
                                    .input(String.format("""
                                            Create a concise summary of the research below.
                                            Go straight with the summary, don't introduce the summary
                                            (don't write "Here's a summary..." or equivalent).

                                            %s
                                            """,
                                            state.report))
                                    .store(true)
                                    .build();

                            Interaction synthesisInteraction = client.create(synthesisParams);
                            state.summary = getText(synthesisInteraction);
                            Jt.markdown(state.summary).use(summaryPlaceholder);

                            ModelInteractionParams infographicParams = ModelInteractionParams.builder()
                                    .model("gemini-3-pro-image-preview")
                                    .input(String.format("""
                                            Create a hand-drawn and hand-written sketchnote style summary infographic,
                                            with a pure white background, use fluo highlighters for the key points,
                                            about the following information:

                                            %s
                                            """, state.summary))
                                    .responseModalities(List.of(Interaction.Modality.IMAGE))
                                    .build();

                            Interaction infographicInteraction = client.create(infographicParams);
                            state.imageBytes = getInfographicData(infographicInteraction);

                            Jt.image(state.imageBytes).use(infographicPlaceholder);
                        }
                    });
                }
            }
        }, 8080).build().start();
    }

    private static List<String> getTopics(Interaction interaction) {
        if (interaction.outputs() == null)
            return List.of();

        return interaction.outputs().stream()
                .filter(c -> c instanceof TextContent)
                .map(c -> ((TextContent) c).text())
                .flatMap(text -> {
                    try {
                        return MAPPER.readValue(text, new TypeReference<List<String>>() {
                        }).stream();
                    } catch (Exception e) {
                        return Stream.<String>empty();
                    }
                })
                .collect(Collectors.toList());
    }

    private static String getText(Interaction interaction) {
        if (interaction.outputs() == null)
            return "";
        return interaction.outputs().stream()
                .filter(c -> c instanceof TextContent)
                .map(c -> ((TextContent) c).text())
                .collect(Collectors.joining("\n"));
    }

    private static byte[] getInfographicData(Interaction interaction) {
        if (interaction.outputs() == null)
            return null;
        return interaction.outputs().stream()
                .filter(c -> c instanceof ImageContent)
                .map(c -> ((ImageContent) c).data())
                .findFirst()
                .orElse(null);
    }
}
