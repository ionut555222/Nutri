import Foundation
import SwiftUI

@MainActor
class AuthManager: ObservableObject {
    @Published var isAuthenticated = false
    @Published var jwtResponse: JwtResponse? {
        didSet {
            saveTokenToKeychain()
        }
    }
    
    static let shared = AuthManager()
    private var networkManager = NetworkManager.shared
    private let keychainKey = "jwt_token"

    private init() {
        loadTokenFromKeychain()
        // Check token validity on init, but be more lenient
        validateCurrentTokenGracefully()
    }
    
    // MARK: - Token Validation
    
    private func validateCurrentToken() {
        guard let jwtResponse = jwtResponse else {
            logout()
            return
        }
        
        if isTokenExpired(jwtResponse.accessToken) {
            print("🔐 Token is expired, logging out...")
            logout()
        }
    }
    
    private func validateCurrentTokenGracefully() {
        guard let jwtResponse = jwtResponse else {
            // Don't auto-logout if no token exists - let user try to login
            print("🔐 No token found, user needs to login")
            return
        }
        
        if isTokenExpired(jwtResponse.accessToken) {
            print("🔐 Token is expired, clearing authentication state...")
            // Clear the expired token but don't force logout state
            self.jwtResponse = nil
            self.isAuthenticated = false
        } else {
            // Token is still valid, maintain authentication
            self.isAuthenticated = true
            print("🔐 Token is valid, user remains authenticated")
        }
    }
    
    private func isTokenExpired(_ token: String) -> Bool {
        guard let payload = extractJWTPayload(from: token),
              let exp = payload["exp"] as? Double else {
            print("❌ Unable to extract expiration from token")
            return true
        }
        
        let expirationDate = Date(timeIntervalSince1970: exp)
        let currentDate = Date()
        let isExpired = currentDate >= expirationDate
        
        if isExpired {
            print("🔐 Token expired at: \(expirationDate), current time: \(currentDate)")
        } else {
            print("🔐 Token valid until: \(expirationDate)")
        }
        
        return isExpired
    }
    
    private func extractJWTPayload(from token: String) -> [String: Any]? {
        let segments = token.components(separatedBy: ".")
        guard segments.count == 3 else {
            return nil
        }
        
        let payloadSegment = segments[1]
        // Add padding if needed
        var paddedPayload = payloadSegment
        let remainder = paddedPayload.count % 4
        if remainder > 0 {
            paddedPayload += String(repeating: "=", count: 4 - remainder)
        }
        
        guard let payloadData = Data(base64Encoded: paddedPayload),
              let json = try? JSONSerialization.jsonObject(with: payloadData) as? [String: Any] else {
            return nil
        }
        
        return json
    }
    
    // MARK: - Authentication Methods

    func login(username: String, password: String) async throws {
        print("🔐 Attempting login for user: \(username)")
        
        // Clear any existing authentication state
        self.isAuthenticated = false
        self.jwtResponse = nil
        
        let loginRequest = LoginRequest(username: username, password: password)
        do {
            let response = try await networkManager.login(loginRequest: loginRequest)
            print("🔐 Received login response for user: \(response.username)")
            
            // Validate the received token
            if isTokenExpired(response.accessToken) {
                print("❌ Received token is already expired")
                throw AuthError.tokenExpired
            }
            
            self.jwtResponse = response
            self.isAuthenticated = true
            print("🔐 Login successful, token valid until: \(getTokenExpirationDate() ?? Date())")
        } catch let error as NetworkError {
            print("❌ Network error during login: \(error)")
            self.isAuthenticated = false
            self.jwtResponse = nil
            
            // Provide more specific error messages
            switch error {
            case .unauthorized:
                throw AuthError.authenticationFailed
            case .networkUnavailable:
                throw NetworkError.networkUnavailable
            case .timeout:
                throw NetworkError.timeout
            default:
                throw error
            }
        } catch {
            print("❌ Login failed with error: \(error)")
            self.isAuthenticated = false
            self.jwtResponse = nil
            throw error
        }
    }
    
