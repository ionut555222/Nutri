import SwiftUI

struct ChatbotView: View {
    @StateObject private var viewModel = ChatViewModel()
    @State private var messageText: String = ""
    @EnvironmentObject var authManager: AuthManager
    
    var body: some View {
        VStack {
            ScrollViewReader { scrollViewProxy in
                ScrollView {
                    VStack(alignment: .leading, spacing: 10) {
                        ForEach(viewModel.messages) { message in
                            HStack {
                                if message.sender == "user" {
                                    Spacer()
                                    Text(message.content)
                                        .padding()
                                        .background(Color.blue)
                                        .foregroundColor(.white)
                                        .cornerRadius(10)
                                } else {
                                    Text(message.content)
                                        .padding()
                                        .background(Color.gray.opacity(0.2))
                                        .cornerRadius(10)
                                    Spacer()
                                }
                            }
                            .id(message.id)
                        }
                        
                        if viewModel.isTyping {
                            HStack {
                                Text("AI Assistant is typing...")
                                    .padding()
                                    .background(Color.gray.opacity(0.2))
                                    .cornerRadius(10)
                                Spacer()
                            }
                        }
                    }
                    .padding()
                }
                .onChange(of: viewModel.messages.count) {
                    if let lastMessage = viewModel.messages.last {
                        scrollViewProxy.scrollTo(lastMessage.id, anchor: .bottom)
                    }
                }
            }
            
            if let errorMessage = viewModel.errorMessage {
                Text(errorMessage)
                    .foregroundColor(.red)
                    .padding()
            }
            
            HStack {
                TextField("Enter your message", text: $messageText)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                    .disabled(viewModel.isTyping)
                
                Button(action: {
                    Task { await sendMessage() }
                }) {
                    Image(systemName: "paperplane.fill")
                        .font(.title)
                }
                .disabled(messageText.isEmpty || viewModel.isTyping)
            }
            .padding()
        }
        .navigationTitle("AI Assistant")
        .task {
            await viewModel.fetchHistory(authManager: authManager)
        }
    }

    func sendMessage() async {
        guard !messageText.isEmpty else { return }
        let currentMessage = messageText
        messageText = ""
        await viewModel.sendMessage(currentMessage, authManager: authManager)
    }
}

struct ChatbotView_Previews: PreviewProvider {
    static var previews: some View {
        ChatbotView()
            .environmentObject(AuthManager.shared)
    }
} 