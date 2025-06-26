import SwiftUI

struct LuxuryProductDetailView: View {
    let product: Product
    @EnvironmentObject private var cartManager: CartManager
    @Environment(\.dismiss) private var dismiss
    @State private var quantity: Int = 1
    @State private var showingAddedToCart = false
    
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: DesignSystem.Spacing.lg) {
                // Product Image
                productImageSection
                
                // Product Info
                productInfoSection
                
                // Quantity Selector
                quantitySection
                
                // Add to Cart Button
                addToCartSection
                
                // Product Description
                if let description = product.description {
                    descriptionSection(description)
                }
                
                Spacer(minLength: DesignSystem.Spacing.xxl)
            }
            .padding(.horizontal, DesignSystem.Spacing.lg)
        }
        .background(DesignSystem.Colors.background)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button("Close") {
                    dismiss()
                }
                .foregroundColor(DesignSystem.Colors.accent)
            }
        }
        .alert("Added to Cart", isPresented: $showingAddedToCart) {
            Button("Continue Shopping", role: .cancel) { }
            Button("View Cart") {
                // Navigate to cart - we'll implement this
            }
        } message: {
            Text("\(product.name) has been added to your cart.")
        }
    }
    
    // MARK: - Product Image Section
    private var productImageSection: some View {
        ZStack {
            Rectangle()
                .fill(DesignSystem.Colors.supporting)
                .frame(height: 300)
                .cornerRadius(DesignSystem.CornerRadius.lg)
            
            Image(systemName: "photo")
                .font(.system(size: 60))
                .foregroundColor(DesignSystem.Colors.textTertiary)
        }
    }
    
    // MARK: - Product Info Section
    private var productInfoSection: some View {
        VStack(alignment: .leading, spacing: DesignSystem.Spacing.md) {
            Text(product.name)
                .font(DesignSystem.Typography.title1)
                .foregroundColor(DesignSystem.Colors.textPrimary)
            
            Text(product.category.name)
                .font(DesignSystem.Typography.body)
                .foregroundColor(DesignSystem.Colors.accent)
                .padding(.horizontal, DesignSystem.Spacing.md)
                .padding(.vertical, DesignSystem.Spacing.xs)
                .background(DesignSystem.Colors.accent.opacity(0.1))
                .cornerRadius(DesignSystem.CornerRadius.sm)
            
            HStack {
                Text("$\(NSDecimalNumber(decimal: product.price).doubleValue, specifier: "%.2f")")
                    .font(DesignSystem.Typography.title2)
                    .foregroundColor(DesignSystem.Colors.textPrimary)
                    .fontWeight(.semibold)
                
                Text("per \(product.unit.rawValue)")
                    .font(DesignSystem.Typography.body)
                    .foregroundColor(DesignSystem.Colors.textSecondary)
                
                Spacer()
            }
        }
    }
    
    // MARK: - Quantity Section
    private var quantitySection: some View {
        VStack(alignment: .leading, spacing: DesignSystem.Spacing.sm) {
            Text("Quantity")
                .font(DesignSystem.Typography.headline)
                .foregroundColor(DesignSystem.Colors.textPrimary)
            
            HStack(spacing: DesignSystem.Spacing.md) {
                Button {
                    if quantity > 1 {
                        quantity -= 1
                    }
                } label: {
                    Image(systemName: "minus.circle")
                        .font(.title2)
                        .foregroundColor(quantity > 1 ? DesignSystem.Colors.textPrimary : DesignSystem.Colors.textTertiary)
                }
                .disabled(quantity <= 1)
                
                Text("\(quantity)")
                    .font(DesignSystem.Typography.title3)
                    .foregroundColor(DesignSystem.Colors.textPrimary)
                    .frame(minWidth: 40)
                
                Button {
                    quantity += 1
                } label: {
                    Image(systemName: "plus.circle")
                        .font(.title2)
                        .foregroundColor(DesignSystem.Colors.textPrimary)
                }
                
                Spacer()
                
                Text("Total: $\(NSDecimalNumber(decimal: product.price).doubleValue * Double(quantity), specifier: "%.2f")")
                    .font(DesignSystem.Typography.bodyMedium)
                    .foregroundColor(DesignSystem.Colors.textSecondary)
            }
        }
        .padding(.vertical, DesignSystem.Spacing.md)
    }
    
    // MARK: - Add to Cart Section
    private var addToCartSection: some View {
        LuxuryButton("Add to Cart", style: .accent) {
            // Add multiple quantities to cart
            for _ in 0..<quantity {
                cartManager.addToCart(productId: product.id)
            }
            showingAddedToCart = true
        }
    }
    
    // MARK: - Description Section
    private func descriptionSection(_ description: String) -> some View {
        VStack(alignment: .leading, spacing: DesignSystem.Spacing.sm) {
            Text("Description")
                .font(DesignSystem.Typography.headline)
                .foregroundColor(DesignSystem.Colors.textPrimary)
            
            Text(description)
                .font(DesignSystem.Typography.body)
                .foregroundColor(DesignSystem.Colors.textSecondary)
                .lineSpacing(4)
        }
        .padding(.vertical, DesignSystem.Spacing.md)
    }
}

// MARK: - Preview
struct LuxuryProductDetailView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            LuxuryProductDetailView(
                product: Product(
                    id: 1,
                    name: "Organic Avocados",
                    description: "Fresh, creamy organic avocados perfect for your morning toast or healthy smoothies.",
                    price: Decimal(4.99),
                    stock: 50,
                    unit: .piece,
                    category: Category(id: 1, name: "Fruits")
                )
            )
            .environmentObject(CartManager.shared)
        }
    }
} 