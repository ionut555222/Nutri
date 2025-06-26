package com.example.project3;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GeminiService {

    private final UserRepository userRepository;
    private final FruitRepository fruitRepository;
    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;
    private final String projectId;
    private final String location;
    private final String modelName;

    public GeminiService(UserRepository userRepository,
                         FruitRepository fruitRepository,
                         OrderRepository orderRepository,
                         ModelMapper modelMapper,
                         @Value("${gemini.project.id}") String projectId,
                         @Value("${gemini.location}") String location,
                         @Value("${gemini.model.name}") String modelName) {
        this.userRepository = userRepository;
        this.fruitRepository = fruitRepository;
        this.orderRepository = orderRepository;
        this.modelMapper = modelMapper;
        this.projectId = projectId;
        this.location = location;
        this.modelName = modelName;
    }

    public String getResponse(String username, ChatRequest chatRequest) throws IOException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        try (VertexAI vertexAI = new VertexAI(projectId, location)) {
            String systemPrompt = buildSystemPrompt(user);
            
            GenerativeModel model = new GenerativeModel(modelName, vertexAI);

            // Build a single string prompt
            StringBuilder fullPrompt = new StringBuilder(systemPrompt);
            fullPrompt.append("\n\n--- Conversation History ---\n");
            for (ChatRequest.ChatMessage msg : chatRequest.getHistory()) {
                fullPrompt.append(msg.getRole()).append(": ").append(msg.getText()).append("\n");
            }
            fullPrompt.append("user: ").append(chatRequest.getMessage()).append("\n");
            fullPrompt.append("model: ");

            GenerateContentResponse response = model.generateContent(fullPrompt.toString());
            return ResponseHandler.getText(response);
        }
    }

    private String buildSystemPrompt(User user) {
        BigDecimal totalSpending = orderRepository.findByUserId(user.getId()).stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String productsJson = "[]";
        try {
            List<FruitDTO> fruitDTOs = fruitRepository.findAll().stream()
                    .map(fruit -> modelMapper.map(fruit, FruitDTO.class))
                    .collect(Collectors.toList());
            productsJson = new ObjectMapper().writeValueAsString(fruitDTOs);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return "SYSTEM INSTRUCTION: You are a customer service chatbot for an online fruit store. Your name is Nutri. " +
                "You are friendly, helpful, and knowledgeable about our products.\n\n" +
                "Here is the product catalog in JSON format:\n" + productsJson + "\n\n" +
                "Here is information about the current customer:\n" +
                "- Username: " + user.getUsername() + "\n" +
                "- Total spending to date: $" + totalSpending + "\n\n" +
                "Your tasks are:\n" +
                "1.  **Answer product questions:** Use the catalog to provide details about fruits.\n" +
                "2.  **Provide delivery information:** Standard delivery is 3-5 business days. Express is 1-2 business days.\n" +
                "3.  **Negotiate prices:** Based on the customer's spending, you can offer discounts. " +
                "If total spending > $50, you can offer a 5% discount on their next order. " +
                "If total spending > $200, you can offer a 10% discount. Be friendly and conversational when offering discounts. " +
                "Don't just state the discount, make it sound like a special offer for a valued customer.\n" +
                "4.  **Maintain conversation context:** The conversation history is provided below. Use it to understand the context.\n\n" +
                "Engage with the user naturally. Start the conversation by introducing yourself if it's the beginning of the chat. " +
                "Respond only as the 'model'.";
    }
} 