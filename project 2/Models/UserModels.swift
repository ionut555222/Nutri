import Foundation

struct User: Codable, Identifiable {
    let id: Int
    let username: String
    let email: String
    let roles: [String]
    var password: String? = nil
}

struct MessageResponse: Codable {
    let message: String
} 