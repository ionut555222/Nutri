import SwiftUI

enum PaymentMethod: String, CaseIterable {
    case cash = "Cash on Delivery"
    case applePay = "Apple Pay"
    
    var icon: String {
        switch self {
        case .cash: return "banknote"
        case .applePay: return "creditcard"
        }
    }
}

struct LuxuryCartView: View {
    @EnvironmentObject private var cartManager: CartManager
    @State private var showCheckout = false
    @State private var isProcessingOrder = false
    
    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Navigation Header
                navigationHeader
                
                if cartManager.items.isEmpty {
                    // Empty cart state
                    LuxuryEmptyState(
                        title: "Your Cart is Empty",
                        message: "Discover our curated collection of premium products",
                        actionTitle: "Start Shopping",
                        action: {
                            // Navigate to products
                        }
                    )
                } else {
                    // Cart content
                    cartContent
                }
            }
            .background(DesignSystem.Colors.background)
            .navigationBarHidden(true)
            .sheet(isPresented: $showCheckout) {
                LuxuryCheckoutView(
                    cartItems: cartManager.items.map { ($0.product, $0.quantity) },
                    total: cartManager.total,
                    onOrderComplete: {
                        Task { @MainActor in
                            await cartManager.clearCart()
                        }
                        showCheckout = false
                    }
                )
            }
        }
    }
    
    // MARK: - Navigation Header
    private var navigationHeader: some View {
        HStack {
            Text("Your Cart")
                .font(DesignSystem.Typography.title1)
                .foregroundColor(DesignSystem.Colors.textPrimary)
            
            Spacer()
            
            if !cartManager.items.isEmpty {
                Button("Clear All") {
                    Task { @MainActor in
                        await cartManager.clearCart()
                    }
                }
                .font(DesignSystem.Typography.caption)
                .foregroundColor(DesignSystem.Colors.error)
            }
        }
        .padding(.horizontal, DesignSystem.Spacing.lg)
        .padding(.vertical, DesignSystem.Spacing.md)
    }
    
    // MARK: - Cart Content
    private var cartContent: some View {
        VStack(spacing: 0) {
            // Cart items
            ScrollView {
                LazyVStack(spacing: DesignSystem.Spacing.md) {
                    ForEach(cartManager.items, id: \.product.id) { item in
                        LuxuryCartItemView(
                            item: (item.product, item.quantity),
                            onQuantityChange: { newQuantity in
                                Task { @MainActor in
                                    await cartManager.updateQuantity(for: item.product.id, quantity: newQuantity)
                                }
                            },
                            onRemove: {
                                Task { @MainActor in
                                    await cartManager.removeFromCart(productId: item.product.id)
                                }
                            }
                        )
                    }
                }
                .padding(.horizontal, DesignSystem.Spacing.lg)
                .padding(.top, DesignSystem.Spacing.md)
            }
            
            // Cart summary
            cartSummary
        }
    }
    
    // MARK: - Cart Summary
    private var cartSummary: some View {
        VStack(spacing: DesignSystem.Spacing.md) {
            Divider()
                .background(DesignSystem.Colors.textTertiary)
            
            VStack(spacing: DesignSystem.Spacing.sm) {
                // Subtotal
                HStack {
                    Text("Subtotal")
                        .bodyText()
                    Spacer()
                    Text("$\(NSDecimalNumber(decimal: cartManager.total).doubleValue, specifier: "%.2f")")
                        .bodyText()
                }
                
                // Estimated delivery
                HStack {
                    Text("Estimated Delivery")
                        .captionText()
                    Spacer()
                    Text("Free")
                        .captionText()
                        .foregroundColor(DesignSystem.Colors.success)
                }
                
                Divider()
                    .background(DesignSystem.Colors.textTertiary)
                
                // Total
                HStack {
                    Text("Total")
                        .font(DesignSystem.Typography.headline)
                        .foregroundColor(DesignSystem.Colors.textPrimary)
                    Spacer()
                    Text("$\(NSDecimalNumber(decimal: cartManager.total).doubleValue, specifier: "%.2f")")
                        .font(DesignSystem.Typography.headline)
                        .fontWeight(.semibold)
                        .foregroundColor(DesignSystem.Colors.textPrimary)
                }
            }
            .padding(.horizontal, DesignSystem.Spacing.lg)
            
            // Checkout button
            LuxuryButton(
                "Proceed to Checkout",
                style: .accent,
                isLoading: isProcessingOrder
            ) {
                showCheckout = true
            }
            .disabled(cartManager.items.isEmpty)
            .padding(.horizontal, DesignSystem.Spacing.lg)
            .padding(.bottom, DesignSystem.Spacing.lg)
        }
        .background(DesignSystem.Colors.surface)
    }
}

