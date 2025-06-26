import SwiftUI

struct LuxuryProductsView: View {
    @EnvironmentObject private var cartManager: CartManager
    @StateObject private var viewModel = LuxuryProductsViewModel()
    @State private var searchText = ""
    @State private var showFilters = false
    
    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Custom Navigation
                navigationHeader
                
                // Filter Bar
                filterBar
                
                // Products Content
                if viewModel.isLoading {
                    LuxuryLoadingView()
                } else if viewModel.filteredProducts.isEmpty {
                    LuxuryEmptyState(
                        title: "No Products Found",
                        message: "Try adjusting your filters or search terms",
                        actionTitle: "Reset Filters",
                        action: {
                            viewModel.resetFilters()
                        }
                    )
                } else {
                    productsGrid
                }
            }
            .background(DesignSystem.Colors.background)
            .navigationBarHidden(true)
            .sheet(isPresented: $showFilters) {
                LuxuryFiltersView(viewModel: viewModel)
            }
            .sheet(item: $viewModel.selectedProduct) { product in
                NavigationView {
                    LuxuryProductDetailView(product: product)
                }
            }
        }
        .searchable(text: $searchText, prompt: "Search products...")
        .onChange(of: searchText) { _, newValue in
            viewModel.searchText = newValue
        }
        .onAppear {
            Task {
                await viewModel.loadProducts()
            }
        }
    }
    
    // MARK: - Navigation Header
    private var navigationHeader: some View {
        HStack {
            Text("Products")
                .font(DesignSystem.Typography.title1)
                .foregroundColor(DesignSystem.Colors.textPrimary)
            
            Spacer()
            
            HStack(spacing: DesignSystem.Spacing.md) {
                // Sort button
                Button {
                    viewModel.toggleSortOrder()
                } label: {
                    Image(systemName: viewModel.sortAscending ? "arrow.up.arrow.down" : "arrow.down.arrow.up")
                        .foregroundColor(DesignSystem.Colors.textPrimary)
                }
                
                // Filter button
                Button {
                    showFilters = true
                } label: {
                    Image(systemName: "line.3.horizontal.decrease.circle")
                        .foregroundColor(DesignSystem.Colors.textPrimary)
                        .overlay(
                            // Filter indicator
                            Circle()
                                .fill(DesignSystem.Colors.accent)
                                .frame(width: 8, height: 8)
                                .offset(x: 8, y: -8)
                                .opacity(viewModel.hasActiveFilters ? 1 : 0)
                        )
                }
            }
        }
        .padding(.horizontal, DesignSystem.Spacing.lg)
        .padding(.vertical, DesignSystem.Spacing.md)
    }
    
    // MARK: - Filter Bar
    private var filterBar: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: DesignSystem.Spacing.sm) {
                // Category filters
                ForEach(viewModel.categories) { category in
                    FilterChip(
                        title: category.name,
                        isSelected: viewModel.selectedCategoryId == category.id
                    ) {
                        viewModel.toggleCategory(category.id)
                    }
                }
                
                // Price range filter
                if viewModel.hasActiveFilters {
                    FilterChip(
                        title: "Clear All",
                        isSelected: false,
                        style: .destructive
                    ) {
                        viewModel.resetFilters()
                    }
                }
            }
            .padding(.horizontal, DesignSystem.Spacing.lg)
        }
        .padding(.vertical, DesignSystem.Spacing.sm)
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
                ForEach(viewModel.filteredProducts) { product in
                    LuxuryProductCard(
                        product: product,
                        onTap: {
                            // Navigate to product detail
                            viewModel.selectedProduct = product
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
            await viewModel.loadProducts()
        }
    }
}

// MARK: - Filter Chip
struct FilterChip: View {
    let title: String
    let isSelected: Bool
    let style: FilterChipStyle
    let action: () -> Void
    
    enum FilterChipStyle {
        case normal
        case destructive
    }
    
    init(
        title: String,
        isSelected: Bool,
        style: FilterChipStyle = .normal,
        action: @escaping () -> Void
    ) {
        self.title = title
        self.isSelected = isSelected
        self.style = style
        self.action = action
    }
    
    var body: some View {
        Button(action: action) {
            Text(title)
                .font(DesignSystem.Typography.caption)
                .foregroundColor(foregroundColor)
                .padding(.horizontal, DesignSystem.Spacing.md)
                .padding(.vertical, DesignSystem.Spacing.sm)
                .background(backgroundColor)
                .cornerRadius(DesignSystem.CornerRadius.pill)
                .overlay(
                    RoundedRectangle(cornerRadius: DesignSystem.CornerRadius.pill)
                        .stroke(borderColor, lineWidth: 1)
                )
        }
    }
    
    private var foregroundColor: Color {
        switch style {
        case .normal:
            return isSelected ? DesignSystem.Colors.primary : DesignSystem.Colors.textSecondary
        case .destructive:
            return DesignSystem.Colors.error
        }
    }
    
