import SwiftUI

// MARK: - Design System
struct DesignSystem {
    
    // MARK: - Colors
    struct Colors {
        // Primary palette
        static let primary = Color.white
        static let secondary = Color(hex: "2C2C2C")
        static let accent = Color(hex: "D4AF37") // Champagne gold
        static let supporting = Color(hex: "F8F8F8")
        static let natural = Color(hex: "87A96B") // Sage green
        
        // Semantic colors
        static let background = Color.white
        static let surface = Color(hex: "F8F8F8")
        static let textPrimary = Color(hex: "2C2C2C")
        static let textSecondary = Color(hex: "6B6B6B")
        static let textTertiary = Color(hex: "9B9B9B")
        
        // State colors
        static let success = Color(hex: "4CAF50")
        static let warning = Color(hex: "FF9800")
        static let error = Color(hex: "F44336")
        
        // Overlay colors
        static let overlay = Color.black.opacity(0.4)
        static let cardShadow = Color.black.opacity(0.08)
    }
    
    // MARK: - Typography
    struct Typography {
        // Font weights
        static let ultraLight = Font.Weight.ultraLight
        static let light = Font.Weight.light
        static let regular = Font.Weight.regular
        static let medium = Font.Weight.medium
        static let semibold = Font.Weight.semibold
        
        // Font sizes
        static let heroTitle = Font.system(size: 48, weight: .ultraLight)
        static let title1 = Font.system(size: 32, weight: .light)
        static let title2 = Font.system(size: 24, weight: .regular)
        static let title3 = Font.system(size: 20, weight: .medium)
        static let headline = Font.system(size: 18, weight: .medium)
        static let body = Font.system(size: 16, weight: .regular)
        static let bodyMedium = Font.system(size: 16, weight: .medium)
        static let caption = Font.system(size: 14, weight: .regular)
        static let captionMedium = Font.system(size: 14, weight: .medium)
        static let small = Font.system(size: 12, weight: .regular)
        
        // Letter spacing
        static let heroLetterSpacing: CGFloat = 1.5
        static let titleLetterSpacing: CGFloat = 0.5
        static let bodyLetterSpacing: CGFloat = 0.0
    }
    
    // MARK: - Spacing
    struct Spacing {
        static let micro: CGFloat = 4
        static let xs: CGFloat = 8
        static let sm: CGFloat = 12
        static let md: CGFloat = 16
        static let lg: CGFloat = 24
        static let xl: CGFloat = 32
        static let xxl: CGFloat = 48
        static let xxxl: CGFloat = 64
        static let hero: CGFloat = 96
    }
    
    // MARK: - Corner Radius
    struct CornerRadius {
        static let xs: CGFloat = 4
        static let sm: CGFloat = 8
        static let md: CGFloat = 12
        static let lg: CGFloat = 16
        static let xl: CGFloat = 24
        static let pill: CGFloat = 50
    }
    
    // MARK: - Shadows
    struct Shadow {
        static let card = Color.black.opacity(0.08)
        static let elevated = Color.black.opacity(0.12)
        static let modal = Color.black.opacity(0.16)
    }
    
    // MARK: - Animation
    struct Animation {
        static let quick = SwiftUI.Animation.easeOut(duration: 0.2)
        static let smooth = SwiftUI.Animation.easeInOut(duration: 0.3)
        static let gentle = SwiftUI.Animation.easeOut(duration: 0.4)
        static let hero = SwiftUI.Animation.easeInOut(duration: 0.6)
    }
}

// Note: Color hex extension already exists in ColorExtension.swift

// MARK: - View Extensions
extension View {
    func luxuryCard() -> some View {
        self
            .background(DesignSystem.Colors.primary)
            .cornerRadius(DesignSystem.CornerRadius.lg)
            .shadow(color: DesignSystem.Shadow.card, radius: 8, x: 0, y: 4)
    }
    
    func luxuryButton(style: LuxuryButtonStyle = .primary) -> some View {
        self
            .font(DesignSystem.Typography.bodyMedium)
            .foregroundColor(style.textColor)
            .padding(.horizontal, DesignSystem.Spacing.xl)
            .padding(.vertical, DesignSystem.Spacing.md)
            .background(style.backgroundColor)
            .cornerRadius(DesignSystem.CornerRadius.md)
            .overlay(
                RoundedRectangle(cornerRadius: DesignSystem.CornerRadius.md)
                    .stroke(style.borderColor, lineWidth: style.borderWidth)
            )
    }
    
    func heroText() -> some View {
        self
            .font(DesignSystem.Typography.heroTitle)
            .foregroundColor(DesignSystem.Colors.textPrimary)
            .kerning(DesignSystem.Typography.heroLetterSpacing)
            .lineSpacing(8)
    }
    
    func titleText() -> some View {
        self
            .font(DesignSystem.Typography.title1)
            .foregroundColor(DesignSystem.Colors.textPrimary)
            .kerning(DesignSystem.Typography.titleLetterSpacing)
    }
    
    func bodyText() -> some View {
        self
            .font(DesignSystem.Typography.body)
            .foregroundColor(DesignSystem.Colors.textPrimary)
            .lineSpacing(4)
    }
    
    func captionText() -> some View {
        self
            .font(DesignSystem.Typography.caption)
            .foregroundColor(DesignSystem.Colors.textSecondary)
    }
}

// MARK: - Button Styles
enum LuxuryButtonStyle {
    case primary
    case secondary
    case accent
    case ghost
    
    var textColor: Color {
        switch self {
        case .primary:
            return DesignSystem.Colors.primary
        case .secondary:
            return DesignSystem.Colors.textPrimary
        case .accent:
            return DesignSystem.Colors.secondary
        case .ghost:
            return DesignSystem.Colors.textPrimary
        }
    }
    
    var backgroundColor: Color {
        switch self {
        case .primary:
            return DesignSystem.Colors.secondary
        case .secondary:
            return DesignSystem.Colors.supporting
        case .accent:
            return DesignSystem.Colors.accent
        case .ghost:
            return Color.clear
        }
    }
    
    var borderColor: Color {
        switch self {
        case .primary, .secondary, .accent:
            return Color.clear
        case .ghost:
            return DesignSystem.Colors.textTertiary
        }
    }
    
    var borderWidth: CGFloat {
        switch self {
        case .primary, .secondary, .accent:
            return 0
        case .ghost:
            return 1
        }
    }
} 