// MARK: - Cart Item View
struct LuxuryCartItemView: View {
    let item: (product: Product, quantity: Int)
    let onQuantityChange: (Int) -> Void
    let onRemove: () -> Void
    
    var body: some View {
        HStack(spacing: DesignSystem.Spacing.md) {
            // Product image - using placeholder since Product doesn't have imageUrl
            Rectangle()
                .fill(DesignSystem.Colors.supporting)
                .overlay(
                    Image(systemName: "photo")
                        .foregroundColor(DesignSystem.Colors.textTertiary)
                )
                .frame(width: 80, height: 80)
                .cornerRadius(DesignSystem.CornerRadius.md)
            
            // Product info
            VStack(alignment: .leading, spacing: DesignSystem.Spacing.xs) {
                Text(item.product.name)
                    .font(DesignSystem.Typography.headline)
                    .foregroundColor(DesignSystem.Colors.textPrimary)
                    .lineLimit(2)
                
                Text("$\(NSDecimalNumber(decimal: item.product.price).doubleValue, specifier: "%.2f") per \(item.product.unit.rawValue)")
                    .captionText()
                
                HStack {
                    Text("Subtotal: $\(NSDecimalNumber(decimal: item.product.price).doubleValue * Double(item.quantity), specifier: "%.2f")")
                        .font(DesignSystem.Typography.bodyMedium)
                        .foregroundColor(DesignSystem.Colors.textPrimary)
                    
                    Spacer()
                }
            }
            
            Spacer()
            
            // Quantity and remove controls
            VStack(spacing: DesignSystem.Spacing.sm) {
                // Remove button
                Button(action: onRemove) {
                    Image(systemName: "trash")
                        .foregroundColor(DesignSystem.Colors.error)
                        .font(.caption)
                }
                
                // Quantity controls
                HStack(spacing: DesignSystem.Spacing.xs) {
                    Button {
                        onQuantityChange(item.quantity - 1)
                    } label: {
                        Image(systemName: "minus")
                            .foregroundColor(DesignSystem.Colors.textPrimary)
                            .font(.caption)
                            .frame(width: 24, height: 24)
                            .background(DesignSystem.Colors.supporting)
                            .cornerRadius(DesignSystem.CornerRadius.xs)
                    }
                    
                    Text("\(item.quantity)")
                        .font(DesignSystem.Typography.captionMedium)
                        .foregroundColor(DesignSystem.Colors.textPrimary)
                        .frame(minWidth: 24)
                    
                    Button {
                        onQuantityChange(item.quantity + 1)
                    } label: {
                        Image(systemName: "plus")
                            .foregroundColor(DesignSystem.Colors.textPrimary)
                            .font(.caption)
                            .frame(width: 24, height: 24)
                            .background(DesignSystem.Colors.supporting)
                            .cornerRadius(DesignSystem.CornerRadius.xs)
                    }
                }
            }
        }
        .padding(DesignSystem.Spacing.md)
        .luxuryCard()
    }
}

// MARK: - Checkout View
struct LuxuryCheckoutView: View {
    let cartItems: [(product: Product, quantity: Int)]
    let total: Decimal
    let onOrderComplete: () -> Void
    
    @Environment(\.dismiss) private var dismiss
    @State private var isProcessing = false
    @State private var currentStep = 0
    
    // Delivery Information
    @State private var fullName = ""
    @State private var address = ""
    @State private var city = ""
    @State private var zipCode = ""
    
    // Payment Information
    @State private var selectedPaymentMethod: PaymentMethod = .cash
    @State private var useApplePay = false
    
    private let steps = ["Delivery", "Payment", "Review"]
    
    // Form validation
    private var isDeliveryFormValid: Bool {
        !fullName.isEmpty && !address.isEmpty && !city.isEmpty && !zipCode.isEmpty
    }
    
    private var isPaymentFormValid: Bool {
        true // Payment method is always valid since it's pre-selected
    }
    
    private var canProceedToNextStep: Bool {
        switch currentStep {
        case 0: return isDeliveryFormValid
        case 1: return isPaymentFormValid
        default: return true
        }
    }
    
    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Progress indicator
                progressIndicator
                
