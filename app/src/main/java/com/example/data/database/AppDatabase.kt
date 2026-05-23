package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.DugamaDao
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        UserEntity::class,
        SellerEntity::class,
        ProductEntity::class,
        CartEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        CategoryEntity::class,
        BundleEntity::class,
        BundleItemEntity::class,
        DeliveryRequestEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dugamaDao(): DugamaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dugama_database"
                )
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Retrieve database and seed in a background thread
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    seedDatabase(database.dugamaDao())
                }
            }
        }

        private suspend fun seedDatabase(dao: DugamaDao) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val currentDate = dateFormat.format(Date())

            // --- 1. Seed Current User (Default Profile for MVP Demo) ---
            val defaultUser = UserEntity(
                userId = "user_1",
                fullName = "Alieu Jallow",
                phone = "+220 123 4567",
                email = "alieu@dugama.gm",
                role = "Buyer", // User can change Roles in UI or profile
                address = "Banjulinding, West Coast",
                profilePhoto = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=200&auto=format&fit=crop",
                dateCreated = currentDate
            )
            dao.insertUser(defaultUser)

            // --- 2. Seed Sellers ---
            val sellers = listOf(
                SellerEntity(
                    sellerId = "seller_fatou",
                    businessName = "Fatou's Garden",
                    sellerType = "Gardener/Farmer",
                    description = "We grow organic, fresh and healthy vegetables in Brikama with love and care.",
                    phone = "+220 312 3456",
                    location = "Brikama, West Coast",
                    logo = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=200&auto=format&fit=crop",
                    verificationStatus = "Verified"
                ),
                SellerEntity(
                    sellerId = "seller_jammeh",
                    businessName = "Jammeh Farm",
                    sellerType = "Gardener/Farmer",
                    description = "Providing high-quality native garden crops, fresh cabbage, okra, and peppers directly from Kerewan.",
                    phone = "+220 721 9876",
                    location = "Kerewan, North Bank",
                    logo = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?q=80&w=200&auto=format&fit=crop",
                    verificationStatus = "Verified"
                ),
                SellerEntity(
                    sellerId = "seller_sanyang",
                    businessName = "Sanyang Green",
                    sellerType = "Gardener/Farmer",
                    description = "Your trusted green source in Sanyang. Clean fresh green leaves, cucumber, sweet peppers of high yield.",
                    phone = "+220 985 1111",
                    location = "Sanyang, West Coast",
                    logo = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=200&auto=format&fit=crop",
                    verificationStatus = "Verified"
                ),
                SellerEntity(
                    sellerId = "seller_binta",
                    businessName = "Binta's Garden",
                    sellerType = "Vendor",
                    description = "Freshly bought from Banjul port & regional community gardens. Reseller of best price vegetables, eggs and essential spices.",
                    phone = "+220 543 2222",
                    location = "Kartong, West Coast",
                    logo = "https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?q=80&w=200&auto=format&fit=crop",
                    verificationStatus = "Verified"
                )
            )
            sellers.forEach { dao.insertSeller(it) }

            // --- 3. Seed Categories ---
            val categories = listOf(
                CategoryEntity("cat_veg", "Vegetables", "ic_veg"),
                CategoryEntity("cat_fruit", "Fruits", "ic_fruit"),
                CategoryEntity("cat_fish", "Fish", "ic_fish"),
                CategoryEntity("cat_spices", "Spices", "ic_spices"),
                CategoryEntity("cat_grains", "Rice & Grains", "ic_grains"),
                CategoryEntity("cat_bread", "Bread", "ic_bread"),
                CategoryEntity("cat_drinks", "Drinks", "ic_drinks"),
                CategoryEntity("cat_garden", "Garden Products", "ic_garden")
            )
            dao.insertCategories(categories)

            // --- 4. Seed Products ---
            val products = listOf(
                ProductEntity(
                    productId = "prod_tomatoes",
                    productName = "Fresh Tomatoes",
                    category = "Vegetables",
                    description = "Plump, red, and juicy vine-ripened tomatoes. Grown organically in Fatou's farm in Brikama, perfect for Domoda stew.",
                    price = 80.0,
                    unit = "kg",
                    quantityAvailable = 45,
                    productImage = "https://images.unsplash.com/photo-1595855759920-86582396756a?q=80&w=500&auto=format&fit=crop",
                    sellerId = "seller_fatou",
                    sellerName = "Fatou's Garden",
                    location = "Brikama",
                    featured = true,
                    dateAdded = currentDate
                ),
                ProductEntity(
                    productId = "prod_fish",
                    productName = "Fresh Fish (Ladyfish)",
                    category = "Fish",
                    description = "Freshly caught Ladyfish, cleaned and ready to cook or smoke. Sourced directly from Tanji fish landing site.",
                    price = 150.0,
                    unit = "kg",
                    quantityAvailable = 25,
                    productImage = "https://images.unsplash.com/photo-1534604973900-c43ab4c2e0ab?q=80&w=500&auto=format&fit=crop",
                    sellerId = "seller_jammeh",
                    sellerName = "Jammeh Farm",
                    location = "Kerewan",
                    featured = true,
                    dateAdded = currentDate
                ),
                ProductEntity(
                    productId = "prod_onions",
                    productName = "Gambia Onions",
                    category = "Vegetables",
                    description = "Local red and golden-skinned onions. Strong flavor, highly aromatic, and absolutely essential for Gambian stew.",
                    price = 60.0,
                    unit = "kg",
                    quantityAvailable = 100,
                    productImage = "https://images.unsplash.com/photo-1508747703725-719777637510?q=80&w=500&auto=format&fit=crop",
                    sellerId = "seller_binta",
                    sellerName = "Binta's Garden",
                    location = "Kartong",
                    featured = true,
                    dateAdded = currentDate
                ),
                ProductEntity(
                    productId = "prod_pepper",
                    productName = "Sweet Pepper (Bell)",
                    category = "Vegetables",
                    description = "Crispy green and red bell peppers packed with vitamins. Crunchy and sweet taste, harvested daily.",
                    price = 100.0,
                    unit = "bag",
                    quantityAvailable = 30,
                    productImage = "https://images.unsplash.com/photo-1563565312-8235d799aa7f?q=80&w=500&auto=format&fit=crop",
                    sellerId = "seller_sanyang",
                    sellerName = "Sanyang Green",
                    location = "Sanyang",
                    featured = true,
                    dateAdded = currentDate
                ),
                ProductEntity(
                    productId = "prod_okra",
                    productName = "Fresh Okra",
                    category = "Vegetables",
                    description = "Tender green okra pods. Essential for cooking Supakanja (okra stew), yielding the perfect slimy texture and rich taste.",
                    price = 120.0,
                    unit = "bundle",
                    quantityAvailable = 20,
                    productImage = "https://images.unsplash.com/photo-1628178121639-6d63d6bded14?q=80&w=500&auto=format&fit=crop",
                    sellerId = "seller_jammeh",
                    sellerName = "Jammeh Farm",
                    location = "Kerewan",
                    featured = true,
                    dateAdded = currentDate
                ),
                ProductEntity(
                    productId = "prod_bread",
                    productName = "Tapalapa Bread",
                    category = "Bread",
                    description = "Traditional Senegambian crusty long bread. Rich, heavy, freshly baked in wood-fired earthen ovens in Serrekunda.",
                    price = 25.0,
                    unit = "loaf",
                    quantityAvailable = 80,
                    productImage = "https://images.unsplash.com/photo-1509440159596-0249088772ff?q=80&w=500&auto=format&fit=crop",
                    sellerId = "seller_binta",
                    sellerName = "Binta's Garden",
                    location = "Banjul",
                    featured = true,
                    dateAdded = currentDate
                ),
                ProductEntity(
                    productId = "prod_mango",
                    productName = "Sweet Mangoes (Dhasheri)",
                    category = "Fruits",
                    description = "Super sweet, fleshy local Gambian mangoes. Picked ripe at Sanyang community orchard, extremely fresh.",
                    price = 50.0,
                    unit = "basket",
                    quantityAvailable = 50,
                    productImage = "https://images.unsplash.com/photo-1553279768-865429fa0078?q=80&w=500&auto=format&fit=crop",
                    sellerId = "seller_sanyang",
                    sellerName = "Sanyang Green",
                    location = "Sanyang",
                    featured = false,
                    dateAdded = currentDate
                ),
                ProductEntity(
                    productId = "prod_paste",
                    productName = "Peanut Paste (Tiga)",
                    category = "Spices",
                    description = "100% roasted local peanuts ground into a smooth, rich paste. The core ingredient for making authentic Gambian Domoda.",
                    price = 90.0,
                    unit = "cup",
                    quantityAvailable = 40,
                    productImage = "https://images.unsplash.com/photo-1590080875515-8a3a8dc5735e?q=80&w=500&auto=format&fit=crop",
                    sellerId = "seller_fatou",
                    sellerName = "Fatou's Garden",
                    location = "Brikama",
                    featured = false,
                    dateAdded = currentDate
                ),
                ProductEntity(
                    productId = "prod_gardenegg",
                    productName = "Garden Eggs (Jakatu)",
                    category = "Garden Products",
                    description = "Native bitter tomatoes/garden eggs. Provides the distinctive Gambian touch to many local soups and rice meals.",
                    price = 80.0,
                    unit = "basket",
                    quantityAvailable = 15,
                    productImage = "https://images.unsplash.com/photo-1596151163148-52763260840b?q=80&w=500&auto=format&fit=crop",
                    sellerId = "seller_binta",
                    sellerName = "Binta's Garden",
                    location = "Kartong",
                    featured = false,
                    dateAdded = currentDate
                )
            )
            products.forEach { dao.insertProduct(it) }

            // --- 5. Seed Bundles ---
            val bundles = listOf(
                BundleEntity(
                    bundleId = "bundle_domoda",
                    bundleName = "Monday Domoda Bundle",
                    bundleImage = "https://images.unsplash.com/photo-1541832676-9b763b0239ab?q=80&w=500&auto=format&fit=crop", // Stew visual representation
                    bundlePrice = 350.0,
                    description = "Authentic Gambian heavy peanut butter stew with fresh pumpkin and local leaves. Feeds 4 people comfortably."
                ),
                BundleEntity(
                    bundleId = "bundle_benachin_mon",
                    bundleName = "Monday Benachin Rice",
                    bundleImage = "https://images.unsplash.com/photo-1512058564366-18510be2db19?q=80&w=500&auto=format&fit=crop", // Fried rice representation
                    bundlePrice = 450.0,
                    description = "One-pot red jollof rice with ladyfish, cabbage, carrots, and bitter tomatoes (jakatu)."
                ),
                BundleEntity(
                    bundleId = "bundle_supakanja",
                    bundleName = "Tuesday Supakanja Okra",
                    bundleImage = "https://images.unsplash.com/photo-1604329760661-e71dc83f8f26?q=80&w=500&auto=format&fit=crop", // Okra stew representation
                    bundlePrice = 500.0,
                    description = "The ultimate Tuesday okra stew with fish, palm oil, bitter tomatoes, and traditional peppers."
                ),
                BundleEntity(
                    bundleId = "bundle_afra",
                    bundleName = "Wednesday Afra Barbecue",
                    bundleImage = "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?q=80&w=500&auto=format&fit=crop", // BBQ representation
                    bundlePrice = 400.0,
                    description = "Barbecue charcoal grilled meat seasoned with spicy mustard sauce, lots of hot onions, and local chili."
                ),
                BundleEntity(
                    bundleId = "bundle_durang",
                    bundleName = "Thursday Durang Stew",
                    bundleImage = "https://images.unsplash.com/photo-1476718406336-bb5a9690ee2a?q=80&w=500&auto=format&fit=crop", // Soup
                    bundlePrice = 380.0,
                    description = "Traditional village peanut stew cooked lighter with pumpkin, fresh herbs, onions, and beef."
                ),
                BundleEntity(
                    bundleId = "bundle_plasma",
                    bundleName = "Friday Plasma Feast",
                    bundleImage = "https://images.unsplash.com/photo-1540420773420-3366772f4999?q=80&w=500&auto=format&fit=crop", // Green stew
                    bundlePrice = 480.0,
                    description = "Friday night greens cooked with peanut butter, smoked bonga fish, palm oil, and fresh habaneros."
                ),
                BundleEntity(
                    bundleId = "bundle_mbahal",
                    bundleName = "Saturday Mbahal Rice",
                    bundleImage = "https://images.unsplash.com/photo-1512058564366-18510be2db19?q=80&w=500&auto=format&fit=crop", // Jollof/Mbahal
                    bundlePrice = 320.0,
                    description = "Local white rice mixed with dried smoked bonga fish, netetou (locust bean), ground peanuts, and spring onions."
                ),
                BundleEntity(
                    bundleId = "bundle_yassa",
                    bundleName = "Sunday Yassa Chicken",
                    bundleImage = "https://images.unsplash.com/photo-1604908176997-125f25cc6f3d?q=80&w=500&auto=format&fit=crop", // Lemon chicken
                    bundlePrice = 600.0,
                    description = "Tangy caramelized onion and lemon mustard chicken stew, a crowd favorite for family Sunday reunions."
                )
            )
            dao.insertBundles(bundles)

            // --- 6. Seed Bundle Items ---
            val bundleItems = listOf(
                // Domoda bundle items
                BundleItemEntity("bi_dom_1", "bundle_domoda", "prod_tomatoes", "Tomatoes", 2),
                BundleItemEntity("bi_dom_2", "bundle_domoda", "prod_onions", "Onions", 1),
                BundleItemEntity("bi_dom_3", "bundle_domoda", "prod_paste", "Peanut Paste", 1),
                BundleItemEntity("bi_dom_4", "bundle_domoda", "prod_veg_oil", "Cooking Oil", 1),
                BundleItemEntity("bi_dom_5", "bundle_domoda", "prod_meat_m", "Primary Meat Selection", 1),
                BundleItemEntity("bi_dom_6", "bundle_domoda", "prod_pepper", "Pepper Capsicums", 1),

                // Monday Benachin items
                BundleItemEntity("bi_ben_1", "bundle_benachin_mon", "prod_tomatoes", "Tomatoes", 1),
                BundleItemEntity("bi_ben_2", "bundle_benachin_mon", "prod_onions", "Onions", 2),
                BundleItemEntity("bi_ben_3", "bundle_benachin_mon", "prod_rice_g", "Sadia Rice", 3),
                BundleItemEntity("bi_ben_4", "bundle_benachin_mon", "prod_fish", "Ladyfish", 1),
                BundleItemEntity("bi_ben_5", "bundle_benachin_mon", "prod_pepper", "Sweet Pepper", 1),

                // Supakanja bundle items
                BundleItemEntity("bi_sup_1", "bundle_supakanja", "prod_okra", "Tender Okra", 3),
                BundleItemEntity("bi_sup_2", "bundle_supakanja", "prod_gardenegg", "Garden Eggs (Jakatu)", 2),
                BundleItemEntity("bi_sup_3", "bundle_supakanja", "prod_palm_oil", "Palm Oil Bottle", 1),
                BundleItemEntity("bi_sup_4", "bundle_supakanja", "prod_fish", "Ladyfish", 2),
                BundleItemEntity("bi_sup_5", "bundle_supakanja", "prod_habanero", "Dry Hot Pepper", 1),

                // Afra items
                BundleItemEntity("bi_afr_1", "bundle_afra", "prod_meat_m", "Choice Smoked Beef", 2),
                BundleItemEntity("bi_afr_2", "bundle_afra", "prod_onions", "Sweet Sliced Onions", 3),
                BundleItemEntity("bi_afr_3", "bundle_afra", "prod_mustard", "Spicy Senegambian Mustard", 1),

                // Durang items
                BundleItemEntity("bi_dur_1", "bundle_durang", "prod_paste", "Peanut Paste", 1),
                BundleItemEntity("bi_dur_2", "bundle_durang", "prod_gardenegg", "Jakatu Bitters", 1),
                BundleItemEntity("bi_dur_3", "bundle_durang", "prod_okra", "Fresh Okra Selection", 2),

                // Plasma items
                BundleItemEntity("bi_pla_1", "bundle_plasma", "prod_spinach", "Fresh Chopped Spinach", 3),
                BundleItemEntity("bi_pla_2", "bundle_plasma", "prod_fish", "Smoked Bonga Fish", 2),
                BundleItemEntity("bi_pla_3", "bundle_plasma", "prod_palm_oil", "Native Palm Oil", 1),

                // Mbahal items
                BundleItemEntity("bi_mba_1", "bundle_mbahal", "prod_rice_g", "Local White Rice Bag", 2),
                BundleItemEntity("bi_mba_2", "bundle_mbahal", "prod_fish", "Smoked Ladyfish", 1),
                BundleItemEntity("bi_mba_3", "bundle_mbahal", "prod_paste", "Groundnut Flour", 1),

                // Yassa items
                BundleItemEntity("bi_yas_1", "bundle_yassa", "prod_chicken", "Plump Local Chicken", 1),
                BundleItemEntity("bi_yas_2", "bundle_yassa", "prod_onions", "Huge Golden Onions", 4),
                BundleItemEntity("bi_yas_3", "bundle_yassa", "prod_lemon", "Fresh Lemon Citrus", 3)
            )
            dao.insertBundleItems(bundleItems)
        }
    }
}
