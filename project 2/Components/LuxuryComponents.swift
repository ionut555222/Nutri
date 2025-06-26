import SwiftUI

// MARK: - Luxury Product Card
struct LuxuryProductCard: View {
    let product: Product
    let onTap: () -> Void
    let onAddToCart: () -> Void
    @State private var isHovered = false
    
    var body: some View {
        VStack(alignment: .leading, spacing: DesignSystem.Spacing.md) {
            // Product Image
            ZStack {
                ProductImageView(
                    imageUrl: product.imageUrl,
                    fallbackImageName: "photo",
                    size: CGSize(width: 200, height: 220)
                )
                .frame(height: 220)
                .frame(maxWidth: .infinity)
                .clipped()
                .cornerRadius(DesignSystem.CornerRadius.lg)
                
                // Hover overlay
                if isHovered {
                    Rectangle()
                        .fill(DesignSystem.Colors.overlay)
                        .cornerRadius(DesignSystem.CornerRadius.lg)
                        .overlay(
                            Button(action: onAddToCart) {
                                Text("Add to Cart")
                                    .luxuryButton(style: .accent)
                            }
                        )
                        .transition(.opacity)
                }
            }
            .onHover { hovering in
                withAnimation(DesignSystem.Animation.smooth) {
                    isHovered = hovering
                }
            }
            
            // Product Info
            VStack(alignment: .leading, spacing: DesignSystem.Spacing.xs) {
                Text(product.name)
                    .font(DesignSystem.Typography.headline)
                    .foregroundColor(DesignSystem.Colors.textPrimary)
                    .lineLimit(2)
                
                if let description = product.description {
                    Text(description)
                        .captionText()
                        .lineLimit(2)
                }
                
                Spacer(minLength: DesignSystem.Spacing.sm)
                
                HStack {
                    Text("$\(NSDecimalNumber(decimal: product.price).doubleValue, specifier: "%.2f")")
                        .font(DesignSystem.Typography.bodyMedium)
                        .foregroundColor(DesignSystem.Colors.textPrimary)
                    
                    Spacer()
                    
                    Text("per \(product.unit.rawValue)")
                        .captionText()
                }
            }
            .padding(.horizontal, DesignSystem.Spacing.sm)
        }
        .luxuryCard()
        .scaleEffect(isHovered ? 1.02 : 1.0)
        .animation(DesignSystem.Animation.gentle, value: isHovered)
        .onTapGesture(perform: onTap)
    }
}

// MARK: - Luxury Category Card
struct LuxuryCategoryCard: View {
    let category: Category
    let onTap: () -> Void
    @State private var isPressed = false
    
    var body: some View {
        ZStack {
            // Background image or color
            Rectangle()
                .fill(
                    LinearGradient(
                        colors: [
                            DesignSystem.Colors.natural.opacity(0.8),
                            DesignSystem.Colors.natural.opacity(0.6)
                        ],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
            
            // Content
            VStack {
                Spacer()
                
                Text(category.name)
                    .font(DesignSystem.Typography.title2)
                    .foregroundColor(DesignSystem.Colors.primary)
                    .fontWeight(.light)
                    .multilineTextAlignment(.center)
                
                Spacer()
            }
            .padding(DesignSystem.Spacing.lg)
        }
        .frame(height: 160)
        .cornerRadius(DesignSystem.CornerRadius.lg)
        .scaleEffect(isPressed ? 0.98 : 1.0)
        .animation(DesignSystem.Animation.quick, value: isPressed)
                    .onLongPressGesture(minimumDuration: 0, maximumDistance: .infinity) {
                // On press
                withAnimation {
                    isPressed = true
                }
            } onPressingChanged: { pressing in
                if !pressing {
                    withAnimation {
                        isPressed = false
                    }
                    onTap()
                }
            }
    }
}

// MARK: - Luxury Navigation Bar
struct LuxuryNavigationBar: View {
    let title: String
    let showBackButton: Bool
    let onBack: (() -> Void)?
    let trailingContent: (() -> AnyView)?
    
    init(
        title: String,
        showBackButton: Bool = false,
        onBack: (() -> Void)? = nil,
        @ViewBuilder trailingContent: @escaping () -> AnyView = { AnyView(EmptyView()) }
    ) {
        self.title = title
        self.showBackButton = showBackButton
        self.onBack = onBack
        self.trailingContent = trailingContent
    }
    
    var body: some View {
        HStack {
            if showBackButton {
                Button(action: onBack ?? {}) {
                    Image(systemName: "chevron.left")
                        .font(.title2)
                        .foregroundColor(DesignSystem.Colors.textPrimary)
                }
            }
            
            Spacer()
            
            Text(title)
                .font(DesignSystem.Typography.headline)
                .foregroundColor(DesignSystem.Colors.textPrimary)
            
            Spacer()
            
            trailingContent?() ?? AnyView(EmptyView())
        }
        .padding(.horizontal, DesignSystem.Spacing.lg)
        .padding(.vertical, DesignSystem.Spacing.md)
        .background(
            DesignSystem.Colors.background
                .shadow(color: DesignSystem.Shadow.card, radius: 1, y: 1)
        )
    }
}

// MARK: - Luxury Button
struct LuxuryButton: View {
    let title: String
    let style: LuxuryButtonStyle
    let isLoading: Bool
    let action: () -> Void
    
    init(
        _ title: String,
        style: LuxuryButtonStyle = .primary,
        isLoading: Bool = false,
        action: @escaping () -> Void
    ) {
        self.title = title
        self.style = style
        self.isLoading = isLoading
        self.action = action
    }
    
    var body: some View {
        Button(action: action) {
            HStack {
                if isLoading {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: style.textColor))
                        .scaleEffect(0.8)
                } else {
                    Text(title)
                }
            }
            .frame(minWidth: 120)
            .luxuryButton(style: style)
        }
        .disabled(isLoading)
    }
}

