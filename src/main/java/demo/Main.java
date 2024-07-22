package demo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    static String openAI_token = "";
    static String gptVersion = "gpt-4o";
    static String gptUrl = "https://api.openai.com/v1/chat/completions";

    public static void main( String[] args ) {
        String question = "Привет! Как дела?";

        String answer = askQuestion(question, "");

        System.out.println(answer);
    }


    public static String askQuestion(String question, String content) {
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("Authorization", "Bearer " + openAI_token);
            return execution.execute(request, body);
        });

        ChatGptRequestDTO requestDTO = createRequest(question, content);

        ResponseEntity<ChatGptResponseDTO> responseEntity = null;

        do {
            try {
                responseEntity = restTemplate.postForEntity(gptUrl, requestDTO, ChatGptResponseDTO.class);
            } catch (HttpServerErrorException e) {
//                log.error("Ошибка соединения с ChatGPT", e);
            }
        } while (responseEntity == null);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return responseEntity.getBody().getChoices().get(0).getMessage().getContent();
        }
        else {
            throw new RuntimeException(responseEntity.getStatusCode().toString());
        }

    }

    private static ChatGptRequestDTO createRequest(String question, String content) {
        ChatGptRequestDTO.Message system = new ChatGptRequestDTO.Message("system", question);
        ChatGptRequestDTO.Message user = new ChatGptRequestDTO.Message("user", content);

        return new ChatGptRequestDTO(gptVersion, Stream.of(system,user).collect(Collectors.toList()));
    }




    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    private static class ChatGptRequestDTO {

        private String model;
        private List<Message> messages;

        @AllArgsConstructor
        @NoArgsConstructor
        @Getter
        public static class Message {

            private String role;
            private String content;

        }
    }

    @NoArgsConstructor
    @Getter
    private static class ChatGptResponseDTO {

        private List<Choice> choices;

        @Getter
        public static class Choice {

            private long index;
            private ChatGptRequestDTO.Message message;

        }
    }
}