    func signup(username: String, email: String, password: String) async throws {
        // Split the username (which is actually full name) into first and last name
        let nameParts = username.split(separator: " ")
        let firstName = String(nameParts.first ?? "")
        let lastName = nameParts.count > 1 ? String(nameParts.dropFirst().joined(separator: " ")) : ""
        
        let signupRequest = SignupRequest(
            username: email, // Use email as username for customer accounts
            email: email,
            password: password,
            firstName: firstName,
            lastName: lastName,
            phoneNumber: nil,
            role: nil
        )
        do {
            let _ = try await networkManager.signup(signupRequest: signupRequest)
            // Log in directly after signup using email as username
            try await login(username: email, password: password)
        } catch {
            throw error
        }
    }

    func logout() {
        print("🔐 Logging out user...")
        isAuthenticated = false
        jwtResponse = nil
        clearTokenFromKeychain()
    }
    
    func clearAuthenticationState() {
        print("🔐 Clearing all authentication state...")
        isAuthenticated = false
        jwtResponse = nil
        clearTokenFromKeychain()
    }
    
    func checkTokenValidity() {
        validateCurrentToken()
    }
    
    func checkAuthenticationStatus() async {
        print("🔍 AuthManager: Checking authentication status...")
        validateCurrentToken()
    }
    
    private func getTokenExpirationDate() -> Date? {
        guard let token = jwtResponse?.accessToken,
              let payload = extractJWTPayload(from: token),
              let exp = payload["exp"] as? Double else {
            return nil
        }
        return Date(timeIntervalSince1970: exp)
    }
    
    // MARK: - Keychain Management
    
    private func saveTokenToKeychain() {
        guard let jwtResponse = jwtResponse else {
            clearTokenFromKeychain()
            return
        }
        
        do {
            let data = try JSONEncoder().encode(jwtResponse)
            let query: [String: Any] = [
                kSecClass as String: kSecClassGenericPassword,
                kSecAttrAccount as String: keychainKey,
                kSecValueData as String: data
            ]
            
            // Delete existing item first
            SecItemDelete(query as CFDictionary)
            
            // Add new item
            let status = SecItemAdd(query as CFDictionary, nil)
            if status == errSecSuccess {
                print("🔐 JWT token saved to keychain")
            } else {
                print("❌ Failed to save JWT token to keychain: \(status)")
            }
        } catch {
            print("❌ Failed to encode JWT token: \(error)")
        }
    }
    
    private func loadTokenFromKeychain() {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: keychainKey,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]
        
        var result: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &result)
        
        if status == errSecSuccess,
           let data = result as? Data {
            do {
                let jwtResponse = try JSONDecoder().decode(JwtResponse.self, from: data)
                
                // Check if the loaded token is still valid
                if isTokenExpired(jwtResponse.accessToken) {
                    print("🔐 Loaded token is expired, clearing...")
                    clearTokenFromKeychain()
                    return
                }
                
                self.jwtResponse = jwtResponse
                self.isAuthenticated = true
                print("🔐 JWT token loaded from keychain and validated")
            } catch {
                print("❌ Failed to decode JWT token from keychain: \(error)")
                clearTokenFromKeychain()
            }
        } else if status != errSecItemNotFound {
            print("❌ Failed to load JWT token from keychain: \(status)")
        }
    }
    
    private func clearTokenFromKeychain() {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: keychainKey
        ]
        
        let status = SecItemDelete(query as CFDictionary)
        if status == errSecSuccess || status == errSecItemNotFound {
            print("🔐 JWT token cleared from keychain")
        } else {
            print("❌ Failed to clear JWT token from keychain: \(status)")
        }
    }
}

// MARK: - Auth Errors

enum AuthError: Error, LocalizedError {
    case tokenExpired
    case invalidToken
    case authenticationFailed
    
    var errorDescription: String? {
        switch self {
        case .tokenExpired:
            return "Your session has expired. Please log in again."
        case .invalidToken:
            return "Invalid authentication token."
        case .authenticationFailed:
            return "Authentication failed. Please check your credentials."
        }
    }
} 