                // Step content
                stepContent
                
                // Bottom actions
                bottomActions
            }
            .background(DesignSystem.Colors.background)
            .navigationTitle("Checkout")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        dismiss()
                    }
                    .foregroundColor(DesignSystem.Colors.textSecondary)
                }
            }
        }
    }
    
    // MARK: - Progress Indicator
    private var progressIndicator: some View {
        HStack {
            ForEach(0..<steps.count, id: \.self) { index in
                HStack {
                    Circle()
                        .fill(index <= currentStep ? DesignSystem.Colors.accent : DesignSystem.Colors.textTertiary)
                        .frame(width: 24, height: 24)
                        .overlay(
                            Text("\(index + 1)")
                                .font(DesignSystem.Typography.small)
                                .foregroundColor(index <= currentStep ? DesignSystem.Colors.secondary : DesignSystem.Colors.background)
                        )
                    
                    Text(steps[index])
                        .font(DesignSystem.Typography.caption)
                        .foregroundColor(index <= currentStep ? DesignSystem.Colors.textPrimary : DesignSystem.Colors.textTertiary)
                    
                    if index < steps.count - 1 {
                        Rectangle()
                            .fill(index < currentStep ? DesignSystem.Colors.accent : DesignSystem.Colors.textTertiary)
                            .frame(height: 2)
                            .frame(maxWidth: .infinity)
                    }
                }
            }
        }
        .padding(.horizontal, DesignSystem.Spacing.lg)
        .padding(.vertical, DesignSystem.Spacing.md)
    }
    
    // MARK: - Step Content
    private var stepContent: some View {
        ScrollView {
            VStack(spacing: DesignSystem.Spacing.lg) {
                switch currentStep {
                case 0:
                    deliveryStep
                case 1:
                    paymentStep
                case 2:
                    reviewStep
                default:
                    EmptyView()
                }
            }
            .padding(.horizontal, DesignSystem.Spacing.lg)
        }
    }
    
    // MARK: - Delivery Step
    private var deliveryStep: some View {
        VStack(alignment: .leading, spacing: DesignSystem.Spacing.lg) {
            Text("Delivery Information")
                .font(DesignSystem.Typography.title2)
                .foregroundColor(DesignSystem.Colors.textPrimary)
            
            VStack(spacing: DesignSystem.Spacing.md) {
                ModernTextField(placeholder: "Full Name", text: $fullName, icon: "person")
                ModernTextField(placeholder: "Address", text: $address, icon: "house")
                ModernTextField(placeholder: "City", text: $city, icon: "location")
                ModernTextField(placeholder: "ZIP Code", text: $zipCode, icon: "envelope")
            }
        }
    }
    
    // MARK: - Payment Step
    private var paymentStep: some View {
        VStack(alignment: .leading, spacing: DesignSystem.Spacing.lg) {
            Text("Payment Method")
                .font(DesignSystem.Typography.title2)
                .foregroundColor(DesignSystem.Colors.textPrimary)
            
            VStack(spacing: DesignSystem.Spacing.md) {
                ForEach(PaymentMethod.allCases, id: \.self) { method in
                    PaymentMethodRow(
                        method: method,
                        isSelected: selectedPaymentMethod == method,
                        onSelect: { selectedPaymentMethod = method }
                    )
                }
            }
            
            if selectedPaymentMethod == .applePay {
                VStack(spacing: DesignSystem.Spacing.sm) {
                    HStack {
                        Image(systemName: "info.circle")
                            .foregroundColor(DesignSystem.Colors.accent)
                        Text("Apple Pay will be processed at the time of delivery")
                            .font(DesignSystem.Typography.caption)
                            .foregroundColor(DesignSystem.Colors.textSecondary)
                        Spacer()
                    }
                }
                .padding(DesignSystem.Spacing.md)
                .background(DesignSystem.Colors.supporting.opacity(0.3))
                .cornerRadius(DesignSystem.CornerRadius.md)
            }
        }
    }
    
    // MARK: - Review Step
    private var reviewStep: some View {
        VStack(alignment: .leading, spacing: DesignSystem.Spacing.lg) {
            Text("Order Review")
                .font(DesignSystem.Typography.title2)
                .foregroundColor(DesignSystem.Colors.textPrimary)
            
            VStack(spacing: DesignSystem.Spacing.md) {
                ForEach(cartItems, id: \.product.id) { item in
                    HStack {
                        Text(item.product.name)
                            .bodyText()
                        Spacer()
                        Text("\(item.quantity)x $\(NSDecimalNumber(decimal: item.product.price).doubleValue, specifier: "%.2f")")
                            .captionText()
                    }
                }
                
                Divider()
                
                HStack {
                    Text("Total")
                        .font(DesignSystem.Typography.headline)
                        .foregroundColor(DesignSystem.Colors.textPrimary)
                    Spacer()
                    Text("$\(NSDecimalNumber(decimal: total).doubleValue, specifier: "%.2f")")
                        .font(DesignSystem.Typography.headline)
                        .fontWeight(.semibold)
                        .foregroundColor(DesignSystem.Colors.textPrimary)
                }
            }
            .padding(DesignSystem.Spacing.md)
            .luxuryCard()
        }
    }
    
    // MARK: - Bottom Actions
    private var bottomActions: some View {
        VStack(spacing: DesignSystem.Spacing.md) {
            if currentStep < steps.count - 1 {
                LuxuryButton("Continue", style: .primary) {
                    withAnimation(DesignSystem.Animation.smooth) {
                        currentStep += 1
                    }
                }
                .disabled(!canProceedToNextStep)
            } else {
                LuxuryButton(
                    "Place Order",
                    style: .accent,
                    isLoading: isProcessing
                ) {
                    processOrder()
                }
            }
            
            if currentStep > 0 {
                LuxuryButton("Back", style: .ghost) {
                    withAnimation(DesignSystem.Animation.smooth) {
                        currentStep -= 1
                    }
                }
            }
        }
        .padding(.horizontal, DesignSystem.Spacing.lg)
        .padding(.bottom, DesignSystem.Spacing.lg)
        .background(DesignSystem.Colors.surface)
    }
    
    private func processOrder() {
        isProcessing = true
        
        Task {
            do {
                // Create cart items from the current cart
                let cartItemsDTO = cartItems.map { item in
                    CartItemDTO(productId: item.product.id, quantity: item.quantity)
                }
                
                // Create checkout request
                let checkoutRequest = CheckoutRequest(cartItems: cartItemsDTO)
                
                // Submit order to backend
                let _ = try await NetworkManager.shared.checkout(request: checkoutRequest)
                
                await MainActor.run {
                    isProcessing = false
                    onOrderComplete()
                }
                
            } catch {
                await MainActor.run {
                    isProcessing = false
                    // Handle error - could show alert here
                    print("Order failed: \(error)")
                }
            }
        }
    }
}

