//
//  project_2App.swift
//  project 2
//
//  Created by ionut popescu on 20/05/2024.
//

import SwiftUI

@main
struct project_2App: App {
    @StateObject private var authManager = AuthManager.shared
    @StateObject private var cartManager = CartManager.shared

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(authManager)
                .environmentObject(cartManager)
        }
    }
} 
