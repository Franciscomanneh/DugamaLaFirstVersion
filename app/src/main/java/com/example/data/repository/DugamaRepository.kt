package com.example.data.repository

import com.example.data.dao.DugamaDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class DugamaRepository(private val dao: DugamaDao) {

    // --- Users ---
    fun getUser(userId: String): Flow<UserEntity?> = dao.getUser(userId)

    suspend fun getUserSync(userId: String): UserEntity? = dao.getUserSync(userId)

    fun getAllUsers(): Flow<List<UserEntity>> = dao.getAllUsers()

    suspend fun saveUser(user: UserEntity) = dao.insertUser(user)

    // --- Sellers ---
    val allSellers: Flow<List<SellerEntity>> = dao.getAllSellers()

    fun getSeller(sellerId: String): Flow<SellerEntity?> = dao.getSeller(sellerId)

    suspend fun saveSeller(seller: SellerEntity) = dao.insertSeller(seller)

    suspend fun updateSeller(seller: SellerEntity) = dao.updateSeller(seller)

    // --- Products ---
    val allProducts: Flow<List<ProductEntity>> = dao.getAllProducts()

    val featuredProducts: Flow<List<ProductEntity>> = dao.getFeaturedProducts()

    fun getProductsByCategory(category: String): Flow<List<ProductEntity>> =
        dao.getProductsByCategory(category)

    fun getProductsBySeller(sellerId: String): Flow<List<ProductEntity>> =
        dao.getProductsBySeller(sellerId)

    fun searchProducts(query: String): Flow<List<ProductEntity>> =
        dao.searchProducts(query)

    fun getProduct(productId: String): Flow<ProductEntity?> = dao.getProduct(productId)

    suspend fun getProductSync(productId: String): ProductEntity? = dao.getProductSync(productId)

    suspend fun saveProduct(product: ProductEntity) = dao.insertProduct(product)

    suspend fun deleteProduct(product: ProductEntity) = dao.deleteProduct(product)

    // --- Cart operations ---
    fun getUserCart(userId: String): Flow<List<CartEntity>> = dao.getUserCart(userId)

    suspend fun addToCart(userId: String, productId: String, quantityAdded: Int) {
        val cartId = "${userId}_${productId}"
        val existingItem = dao.getCartItem(cartId)
        if (existingItem != null) {
            val newQty = existingItem.quantity + quantityAdded
            if (newQty > 0) {
                dao.updateCartQuantity(cartId, newQty)
            } else {
                dao.deleteCartItem(cartId)
            }
        } else {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dateStr = dateFormat.format(Date())
            dao.insertCartItem(
                CartEntity(
                    cartId = cartId,
                    userId = userId,
                    productId = productId,
                    quantity = quantityAdded,
                    dateAdded = dateStr
                )
            )
        }
    }

    suspend fun updateCartQty(cartId: String, newQty: Int) {
        if (newQty > 0) {
            dao.updateCartQuantity(cartId, newQty)
        } else {
            dao.deleteCartItem(cartId)
        }
    }

    suspend fun deleteFromCart(cartId: String) = dao.deleteCartItem(cartId)

    suspend fun clearCart(userId: String) = dao.clearUserCart(userId)

    // --- Orders Checkout Flow (Highly Functional & Real) ---
    val allOrders: Flow<List<OrderEntity>> = dao.getAllOrders()

    fun getBuyerOrders(buyerId: String): Flow<List<OrderEntity>> =
        dao.getBuyerOrders(buyerId)

    fun getSellerOrders(sellerId: String): Flow<List<OrderEntity>> =
        dao.getSellerOrders(sellerId)

    fun getOrder(orderId: String): Flow<OrderEntity?> = dao.getOrder(orderId)

    fun getOrderItems(orderId: String): Flow<List<OrderItemEntity>> =
        dao.getOrderItems(orderId)

    /**
     * Executes an checkout or place order operation.
     * Inserts the order metadata, line items, decreases product inventory,
     * and clears the buyer's cart.
     */
    suspend fun checkoutOrder(
        userId: String,
        fullName: String,
        deliveryAddress: String,
        phone: String,
        paymentMethod: String,
        cartItems: List<Pair<CartEntity, ProductEntity>>
    ): OrderEntity? {
        if (cartItems.isEmpty()) return null

        val orderId = "ORD-${UUID.randomUUID().toString().take(8).uppercase()}"
        val totalAmount = cartItems.sumOf { it.first.quantity * it.second.price }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateStr = dateFormat.format(Date())

        // Create Order Entity
        // Note: For simplicity of a marketplace MVP, we group them into a single primary order
        val orderEntity = OrderEntity(
            orderId = orderId,
            buyerId = userId,
            buyerName = fullName,
            totalAmount = totalAmount,
            paymentMethod = paymentMethod,
            deliveryAddress = deliveryAddress,
            orderStatus = "Pending",
            deliveryStatus = "Pending",
            sellerId = cartItems.first().second.sellerId, // Primary seller
            orderDate = dateStr
        )

        dao.insertOrder(orderEntity)

        // Make order items
        val orderItemEntities = cartItems.map { (cart, product) ->
            OrderItemEntity(
                orderItemId = "ITEM-${UUID.randomUUID().toString().take(10).uppercase()}",
                orderId = orderId,
                productId = product.productId,
                productName = product.productName,
                quantity = cart.quantity,
                price = product.price,
                subtotal = cart.quantity * product.price
            )
        }
        dao.insertOrderItems(orderItemEntities)

        // Update inventory levels, decrementing stock
        cartItems.forEach { (cart, product) ->
            val remainingStock = maxOf(0, product.quantityAvailable - cart.quantity)
            dao.updateProductQuantity(product.productId, remainingStock)
        }

        // Create a Delivery Request for this Order
        val deliveryRequest = DeliveryRequestEntity(
            deliveryId = "DEL-${UUID.randomUUID().toString().take(6).uppercase()}",
            orderId = orderId,
            driverName = "Modou Lamin (Delivery Captain)",
            deliveryStatus = "Assigned",
            deliveryFee = 50.0, // Standard delivery fee in Dalasis (approx D50)
            deliveryTime = "Under 60 Minutes"
        )
        dao.insertDeliveryRequest(deliveryRequest)

        // Clear cart
        dao.clearUserCart(userId)

        return orderEntity
    }

    suspend fun updateOrderStatus(orderId: String, status: String, deliveryStatus: String) =
        dao.updateOrderStatus(orderId, status, deliveryStatus)

    // --- Deliveries ---
    val allDeliveries: Flow<List<DeliveryRequestEntity>> = dao.getAllDeliveries()

    fun getDeliveryForOrder(orderId: String): Flow<DeliveryRequestEntity?> =
        dao.getDeliveryRequest(orderId)

    suspend fun updateDeliveryStatus(deliveryId: String, status: String) =
        dao.updateDeliveryRequestStatus(deliveryId, status)

    // --- Categories ---
    val allCategories: Flow<List<CategoryEntity>> = dao.getAllCategories()

    // --- Bundles ---
    val allBundles: Flow<List<BundleEntity>> = dao.getAllBundles()

    fun getBundleItems(bundleId: String): Flow<List<BundleItemEntity>> = dao.getBundleItems(bundleId)

    suspend fun getBundleItemsSync(bundleId: String): List<BundleItemEntity> =
        dao.getBundleItemsSync(bundleId)

    suspend fun saveBundle(bundle: BundleEntity) = dao.insertBundles(listOf(bundle))

    suspend fun saveBundleItems(items: List<BundleItemEntity>) = dao.insertBundleItems(items)

    suspend fun getOrderItemsSync(orderId: String): List<OrderItemEntity> = dao.getOrderItemsSync(orderId)

}
