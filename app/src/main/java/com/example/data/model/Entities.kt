package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val fullName: String,
    val phone: String,
    val email: String,
    val role: String, // Buyer, Gardener, Vendor, Admin
    val address: String,
    val profilePhoto: String,
    val dateCreated: String
) : Serializable

@Entity(tableName = "sellers")
data class SellerEntity(
    @PrimaryKey val sellerId: String,
    val businessName: String,
    val sellerType: String, // Gardener/Farmer, Vendor
    val description: String,
    val phone: String,
    val location: String,
    val logo: String,
    val verificationStatus: String // Pending, Verified
) : Serializable

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val productId: String,
    val productName: String,
    val category: String, // Vegetables, Fruits, Fish, Spices, Rice & Grains, Bread, Drinks, Garden Products
    val description: String,
    val price: Double,
    val unit: String, // kg, basket, bundle, bag, loaf, etc.
    val quantityAvailable: Int,
    val productImage: String,
    val sellerId: String,
    val sellerName: String,
    val location: String,
    val featured: Boolean,
    val dateAdded: String
) : Serializable

@Entity(tableName = "cart")
data class CartEntity(
    @PrimaryKey val cartId: String, // Simple combination: "userId_productId"
    val userId: String,
    val productId: String,
    val quantity: Int,
    val dateAdded: String
) : Serializable

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val buyerId: String,
    val buyerName: String,
    val totalAmount: Double,
    val paymentMethod: String, // Cash on Delivery, Mobile Money
    val deliveryAddress: String,
    val orderStatus: String, // Pending, Preparing, Out for Delivery, Delivered
    val deliveryStatus: String, // Assigned, In Transit, Completed
    val sellerId: String, // Primary seller or multiple (summarized)
    val orderDate: String
) : Serializable

@Entity(tableName = "order_items")
data class OrderItemEntity(
    @PrimaryKey val orderItemId: String,
    val orderId: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val price: Double,
    val subtotal: Double
) : Serializable

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val categoryId: String,
    val categoryName: String,
    val categoryIcon: String // Name of graphic resource or icon identifier
) : Serializable

@Entity(tableName = "bundles")
data class BundleEntity(
    @PrimaryKey val bundleId: String,
    val bundleName: String,
    val bundleImage: String,
    val bundlePrice: Double,
    val description: String
) : Serializable

@Entity(tableName = "bundle_items")
data class BundleItemEntity(
    @PrimaryKey val bundleItemId: String,
    val bundleId: String,
    val productId: String,
    val productName: String,
    val quantity: Int
) : Serializable

@Entity(tableName = "delivery_requests")
data class DeliveryRequestEntity(
    @PrimaryKey val deliveryId: String,
    val orderId: String,
    val driverName: String,
    val deliveryStatus: String, // Assigned, Out for delivery, Completed
    val deliveryFee: Double,
    val deliveryTime: String
) : Serializable
