import Foundation

struct Category: Codable, Identifiable, Hashable {
    let id: Int
    let name: String
}

struct Product: Codable, Identifiable, Hashable {
    let id: Int
    let name: String
    let description: String?
    let price: Decimal
    let stock: Int?
    let unit: Unit
    let category: Category
    let imageFilename: String?
    let imageUrl: String?
    
    var stockCount: Int {
        stock ?? 0
    }

    enum CodingKeys: String, CodingKey {
        case id, name, description, price, stock, unit
        case categoryId
        case categoryName
        case imageFilename
        case imageUrl
    }
    
    // The robust, correct decoder
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        id = try container.decode(Int.self, forKey: .id)
        name = try container.decode(String.self, forKey: .name)
        description = try container.decodeIfPresent(String.self, forKey: .description)
        stock = try container.decodeIfPresent(Int.self, forKey: .stock)
        
        // Handle unit with fallback
        if let unitValue = try? container.decode(Unit.self, forKey: .unit) {
            unit = unitValue
        } else if let unitString = try? container.decode(String.self, forKey: .unit) {
            unit = Unit(rawValue: unitString.uppercased()) ?? Unit.piece
        } else {
            unit = Unit.piece // Default fallback
        }
        
        let categoryId = try container.decode(Int.self, forKey: .categoryId)
        let categoryName = try container.decode(String.self, forKey: .categoryName)
        category = Category(id: categoryId, name: categoryName)
        
        imageFilename = try container.decodeIfPresent(String.self, forKey: .imageFilename)
        imageUrl = try container.decodeIfPresent(String.self, forKey: .imageUrl)
        
        if let decimalPrice = try? container.decode(Decimal.self, forKey: .price) {
            price = decimalPrice
        } else if let doublePrice = try? container.decode(Double.self, forKey: .price) {
            price = Decimal(doublePrice)
        } else if let stringPrice = try? container.decode(String.self, forKey: .price) {
            if let decimalPrice = Decimal(string: stringPrice) {
                price = decimalPrice
            } else {
                let formatter = NumberFormatter()
                formatter.decimalSeparator = ","
                if let number = formatter.number(from: stringPrice) {
                    price = number.decimalValue
                } else {
                    throw DecodingError.dataCorruptedError(forKey: .price, in: container, debugDescription: "Price string '\(stringPrice)' is not a recognized number format.")
                }
            }
        } else {
            throw DecodingError.dataCorruptedError(forKey: .price, in: container, debugDescription: "Price is not in a recognized format (Decimal, Double, or String).")
        }
    }
    
    func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(id, forKey: .id)
        try container.encode(name, forKey: .name)
        try container.encodeIfPresent(description, forKey: .description)
        try container.encode(price, forKey: .price)
        try container.encodeIfPresent(stock, forKey: .stock)
        try container.encode(unit, forKey: .unit)
        try container.encode(category.id, forKey: .categoryId)
        try container.encode(category.name, forKey: .categoryName)
        try container.encodeIfPresent(imageFilename, forKey: .imageFilename)
        try container.encodeIfPresent(imageUrl, forKey: .imageUrl)
    }
    
    // Convenience initializer for previews and tests
    init(id: Int, name: String, description: String?, price: Decimal, stock: Int?, unit: Unit, category: Category, imageFilename: String? = nil, imageUrl: String? = nil) {
        self.id = id
        self.name = name
        self.description = description
        self.price = price
        self.stock = stock
        self.unit = unit
        self.category = category
        self.imageFilename = imageFilename
        self.imageUrl = imageUrl
    }

    // MARK: - Equatable & Hashable Conformance
    static func == (lhs: Product, rhs: Product) -> Bool {
        lhs.id == rhs.id
    }

    func hash(into hasher: inout Hasher) {
        hasher.combine(id)
    }
}

struct Order: Codable, Identifiable {
    let id: Int?
    var customerName: String?
    var shippingAddress: String?
    let orderDate: String?
    let totalAmount: Decimal
    var items: [OrderItem]?
    let userId: Int?
    let username: String?
}

struct OrderItem: Codable, Identifiable, Hashable {
    let id: Int
    let quantity: Int
    let price: Decimal
    let productId: Int
    let productName: String
    
    // Legacy support - keep these for backward compatibility with existing data
    var fruitId: Int { productId }
    var fruitName: String { productName }
    
    enum CodingKeys: String, CodingKey {
        case id, quantity, price
        case productId, productName
        case fruitId, fruitName // Legacy keys
    }
    
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        id = try container.decode(Int.self, forKey: .id)
        quantity = try container.decode(Int.self, forKey: .quantity)
        price = try container.decode(Decimal.self, forKey: .price)
        
        // Try new keys first, fall back to legacy keys
        if let pid = try? container.decode(Int.self, forKey: .productId) {
            productId = pid
        } else {
            productId = try container.decode(Int.self, forKey: .fruitId)
        }
        
        if let pname = try? container.decode(String.self, forKey: .productName) {
            productName = pname
        } else {
            productName = try container.decode(String.self, forKey: .fruitName)
        }
    }
    
    func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(id, forKey: .id)
        try container.encode(quantity, forKey: .quantity)
        try container.encode(price, forKey: .price)
        try container.encode(productId, forKey: .productId)
        try container.encode(productName, forKey: .productName)
        // Also encode legacy keys for backward compatibility
        try container.encode(productId, forKey: .fruitId)
        try container.encode(productName, forKey: .fruitName)
    }
}

// MARK: - DTOs for checkout
struct CartItemDTO: Codable {
    let productId: Int
    let quantity: Int
    
    // Legacy support
    var fruitId: Int { productId }
    
    enum CodingKeys: String, CodingKey {
        case productId, quantity
        case fruitId // Legacy key
    }
    
    init(productId: Int, quantity: Int) {
        self.productId = productId
        self.quantity = quantity
    }
    
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        quantity = try container.decode(Int.self, forKey: .quantity)
        
        // Try new key first, fall back to legacy key
        if let pid = try? container.decode(Int.self, forKey: .productId) {
            productId = pid
        } else {
            productId = try container.decode(Int.self, forKey: .fruitId)
        }
    }
    
    func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(quantity, forKey: .quantity)
        try container.encode(productId, forKey: .productId)
        // Also encode legacy key for backward compatibility
        try container.encode(productId, forKey: .fruitId)
    }
}

struct CheckoutRequest: Codable {
    let cartItems: [CartItemDTO]
}

// MARK: - Enums
enum OrderStatus: String, Codable, CaseIterable {
    case placed = "Placed"
    case processing = "Processing"
    case shipped = "Shipped"
    case delivered = "Delivered"
    case cancelled = "Cancelled"
}

enum Unit: String, Codable, CaseIterable {
    case kg = "KG"
    case piece = "PIECE"
    case pack = "PACK"
    case dozen = "DOZEN"
    case liter = "LITER"
    case gram = "GRAM"
    case pound = "POUND"
    
    var displayName: String {
        switch self {
        case .kg: return "kg"
        case .piece: return "piece"
        case .pack: return "pack"
        case .dozen: return "dozen"
        case .liter: return "liter"
        case .gram: return "gram"
        case .pound: return "pound"
        }
    }
} 