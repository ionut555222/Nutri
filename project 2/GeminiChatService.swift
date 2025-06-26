import Foundation

// MARK: - Gemini API Request/Response Models

struct GeminiRequest: Encodable {
    let contents: [Content]
}

struct Content: Codable {
    let role: String? // "user" or "model"
    let parts: [Part]
}

struct Part: Codable {
    let text: String
}

struct GeminiResponse: Decodable {
    let candidates: [Candidate]
    
    struct Candidate: Decodable {
        let content: Content
        
        struct Content: Decodable {
            let parts: [Part]
            
            struct Part: Decodable {
                let text: String
            }
        }
    }
    
    var responseText: String? {
        return candidates.first?.content.parts.first?.text
    }
}

// MARK: - Gemini Chat Service

class GeminiChatService {
    private let apiKey: String
    private let urlSession = URLSession.shared
    private let apiURL = URL(string: "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent")!

    init() {
        // NOTE: Make sure to replace "YOUR_API_KEY" with your actual Gemini API key.
        // You should store this securely, for example, in a `Secrets.swift` file
        // which is included in your .gitignore to prevent committing it to version control.
        self.apiKey = Secrets.geminiApiKey
    }

    func sendMessage(history: [Content]) async throws -> String {
        var request = URLRequest(url: apiURL)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.url?.append(queryItems: [URLQueryItem(name: "key", value: apiKey)])
        
        let requestBody = GeminiRequest(contents: history)
        
        do {
            let requestData = try JSONEncoder().encode(requestBody)
            request.httpBody = requestData
        } catch {
            print("Failed to encode request: \(error)")
            throw error
        }
        
        let (data, response) = try await urlSession.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            let errorBody = String(data: data, encoding: .utf8) ?? "No error body"
            print("API Error: \(response) - \(errorBody)")
            throw URLError(.badServerResponse)
        }
        
        do {
            let geminiResponse = try JSONDecoder().decode(GeminiResponse.self, from: data)
            if let textResponse = geminiResponse.responseText {
                return textResponse
            } else {
                throw URLError(.cannotParseResponse)
            }
        } catch {
            print("Failed to decode response: \(error)")
            throw error
        }
    }
} 