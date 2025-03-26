//
//  Item.swift
//  project 2
//
//  Created by Ionut Popescu on 26.03.2025.
//

import Foundation
import SwiftData

@Model
final class Item {
    var timestamp: Date
    
    init(timestamp: Date) {
        self.timestamp = timestamp
    }
}
