import SwiftUI

struct ProfileView: View {
    @EnvironmentObject var authManager: AuthManager
    @StateObject private var viewModel = ProfileViewModel()
    @State private var isEditing = false
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: DesignSystem.Spacing.xxl) {
                    // Header Section
                    headerSection
                    
                    // Profile Information
                    if let profile = viewModel.customerProfile {
                        profileSection(profile)
                    } else {
                        emptyProfileSection
                    }
                    
                    // Account Actions
                    actionsSection
                }
                .padding(.horizontal, DesignSystem.Spacing.lg)
                .padding(.vertical, DesignSystem.Spacing.xl)
            }
            .background(DesignSystem.Colors.background)
            .navigationBarHidden(true)
            .refreshable {
                await viewModel.loadProfile()
            }
        }
        .onAppear {
            Task {
                await viewModel.loadProfile()
            }
        }
        .sheet(isPresented: $isEditing) {
            EditProfileView(profile: viewModel.customerProfile) { updatedProfile in
                await viewModel.updateProfile(updatedProfile)
                isEditing = false
            }
        }
    }
    
    // MARK: - Header Section
    private var headerSection: some View {
        VStack(spacing: DesignSystem.Spacing.md) {
            // Profile Avatar
            Circle()
                .fill(
                    LinearGradient(
                        colors: [DesignSystem.Colors.accent, DesignSystem.Colors.primary],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
                .frame(width: 100, height: 100)
                .overlay(
                    Text(viewModel.initials)
                        .font(.system(size: 36, weight: .light, design: .default))
                        .foregroundColor(.white)
                )
            
            VStack(spacing: DesignSystem.Spacing.xs) {
                Text(viewModel.displayName)
                    .titleText()
                
                if let email = authManager.jwtResponse?.email {
                    Text(email)
                        .font(DesignSystem.Typography.caption)
                        .foregroundColor(DesignSystem.Colors.textSecondary)
                }
            }
        }
    }
    
    // MARK: - Profile Section
    private func profileSection(_ profile: CustomerProfile) -> some View {
        VStack(spacing: DesignSystem.Spacing.lg) {
            // Section Header
            HStack {
                Text("Personal Information")
                    .font(DesignSystem.Typography.headline)
                    .foregroundColor(DesignSystem.Colors.textPrimary)
                
                Spacer()
                
                Button("Edit") {
                    isEditing = true
                }
                .font(DesignSystem.Typography.caption)
                .foregroundColor(DesignSystem.Colors.accent)
            }
            
            // Profile Details
            VStack(spacing: DesignSystem.Spacing.sm) {
                if let firstName = profile.firstName, !firstName.isEmpty {
                    ProfileDetailRow(label: "First Name", value: firstName)
                }
                
                if let lastName = profile.lastName, !lastName.isEmpty {
                    ProfileDetailRow(label: "Last Name", value: lastName)
                }
                
                if let phone = profile.phoneNumber, !phone.isEmpty {
                    ProfileDetailRow(label: "Phone", value: phone)
                }
                
                if let address = profile.address, !address.isEmpty {
                    ProfileDetailRow(label: "Address", value: address)
                }
                
                if let dob = profile.dateOfBirth, !dob.isEmpty {
                    ProfileDetailRow(label: "Date of Birth", value: dob)
                }
            }
        }
        .padding(DesignSystem.Spacing.lg)
        .background(DesignSystem.Colors.surface)
        .cornerRadius(DesignSystem.CornerRadius.lg)
    }
    
    // MARK: - Empty Profile Section
    private var emptyProfileSection: some View {
        VStack(spacing: DesignSystem.Spacing.lg) {
            Image(systemName: "person.badge.plus")
                .font(.system(size: 48))
                .foregroundColor(DesignSystem.Colors.textTertiary)
            
            Text("Complete Your Profile")
                .font(DesignSystem.Typography.headline)
                .foregroundColor(DesignSystem.Colors.textPrimary)
            
            Text("Add your personal information to get a better shopping experience")
                .font(DesignSystem.Typography.body)
                .foregroundColor(DesignSystem.Colors.textSecondary)
                .multilineTextAlignment(.center)
            
            LuxuryButton("Add Information", style: .primary) {
                isEditing = true
            }
        }
        .padding(DesignSystem.Spacing.xxl)
        .background(DesignSystem.Colors.surface)
        .cornerRadius(DesignSystem.CornerRadius.lg)
    }
    
    // MARK: - Actions Section
    private var actionsSection: some View {
        VStack(spacing: DesignSystem.Spacing.md) {
            LuxuryButton("Order History", style: .secondary) {
                // Navigate to order history
            }
            
            LuxuryButton("Settings", style: .secondary) {
                // Navigate to settings
            }
            
            LuxuryButton("Logout", style: .ghost) {
                authManager.logout()
            }
        }
    }
}

// MARK: - Profile Detail Row
struct ProfileDetailRow: View {
    let label: String
    let value: String
    
    var body: some View {
        HStack {
            Text(label)
                .font(DesignSystem.Typography.caption)
                .foregroundColor(DesignSystem.Colors.textSecondary)
                .frame(width: 100, alignment: .leading)
            
            Text(value)
                .font(DesignSystem.Typography.body)
                .foregroundColor(DesignSystem.Colors.textPrimary)
            
            Spacer()
        }
    }
}

// MARK: - View Model
@MainActor
class ProfileViewModel: ObservableObject {
    @Published var customerProfile: CustomerProfile?
    @Published var isLoading = false
    @Published var errorMessage: String?
    
    private let networkManager = NetworkManager.shared
    
    var displayName: String {
        if let profile = customerProfile {
            let firstName = profile.firstName ?? ""
            let lastName = profile.lastName ?? ""
            if !firstName.isEmpty || !lastName.isEmpty {
                return "\(firstName) \(lastName)".trimmingCharacters(in: .whitespaces)
            }
        }
        return AuthManager.shared.jwtResponse?.username ?? "User"
    }
    
    var initials: String {
        if let profile = customerProfile {
            let firstName = profile.firstName ?? ""
            let lastName = profile.lastName ?? ""
            let firstInitial = firstName.first?.uppercased() ?? ""
            let lastInitial = lastName.first?.uppercased() ?? ""
            if !firstInitial.isEmpty || !lastInitial.isEmpty {
                return "\(firstInitial)\(lastInitial)"
            }
        }
        
        if let username = AuthManager.shared.jwtResponse?.username {
            return String(username.prefix(2)).uppercased()
        }
        
        return "U"
    }
    
    func loadProfile() async {
        isLoading = true
        defer { isLoading = false }
        
        do {
            customerProfile = try await networkManager.getProfile()
        } catch {
            errorMessage = "Failed to load profile: \(error.localizedDescription)"
            print("Failed to load profile: \(error)")
        }
    }
    
    func updateProfile(_ profile: CustomerProfile) async {
        isLoading = true
        defer { isLoading = false }
        
        do {
            customerProfile = try await networkManager.updateProfile(profile)
        } catch {
            errorMessage = "Failed to update profile: \(error.localizedDescription)"
            print("Failed to update profile: \(error)")
        }
    }
}

// MARK: - Edit Profile View
struct EditProfileView: View {
    let profile: CustomerProfile?
    let onSave: (CustomerProfile) async -> Void
    
    @Environment(\.dismiss) private var dismiss
    @State private var firstName = ""
    @State private var lastName = ""
    @State private var phoneNumber = ""
    @State private var address = ""
    @State private var dateOfBirth = ""
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: DesignSystem.Spacing.lg) {
                    ModernTextField(placeholder: "First Name", text: $firstName, icon: "person")
                    ModernTextField(placeholder: "Last Name", text: $lastName, icon: "person")
                    ModernTextField(placeholder: "Phone Number", text: $phoneNumber, icon: "phone")
                    ModernTextField(placeholder: "Address", text: $address, icon: "house")
                    ModernTextField(placeholder: "Date of Birth (YYYY-MM-DD)", text: $dateOfBirth, icon: "calendar")
                }
                .padding(DesignSystem.Spacing.lg)
            }
            .background(DesignSystem.Colors.background)
            .navigationTitle("Edit Profile")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        dismiss()
                    }
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Save") {
                        Task {
                            let updatedProfile = CustomerProfile(
                                id: profile?.id,
                                firstName: firstName.isEmpty ? nil : firstName,
                                lastName: lastName.isEmpty ? nil : lastName,
                                email: profile?.email,
                                phoneNumber: phoneNumber.isEmpty ? nil : phoneNumber,
                                address: address.isEmpty ? nil : address,
                                dateOfBirth: dateOfBirth.isEmpty ? nil : dateOfBirth,
                                preferences: profile?.preferences
                            )
                            await onSave(updatedProfile)
                            await MainActor.run {
                                dismiss()
                            }
                        }
                    }
                    .fontWeight(.medium)
                }
            }
        }
        .onAppear {
            loadExistingData()
        }
    }
    
    private func loadExistingData() {
        firstName = profile?.firstName ?? ""
        lastName = profile?.lastName ?? ""
        phoneNumber = profile?.phoneNumber ?? ""
        address = profile?.address ?? ""
        dateOfBirth = profile?.dateOfBirth ?? ""
    }
}

// MARK: - Preview
struct ProfileView_Previews: PreviewProvider {
    static var previews: some View {
        let authManager = AuthManager.shared
        authManager.isAuthenticated = true
        authManager.jwtResponse = JwtResponse(
            token: "fake",
            type: "Bearer",
            id: 1,
            username: "testuser",
            email: "test@example.com",
            fullName: "Test User",
            emailVerified: true,
            roles: ["ROLE_CUSTOMER"]
        )
        
        return ProfileView()
            .environmentObject(authManager)
    }
} 