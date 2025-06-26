import SwiftUI

struct ContentView: View {
    @EnvironmentObject private var authManager: AuthManager
    @StateObject private var cartManager = CartManager.shared
    @State private var isInitialized = false
    
    var body: some View {
        if !isInitialized {
            // Show loading screen while checking authentication
            VStack {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: DesignSystem.Colors.accent))
                    .scaleEffect(1.5)
                Text("Loading...")
                    .foregroundColor(DesignSystem.Colors.textPrimary)
                    .padding(.top)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(DesignSystem.Colors.background)
            .onAppear {
                // Initialize authentication state
                Task {
                    await authManager.checkAuthenticationStatus()
                    isInitialized = true
                }
            }
        } else if authManager.isAuthenticated {
            TabView {
                LuxuryHomeView()
                    .environmentObject(cartManager)
                    .tabItem {
                        Label("Home", systemImage: "house")
                    }
                
                LuxuryProductsView()
                    .environmentObject(cartManager)
                    .tabItem {
                        Label("Products", systemImage: "grid.circle")
                    }
                
                LuxuryCartView()
                    .environmentObject(cartManager)
                    .tabItem {
                        Label("Cart", systemImage: "bag")
                    }

                ChatbotView()
                    .tabItem {
                        Label("Chat", systemImage: "message.circle")
                    }

                ProfileView()
                    .tabItem {
                        Label("Profile", systemImage: "person.circle")
                    }
            }
            .accentColor(DesignSystem.Colors.accent)
            .background(DesignSystem.Colors.background)
            .onAppear {
                // Load cart data when app appears
                Task {
                    await cartManager.loadCart()
                }
            }
        } else {
            LuxuryLoginView()
                .environmentObject(authManager)
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
            .environmentObject(AuthManager.shared)
            .environmentObject(CartManager.shared)
    }
} 