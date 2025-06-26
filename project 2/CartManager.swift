import Foundation
import SwiftUI

@MainActor
class CartManager: ObservableObject {
    @Published var items: [CartItem] = []
    @Published var total: Decimal = 0
    @Published var isLoading = false
    
    static let shared = CartManager()
    private let networkManager = NetworkManager.shared
    
    private init() {
        // Load cart from backend when initialized
        Task {
            await loadCart()
        }
    }
    
    var itemCount: Int {
        items.reduce(0) { $0 + $1.quantity }
    }
    
    // MARK: - Backend Integration
    
    func loadCart() async {
        isLoading = true
        defer { isLoading = false }
        
        do {
            items = try await networkManager.getCart()
            calculateTotal()
        } catch {
            print("Failed to load cart: \(error)")
            // Keep existing items if load fails
        }
    }
    
    func addToCart(productId: Int, quantity: Int = 1) async {
        isLoading = true
        defer { isLoading = false }
        
        do {
            _ = try await networkManager.addToCart(productId: productId, quantity: quantity)
            await loadCart() // Refresh cart from backend
        } catch {
            print("Failed to add to cart: \(error)")
        }
    }
    
    func updateQuantity(for productId: Int, quantity: Int) async {
        isLoading = true
        defer { isLoading = false }
        
        if quantity <= 0 {
            await removeFromCart(productId: productId)
            return
        }
        
        do {
            _ = try await networkManager.updateCartItem(productId: productId, quantity: quantity)
            await loadCart() // Refresh cart from backend
        } catch {
            print("Failed to update cart item: \(error)")
        }
    }
    
    func removeFromCart(productId: Int) async {
        isLoading = true
        defer { isLoading = false }
        
        do {
            _ = try await networkManager.removeFromCart(productId: productId)
            await loadCart() // Refresh cart from backend
        } catch {
            print("Failed to remove from cart: \(error)")
        }
    }
    
    func clearCart() async {
        isLoading = true
        defer { isLoading = false }
        
        do {
            _ = try await networkManager.clearCart()
            items = []
            total = 0
        } catch {
            print("Failed to clear cart: \(error)")
        }
    }
    
    // MARK: - Helper Methods
    
    private func calculateTotal() {
        total = items.reduce(Decimal(0)) { sum, item in
            sum + (item.product.price * Decimal(item.quantity))
        }
    }
    
    // MARK: - Legacy Methods (for backward compatibility)
    
    func addToCart(productId: Int) {
        Task {
            await addToCart(productId: productId, quantity: 1)
            // Add haptic feedback
            let impactFeedback = UIImpactFeedbackGenerator(style: .medium)
            impactFeedback.impactOccurred()
        }
    }
    
    func removeItem(withId id: Int) {
        if let item = items.first(where: { $0.id == id }) {
            Task {
                await removeFromCart(productId: item.product.id)
            }
        }
    }
    
    func increaseQuantity(for id: Int) {
        if let item = items.first(where: { $0.id == id }) {
            Task {
                await updateQuantity(for: item.product.id, quantity: item.quantity + 1)
            }
        }
    }
    
    func decreaseQuantity(for id: Int) {
        if let item = items.first(where: { $0.id == id }) {
            Task {
                await updateQuantity(for: item.product.id, quantity: item.quantity - 1)
            }
        }
    }
} 