// MARK: - Luxury Search Bar
struct LuxurySearchBar: View {
    @Binding var text: String
    let placeholder: String
    let onSearchButtonClicked: () -> Void
    @State private var isEditing = false
    
    var body: some View {
        HStack {
            HStack {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(DesignSystem.Colors.textTertiary)
                
                TextField(placeholder, text: $text, onEditingChanged: { editing in
                    isEditing = editing
                }, onCommit: {
                    onSearchButtonClicked()
                })
                .font(DesignSystem.Typography.body)
                .foregroundColor(DesignSystem.Colors.textPrimary)
                
                if !text.isEmpty {
                    Button(action: {
                        text = ""
                    }) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(DesignSystem.Colors.textTertiary)
                    }
                }
            }
            .padding(.horizontal, DesignSystem.Spacing.md)
            .padding(.vertical, DesignSystem.Spacing.sm)
            .background(DesignSystem.Colors.supporting)
            .cornerRadius(DesignSystem.CornerRadius.pill)
            
            if isEditing {
                Button("Cancel") {
                    text = ""
                    isEditing = false
                    UIApplication.shared.endEditing()
                }
                .font(DesignSystem.Typography.body)
                .foregroundColor(DesignSystem.Colors.textSecondary)
                .transition(.move(edge: .trailing))
            }
        }
        .animation(DesignSystem.Animation.smooth, value: isEditing)
    }
}

// MARK: - Luxury Loading View
struct LuxuryLoadingView: View {
    var body: some View {
        VStack(spacing: DesignSystem.Spacing.lg) {
            ProgressView()
                .progressViewStyle(CircularProgressViewStyle(tint: DesignSystem.Colors.accent))
                .scaleEffect(1.2)
            
            Text("Loading...")
                .captionText()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(DesignSystem.Colors.background)
    }
}

// MARK: - Luxury Empty State
struct LuxuryEmptyState: View {
    let title: String
    let message: String
    let actionTitle: String?
    let action: (() -> Void)?
    
    init(
        title: String,
        message: String,
        actionTitle: String? = nil,
        action: (() -> Void)? = nil
    ) {
        self.title = title
        self.message = message
        self.actionTitle = actionTitle
        self.action = action
    }
    
    var body: some View {
        VStack(spacing: DesignSystem.Spacing.lg) {
            Image(systemName: "tray")
                .font(.system(size: 48))
                .foregroundColor(DesignSystem.Colors.textTertiary)
            
            VStack(spacing: DesignSystem.Spacing.sm) {
                Text(title)
                    .font(DesignSystem.Typography.title3)
                    .foregroundColor(DesignSystem.Colors.textPrimary)
                
                Text(message)
                    .bodyText()
                    .multilineTextAlignment(.center)
            }
            
            if let actionTitle = actionTitle, let action = action {
                LuxuryButton(actionTitle, style: .accent, action: action)
            }
        }
        .padding(DesignSystem.Spacing.xxl)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

// MARK: - UIApplication Extension
extension UIApplication {
    func endEditing() {
        sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
    }
} 