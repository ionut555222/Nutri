import Foundation
import SwiftUI

enum NetworkError: Error, LocalizedError {
    case badURL
    case requestFailed(Error)
    case decodingFailed(Error)
    case invalidResponse(String)
    case unauthorized
    case serverError(Int, String)
    case networkUnavailable
    case timeout
    case unknown

    var errorDescription: String? {
        switch self {
        case .badURL:
            return "Invalid server URL configuration"
        case .requestFailed(let error):
            return "Network request failed: \(error.localizedDescription)"
        case .decodingFailed(let error):
            return "Failed to process server response: \(error.localizedDescription)"
        case .invalidResponse(let message):
            return "Invalid server response: \(message)"
        case .unauthorized:
            return "Invalid username or password"
        case .serverError(let code, let message):
            return "Server error (\(code)): \(message)"
        case .networkUnavailable:
            return "No internet connection available"
        case .timeout:
            return "Request timed out. Please try again"
        case .unknown:
            return "An unexpected error occurred"
        }
    }
}

@MainActor
final class NetworkManager: ObservableObject {
    static let shared = NetworkManager()
    
    private let baseURL: String
    private let session: URLSession
    private let maxRetries = 3
    private let retryDelay: TimeInterval = 1.0

    private var token: String? {
        // Check if auth manager is available and token is valid
        guard let authManager = AuthManager.shared as AuthManager?,
              let jwtResponse = authManager.jwtResponse else {
            print("🔑 Token check: NO AUTH MANAGER OR JWT RESPONSE")
            return nil
        }
        
        // Check token validity before using it
        let tokenValue = jwtResponse.accessToken
        print("🔑 Token check: \(tokenValue.isEmpty ? "EMPTY" : "AVAILABLE")")
        if !tokenValue.isEmpty {
            print("🔑 Token preview: \(String(tokenValue.prefix(20)))...")
        }
        return tokenValue.isEmpty ? nil : tokenValue
    }

    private init() {
        // Configuration-based URL with fallback
        if let configURL = Bundle.main.object(forInfoDictionaryKey: "API_BASE_URL") as? String {
            self.baseURL = configURL
        } else {
            // Fallback to localhost for development
            self.baseURL = "http://localhost:8080/api"
        }
        
        let configuration = URLSessionConfiguration.default
        configuration.timeoutIntervalForRequest = 30
        configuration.timeoutIntervalForResource = 60
        configuration.httpCookieAcceptPolicy = .never
        configuration.httpShouldSetCookies = false
        configuration.requestCachePolicy = .reloadIgnoringLocalCacheData
        
        self.session = URLSession(configuration: configuration)
        
        print("🌐 NetworkManager initialized with baseURL: \(baseURL)")
    }

    // MARK: - Core Network Function with Retry Logic

    func fetch<T: Codable>(
        endpoint: String, 
        method: String = "GET", 
        body: Data? = nil, 
        requiresAuth: Bool = true,
        retryCount: Int = 0
    ) async throws -> T {
        
        guard let url = URL(string: baseURL + endpoint) else {
            throw NetworkError.badURL
        }
        
        print("🌐 \(method) \(url.absoluteString) (attempt \(retryCount + 1)/\(maxRetries + 1))")
        
        var request = URLRequest(url: url)
        request.httpMethod = method
        request.httpBody = body
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("application/json", forHTTPHeaderField: "Accept")

        if requiresAuth {
            if let token = token {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
                print("🔐 Added Authorization header with token")
            } else {
                print("❌ NO TOKEN AVAILABLE for authenticated request!")
            }
        }

        do {
        let (data, response) = try await session.data(for: request)
        
            if let responseString = String(data: data, encoding: .utf8), !responseString.isEmpty {
                print("📦 Response (\(data.count) bytes): \(responseString.prefix(200))...")
        }
        
        guard let httpResponse = response as? HTTPURLResponse else {
            throw NetworkError.invalidResponse("Invalid response type")
        }
        
        print("📥 Response status: \(httpResponse.statusCode)")
        
            return try handleResponse(data: data, httpResponse: httpResponse)
            
        } catch {
            // Handle specific errors and retry logic
            if shouldRetry(error: error) && retryCount < maxRetries {
                print("⚠️ Request failed, retrying in \(retryDelay)s... (\(retryCount + 1)/\(maxRetries))")
                try await Task.sleep(nanoseconds: UInt64(retryDelay * 1_000_000_000))
                return try await fetch(
                    endpoint: endpoint,
                    method: method, 
                    body: body,
                    requiresAuth: requiresAuth,
                    retryCount: retryCount + 1
                )
            }
            
            throw mapError(error)
        }
    }
    
