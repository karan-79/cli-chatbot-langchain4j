package org.example.raw;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.web.search.WebSearchOrganicResult;
import org.example.Tools;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class WithoutHighLevelAbstractionChat {

    public static void main(String[] args) {

        ChatLanguageModel model = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .strictTools(true)
                .modelName(OpenAiChatModelName.GPT_4_O_MINI_2024_07_18)
                .build();

//        var chatMemory = MessageWindowChatMemory.withMaxMessages(20);

        var toolSpecifications = ToolSpecifications.toolSpecificationsFrom(Tools.class);


        var chatMemory = new ArrayList<ChatMessage>();

        chatMemory.add(SystemMessage.from("You are an AI assistant chat bot who have ability to surf internet via tools," +
                " you must reply to users query in case it requires internet search do include the reference links if tool provides them"));

        var scanner = new Scanner(System.in);
        while (true) {

            try {
                System.out.print("User: ");
                var userInput = scanner.nextLine();

                if (List.of("q", "quit", "exit").contains(userInput.trim())) {
                    System.out.print("Assitant: Bye Bye");
                    break;
                }

                chatMemory.add(UserMessage.from(userInput));

//                var answer = model.generate(chatMemory.messages(), toolSpecifications);

                var answer = model.generate(chatMemory, toolSpecifications);
                chatMemory.add(answer.content());

                if (answer.content().hasToolExecutionRequests()) {
                    for (var toolReq : answer.content().toolExecutionRequests()) {
                        var toolName = toolReq.name();

                        if(!"searchInternet".equals(toolName)) {
                            continue;
                        }

                        var objMapper = new ObjectMapper();

                        var input = objMapper.readTree(toolReq.arguments()).get("arg0").textValue();

                        var toolResults = new Tools().searchInternet(input);

                        var toolResultContent = writeToolResultsInText(toolResults);

                        var toolMessage = ToolExecutionResultMessage.from(toolReq, toolResultContent);
                        chatMemory.add(toolMessage);


//                        var aiAnswerWithToolCall = model.generate(chatMemory.messages(), toolSpecifications);
                        var aiAnswerWithToolCall = model.generate(chatMemory, toolSpecifications);
                        System.out.println("Assistant (after tool execution): " +
                                aiAnswerWithToolCall.content().text());

                        chatMemory.add(aiAnswerWithToolCall.content());

                    }
                } else {
                    System.out.println("Assitant: " + answer.content().text());
                }


            } catch (Exception e) {
                System.out.println("Something went wrong");
                break;
            }

        }


    }

    @NotNull
    private static String writeToolResultsInText(List<WebSearchOrganicResult> toolResults) {
        return toolResults.stream().reduce(new StringBuilder(), ((s, webSearchOrganicResult) -> {
            s.append("Title: ").append(webSearchOrganicResult.title());
            s.append("\n Content: ").append(webSearchOrganicResult.content());
            s.append("\n Summary: ").append(webSearchOrganicResult.snippet());

            s.append("\n");
            return s;
        }), (a, b) -> {
            a.append(b.toString());
            return a;
        }).toString();
    }
}