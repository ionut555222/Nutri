import SwiftUI

struct Theme {
    static let background = Color.black
    static let primary = Color.blue
    static let surface = Color.gray.opacity(0.15)
    static let accent = Color.red
    static let gradientStart = Color.blue
    static let gradientEnd = Color.purple
}

struct ModernTextField: View {
    let placeholder: String
    @Binding var text: String
    let icon: String
    
    var body: some View {
        HStack {
            Image(systemName: icon)
                .foregroundColor(Theme.primary)
                .frame(width: 24)
            
            TextField(placeholder, text: $text)
                .foregroundColor(.white)
                .tint(Theme.primary)
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(Color.black.opacity(0.3))
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(Theme.primary.opacity(0.4), lineWidth: 1)
                )
        )
    }
} 