// MARK: - Payment Method Row
struct PaymentMethodRow: View {
    let method: PaymentMethod
    let isSelected: Bool
    let onSelect: () -> Void
    
    var body: some View {
        Button(action: onSelect) {
            HStack(spacing: DesignSystem.Spacing.md) {
                Image(systemName: method.icon)
                    .foregroundColor(isSelected ? DesignSystem.Colors.accent : DesignSystem.Colors.textSecondary)
                    .frame(width: 24)
                
                Text(method.rawValue)
                    .font(DesignSystem.Typography.body)
                    .foregroundColor(DesignSystem.Colors.textPrimary)
                
                Spacer()
                
                Image(systemName: isSelected ? "checkmark.circle.fill" : "circle")
                    .foregroundColor(isSelected ? DesignSystem.Colors.accent : DesignSystem.Colors.textTertiary)
            }
            .padding(DesignSystem.Spacing.md)
            .background(
                RoundedRectangle(cornerRadius: DesignSystem.CornerRadius.md)
                    .fill(isSelected ? DesignSystem.Colors.accent.opacity(0.1) : DesignSystem.Colors.surface)
                    .overlay(
                        RoundedRectangle(cornerRadius: DesignSystem.CornerRadius.md)
                            .stroke(isSelected ? DesignSystem.Colors.accent : DesignSystem.Colors.textTertiary.opacity(0.3), lineWidth: 1)
                    )
            )
        }
        .buttonStyle(PlainButtonStyle())
    }
}

// Note: Using ModernTextField component instead of custom LuxuryTextField to avoid naming conflicts

// MARK: - Preview
struct LuxuryCartView_Previews: PreviewProvider {
    static var previews: some View {
        LuxuryCartView()
            .environmentObject(CartManager.shared)
    }
} 