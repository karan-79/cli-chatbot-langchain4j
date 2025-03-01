package org.example.highlevel;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.service.AiServices;
import org.example.ChatService;
import org.example.Tools;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        ChatLanguageModel model = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
//                .strictTools(true)
                .logRequests(true)
                .logResponses(true)
                .modelName(OpenAiChatModelName.GPT_4_O_MINI)
                .build();

        ChatService service = AiServices.builder(ChatService.class)
                .chatLanguageModel(model)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .tools(new Tools())
                .build();
        var scanner = new Scanner(System.in);
        while (true) {

            try {
                System.out.print("User: ");
                var userInput = scanner.nextLine();

                if(List.of("q", "quit", "exit").contains(userInput.trim())) {
                    System.out.print("Assitant: Bye Bye");
                    break;
                }

                var answer = service.chat(userInput);
                System.out.println("Assitant: " + answer);

            } catch (Exception e) {
                System.out.println("Something went wrong");
                break;
            }

        }

    }
}