    private func handleResponse<T: Codable>(data: Data, httpResponse: HTTPURLResponse) throws -> T {
        switch httpResponse.statusCode {
        case 200...299:
        do {
            let decoder = JSONDecoder()
            return try decoder.decode(T.self, from: data)
        } catch {
            print("❌ Decoding error: \(error)")
                if let jsonString = String(data: data, encoding: .utf8) {
                    print("📄 Raw response: \(jsonString)")
                }
            throw NetworkError.decodingFailed(error)
        }
            
        case 401:
            print("❌ 401 Unauthorized - Auto-logging out user")
            // Auto-logout on 401 errors (expired/invalid token)
            Task { @MainActor in
                AuthManager.shared.logout()
            }
            throw NetworkError.unauthorized
            
        case 400...499:
            let errorMessage = parseErrorMessage(from: data) ?? "Client error"
            throw NetworkError.serverError(httpResponse.statusCode, errorMessage)
            
        case 500...599:
            let errorMessage = parseErrorMessage(from: data) ?? "Server error"
            throw NetworkError.serverError(httpResponse.statusCode, errorMessage)
            
        default:
            throw NetworkError.invalidResponse("Unexpected status code: \(httpResponse.statusCode)")
        }
    }
    
    private func parseErrorMessage(from data: Data) -> String? {
        if let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
           let message = json["message"] as? String {
            return message
        }
        return String(data: data, encoding: .utf8)
    }
    
    private func shouldRetry(error: Error) -> Bool {
        if let urlError = error as? URLError {
            switch urlError.code {
            case .timedOut, .networkConnectionLost, .notConnectedToInternet:
                return true
            default:
                return false
            }
        }
        return false
    }
    
    private func mapError(_ error: Error) -> NetworkError {
        if let urlError = error as? URLError {
            switch urlError.code {
            case .notConnectedToInternet, .networkConnectionLost:
                return .networkUnavailable
            case .timedOut:
                return .timeout
            default:
                return .requestFailed(urlError)
            }
        }
        return .requestFailed(error)
    }

    // MARK: - Convenience Methods
    
    func post<T: Codable, U: Codable>(
        endpoint: String, 
        body: U, 
        requiresAuth: Bool = true
    ) async throws -> T {
        let data = try JSONEncoder().encode(body)
        return try await fetch(
            endpoint: endpoint, 
            method: "POST", 
            body: data, 
            requiresAuth: requiresAuth
        )
    }
    
    func put<T: Codable, U: Codable>(
        endpoint: String, 
        body: U, 
        requiresAuth: Bool = true
    ) async throws -> T {
        let data = try JSONEncoder().encode(body)
        return try await fetch(
            endpoint: endpoint, 
            method: "PUT", 
            body: data, 
            requiresAuth: requiresAuth
        )
    }
    
    func delete(endpoint: String, requiresAuth: Bool = true) async throws {
        let _: EmptyResponse = try await fetch(
            endpoint: endpoint, 
            method: "DELETE", 
            requiresAuth: requiresAuth
        )
    }

    // MARK: - Authentication API
    
    func login(loginRequest: LoginRequest) async throws -> JwtResponse {
        try await post(endpoint: "/auth/customer/signin", body: loginRequest, requiresAuth: false)
    }
    
    func signup(signupRequest: SignupRequest) async throws -> MessageResponse {
        try await post(endpoint: "/auth/customer/signup", body: signupRequest, requiresAuth: false)
    }

    // MARK: - Product API
    
    func fetchCategories() async throws -> [Category] {
        try await fetch(endpoint: "/categories", requiresAuth: false)
    }

    func fetchProducts(categoryId: Int? = nil) async throws -> [Product] {
        var endpoint = "/fruits"
        if let categoryId = categoryId {
            endpoint += "?categoryId=\(categoryId)"
        }
        return try await fetch(endpoint: endpoint, requiresAuth: false)
    }

