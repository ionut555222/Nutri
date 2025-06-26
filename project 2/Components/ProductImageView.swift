import SwiftUI

struct ProductImageView: View {
    let imageUrl: String?
    let fallbackImageName: String
    let size: CGSize
    
    init(imageUrl: String?, fallbackImageName: String = "photo", size: CGSize = CGSize(width: 60, height: 60)) {
        self.imageUrl = imageUrl
        self.fallbackImageName = fallbackImageName
        self.size = size
    }
    
    var body: some View {
        Group {
            if let imageUrl = imageUrl, !imageUrl.isEmpty {
                AsyncImage(url: URL(string: "http://localhost:8080\(imageUrl)")) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                } placeholder: {
                    RoundedRectangle(cornerRadius: 8)
                        .fill(DesignSystem.Colors.surface)
                        .overlay(
                            ProgressView()
                                .scaleEffect(0.8)
                                .foregroundColor(DesignSystem.Colors.textSecondary)
                        )
                }
                .frame(width: size.width, height: size.height)
                .clipped()
                .cornerRadius(8)
            } else {
                // Fallback image
                RoundedRectangle(cornerRadius: 8)
                    .fill(
                        LinearGradient(
                            colors: [
                                DesignSystem.Colors.surface,
                                DesignSystem.Colors.natural.opacity(0.1)
                            ],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .frame(width: size.width, height: size.height)
                    .overlay(
                        Image(systemName: fallbackImageName)
                            .font(.system(size: min(size.width, size.height) * 0.4))
                            .foregroundColor(DesignSystem.Colors.textTertiary)
                    )
            }
        }
    }
}

// MARK: - Different size variants
extension ProductImageView {
    static func small(imageUrl: String?, fallbackImageName: String = "photo") -> ProductImageView {
        ProductImageView(imageUrl: imageUrl, fallbackImageName: fallbackImageName, size: CGSize(width: 40, height: 40))
    }
    
    static func medium(imageUrl: String?, fallbackImageName: String = "photo") -> ProductImageView {
        ProductImageView(imageUrl: imageUrl, fallbackImageName: fallbackImageName, size: CGSize(width: 60, height: 60))
    }
    
    static func large(imageUrl: String?, fallbackImageName: String = "photo") -> ProductImageView {
        ProductImageView(imageUrl: imageUrl, fallbackImageName: fallbackImageName, size: CGSize(width: 120, height: 120))
    }
    
    static func hero(imageUrl: String?, fallbackImageName: String = "photo") -> ProductImageView {
        ProductImageView(imageUrl: imageUrl, fallbackImageName: fallbackImageName, size: CGSize(width: 200, height: 200))
    }
}

// MARK: - Preview
struct ProductImageView_Previews: PreviewProvider {
    static var previews: some View {
        VStack(spacing: 20) {
            ProductImageView.small(imageUrl: nil)
            ProductImageView.medium(imageUrl: nil)
            ProductImageView.large(imageUrl: nil)
            ProductImageView.hero(imageUrl: nil)
        }
        .padding()
        .background(DesignSystem.Colors.background)
    }
} 