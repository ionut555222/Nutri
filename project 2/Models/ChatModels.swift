import Foundation

struct ChatMessage: Codable, Identifiable {
    var id: UUID = UUID()
    var sender: String // "user" or "bot"
    var content: String
    
    enum CodingKeys: String, CodingKey {
        case id
        case sender
        case content
    }
    
    // Custom initializer for convenience in the app
    init(id: UUID = UUID(), sender: String, content: String) {
        self.id = id
        self.sender = sender
        self.content = content
    }
} 