import SwiftUI

struct LuxuryHomeView: View {
    @EnvironmentObject private var cartManager: CartManager
    @StateObject private var viewModel = LuxuryHomeViewModel()
    @State private var searchText = ""
    @State private var selectedProduct: Product?
    @State private var selectedCategory: Category?
    @State private var showAllProducts = false
    
    var body: some View {
        NavigationView {
            ScrollView {
                LazyVStack(spacing: 0) {
                    // Hero Section
                    heroSection
                    
                    // Featured Products
                    featuredSection
                    
                    // Categories
                    categoriesSection
                    
                    // New Arrivals
                    if !viewModel.newArrivals.isEmpty {
                        newArrivalsSection
                    }
                }
            }
            .background(DesignSystem.Colors.background)
            .refreshable {
                await viewModel.refresh()
            }
        }
        .searchable(text: $searchText, prompt: "Search premium products...")
        .onAppear {
            Task {
                await viewModel.loadData()
            }
        }
        .sheet(item: $selectedProduct) { product in
            NavigationView {
                LuxuryProductDetailView(product: product)
            }
        }
        .sheet(isPresented: $showAllProducts) {
            NavigationView {
                LuxuryProductsView()
            }
        }
        .sheet(item: $selectedCategory) { category in
            LuxuryCategoryView(category: category)
        }
    }
    
    // MARK: - Hero Section
    private var heroSection: some View {
        ZStack {
            // Background gradient
            LinearGradient(
                colors: [
                    DesignSystem.Colors.natural.opacity(0.1),
                    DesignSystem.Colors.background
                ],
                startPoint: .top,
                endPoint: .bottom
            )
            .frame(height: 400)
            
            VStack(spacing: DesignSystem.Spacing.lg) {
                Spacer()
                
                // Hero text
                VStack(spacing: DesignSystem.Spacing.md) {
                    Text("Curated Excellence")
                        .heroText()
                        .multilineTextAlignment(.center)
                    
                    Text("Handpicked premium products\ndelivered to your doorstep")
                        .font(DesignSystem.Typography.body)
                        .foregroundColor(DesignSystem.Colors.textSecondary)
                        .multilineTextAlignment(.center)
                        .lineSpacing(4)
                }
                
                // CTA Button
                LuxuryButton("Explore Collection", style: .accent) {
                    showAllProducts = true
                }
                
                Spacer()
            }
            .padding(.horizontal, DesignSystem.Spacing.xl)
        }
    }
    
    // MARK: - Featured Section
    private var featuredSection: some View {
        VStack(alignment: .leading, spacing: DesignSystem.Spacing.lg) {
            // Section header
            HStack {
                Text("Featured")
                    .titleText()
                
                Spacer()
                
                Button("View All") {
                    showAllProducts = true
                }
                .font(DesignSystem.Typography.caption)
                .foregroundColor(DesignSystem.Colors.accent)
            }
            .padding(.horizontal, DesignSystem.Spacing.lg)
            
            // Featured products scroll
            ScrollView(.horizontal, showsIndicators: false) {
                LazyHStack(spacing: DesignSystem.Spacing.md) {
                    ForEach(viewModel.featuredProducts) { product in
                        LuxuryProductCard(
                            product: product,
                            onTap: {
                                selectedProduct = product
                            },
                            onAddToCart: {
                                cartManager.addToCart(productId: product.id)
                            }
                        )
                        .frame(width: 200)
                    }
                }
                .padding(.horizontal, DesignSystem.Spacing.lg)
            }
        }
        .padding(.vertical, DesignSystem.Spacing.xxl)
    }
    
    // MARK: - Categories Section
    private var categoriesSection: some View {
        VStack(alignment: .leading, spacing: DesignSystem.Spacing.lg) {
            Text("Shop by Category")
                .titleText()
                .padding(.horizontal, DesignSystem.Spacing.lg)
            
            LazyVGrid(
                columns: [
                    GridItem(.flexible()),
                    GridItem(.flexible())
                ],
                spacing: DesignSystem.Spacing.md
            ) {
                ForEach(viewModel.categories) { category in
                    LuxuryCategoryCard(category: category) {
                        selectedCategory = category
                    }
                }
            }
            .padding(.horizontal, DesignSystem.Spacing.lg)
        }
        .padding(.vertical, DesignSystem.Spacing.xxl)
        .background(DesignSystem.Colors.surface)
    }
    
    // MARK: - New Arrivals Section
    private var newArrivalsSection: some View {
        VStack(alignment: .leading, spacing: DesignSystem.Spacing.lg) {
            HStack {
                Text("New Arrivals")
                    .titleText()
                
                Spacer()
                
                Button("View All") {
                    showAllProducts = true
                }
                .font(DesignSystem.Typography.caption)
                .foregroundColor(DesignSystem.Colors.accent)
            }
            .padding(.horizontal, DesignSystem.Spacing.lg)
            
            LazyVGrid(
                columns: [
                    GridItem(.flexible()),
                    GridItem(.flexible())
                ],
                spacing: DesignSystem.Spacing.md
            ) {
                ForEach(viewModel.newArrivals.prefix(4)) { product in
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
        }
        .padding(.vertical, DesignSystem.Spacing.xxl)
    }
}

// MARK: - View Model
@MainActor
class LuxuryHomeViewModel: ObservableObject {
    @Published var featuredProducts: [Product] = []
    @Published var categories: [Category] = []
    @Published var newArrivals: [Product] = []
    @Published var isLoading = false
    @Published var errorMessage: String?
    
    private let networkManager = NetworkManager.shared
    
    func loadData() async {
        isLoading = true
        defer { isLoading = false }
        
        do {
            // Load featured products (first 5)
            let products = try await networkManager.fetchProducts()
            featuredProducts = Array(products.prefix(5))
            
            // Load new arrivals (last 8 products)
            newArrivals = Array(products.suffix(8))
            
            // Load categories
            categories = try await networkManager.fetchCategories()
            
        } catch {
            errorMessage = "Failed to load data: \(error.localizedDescription)"
        }
    }
    
    func refresh() async {
        await loadData()
    }
}

// MARK: - Preview
struct LuxuryHomeView_Previews: PreviewProvider {
    static var previews: some View {
        LuxuryHomeView()
            .environmentObject(CartManager.shared)
    }
} 