    private var backgroundColor: Color {
        switch style {
        case .normal:
            return isSelected ? DesignSystem.Colors.accent : DesignSystem.Colors.supporting
        case .destructive:
            return DesignSystem.Colors.supporting
        }
    }
    
    private var borderColor: Color {
        switch style {
        case .normal:
            return isSelected ? DesignSystem.Colors.accent : DesignSystem.Colors.textTertiary
        case .destructive:
            return DesignSystem.Colors.error
        }
    }
}

// MARK: - View Model
@MainActor
class LuxuryProductsViewModel: ObservableObject {
    @Published var products: [Product] = []
    @Published var categories: [Category] = []
    @Published var filteredProducts: [Product] = []
    @Published var selectedCategoryId: Int?
    @Published var searchText = "" {
        didSet {
            filterProducts()
        }
    }
    @Published var sortAscending = true {
        didSet {
            filterProducts()
        }
    }
    @Published var isLoading = false
    @Published var selectedProduct: Product?
    
    private let networkManager = NetworkManager.shared
    
    var hasActiveFilters: Bool {
        selectedCategoryId != nil
    }
    
    func loadProducts() async {
        isLoading = true
        defer { isLoading = false }
        
        do {
            products = try await networkManager.fetchProducts()
            categories = try await networkManager.fetchCategories()
            filterProducts()
        } catch {
            // Handle error
        }
    }
    
    func toggleCategory(_ categoryId: Int) {
        if selectedCategoryId == categoryId {
            selectedCategoryId = nil
        } else {
            selectedCategoryId = categoryId
        }
        filterProducts()
    }
    
    func toggleSortOrder() {
        sortAscending.toggle()
    }
    
    func resetFilters() {
        selectedCategoryId = nil
        searchText = ""
        filterProducts()
    }
    
    private func filterProducts() {
        var filtered = products
        
        // Filter by category
        if let categoryId = selectedCategoryId {
            filtered = filtered.filter { $0.category.id == categoryId }
        }
        
        // Filter by search text
        if !searchText.isEmpty {
            filtered = filtered.filter {
                $0.name.localizedCaseInsensitiveContains(searchText) ||
                ($0.description?.localizedCaseInsensitiveContains(searchText) ?? false)
            }
        }
        
        // Sort products
        filtered = filtered.sorted(by: { first, second in
            if sortAscending {
                return NSDecimalNumber(decimal: first.price).doubleValue < NSDecimalNumber(decimal: second.price).doubleValue
            } else {
                return NSDecimalNumber(decimal: first.price).doubleValue > NSDecimalNumber(decimal: second.price).doubleValue
            }
        })
        
        filteredProducts = filtered
    }
}

// MARK: - Filters View
struct LuxuryFiltersView: View {
    @ObservedObject var viewModel: LuxuryProductsViewModel
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        NavigationView {
            VStack(alignment: .leading, spacing: DesignSystem.Spacing.lg) {
                Text("Filters")
                    .titleText()
                    .padding(.horizontal, DesignSystem.Spacing.lg)
                
                ScrollView {
                    VStack(alignment: .leading, spacing: DesignSystem.Spacing.xl) {
                        // Categories
                        VStack(alignment: .leading, spacing: DesignSystem.Spacing.md) {
                            Text("Categories")
                                .font(DesignSystem.Typography.headline)
                                .foregroundColor(DesignSystem.Colors.textPrimary)
                            
                            LazyVGrid(
                                columns: [
                                    GridItem(.flexible()),
                                    GridItem(.flexible())
                                ],
                                spacing: DesignSystem.Spacing.sm
                            ) {
                                ForEach(viewModel.categories) { category in
                                    FilterChip(
                                        title: category.name,
                                        isSelected: viewModel.selectedCategoryId == category.id
                                    ) {
                                        viewModel.toggleCategory(category.id)
                                    }
                                }
                            }
                        }
                        
                        Spacer()
                    }
                    .padding(.horizontal, DesignSystem.Spacing.lg)
                }
                
                // Apply button
                VStack(spacing: DesignSystem.Spacing.md) {
                    if viewModel.hasActiveFilters {
                        LuxuryButton("Clear All", style: .ghost) {
                            viewModel.resetFilters()
                            dismiss()
                        }
                    }
                    
                    LuxuryButton("Apply Filters", style: .primary) {
                        dismiss()
                    }
                }
                .padding(.horizontal, DesignSystem.Spacing.lg)
                .padding(.bottom, DesignSystem.Spacing.lg)
            }
            .background(DesignSystem.Colors.background)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") {
                        dismiss()
                    }
                    .foregroundColor(DesignSystem.Colors.accent)
                }
            }
        }
    }
}

// MARK: - Preview
struct LuxuryProductsView_Previews: PreviewProvider {
    static var previews: some View {
        LuxuryProductsView()
            .environmentObject(CartManager.shared)
    }
} 