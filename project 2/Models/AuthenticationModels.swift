import Foundation

struct LoginRequest: Codable {
    let username: String
    let password: String
}

struct SignupRequest: Codable {
    let username, email, password: String
    let firstName, lastName: String
    let phoneNumber: String?
    let role: [String]?
}

struct JwtResponse: Codable {
    let token: String
    let type: String?
    let id: Int
    let username: String
    let email: String
    let fullName: String?
    let emailVerified: Bool?
    let roles: [String]?
    
    // Computed property for backward compatibility
    var accessToken: String {
        return token
    }
    
    // Memberwise initializer
    init(token: String, type: String?, id: Int, username: String, email: String, fullName: String?, emailVerified: Bool?, roles: [String]?) {
        self.token = token
        self.type = type
        self.id = id
        self.username = username
        self.email = email
        self.fullName = fullName
        self.emailVerified = emailVerified
        self.roles = roles
    }
    
    // Custom decoding to handle optional fields gracefully
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        token = try container.decode(String.self, forKey: .token)
        type = try container.decodeIfPresent(String.self, forKey: .type)
        id = try container.decode(Int.self, forKey: .id)
        username = try container.decode(String.self, forKey: .username)
        email = try container.decode(String.self, forKey: .email)
        fullName = try container.decodeIfPresent(String.self, forKey: .fullName)
        emailVerified = try container.decodeIfPresent(Bool.self, forKey: .emailVerified)
        roles = try container.decodeIfPresent([String].self, forKey: .roles)
    }
} 