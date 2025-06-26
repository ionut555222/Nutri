import SwiftUI

struct LuxuryCategoryView: View {
    let category: Category
    @EnvironmentObject private var cartManager: CartManager
    @StateObject private var viewModel = LuxuryCategoryViewModel()
    @Environment(\.dismiss) private var dismiss
    @State private var selectedProduct: Product?
    
    var body: some View {
        VStack(spacing: 0) {
            // Custom Navigation
            navigationHeader
            
            // Products Content
            if viewModel.isLoading {
                LuxuryLoadingView()
            } else if viewModel.categoryProducts.isEmpty {
                LuxuryEmptyState(
                    title: "No Products Found",
                    message: "We don't have any products in this category yet.",
                    actionTitle: "Browse All Products",
                    action: {
                        dismiss()
                    }
                )
            } else {
                productsGrid
            }
        }
        .background(DesignSystem.Colors.background)
        .onAppear {
            Task {
                await viewModel.loadCategoryProducts(categoryId: category.id)
            }
        }
        .sheet(item: $selectedProduct) { product in
            NavigationView {
                LuxuryProductDetailView(product: product)
            }
        }
    }
    
    // MARK: - Navigation Header
    private var navigationHeader: some View {
        HStack {
            Button {
                dismiss()
            } label: {
                Image(systemName: "chevron.left")
                    .font(.title2)
                    .foregroundColor(DesignSystem.Colors.textPrimary)
            }
            
            Spacer()
            
            Text(category.name)
                .font(DesignSystem.Typography.title2)
                .foregroundColor(DesignSystem.Colors.textPrimary)
            
            Spacer()
            
            // Placeholder for symmetry
            Image(systemName: "chevron.left")
                .font(.title2)
                .opacity(0)
        }
        .padding(.horizontal, DesignSystem.Spacing.lg)
        .padding(.vertical, DesignSystem.Spacing.md)
    }
    
    // MARK: - Products Grid
    private var productsGrid: some View {
        ScrollView {
            LazyVGrid(
                columns: [
                    GridItem(.flexible(), spacing: DesignSystem.Spacing.md),
                    GridItem(.flexible(), spacing: DesignSystem.Spacing.md)
                ],
                spacing: DesignSystem.Spacing.lg
            ) {
                ForEach(viewModel.categoryProducts) { product in
                    LuxuryProductCard(
                        product: product,
                        onTap: {
                            selectedProduct = product
                        },
                        onAddToCart: {
                            cartManager.addToCart(productId: product.id)
                        }
                    )
                }
            }
            .padding(.horizontal, DesignSystem.Spacing.lg)
            .padding(.bottom, DesignSystem.Spacing.xxl)
        }
        .refreshable {
            await viewModel.loadCategoryProducts(categoryId: category.id)
        }
    }
}

// MARK: - View Model
@MainActor
class LuxuryCategoryViewModel: ObservableObject {
    @Published var categoryProducts: [Product] = []
    @Published var isLoading = false
    
    private let networkManager = NetworkManager.shared
    
    func loadCategoryProducts(categoryId: Int) async {
        isLoading = true
        defer { isLoading = false }
        
        do {
            let allProducts = try await networkManager.fetchProducts()
            categoryProducts = allProducts.filter { $0.category.id == categoryId }
        } catch {
            // Handle error
            categoryProducts = []
        }
    }
}

// MARK: - Preview
struct LuxuryCategoryView_Previews: PreviewProvider {
    static var previews: some View {
        LuxuryCategoryView(
            category: Category(
                id: 1,
                name: "Fruits"
            )
        )
        .environmentObject(CartManager.shared)
    }
} 