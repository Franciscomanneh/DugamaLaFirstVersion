package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DugamaDao {

    // --- Users ---
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun getUser(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserSync(userId: String): UserEntity?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // --- Sellers ---
    @Query("SELECT * FROM sellers")
    fun getAllSellers(): Flow<List<SellerEntity>>

    @Query("SELECT * FROM sellers WHERE sellerId = :sellerId LIMIT 1")
    fun getSeller(sellerId: String): Flow<SellerEntity?>

    @Query("SELECT * FROM sellers WHERE sellerId = :sellerId LIMIT 1")
    suspend fun getSellerSync(sellerId: String): SellerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeller(seller: SellerEntity)

    @Update
    suspend fun updateSeller(seller: SellerEntity)

    // --- Products ---
    @Query("SELECT * FROM products ORDER BY dateAdded DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY dateAdded DESC")
    suspend fun getAllProductsSync(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE featured = 1")
    fun getFeaturedProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE category = :category")
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE sellerId = :sellerId")
    fun getProductsBySeller(sellerId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE productName LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE productId = :productId LIMIT 1")
    fun getProduct(productId: String): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE productId = :productId LIMIT 1")
    suspend fun getProductSync(productId: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Query("UPDATE products SET quantityAvailable = :qty WHERE productId = :productId")
    suspend fun updateProductQuantity(productId: String, qty: Int)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    // --- Cart ---
    @Query("SELECT * FROM cart WHERE userId = :userId")
    fun getUserCart(userId: String): Flow<List<CartEntity>>

    @Query("SELECT * FROM cart WHERE userId = :userId")
    suspend fun getUserCartSync(userId: String): List<CartEntity>

    @Query("SELECT * FROM cart WHERE cartId = :cartId LIMIT 1")
    suspend fun getCartItem(cartId: String): CartEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartEntity)

    @Query("UPDATE cart SET quantity = :qty WHERE cartId = :cartId")
    suspend fun updateCartQuantity(cartId: String, qty: Int)

    @Query("DELETE FROM cart WHERE cartId = :cartId")
    suspend fun deleteCartItem(cartId: String)

    @Query("DELETE FROM cart WHERE userId = :userId")
    suspend fun clearUserCart(userId: String)

    // --- Orders ---
    @Query("SELECT * FROM orders ORDER BY orderDate DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE buyerId = :buyerId ORDER BY orderDate DESC")
    fun getBuyerOrders(buyerId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE sellerId = :sellerId ORDER BY orderDate DESC")
    fun getSellerOrders(sellerId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE orderId = :orderId LIMIT 1")
    fun getOrder(orderId: String): Flow<OrderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Query("UPDATE orders SET orderStatus = :status, deliveryStatus = :delStatus WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String, delStatus: String)

    // --- Order Items ---
    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun getOrderItems(orderId: String): Flow<List<OrderItemEntity>>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getOrderItemsSync(orderId: String): List<OrderItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    // --- Categories ---
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    // --- Bundles ---
    @Query("SELECT * FROM bundles")
    fun getAllBundles(): Flow<List<BundleEntity>>

    @Query("SELECT * FROM bundles WHERE bundleId = :bundleId LIMIT 1")
    fun getBundle(bundleId: String): Flow<BundleEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBundles(bundles: List<BundleEntity>)

    // --- Bundle Items ---
    @Query("SELECT * FROM bundle_items WHERE bundleId = :bundleId")
    fun getBundleItems(bundleId: String): Flow<List<BundleItemEntity>>

    @Query("SELECT * FROM bundle_items WHERE bundleId = :bundleId")
    suspend fun getBundleItemsSync(bundleId: String): List<BundleItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBundleItems(items: List<BundleItemEntity>)

    // --- Delivery Requests ---
    @Query("SELECT * FROM delivery_requests")
    fun getAllDeliveries(): Flow<List<DeliveryRequestEntity>>

    @Query("SELECT * FROM delivery_requests WHERE orderId = :orderId LIMIT 1")
    fun getDeliveryRequest(orderId: String): Flow<DeliveryRequestEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeliveryRequest(req: DeliveryRequestEntity)

    @Query("UPDATE delivery_requests SET deliveryStatus = :status WHERE deliveryId = :id")
    suspend fun updateDeliveryRequestStatus(id: String, status: String)
}
