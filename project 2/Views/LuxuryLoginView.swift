import SwiftUI

struct LuxuryLoginView: View {
    @EnvironmentObject private var authManager: AuthManager
    @State private var email = ""
    @State private var password = ""
    @State private var isSignUp = false
    @State private var fullName = ""
    @State private var confirmPassword = ""
    @State private var showPassword = false
    @State private var isLoading = false
    
    var body: some View {
        GeometryReader { geometry in
            ZStack {
                // Background
                backgroundGradient
                
                ScrollView {
                    VStack(spacing: DesignSystem.Spacing.xxl) {
                        Spacer(minLength: geometry.size.height * 0.1)
                        
                        // Logo/Brand section
                        brandSection
                        
                        // Form section
                        formSection
                        
                        // Switch between login/signup
                        switchSection
                        
                        Spacer(minLength: DesignSystem.Spacing.xxl)
                    }
                    .padding(.horizontal, DesignSystem.Spacing.xl)
                }
            }
        }
        .ignoresSafeArea()
    }
    
    // MARK: - Background Gradient
    private var backgroundGradient: some View {
        LinearGradient(
            colors: [
                DesignSystem.Colors.background,
                DesignSystem.Colors.natural.opacity(0.05),
                DesignSystem.Colors.background
            ],
            startPoint: .topLeading,
            endPoint: .bottomTrailing
        )
    }
    