    // MARK: - Order API

    func fetchOrders() async throws -> [Order] {
        try await fetch(endpoint: "/orders")
    }

    func checkout(request: CheckoutRequest) async throws -> Order {
        try await post(endpoint: "/orders/checkout", body: request)
    }

    // MARK: - Customer Profile API
    
    func getProfile() async throws -> CustomerProfile {
        try await fetch(endpoint: "/customers/profile")
    }
    
    func updateProfile(_ profile: CustomerProfile) async throws -> CustomerProfile {
        try await put(endpoint: "/customers/profile", body: profile)
    }

    // MARK: - Chat API
    
    func getChatHistory() async throws -> [ChatMessage] {
        try await fetch(endpoint: "/chat/history")
    }
    
    func saveChatMessage(_ message: ChatMessage) async throws -> MessageResponse {
        try await post(endpoint: "/chat/messages", body: message)
    }

    // MARK: - Cart Management (Enhanced)
    
    private let cartKey = "cart_items_v2"
    
    func addToCart(productId: Int, quantity: Int = 1) async throws -> MessageResponse {
        // Get product details from backend first
        let products = try await fetchProducts()
        guard let product = products.first(where: { $0.id == productId }) else {
            throw NetworkError.invalidResponse("Product not found")
        }
        
        // Update local cart
        var cartItems = loadCartFromLocalStorage()
        
        if let existingIndex = cartItems.firstIndex(where: { $0.product.id == productId }) {
            let newQuantity = cartItems[existingIndex].quantity + quantity
            cartItems[existingIndex] = CartItem(
                id: cartItems[existingIndex].id,
                product: product,
                quantity: newQuantity,
                addedDate: cartItems[existingIndex].addedDate
            )
        } else {
            let newItem = CartItem(
                id: (cartItems.map { $0.id }.max() ?? 0) + 1,
                product: product,
                quantity: quantity,
                addedDate: ISO8601DateFormatter().string(from: Date())
            )
            cartItems.append(newItem)
        }
        
        saveCartToLocalStorage(cartItems)
        return MessageResponse(message: "Added to cart successfully")
    }
    
    func updateCartItem(productId: Int, quantity: Int) async throws -> MessageResponse {
        var cartItems = loadCartFromLocalStorage()
        
        if let index = cartItems.firstIndex(where: { $0.product.id == productId }) {
            if quantity <= 0 {
                cartItems.remove(at: index)
            } else {
                cartItems[index] = CartItem(
                    id: cartItems[index].id,
                    product: cartItems[index].product,
                    quantity: quantity,
                    addedDate: cartItems[index].addedDate
                )
            }
            saveCartToLocalStorage(cartItems)
        }
        
        return MessageResponse(message: "Cart updated successfully")
    }
    
    func removeFromCart(productId: Int) async throws -> MessageResponse {
        var cartItems = loadCartFromLocalStorage()
        cartItems.removeAll { $0.product.id == productId }
        saveCartToLocalStorage(cartItems)
        return MessageResponse(message: "Item removed from cart")
    }
    
    func getCart() async throws -> [CartItem] {
        return loadCartFromLocalStorage()
    }
    
    func clearCart() async throws -> MessageResponse {
        saveCartToLocalStorage([])
        return MessageResponse(message: "Cart cleared successfully")
    }
    
    // MARK: - Local Storage Management
    
    private func saveCartToLocalStorage(_ items: [CartItem]) {
        do {
            let data = try JSONEncoder().encode(items)
            UserDefaults.standard.set(data, forKey: cartKey)
            print("💾 Cart saved: \(items.count) items")
        } catch {
            print("❌ Failed to save cart: \(error)")
        }
    }
    
    private func loadCartFromLocalStorage() -> [CartItem] {
        guard let data = UserDefaults.standard.data(forKey: cartKey) else {
            return []
        }
        
        do {
            let items = try JSONDecoder().decode([CartItem].self, from: data)
            print("📱 Cart loaded: \(items.count) items")
            return items
        } catch {
            print("❌ Failed to load cart: \(error)")
            return []
        }
    }
    
    // MARK: - Image Upload API
    
