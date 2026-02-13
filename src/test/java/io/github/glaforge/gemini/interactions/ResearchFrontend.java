package io.github.glaforge.gemini.interactions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import io.github.glaforge.gemini.interactions.model.Config.DeepResearchAgentConfig;
import io.github.glaforge.gemini.interactions.model.Config.ThinkingSummaries;
import io.github.glaforge.gemini.interactions.model.Content.ImageContent;
import io.github.glaforge.gemini.interactions.model.Content.TextContent;
import io.github.glaforge.gemini.interactions.model.Events.ContentDelta;
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
            Jt.title("🔎 Deep Research Agent").use();

            // subject form
            var formSubject = Jt.form().key("form_subject").use();
            String subject = Jt.textArea("Subject")
                    .key("subject")
                    .placeholder("Enter the subject you want to research...")
                    .use(formSubject);
            var columns = Jt.columns(2)
                    .widths(List.of(0.9, 0.1))
                    .use(formSubject);

            Jt.formSubmitButton("Clear All").onClick(b -> {
                Jt.setComponentState("subject", "");
                Jt.sessionState().remove("topics");
            }).use(columns.col(1));

            Jt.formSubmitButton("Explore Topics").type("primary").onClick(b -> {
                Jt.sessionState().remove("topics");
            }).use(columns.col(0));

            if (subject == null || subject.isBlank()) {
                // wait for user to set subject and hit form submit button
                return;
            }

            // topics selection form
            Jt.header("Topics").use();
            var formTopics = Jt.form().key("form_topics").use();
            var topicsContainer = Jt.empty().key("topics_container").use(formTopics);

            @SuppressWarnings("unchecked")
            List<String> topics = (List<String>) Jt.sessionState().computeIfAbsent("topics", k -> {
                Jt.info("Preparing topics...").icon(":hourglass:").use(topicsContainer);

                ModelInteractionParams planParams = ModelInteractionParams.builder()
                        .model("gemini-3-flash-preview")
                        .input(String.format("""
                                Find a list of topics to research on the following subject:
                                %s
                                """, subject))
                        .responseFormat(GSchema.fromClass(String[].class))
                        .tools(new GoogleSearch())
                        .store(true)
                        .build();

                Interaction planInteraction = client.create(planParams);

                return getTopics(planInteraction);
            });

            var topicSelectionContainer = Jt.container().key("topics").use(topicsContainer);

            List<String> selectedTopics = topics.stream()
                    .filter(topic -> Jt.checkbox(topic).use(topicSelectionContainer))
                    .toList();

            Jt.formSubmitButton("Launch Research").type("primary").use(formTopics);

            if (selectedTopics.isEmpty()) {
                // wait for user to select topics and hit form submit button
                return;
            }

            // reporting tabs
            Jt.header("Report").use();

            var tabs = Jt.tabs(List.of("Full Report", "Summary", "Infographic")).use();

            var reportContainer = Jt.container().key("reportContainer").use(tabs.tab(0));
            var reportPlaceholder = Jt.empty().key("fullReport").use(reportContainer);
            var summaryPlaceholder = Jt.empty().key("summary").use(tabs.tab(1));
            var infographicPlaceholder = Jt.empty().key("infographic").use(tabs.tab(2));

            /// put placeholders while results are being computed
            Jt.info("Preparing full report...").icon(":hourglass:").use(reportPlaceholder);
            Jt.info("Preparing summary...").icon(":hourglass:").use(summaryPlaceholder);
            Jt.info("Preparing infographic...").icon(":hourglass:").use(infographicPlaceholder);

            /// compute/fetch report section
            var topicsList = selectedTopics.stream().map(t -> "- " + t).collect(Collectors.joining("\n"));
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
                            """, subject, topicsList))
                    .background(true)
                    .stream(true)
                    .agentConfig(new DeepResearchAgentConfig(ThinkingSummaries.AUTO))
                    .store(true)
                    .build();

            StringBuilder reportBuilder = new StringBuilder();

            long startTime = System.currentTimeMillis();

            client.stream(researchParams).forEach(event -> {
                if (event instanceof ContentDelta delta) {
                    if (delta.delta() instanceof ThoughtSummaryDelta thought) {
                        if (thought.content() instanceof TextContent textContent) {
                            long elapsed = System.currentTimeMillis() - startTime;
                            String timeString = String.format("%dm%ds", elapsed / 60000, (elapsed % 60000) / 1000);
                            Jt.markdown("⏱️ `" + timeString + "` " + textContent.text()).use(reportPlaceholder);
                        }
                    } else if (delta.delta() instanceof TextDelta textPart) {
                        reportBuilder.append(textPart.text());
                        Jt.markdown(transformCitations(reportBuilder.toString())).use(reportPlaceholder);
                    }
                } else {
                    System.out.printf("%nEVENT: %s\n", event);
                }
            });

            var rawReportExpander = Jt.expander("Raw Markdown Report").use(reportContainer);
            Jt.text(transformCitations(reportBuilder.toString())).use(rawReportExpander);

            // compute/fetch summary
            ModelInteractionParams summaryParams = ModelInteractionParams.builder()
                    .model("gemini-3-pro-preview")
                    .input(String.format("""
                            Create a concise summary of the research below.
                            Go straight with the summary, don't introduce the summary
                            (don't write "Here's a summary..." or equivalent).

                            %s
                            """, reportBuilder))
                    .store(true)
                    .build();

            Interaction summaryInteraction = client.create(summaryParams);
            String summaryText = getText(summaryInteraction);
            Jt.markdown(summaryText).use(summaryPlaceholder);

            // compute/fetch infographics
            ModelInteractionParams infographicParams = ModelInteractionParams.builder()
                    .model("gemini-3-pro-image-preview")
                    .input(String.format("""
                            Create a hand-drawn and hand-written sketchnote style summary infographic,
                            with a pure white background, use fluo highlighters for the key points,
                            about the following information:

                            %s
                            """, summaryText))
                    .responseModalities(Interaction.Modality.IMAGE)
                    .build();

            Interaction infographicInteraction = client.create(infographicParams);
            var imageBytes = getInfographicData(infographicInteraction);

            Jt.image(imageBytes).use(infographicPlaceholder);
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

    static String transformCitations(String report) {
        if (report == null || report.isBlank()) {
            return report;
        }

        // 1. Find the sources section
        int sourcesIndex = report.lastIndexOf("**Sources:**");
        if (sourcesIndex == -1) {
            sourcesIndex = report.lastIndexOf("Sources:");
        }

        if (sourcesIndex == -1) {
            return report;
        }

        String contentBefore = report.substring(0, sourcesIndex);
        String sourcesSection = report.substring(sourcesIndex);

        // 2. Parse sources to build ID -> URL map
        Map<String, String> urlMap = new HashMap<>();

        // Regex for parsing sources lines: "1. [Title](URL)"
        // Using multiline mode to match multiple lines
        Pattern sourcePattern = Pattern.compile("^\\s*(\\d+)\\.\\s+\\[.*?\\]\\((.*?)\\)", Pattern.MULTILINE);
        Matcher sourceMatcher = sourcePattern.matcher(sourcesSection);

        while (sourceMatcher.find()) {
            String id = sourceMatcher.group(1);
            String url = sourceMatcher.group(2);
            urlMap.put(id, url);
        }

        // 3. Replace citations in the content
        // Pattern for "[cite: 1, 2]"
        Pattern citePattern = Pattern.compile("\\[cite:\\s*([\\d,\\s]+)\\]");
        Matcher citeMatcher = citePattern.matcher(contentBefore);

        StringBuilder sb = new StringBuilder();
        while (citeMatcher.find()) {
            String idsPart = citeMatcher.group(1);
            String[] ids = idsPart.split(",");
            StringBuilder replaced = new StringBuilder();
            List<String> links = new ArrayList<>();
            for (String id : ids) {
                id = id.trim();
                String url = urlMap.get(id);
                if (url != null) {
                    links.add("[" + id + "](" + url + ")");
                } else {
                    links.add("[" + id + "](#source-" + id + ")");
                }
            }
            replaced.append("<sup>").append(String.join(", ", links)).append("</sup>");
            citeMatcher.appendReplacement(sb, Matcher.quoteReplacement(replaced.toString()));
        }
        citeMatcher.appendTail(sb);

        return sb.toString() + sourcesSection;
    }
}