    // MARK: - Brand Section
    private var brandSection: some View {
        VStack(spacing: DesignSystem.Spacing.lg) {
            // Logo placeholder
            Circle()
                .fill(
                    LinearGradient(
                        colors: [
                            DesignSystem.Colors.accent,
                            DesignSystem.Colors.natural
                        ],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
                .frame(width: 80, height: 80)
                .overlay(
                    Image(systemName: "crown")
                        .font(.system(size: 32))
                        .foregroundColor(DesignSystem.Colors.primary)
                )
            
            VStack(spacing: DesignSystem.Spacing.sm) {
                Text("Welcome")
                    .font(DesignSystem.Typography.title1)
                    .foregroundColor(DesignSystem.Colors.textPrimary)
                    .kerning(1.0)
                
                Text("Experience luxury shopping\nat your fingertips")
                    .font(DesignSystem.Typography.body)
                    .foregroundColor(DesignSystem.Colors.textSecondary)
                    .multilineTextAlignment(.center)
                    .lineSpacing(4)
            }
        }
    }
    
    // MARK: - Form Section
    private var formSection: some View {
        VStack(spacing: DesignSystem.Spacing.lg) {
            // Form container
            VStack(spacing: DesignSystem.Spacing.md) {
                if isSignUp {
                    signUpForm
                } else {
                    loginForm
                }
            }
            .padding(DesignSystem.Spacing.xl)
            .luxuryCard()
            .animation(DesignSystem.Animation.gentle, value: isSignUp)
            
            // Action button
            actionButton
        }
    }
    
    // MARK: - Login Form
    private var loginForm: some View {
        VStack(spacing: DesignSystem.Spacing.md) {
            Text("Sign In")
                .font(DesignSystem.Typography.title2)
                .foregroundColor(DesignSystem.Colors.textPrimary)
                .frame(maxWidth: .infinity, alignment: .leading)
            
            VStack(spacing: DesignSystem.Spacing.md) {
                LuxuryTextField(
                    placeholder: "Email",
                    text: $email,
                    keyboardType: .emailAddress,
                    icon: "envelope"
                )
                
                LuxurySecureField(
                    placeholder: "Password",
                    text: $password,
                    showPassword: $showPassword,
                    icon: "lock"
                )
            }
            
            // Forgot password
            HStack {
                Spacer()
                Button("Forgot Password?") {
                    // Handle forgot password
                }
                .font(DesignSystem.Typography.caption)
                .foregroundColor(DesignSystem.Colors.accent)
            }
        }
    }
    
    // MARK: - Sign Up Form
    private var signUpForm: some View {
        VStack(spacing: DesignSystem.Spacing.md) {
            Text("Create Account")
                .font(DesignSystem.Typography.title2)
                .foregroundColor(DesignSystem.Colors.textPrimary)
                .frame(maxWidth: .infinity, alignment: .leading)
            
            VStack(spacing: DesignSystem.Spacing.md) {
                LuxuryTextField(
                    placeholder: "Full Name",
                    text: $fullName,
                    icon: "person"
                )
                
                LuxuryTextField(
                    placeholder: "Email",
                    text: $email,
                    keyboardType: .emailAddress,
                    icon: "envelope"
                )
                
                LuxurySecureField(
                    placeholder: "Password",
                    text: $password,
                    showPassword: $showPassword,
                    icon: "lock"
                )
                
                LuxurySecureField(
                    placeholder: "Confirm Password",
                    text: $confirmPassword,
                    showPassword: $showPassword,
                    icon: "lock.fill"
                )
            }
        }
    }
    
    // MARK: - Action Button
    private var actionButton: some View {
        LuxuryButton(
            isSignUp ? "Create Account" : "Sign In",
            style: .accent,
            isLoading: isLoading
        ) {
            handleAuthAction()
        }
        .disabled(!isFormValid)
    }
    
    // MARK: - Switch Section
    private var switchSection: some View {
        HStack {
            Text(isSignUp ? "Already have an account?" : "Don't have an account?")
                .font(DesignSystem.Typography.body)
                .foregroundColor(DesignSystem.Colors.textSecondary)
            
            Button(isSignUp ? "Sign In" : "Sign Up") {
                withAnimation(DesignSystem.Animation.smooth) {
                    isSignUp.toggle()
                    clearForm()
                }
            }
            .font(DesignSystem.Typography.bodyMedium)
            .foregroundColor(DesignSystem.Colors.accent)
        }
    }
    
    // MARK: - Helper Properties
    private var isFormValid: Bool {
        if isSignUp {
            return !fullName.isEmpty &&
                   !email.isEmpty &&
                   !password.isEmpty &&
                   !confirmPassword.isEmpty &&
                   password == confirmPassword &&
                   password.count >= 6
        } else {
            return !email.isEmpty && !password.isEmpty
        }
    }
    
    // MARK: - Helper Methods
    private func handleAuthAction() {
        isLoading = true
        
        Task {
            do {
                if isSignUp {
                    try await authManager.signup(
                        username: fullName,
                        email: email,
                        password: password
                    )
                } else {
                    try await authManager.login(
                        username: email,
                        password: password
                    )
                }
            } catch {
                // Handle error
                print("Auth error: \(error)")
            }
            
            await MainActor.run {
                isLoading = false
            }
        }
    }
    
    private func clearForm() {
        fullName = ""
        email = ""
        password = ""
        confirmPassword = ""
        showPassword = false
    }
}

// MARK: - Luxury Text Field with Icon
struct LuxuryTextField: View {
    let placeholder: String
    @Binding var text: String
    let keyboardType: UIKeyboardType
    let icon: String
    
    init(
        placeholder: String,
        text: Binding<String>,
        keyboardType: UIKeyboardType = .default,
        icon: String
    ) {
        self.placeholder = placeholder
        self._text = text
        self.keyboardType = keyboardType
        self.icon = icon
    }
    
    var body: some View {
        HStack(spacing: DesignSystem.Spacing.md) {
            Image(systemName: icon)
                .foregroundColor(DesignSystem.Colors.textTertiary)
                .frame(width: 20)
            
            TextField(placeholder, text: $text)
                .font(DesignSystem.Typography.body)
                .foregroundColor(DesignSystem.Colors.textPrimary)
                .keyboardType(keyboardType)
        }
        .padding(DesignSystem.Spacing.md)
        .background(DesignSystem.Colors.supporting)
        .cornerRadius(DesignSystem.CornerRadius.md)
        .overlay(
            RoundedRectangle(cornerRadius: DesignSystem.CornerRadius.md)
                .stroke(DesignSystem.Colors.textTertiary, lineWidth: 1)
        )
    }
}

// MARK: - Luxury Secure Field
struct LuxurySecureField: View {
    let placeholder: String
    @Binding var text: String
    @Binding var showPassword: Bool
    let icon: String
    
    var body: some View {
        HStack(spacing: DesignSystem.Spacing.md) {
            Image(systemName: icon)
                .foregroundColor(DesignSystem.Colors.textTertiary)
                .frame(width: 20)
            
            Group {
                if showPassword {
                    TextField(placeholder, text: $text)
                } else {
                    SecureField(placeholder, text: $text)
                }
            }
            .font(DesignSystem.Typography.body)
            .foregroundColor(DesignSystem.Colors.textPrimary)
            
            Button {
                showPassword.toggle()
            } label: {
                Image(systemName: showPassword ? "eye.slash" : "eye")
                    .foregroundColor(DesignSystem.Colors.textTertiary)
            }
        }
        .padding(DesignSystem.Spacing.md)
        .background(DesignSystem.Colors.supporting)
        .cornerRadius(DesignSystem.CornerRadius.md)
        .overlay(
            RoundedRectangle(cornerRadius: DesignSystem.CornerRadius.md)
                .stroke(DesignSystem.Colors.textTertiary, lineWidth: 1)
        )
    }
}

// MARK: - Preview
struct LuxuryLoginView_Previews: PreviewProvider {
    static var previews: some View {
        LuxuryLoginView()
            .environmentObject(AuthManager.shared)
    }
} 