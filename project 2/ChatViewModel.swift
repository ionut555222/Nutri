import Foundation
import SwiftUI

@MainActor
class ChatViewModel: ObservableObject {
    @Published var messages: [ChatMessage] = []
    @Published var errorMessage: String?
    @Published var isTyping: Bool = false

    private let geminiService = GeminiChatService()
    private let networkManager = NetworkManager.shared

    func fetchHistory(authManager: AuthManager) async {
        guard authManager.isAuthenticated else { return }
        
        do {
            self.messages = try await networkManager.getChatHistory()
        } catch {
            errorMessage = "Failed to fetch chat history: \(error.localizedDescription)"
        }
    }
    
    func sendMessage(_ message: String, authManager: AuthManager) async {
        guard authManager.isAuthenticated else {
            errorMessage = "You must be logged in to use the chat."
            return
        }
        
        // Add user message to UI and save to backend
        let userMessage = ChatMessage(sender: "user", content: message)
        messages.append(userMessage)
        isTyping = true
        errorMessage = nil
        
        Task { try? await networkManager.saveChatMessage(userMessage) }

        do {
            // Fetch context for the AI
            let products = try? await networkManager.fetchProducts()
            let orders = try? await networkManager.fetchOrders()

            // Build the contextual prompt
            let prompt = buildPrompt(userMessage: message, products: products, orders: orders)
            
            // Prepare history for Gemini - send the single, context-rich prompt
            let promptContent = Content(role: "user", parts: [Part(text: prompt)])
            let historyForAPI = [promptContent]

            // Get response from Gemini
            let responseText = try await geminiService.sendMessage(history: historyForAPI)
            
            // Add bot response to UI and save to backend
            let botMessage = ChatMessage(sender: "bot", content: responseText)
            messages.append(botMessage)
            Task { try? await networkManager.saveChatMessage(botMessage) }

        } catch {
            errorMessage = "Failed to get response from AI. Please check your API key and network connection."
            print("Error sending message to Gemini: \(error)")
        }
        
        isTyping = false
    }

    private func buildPrompt(userMessage: String, products: [Product]?, orders: [Order]?) -> String {
        var systemPrompt = """
        You are an intelligent AI assistant for a retail store. Your goal is to be helpful and provide accurate information to customers.

        Here are the rules and context you must follow:
        - Answer questions based *only* on the context provided below.
        - If the information is not in the context, say you don't have that information. Do not make things up.
        - Be friendly and conversational.
        - When asked about products, use the 'Available Products' list.
        - When asked about delivery or past orders, use the 'User's Order History'.
        - For price negotiations, you can offer a 5% discount, but *only* if the user has more than 3 past orders. Mention that this is a loyalty discount.
        """

        if let products = products, !products.isEmpty {
            let productList = products.map { "- \($0.name) (Price: $\($0.price), Stock: \($0.stockCount), Category: \($0.category.name))" }.joined(separator: "\n")
            systemPrompt += "\n\n[Context: Available Products]\n\(productList)"
        }

        if let orders = orders, !orders.isEmpty {
            let orderList = orders.map { order -> String in
                let itemsString = (order.items ?? []).map { item -> String in
                    return "\(item.quantity)x \(item.productName)"
                }.joined(separator: ", ")
                
                let orderId = order.id ?? 0
                let orderDate = order.orderDate ?? "N/A"
                
                return "- Order #\(orderId) on \(orderDate): \(itemsString.isEmpty ? "No items" : itemsString)"
            }.joined(separator: "\n")
            systemPrompt += "\n\n[Context: User's Order History]\n\(orderList)"
        }

        systemPrompt += "\n\n[User's Message]\n\(userMessage)"
        
        print("--- Sending Prompt to Gemini ---")
        print(systemPrompt)
        print("-----------------------------")

        return systemPrompt
    }
} 