    func uploadProductImage(productId: Int, imageData: Data, fileName: String) async throws -> ImageUploadResponse {
        guard let url = URL(string: baseURL + "/images/upload/fruit/\(productId)") else {
            throw NetworkError.badURL
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        
        // Add authorization header
        if let token = token {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        
        // Create multipart form data
        let boundary = UUID().uuidString
        request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
        
        let formData = createMultipartFormData(
            imageData: imageData,
            fileName: fileName,
            boundary: boundary
        )
        
        request.httpBody = formData
        
        do {
            let (data, response) = try await session.data(for: request)
            
            guard let httpResponse = response as? HTTPURLResponse else {
                throw NetworkError.invalidResponse("Invalid response type")
            }
            
            if httpResponse.statusCode == 200 {
                let uploadResponse = try JSONDecoder().decode(ImageUploadResponse.self, from: data)
                return uploadResponse
            } else {
                let errorMessage = String(data: data, encoding: .utf8) ?? "Upload failed"
                throw NetworkError.serverError(httpResponse.statusCode, errorMessage)
            }
        } catch {
            throw mapError(error)
        }
    }
    
    func uploadImage(imageData: Data, fileName: String) async throws -> ImageUploadResponse {
        guard let url = URL(string: baseURL + "/images/upload") else {
            throw NetworkError.badURL
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        
        // Add authorization header
        if let token = token {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        
        // Create multipart form data
        let boundary = UUID().uuidString
        request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
        
        let formData = createMultipartFormData(
            imageData: imageData,
            fileName: fileName,
            boundary: boundary
        )
        
        request.httpBody = formData
        
        do {
            let (data, response) = try await session.data(for: request)
            
            guard let httpResponse = response as? HTTPURLResponse else {
                throw NetworkError.invalidResponse("Invalid response type")
            }
            
            if httpResponse.statusCode == 200 {
                let uploadResponse = try JSONDecoder().decode(ImageUploadResponse.self, from: data)
                return uploadResponse
            } else {
                let errorMessage = String(data: data, encoding: .utf8) ?? "Upload failed"
                throw NetworkError.serverError(httpResponse.statusCode, errorMessage)
            }
        } catch {
            throw mapError(error)
        }
    }
    
    private func createMultipartFormData(imageData: Data, fileName: String, boundary: String) -> Data {
        var formData = Data()
        
        // Add file data
        formData.append("--\(boundary)\r\n".data(using: .utf8)!)
        formData.append("Content-Disposition: form-data; name=\"file\"; filename=\"\(fileName)\"\r\n".data(using: .utf8)!)
        formData.append("Content-Type: image/png\r\n\r\n".data(using: .utf8)!)
        formData.append(imageData)
        formData.append("\r\n".data(using: .utf8)!)
        
        // End boundary
        formData.append("--\(boundary)--\r\n".data(using: .utf8)!)
        
        return formData
    }

    // MARK: - Health Check
    
    func healthCheck() async throws -> [String: String] {
        try await fetch(endpoint: "/health/ping", requiresAuth: false)
    }
    }
    
// MARK: - Helper Types

struct EmptyResponse: Codable {}

struct ImageUploadResponse: Codable {
    let filename: String?
    let url: String?
    let message: String?
    let error: String?
    let originalName: String?
    let size: String?
    let fruitId: String?
}

// MARK: - Extensions for better UX

extension NetworkManager {
    func isServerReachable() async -> Bool {
        do {
            let _: [String: String] = try await healthCheck()
            return true
        } catch {
            return false
        }
    }
}

// MARK: - Data Models for API

struct CartItem: Codable, Identifiable {
    let id: Int
    let product: Product
    let quantity: Int
    let addedDate: String?
}

struct CustomerProfile: Codable {
    let id: Int?
    var firstName: String?
    var lastName: String?
    var email: String?
    var phoneNumber: String?
    var address: String?
    var dateOfBirth: String?
    var preferences: String?
}

// MARK: - Product Data Transfer Object
struct ProductData: Codable {
    let name: String
    let description: String?
    let price: Decimal
    let stock: Int?
    let unit: Unit
    let categoryId: Int
    
    // Legacy support - keep FruitData as alias
    typealias FruitData = ProductData
}