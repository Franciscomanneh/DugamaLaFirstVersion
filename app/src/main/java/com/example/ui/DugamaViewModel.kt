package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.DugamaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class DugamaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DugamaRepository
    val currentUserId = "user_1" // Default user for offline MVP Demo

    // Current User Profile Flow
    val currentUser: StateFlow<UserEntity?>

    // Main bottom navigation tabs: "Home", "Market", "Checkout", "Profile"
    private val _currentTab = MutableStateFlow("Home")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    // Categories list based on DB
    val categories: StateFlow<List<CategoryEntity>>

    // Products Flow
    val allProducts: StateFlow<List<ProductEntity>>
    val featuredProducts: StateFlow<List<ProductEntity>>

    // Food Bundles Flow
    val bundles: StateFlow<List<BundleEntity>>

    // Seller Credits balance (purchased via Wave, AfriMoney, QMoney)
    private val _sellerCredits = MutableStateFlow<Map<String, Int>>(mapOf("seller_fatou" to 15, "seller_jammeh" to 0))
    val sellerCredits: StateFlow<Map<String, Int>> = _sellerCredits.asStateFlow()

    // Map of active product boosts with expiry millisecond timestamp
    private val _productBoosts = MutableStateFlow<Map<String, Long>>(mapOf())
    val productBoosts: StateFlow<Map<String, Long>> = _productBoosts.asStateFlow()

    // Daily 35-Minute Fair Exposure Discovery Window state
    private val _isDiscoveryWindowActive = MutableStateFlow(false)
    val isDiscoveryWindowActive: StateFlow<Boolean> = _isDiscoveryWindowActive.asStateFlow()

    // Active User Cart with joint Product details
    private val _cartItemsWithProducts = MutableStateFlow<List<Pair<CartEntity, ProductEntity>>>(emptyList())
    val cartItemsWithProducts: StateFlow<List<Pair<CartEntity, ProductEntity>>> = _cartItemsWithProducts.asStateFlow()

    // User Orders Flow
    val userOrders: StateFlow<List<OrderEntity>>
    val allOrdersForAdmin: StateFlow<List<OrderEntity>>

    // Sellers Flow (Admin / Directory)
    val allSellers: StateFlow<List<SellerEntity>>

    // Active Selected Product & Bundle Details for modals/detail page
    private val _selectedProduct = MutableStateFlow<ProductEntity?>(null)
    val selectedProduct: StateFlow<ProductEntity?> = _selectedProduct.asStateFlow()

    private val _selectedBundle = MutableStateFlow<BundleEntity?>(null)
    val selectedBundle: StateFlow<BundleEntity?> = _selectedBundle.asStateFlow()

    private val _selectedBundleItems = MutableStateFlow<List<BundleItemEntity>>(emptyList())
    val selectedBundleItems: StateFlow<List<BundleItemEntity>> = _selectedBundleItems.asStateFlow()

    // Search and Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("All")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    // Delivery and admin listings
    val allDeliveries: StateFlow<List<DeliveryRequestEntity>>

    // Current simulated screen or flows: e.g. "Home", "Market", "Checkout", "Profile", "ProductDetail", "BundleDetail", "SellerDashboard", "AdminDashboard", "SellerProfile"
    private val _navigationStack = MutableStateFlow<List<String>>(listOf("Home"))
    val navigationStack: StateFlow<List<String>> = _navigationStack.asStateFlow()

    private val _activeSellerIdForProfile = MutableStateFlow<String?>(null)
    val activeSellerIdForProfile: StateFlow<String?> = _activeSellerIdForProfile.asStateFlow()

    private val _loggedInSellerId = MutableStateFlow<String>("seller_fatou")
    val loggedInSellerId: StateFlow<String> = _loggedInSellerId.asStateFlow()

    // Feedback notifications / Toast state
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // --- Dynamic Operating Locations ---
    private val _availableLocations = MutableStateFlow<List<String>>(listOf("Banjulinding", "Yundum", "Lamin", "Busumbala"))
    val availableLocations: StateFlow<List<String>> = _availableLocations.asStateFlow()

    // --- Dynamic Global Announcements (Notification Banner/Popup) ---
    private val _activeAnnouncement = MutableStateFlow<String?>("Fresh rain-fed harvest is arriving from Banjulinding community gardens! Log into profile to register as a gardener.")
    val activeAnnouncement: StateFlow<String?> = _activeAnnouncement.asStateFlow()

    // --- Seller Rules & Terms Text ---
    private val _sellerTerms = MutableStateFlow<String>(
        "1. All garden crops must be harvested fresh daily.\n" +
        "2. Zero synthetic chemical feedback in the community zoning.\n" +
        "3. Clear weights measured transparently (by kg or native basis basket).\n" +
        "4. Seamless Mobile Money compatibility (Wave, QMoney).\n" +
        "5. Cooperative driver coordination fee of 5% applies."
    )
    val sellerTerms: StateFlow<String> = _sellerTerms.asStateFlow()

    // --- Support & Farmer Donations Ledger system ---
    data class SupportDonation(
        val donationId: String,
        val donorName: String,
        val tier: String,
        val amount: Double,
        val paymentMethod: String,
        val phoneNumber: String,
        val dateAdded: String
    )
    private val _supportDonations = MutableStateFlow<List<SupportDonation>>(listOf(
        SupportDonation("DON-7761A", "Modou Bah", "Weekly Water Pump Fuel", 500.0, "Wave Mobile Money", "+220 771 9922", "2026-05-23 10:15:00"),
        SupportDonation("DON-1102X", "Aji Fatou Cham", "Monthly Co-op Sponsoring", 2000.0, "QMoney", "+220 334 1109", "2026-05-23 12:44:00")
    ))
    val supportDonations: StateFlow<List<SupportDonation>> = _supportDonations.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = DugamaRepository(database.dugamaDao())

        currentUser = repository.getUser(currentUserId)
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

        allSellers = repository.allSellers
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        categories = repository.allCategories
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allProducts = repository.allProducts
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Dynamic, intelligent, synchronized featured products based on active boosts
        // and Daily 35-min exposure discovery window
        featuredProducts = combine(
            repository.allProducts,
            _productBoosts,
            _isDiscoveryWindowActive
        ) { products, boosts, discoveryActive ->
            val now = System.currentTimeMillis()
            val boostedProducts = products.filter { boosts.getOrDefault(it.productId, 0L) > now }

            if (discoveryActive) {
                // Fair 35-minute exposure window active: feature and randomize non-boosted products
                val nonBoosted = products.filter { boosts.getOrDefault(it.productId, 0L) <= now }
                (boostedProducts + nonBoosted.shuffled().take(3)).distinct()
            } else {
                if (boostedProducts.isNotEmpty()) {
                    // Intelligently rotate / prioritize boosted products fairly
                    boostedProducts.sortedByDescending { boosts.getOrDefault(it.productId, 0L) }
                } else {
                    // Fallback to seeded standard featured items
                    products.filter { it.featured }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

        bundles = repository.allBundles
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        userOrders = repository.getBuyerOrders(currentUserId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allOrdersForAdmin = repository.allOrders
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allDeliveries = repository.allDeliveries
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Monitor Cart & Product changes to keep the Cart list up-to-date with actual Product Objects
        viewModelScope.launch {
            combine(
                repository.getUserCart(currentUserId),
                repository.allProducts
            ) { cartList, productList ->
                cartList.mapNotNull { cartItem ->
                    val product = productList.find { it.productId == cartItem.productId }
                    if (product != null) Pair(cartItem, product) else null
                }
            }.collect { jointList ->
                _cartItemsWithProducts.value = jointList
            }
        }
    }

    // --- Navigation System ---
    fun selectTab(tab: String) {
        _currentTab.value = tab
        _navigationStack.value = listOf(tab) // Reset navigation stack back to core tab
    }

    fun navigateTo(screen: String) {
        val current = _navigationStack.value.toMutableList()
        current.add(screen)
        _navigationStack.value = current
    }

    fun navigateBack() {
        val current = _navigationStack.value.toMutableList()
        if (current.size > 1) {
            current.removeAt(current.size - 1)
            _navigationStack.value = current
        }
    }

    fun showProductDetails(product: ProductEntity) {
        _selectedProduct.value = product
        navigateTo("ProductDetail")
    }

    fun showBundleDetails(bundle: BundleEntity) {
        _selectedBundle.value = bundle
        _selectedBundleItems.value = emptyList()
        viewModelScope.launch {
            repository.getBundleItems(bundle.bundleId).collect {
                _selectedBundleItems.value = it
            }
        }
        navigateTo("BundleDetail")
    }

    fun showSellerProfile(sellerId: String) {
        _activeSellerIdForProfile.value = sellerId
        navigateTo("SellerProfile")
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    // --- Cart Actions ---
    fun addProductToCart(productId: String, quantity: Int = 1) {
        viewModelScope.launch {
            val matchingProduct = repository.getProductSync(productId)
            if (matchingProduct != null) {
                repository.addToCart(currentUserId, productId, quantity)
                showToast("${matchingProduct.productName} added to checkout cart")
            }
        }
    }

    fun updateCartItemQuantity(cartId: String, newQty: Int) {
        viewModelScope.launch {
            repository.updateCartQty(cartId, newQty)
        }
    }

    fun removeCartItem(cartId: String) {
        viewModelScope.launch {
            repository.deleteFromCart(cartId)
            showToast("Item removed from cart")
        }
    }

    // --- Add bundle to cart ---
    fun addBundleToCart(bundle: BundleEntity, customItems: List<BundleItemEntity>) {
        viewModelScope.launch {
            // Collect the bundle components, resolving to actual products or adding placeholders
            customItems.forEach { item ->
                // Look for existing product
                val matchingProduct = repository.allProducts.first().find { it.productId == item.productId }
                if (matchingProduct != null) {
                    repository.addToCart(currentUserId, matchingProduct.productId, item.quantity)
                } else {
                    // Create dynamic product representing this bundle item or add to cart directly
                    // In a modern simplified model, we reference seeded products or make a fallback
                    // Seeded products are: prod_tomatoes, prod_onions, prod_pepper, prod_okra, etc.
                    repository.addToCart(currentUserId, item.productId, item.quantity)
                }
            }
            showToast("${bundle.bundleName} ingredients added to cart!")
            selectTab("Checkout") // Navigate to cart
        }
    }

    fun addBundleToCartById(bundle: BundleEntity) {
        viewModelScope.launch {
            val items = repository.getBundleItemsSync(bundle.bundleId)
            addBundleToCart(bundle, items)
        }
    }

    // --- Profile Actions ---
    fun updateUserProfile(name: String, phone: String, address: String, profilePhoto: String) {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                val updated = user.copy(fullName = name, phone = phone, address = address, profilePhoto = profilePhoto)
                repository.saveUser(updated)
                showToast("Profile details updated successfully")
            }
        }
    }

    fun switchUserRole(role: String) {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                val updated = user.copy(role = role)
                repository.saveUser(updated)
                showToast("Switched role context to $role")
            }
        }
    }

    // --- Seller / Gardener Actions ---
    fun registerSeller(businessName: String, sellerType: String, description: String, phone: String, location: String, logoUrl: String) {
        viewModelScope.launch {
            val sId = "seller_${businessName.replace(" ", "_").lowercase()}"
            val newSeller = SellerEntity(
                sellerId = sId,
                businessName = businessName,
                sellerType = sellerType,
                description = description,
                phone = phone,
                location = location,
                logo = if (logoUrl.isNotEmpty()) logoUrl else "https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=200&auto=format&fit=crop",
                verificationStatus = "Pending" // Starts as Pending, requiring Admin Approval in registry
            )
            repository.saveSeller(newSeller)
            _loggedInSellerId.value = sId

            // Switch role to unified "Seller"
            switchUserRole("Seller")

            showToast("Seller registration submitted! Pending Admin review & approval.")
            navigateTo("SellerDashboard")
        }
    }

    fun uploadProduct(productName: String, category: String, price: Double, unit: String, quantity: Int, description: String, logoUrl: String) {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                val sellerId = _loggedInSellerId.value
                val sellerObj = allSellers.value.find { it.sellerId == sellerId }
                val sellerName = sellerObj?.businessName ?: "My Garden Store"
                val sellerLoc = sellerObj?.location?.substringBefore(",") ?: user.address.substringBefore(",")
                val randId = "prod_${UUID.randomUUID().toString().take(6).lowercase()}"

                val newProd = ProductEntity(
                    productId = randId,
                    productName = productName,
                    category = category,
                    description = description,
                    price = price,
                    unit = unit,
                    quantityAvailable = quantity,
                    productImage = if (logoUrl.isNotEmpty()) logoUrl else "https://images.unsplash.com/photo-1595855759920-86582396756a?q=80&w=500&auto=format&fit=crop",
                    sellerId = sellerId,
                    sellerName = sellerName,
                    location = sellerLoc,
                    featured = false,
                    dateAdded = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                )

                repository.saveProduct(newProd)
                showToast("$productName successfully added to market inventory!")
                navigateBack() // Go back from dynamic upload form
            }
        }
    }

    // --- Checkout & Place Order Action ---
    fun placeOrder(address: String, phone: String, paymentMethod: String, context: Context) {
        val user = currentUser.value ?: return
        val itemsInCart = cartItemsWithProducts.value
        if (itemsInCart.isEmpty()) {
            showToast("Your checkout cart is empty!")
            return
        }

        viewModelScope.launch {
            val order = repository.checkoutOrder(user.userId, user.fullName, address, phone, paymentMethod, itemsInCart)
            if (order != null) {
                showToast("Order placed successfully!")

                // Generate formatted WhatsApp text as requested
                val itemsSummary = itemsInCart.joinToString("\n") { (cart, prod) ->
                    "* ${prod.productName} x${cart.quantity}"
                }
                val totalWithDelStr = "D${order.totalAmount.toInt() + 50}" // Adding 50 Dalasi delivery charge

                val formattedMsg = """
                    New Dugama Order
                    
                    Customer: ${user.fullName}
                    Phone: $phone
                    Items:
                    $itemsSummary
                    
                    Total: $totalWithDelStr (Includes Delivery)
                    Address: $address
                    Payment: $paymentMethod
                    
                    Please prepare this order.
                """.trimIndent()

                // Save formatted message in navigation stack or share directly with WhatsApp Intent
                shareToWhatsApp(formattedMsg, phone, context)

                // Navigate back to Home and show user orders list
                selectTab("Profile")
                navigateTo("MyOrdersScreen")
            }
        }
    }

    private fun shareToWhatsApp(text: String, phone: String, context: Context) {
        try {
            // Attempt opening WhatsApp with the message text using package manager or generic share intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                // Filter specifically for WhatsApp packages
                `package` = "com.whatsapp"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            // Fallback to general sharing or print message. Very robust!
            try {
                val genericIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(Intent.createChooser(genericIntent, "Share Order via WhatsApp"))
            } catch (ex: Exception) {
                showToast("Order saved locally. WhatsApp sharing failed relative to simulator.")
            }
        }
    }

    // --- Admin Dashboard Actions ---
    fun approveSellerVerification(sellerId: String) {
        viewModelScope.launch {
            val currentSellerList = allSellers.value
            currentSellerList.find { it.sellerId == sellerId }?.let { seller ->
                val approved = seller.copy(verificationStatus = "Verified")
                repository.updateSeller(approved)
                showToast("${seller.businessName} has been fully approved & verified!")
            }
        }
    }

    fun updateOrderDispatchState(orderId: String, status: String, delStatus: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status, delStatus)
            showToast("Order $orderId status updated to $status")
        }
    }

    // Community Support & Farmer Subscription
    fun submitDonation(donorName: String, tier: String, amount: Double, paymentMethod: String, phone: String) {
        val nextId = "DON-${UUID.randomUUID().toString().take(5).uppercase()}"
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val newDonation = SupportDonation(nextId, donorName, tier, amount, paymentMethod, phone, dateStr)
        val currentList = _supportDonations.value.toMutableList()
        currentList.add(0, newDonation)
        _supportDonations.value = currentList
        showToast("D$amount Donation processed! Thank you for supporting West Coast farmers!")
    }

    // Announcements
    fun postGlobalAnnouncement(announcement: String?) {
        _activeAnnouncement.value = announcement
        if (announcement != null) {
            showToast("Global announcement pinned for app users!")
        } else {
            showToast("Announcement cleared.")
        }
    }

    // Operating Location Management
    fun addNewLocation(location: String) {
        if (location.isNotBlank() && !_availableLocations.value.contains(location)) {
            val current = _availableLocations.value.toMutableList()
            current.add(location)
            _availableLocations.value = current
            showToast("Operating zone '$location' added!")
        }
    }

    fun removeLocation(location: String) {
        val current = _availableLocations.value.toMutableList()
        if (current.remove(location)) {
            _availableLocations.value = current
            showToast("Operating zone '$location' removed.")
        }
    }

    // Editable Terms
    fun updateSellerTerms(newTerms: String) {
        _sellerTerms.value = newTerms
        showToast("Seller rules & terms updated successfully!")
    }

    // --- Seller Credit & Boost Actions ---
    fun buyCredits(sellerId: String, amount: Int, paymentMethod: String, phone: String) {
        val current = _sellerCredits.value.toMutableMap()
        val existing = current[sellerId] ?: 0
        current[sellerId] = existing + amount
        _sellerCredits.value = current
        showToast("Purchased $amount credits via $paymentMethod for phone $phone! 🪙")
    }

    fun boostProduct(productId: String, sellerId: String, durationHours: Int) {
        val cost = if (durationHours == 12) 5 else 10
        val currentCredits = _sellerCredits.value[sellerId] ?: 0
        if (currentCredits < cost) {
            showToast("Insufficient credits! Top up using Wave, AfriMoney, or QMoney.")
            return
        }

        // Deduct credits
        val currentCreditsMap = _sellerCredits.value.toMutableMap()
        currentCreditsMap[sellerId] = currentCredits - cost
        _sellerCredits.value = currentCreditsMap

        // Register/Extend boost expiry
        val currentBoostsMap = _productBoosts.value.toMutableMap()
        val durationMillis = durationHours * 60 * 60 * 1000L
        currentBoostsMap[productId] = System.currentTimeMillis() + durationMillis
        _productBoosts.value = currentBoostsMap

        showToast("Product boosted successfully for $durationHours hours! 🔥")
    }

    fun toggleDiscoveryWindow() {
        _isDiscoveryWindowActive.value = !_isDiscoveryWindowActive.value
        if (_isDiscoveryWindowActive.value) {
            showToast("Discovery Window ACTIVE! Spotlighting non-boosted items.")
        } else {
            showToast("Discovery Window closed.")
        }
    }

    // --- Product Edit/Delete Inventory Controls ---
    fun editProduct(productId: String, name: String, price: Double, qty: Int, unit: String) {
        viewModelScope.launch {
            val product = repository.getProductSync(productId)
            if (product != null) {
                val updated = product.copy(
                    productName = name,
                    price = price,
                    quantityAvailable = qty,
                    unit = unit
                )
                repository.saveProduct(updated)
                showToast("Product updated successfully!")
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            val product = repository.getProductSync(productId)
            if (product != null) {
                repository.deleteProduct(product)
                showToast("Product '${product.productName}' deleted.")
            }
        }
    }

    // Admin direct adding of products to any category/seller
    fun adminAddProduct(
        productName: String,
        category: String,
        price: Double,
        unit: String,
        quantity: Int,
        description: String,
        imageUrl: String,
        sellerId: String,
        sellerName: String,
        location: String
    ) {
        viewModelScope.launch {
            val randId = "prod_${UUID.randomUUID().toString().take(6).lowercase()}"
            val newProd = ProductEntity(
                productId = randId,
                productName = productName,
                category = category,
                description = description,
                price = price,
                unit = unit,
                quantityAvailable = quantity,
                productImage = if (imageUrl.isNotBlank()) imageUrl else "https://images.unsplash.com/photo-1595855759920-86582396756a?q=80&w=500&auto=format&fit=crop",
                sellerId = sellerId,
                sellerName = sellerName,
                location = location,
                featured = true,
                dateAdded = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            )
            repository.saveProduct(newProd)
            showToast("Admin successfully launched $productName to category $category!")
        }
    }

    // Admin direct adding of dynamic food bundles
    fun adminAddBundle(
        bundleName: String,
        bundlePrice: Double,
        description: String,
        imageUrl: String,
        itemsSummaryList: List<Pair<String, Int>>
    ) {
        viewModelScope.launch {
            val bundleId = "bundle_${UUID.randomUUID().toString().take(6).lowercase()}"
            val newBundle = BundleEntity(
                bundleId = bundleId,
                bundleName = bundleName,
                bundleImage = if (imageUrl.isNotBlank()) imageUrl else "https://images.unsplash.com/photo-1541832676-9b763b0239ab?q=80&w=500&auto=format&fit=crop",
                bundlePrice = bundlePrice,
                description = description
            )
            repository.saveBundle(newBundle)

            val bundleItems = itemsSummaryList.mapIndexed { index, pair ->
                BundleItemEntity(
                    bundleItemId = "bi_${bundleId}_$index",
                    bundleId = bundleId,
                    productId = "prod_bundle_part_${index}_${bundleId}",
                    productName = pair.first,
                    quantity = pair.second
                )
            }
            repository.saveBundleItems(bundleItems)
            showToast("Admin successfully created bundle: $bundleName!")
        }
    }
}
