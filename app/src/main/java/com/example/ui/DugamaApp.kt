package com.example.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.*
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DugamaApp(viewModel: DugamaViewModel) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val navStack by viewModel.navigationStack.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val activeAnnouncement by viewModel.activeAnnouncement.collectAsStateWithLifecycle()
    var showNoticePopup by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(true) }

    val currentScreen = navStack.lastOrNull() ?: currentTab

    if (showNoticePopup && activeAnnouncement != null) {
        AlertDialog(
            onDismissRequest = { showNoticePopup = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Official Notice 📢",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            text = {
                Text(
                    text = activeAnnouncement ?: "",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                Button(
                    onClick = { showNoticePopup = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Acknowledge notice", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }

    // Handle back presses inside nested pages
    BackHandler(enabled = navStack.size > 1) {
        viewModel.navigateBack()
    }

    // Handle visual toast popups
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Scaffold(
        topBar = {
            if (currentScreen in listOf("Home", "Market", "Checkout", "Profile")) {
                TopAppBar(
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Dugama",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = currentUser?.address?.split(",")?.firstOrNull() ?: "Serrekunda",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { showNoticePopup = true }) {
                            Box {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                                        .align(Alignment.TopEnd)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            if (currentScreen in listOf("Home", "Market", "Checkout", "Profile")) {
                val cartItems by viewModel.cartItemsWithProducts.collectAsStateWithLifecycle()
                val totalCartCount = cartItems.sumOf { it.first.quantity }

                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    val items = listOf(
                        TabItem("Home", Icons.Default.Home, "Home"),
                        TabItem("Market", Icons.Default.Store, "Market"),
                        TabItem("Checkout", Icons.Default.ShoppingCart, "Checkout", badgeCount = totalCartCount),
                        TabItem("Profile", Icons.Default.Person, "Profile")
                    )

                    items.forEach { tab ->
                        NavigationBarItem(
                            selected = currentTab == tab.name,
                            onClick = { viewModel.selectTab(tab.name) },
                            icon = {
                                Box {
                                    Icon(imageVector = tab.icon, contentDescription = tab.label)
                                    if (tab.badgeCount > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.tertiary,
                                            contentColor = Color.White,
                                            modifier = Modifier.align(Alignment.TopEnd).offset(8.dp, (-4).dp)
                                        ) {
                                            Text(text = tab.badgeCount.toString(), fontSize = 10.sp)
                                        }
                                    }
                                }
                            },
                            label = { Text(tab.label, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    slideInHorizontally { width -> width / 3 } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width / 3 } + fadeOut()
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    "Home" -> HomeScreen(viewModel)
                    "Market" -> MarketScreen(viewModel)
                    "Checkout" -> CheckoutScreen(viewModel)
                    "Profile" -> ProfileScreen(viewModel)

                    // Sub-navigation screens
                    "ProductDetail" -> ProductDetailScreen(viewModel)
                    "BundleDetail" -> BundleDetailScreen(viewModel)
                    "MyOrdersScreen" -> MyOrdersScreen(viewModel)
                    "BecomeSeller" -> BecomeSellerFormScreen(viewModel)
                    "SellerDashboard" -> SellerDashboardScreen(viewModel)
                    "AdminDashboard" -> AdminDashboardScreen(viewModel)
                    "SellerProfile" -> SellerProfileScreen(viewModel)
                    "CommunitySupport" -> CommunitySupportScreen(viewModel)
                    "SystemConfig" -> SystemConfigScreen(viewModel)

                    // Fallback
                    else -> HomeScreen(viewModel)
                }
            }
        }
    }
}

private data class TabItem(
    val name: String,
    val icon: ImageVector,
    val label: String,
    val badgeCount: Int = 0
)

// --- REUSABLE UTILITIES ---

@Composable
fun AsyncImageWithFallback(
    model: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale,
            onError = {
                // Fallback handled beautifully by showing clean organic icon on gray / green backdrop
            }
        )
        // If image stays blank or shows error, we show an elegant brand indicator
        if (model.isEmpty() || model.startsWith("ic_")) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

// --- SCREEN 1: HOME SCREEN ---

@Composable
fun HomeScreen(viewModel: DugamaViewModel) {
    val products by viewModel.featuredProducts.collectAsStateWithLifecycle()
    val bundles by viewModel.bundles.collectAsStateWithLifecycle()
    val sellers by viewModel.allSellers.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // --- HERO BANNER SECTION ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
            ) {
                // Background artistic circles
                Canvas(modifier = Modifier.spaceMinSize(200.dp)) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.06f),
                        radius = size.minDimension * 0.7f,
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.1f)
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Fresh Market Shopping\nMade Easy in The Gambia",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 26.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Buy vegetables, fish, spices, and daily ingredients directly without going to the market. Support local gardeners!",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { viewModel.selectTab("Market") },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("hero_shop_market_button"),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Shop Market", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        OutlinedButton(
                            onClick = {
                                val user = viewModel.currentUser.value
                                if (user?.role in listOf("Gardener", "Vendor")) {
                                    viewModel.navigateTo("SellerDashboard")
                                } else {
                                    viewModel.navigateTo("BecomeSeller")
                                }
                            },
                            border = BorderStroke(1.5.dp, Color.White),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("hero_sell_products_button"),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Sell Your Products", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // --- CATEGORIES SECTIONS ---
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Categories",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    TextButton(onClick = { viewModel.selectTab("Market") }) {
                        Text("See all", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }

                val categoriesList = listOf(
                    CategoryItem("Vegetables", "🥬"),
                    CategoryItem("Fruits", "🍌"),
                    CategoryItem("Fish", "🐟"),
                    CategoryItem("Spices", "🌶️"),
                    CategoryItem("Rice & Grains", "🌾"),
                    CategoryItem("Bread", "🍞"),
                    CategoryItem("Drinks", "🧃"),
                    CategoryItem("Garden", "🪴")
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(categoriesList) { cat ->
                        // Dynamic background mapping matching the Vibrant Palette theme
                        val catBgColor = when (cat.name) {
                            "Vegetables" -> Color(0xFFFFEDD5) // Orange-100
                            "Fruits" -> Color(0xFFFEF08A)     // Yellow-100
                            "Fish" -> Color(0xFFDBEAFE)       // Blue-100
                            "Spices" -> Color(0xFFFEE2E2)     // Red-100
                            "Rice & Grains" -> Color(0xFFFEF3C7) // Amber-100
                            "Bread" -> Color(0xFFE2E8F0)      // Slate-200
                            "Drinks" -> Color(0xFFCCFBF1)     // Teal-100
                            "Garden" -> Color(0xFFDCFCE7)     // Green-100
                            else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    viewModel.setCategoryFilter(cat.name)
                                    viewModel.selectTab("Market")
                                }
                                .width(70.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(
                                        catBgColor,
                                        RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(cat.emoji, fontSize = 28.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (cat.name == "Rice & Grains") "Grains" else cat.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // --- FEATURED GARDENERS/SELLERS SECTIONS ---
        item {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Featured Gardeners",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    TextButton(onClick = { viewModel.navigateTo("AdminDashboard") }) {
                        Text("View all", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }

                if (sellers.isEmpty()) {
                    Text(
                        "Loading featured sellers...",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 12.sp
                    )
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(sellers) { seller ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .width(160.dp)
                                    .clickable { viewModel.showSellerProfile(seller.sellerId) }
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(90.dp)
                                    ) {
                                        AsyncImageWithFallback(
                                            model = seller.logo,
                                            contentDescription = seller.businessName,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .padding(6.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.primaryContainer,
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                .align(Alignment.TopEnd)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.tertiary,
                                                    modifier = Modifier.size(10.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    "4.8",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                    }
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = seller.businessName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = seller.location.split(",").first(),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Fresh ${if (seller.sellerType == "Vendor") "Vendor" else "Okra & Herbs"}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- COMMUNITY SUPPORT CTA ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { viewModel.navigateTo("CommunitySupport") },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Support Gambia's Farmers 🌱",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Sponsor organic fertilizers, seed kits, and solar water pumps in Banjulinding.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                            lineHeight = 15.sp
                        )
                    }
                    Button(
                        onClick = { viewModel.navigateTo("CommunitySupport") },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("Sponsor", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // --- DUGAMA BUNDLES SECTIONS ---
        item {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Dugama Bundles by Day 🗓️",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Authentic Gambian cooking meal kits with fresh chopped ingredients.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Days of the Week Selector
                var selectedDay by remember { mutableStateOf("Monday") }
                val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    items(daysOfWeek) { day ->
                        val isSelected = selectedDay == day
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedDay = day },
                            label = { Text(day, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                val filteredBundles = bundles.filter { it.bundleName.contains(selectedDay) }

                if (filteredBundles.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp)).padding(16.dp)) {
                        Text(
                            "No custom $selectedDay bundles created yet. Drag another day or check with admin details!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(filteredBundles) { bundle ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .width(260.dp)
                                    .clickable { viewModel.showBundleDetails(bundle) }
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                    ) {
                                        AsyncImageWithFallback(
                                            model = bundle.bundleImage,
                                            contentDescription = bundle.bundleName,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                .align(Alignment.BottomStart)
                                        ) {
                                            Text(
                                                text = "D${bundle.bundlePrice.toInt()}",
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = bundle.bundleName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Estimated Purpose: $selectedDay Family Meal Kit",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = bundle.description,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            lineHeight = 15.sp
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            OutlinedButton(
                                                onClick = { viewModel.showBundleDetails(bundle) },
                                                modifier = Modifier.weight(1f).height(36.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text("Ingredients", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Button(
                                                onClick = {
                                                    viewModel.addBundleToCartById(bundle)
                                                },
                                                modifier = Modifier.weight(1.3f).height(36.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text("Add Bundle", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

              // --- MARKET PREVIEW SECTION ---
        item {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Marketplace Selection 🥬",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                if (products.isEmpty()) {
                    Text(
                        "Loading fresh produce...",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 12.sp
                    )
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(products.take(4)) { product ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .width(135.dp)
                                    .clickable { viewModel.showProductDetails(product) }
                            ) {
                                Column {
                                    AsyncImageWithFallback(
                                        model = product.productImage,
                                        contentDescription = product.productName,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(95.dp)
                                    )
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = product.productName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "D${product.price.toInt()} / ${product.unit}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Button(
                                            onClick = { viewModel.addProductToCart(product.productId, 1) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(30.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Add,
                                                    contentDescription = "Add",
                                                    modifier = Modifier.size(12.dp),
                                                    tint = Color.White
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Add to Cart", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { viewModel.selectTab("Market") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(44.dp)
                            .testTag("view_more_market_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("View More Fresh Produce", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // --- FOR EVERYONE INFOGRAPHIC SECTION ---
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "For Everyone",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    InfoCard(
                        title = "For Buyers",
                        description = "Order market fresh items and get lightning fast delivery.",
                        iconEmoji = "🧺",
                        color = Color(0xFFE8F5E9),
                        modifier = Modifier.weight(1f)
                    )
                    InfoCard(
                        title = "For Gardeners",
                        description = "Sell produce directly from your garden & set your prices.",
                        iconEmoji = "👩‍🌾",
                        color = Color(0xFFFFF3E0),
                        modifier = Modifier.weight(1f)
                    )
                    InfoCard(
                        title = "For Vendors",
                        description = "Buy in bulk, manage dynamic dropshipping logs.",
                        iconEmoji = "🏪",
                        color = Color(0xFFE3F2FD),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // --- SUPPORT LOCAL FARMERS SECTION ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Support Local Farmers",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Buy local, grow local, grow Gambia. 100% of profit goes directly to community vendors & farmers.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.showToast("Gambia Community Seed Fund coming soon!") },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            Text("Support Now", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("🇬🇲", fontSize = 60.sp)
                }
            }
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    description: String,
    iconEmoji: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(155.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(iconEmoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(2.dp))
            Text(description, fontSize = 9.sp, color = Color.DarkGray, lineHeight = 11.sp)
        }
    }
}

private data class CategoryItem(val name: String, val emoji: String)

// --- SCREEN 2: MARKET GRID SCREEN ---

@Composable
fun MarketScreen(viewModel: DugamaViewModel) {
    val rawProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()

    // Filter products locally for search and category choice
    val products = remember(rawProducts, searchQuery, selectedCategory) {
        rawProducts.filter { prod ->
            val matchesSearch = prod.productName.contains(searchQuery, ignoreCase = true) ||
                prod.category.contains(searchQuery, ignoreCase = true) ||
                prod.sellerName.contains(searchQuery, ignoreCase = true)

            val matchesCategory = selectedCategory == "All" || prod.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Bar Area
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("search_bar"),
            placeholder = { Text("Search fresh vegetables, fish...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        // Categories Quick Buttons (Chips)
        val chips = listOf("All", "Vegetables", "Fruits", "Fish", "Spices", "Rice & Grains", "Bread", "Drinks", "Garden Products")
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chips) { chip ->
                val isSelected = selectedCategory == chip
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setCategoryFilter(chip) },
                    label = { Text(chip, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
                        enabled = true, selected = isSelected
                    ),
                    modifier = Modifier.testTag("chip_${chip.lowercase().replace(" ","_")}")
                )
            }
        }

        // Product Catalog Grid
        if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No products found",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Try adjusting your search filters.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("products_grid")
            ) {
                items(products) { product ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.showProductDetails(product) }
                            .testTag("product_card_${product.productId}")
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            ) {
                                AsyncImageWithFallback(
                                    model = product.productImage,
                                    contentDescription = product.productName,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .align(Alignment.TopStart)
                                ) {
                                    Text(
                                        text = "D${product.price.toInt()} / ${product.unit}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = product.productName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "by ${product.sellerName}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = product.location,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.addProductToCart(product.productId, 1) },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Add to Cart", modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 3: CART / CHECKOUT FLOW ---

@Composable
fun CheckoutScreen(viewModel: DugamaViewModel) {
    val cartWithProducts by viewModel.cartItemsWithProducts.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (cartWithProducts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🛒", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Your Checkout Cart is Empty",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Explore Gambian markets, buy fresh bundles and support local agriculture.",
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.selectTab("Market") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Go Shop Market", fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        var prefilledPhone by remember { mutableStateOf(currentUser?.phone ?: "") }
        var prefilledAddress by remember { mutableStateOf(currentUser?.address ?: "Banjulinding, West Coast") }
        var deliveryNotes by remember { mutableStateOf("") }
        var paymentMethod by remember { mutableStateOf("Cash on Delivery") }

        val availableLocations by viewModel.availableLocations.collectAsStateWithLifecycle()
        var selectedLocation by remember { mutableStateOf(availableLocations.firstOrNull() ?: "Banjulinding") }
        var expandedAddressDropdown by remember { mutableStateOf(false) }

        val subtotal = remember(cartWithProducts) {
            cartWithProducts.sumOf { it.first.quantity * it.second.price }
        }
        val deliveryFee = 50.0 // 50 Dalasi
        val grandTotal = subtotal + deliveryFee

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "My Cart",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Products in cart list
            items(cartWithProducts) { (cart, product) ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImageWithFallback(
                            model = product.productImage,
                            contentDescription = product.productName,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.productName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "D${product.price.toInt()} / ${product.unit}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // Quantity selector
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = { viewModel.updateCartItemQuantity(cart.cartId, cart.quantity - 1) },
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Minus", modifier = Modifier.size(12.dp))
                                }
                                Text(
                                    text = cart.quantity.toString(),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { viewModel.updateCartItemQuantity(cart.cartId, cart.quantity + 1) },
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Plus", modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                        // Total item sub-cost and delete icon
                        Column(horizontalAlignment = Alignment.End) {
                            IconButton(onClick = { viewModel.removeCartItem(cart.cartId) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f))
                            }
                            Text(
                                text = "D${(product.price * cart.quantity).toInt()}",
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // DELIVERY DETAILS FORM
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Delivery Details", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Delivery Location Community Zone", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { expandedAddressDropdown = true },
                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                            ) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text(selectedLocation, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                            DropdownMenu(
                                expanded = expandedAddressDropdown,
                                onDismissRequest = { expandedAddressDropdown = false }
                            ) {
                                availableLocations.forEach { loc ->
                                    DropdownMenuItem(
                                        text = { Text(loc, fontSize = 13.sp) },
                                        onClick = {
                                            selectedLocation = loc
                                            prefilledAddress = "$loc, West Coast"
                                            expandedAddressDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = prefilledAddress,
                            onValueChange = { prefilledAddress = it },
                            label = { Text("Detailed Delivery Street Address", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("delivery_address_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = prefilledPhone,
                            onValueChange = { prefilledPhone = it },
                            label = { Text("Phone Number", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("delivery_phone_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = deliveryNotes,
                            onValueChange = { deliveryNotes = it },
                            label = { Text("Delivery Instructions (Optional)", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // PAYMENT SELECTION
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Payment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Payment Options", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        val paymentMethods = listOf("Cash on Delivery", "Mobile Money")
                        paymentMethods.forEach { method ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { paymentMethod = method }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = paymentMethod == method,
                                    onClick = { paymentMethod = method },
                                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                )
                                Text(
                                    text = method,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // FINAL BILL SUMMARY PANEL
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Items Subtotal", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            Text("D${subtotal.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Standard Delivery", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            Text("D${deliveryFee.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Grand Total", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            Text("D${grandTotal.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // PLACE ORDER BUTTON
            item {
                Button(
                    onClick = {
                        if (prefilledPhone.isEmpty() || prefilledAddress.isEmpty()) {
                            viewModel.showToast("Please enter a valid Address and Phone Number")
                        } else {
                            viewModel.placeOrder(prefilledAddress, prefilledPhone, paymentMethod, context)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("place_order_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Place Order & Share on WhatsApp", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

// --- SCREEN 4: PROFILE SCREEN ---

@Composable
fun ProfileScreen(viewModel: DugamaViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Details Header
        item {
            currentUser?.let { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImageWithFallback(
                            model = user.profilePhoto,
                            contentDescription = user.fullName,
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user.fullName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Text(
                                text = user.phone,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Role: ${user.role}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }

        // ROLE SWITCHER BUTTONS (To simulate Buyer vs Seller vs Admin Views)
        item {
            Column {
                Text(
                    text = "App Control Center (Toggle Role)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val roles = listOf("Buyer", "Seller", "Admin")
                    roles.forEach { role ->
                        val isSelected = currentUser?.role == role
                        Button(
                            onClick = { viewModel.switchUserRole(role) },
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(role, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // SETTINGS & EXTRA MODULE LINKS
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column {
                    ProfileMenuRow(
                        icon = Icons.Default.Receipt,
                        title = "My Orders List",
                        onClick = { viewModel.navigateTo("MyOrdersScreen") }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Dynamic seller dashboard vs seller application link
                    if (currentUser?.role == "Seller" || currentUser?.role == "Gardener" || currentUser?.role == "Vendor") {
                        ProfileMenuRow(
                            icon = Icons.Default.Storefront,
                            title = "My Seller Dashboard",
                            onClick = { viewModel.navigateTo("SellerDashboard") }
                        )
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    } else if (currentUser?.role != "Admin") {
                        ProfileMenuRow(
                            icon = Icons.Default.AddBusiness,
                            title = "Become a Seller / Register Store",
                            onClick = { viewModel.navigateTo("BecomeSeller") }
                        )
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    }

                    if (currentUser?.role == "Admin") {
                        ProfileMenuRow(
                            icon = Icons.Default.AdminPanelSettings,
                            title = "Admin Dashboard panel",
                            onClick = { viewModel.navigateTo("AdminDashboard") }
                        )
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    }

                    ProfileMenuRow(
                        icon = Icons.Default.Settings,
                        title = "System Configuration Settings",
                        onClick = { viewModel.navigateTo("SystemConfig") }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    ProfileMenuRow(
                        icon = Icons.Default.Help,
                        title = "Help & Community Support",
                        onClick = { viewModel.navigateTo("CommunitySupport") }
                    )
                }
            }
        }

        // About app / Gambia indicator
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "DUGAMA APP MVP • V1.0.0",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "Designed for Brikama, Banjul, Bakau, and Serrekunda 🇬🇲",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(16.dp)
        )
    }
}

// --- SUB SCREEN: MY ORDERS HISTORY LIST ---

@Composable
fun MyOrdersScreen(viewModel: DugamaViewModel) {
    val orders by viewModel.userOrders.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Headers with back arrow
        Surface(
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "My Direct Orders",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        if (orders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📦", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("No Orders Placed Yet", fontWeight = FontWeight.Bold)
                    Text("Place food orders to check records here locally.", fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(orders) { order ->
                    var isExpanded by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = order.orderId,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = order.orderDate.split(" ").firstOrNull() ?: "",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Grand Total: D${order.totalAmount.toInt() + 50}", // including delivery D50
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Payment: ${order.paymentMethod}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (order.orderStatus == "Delivered") Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = order.orderStatus,
                                        color = if (order.orderStatus == "Delivered") Color(0xFF2E7D32) else Color(0xFFE65100),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // EXPANDED ORDER ITEMS DETAILS
                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(6.dp))

                                Text("Assigned Delivery Courier: Modou Lamin", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("Delivery Address: ${order.deliveryAddress}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(6.dp))

                                // Simple static progression bar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    StepperBullet("1. Pending", true)
                                    StepperBullet("2. Preparing", order.orderStatus in listOf("Preparing", "Out for Delivery", "Delivered"))
                                    StepperBullet("3. Delivered", order.orderStatus == "Delivered")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepperBullet(text: String, isPassed: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(if (isPassed) MaterialTheme.colorScheme.primary else Color.LightGray, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, fontSize = 10.sp, fontWeight = if (isPassed) FontWeight.Bold else FontWeight.Normal)
    }
}

// --- SUB SCREEN: BECOME A SELLER / REGISTER FORM ---

@Composable
fun BecomeSellerFormScreen(viewModel: DugamaViewModel) {
    var businessName by remember { mutableStateOf("") }
    var sellerType by remember { mutableStateOf("Gardener/Farmer") }
    var description by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+220 ") }
    var logoUrl by remember { mutableStateOf("") }
    val availableLocations by viewModel.availableLocations.collectAsStateWithLifecycle()
    var location by remember { mutableStateOf(availableLocations.firstOrNull() ?: "Banjulinding") }
    var locationDropdownExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(tonalElevation = 2.dp, color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text("Register Store", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(start = 8.dp))
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Text(
                    text = "Launch your marketplace store in Gambia",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Fill this simple, elder-friendly form to list products publicly instantly.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text("Farm/Business Name") },
                    modifier = Modifier.fillMaxWidth().testTag("seller_register_business_name"),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
            }

            item {
                Text("Seller Category Type (Choose One)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                val types = listOf("Gardener/Farmer", "Vendor")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    types.forEach { type ->
                        val isSel = sellerType == type
                        Button(
                            onClick = { sellerType = type },
                            modifier = Modifier.height(38.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(type, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Text("Select Farm Location Community Zone", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { locationDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(location, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(
                        expanded = locationDropdownExpanded,
                        onDismissRequest = { locationDropdownExpanded = false }
                    ) {
                        availableLocations.forEach { loc ->
                            DropdownMenuItem(
                                text = { Text(loc) },
                                onClick = {
                                    location = loc
                                    locationDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Custom Location / Landmark (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Phone number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Tell us brief bio/description of your farm") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(8.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = logoUrl,
                    onValueChange = { logoUrl = it },
                    label = { Text("Farm / Profile Image URL (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (businessName.isEmpty() || description.isEmpty() || phone.isEmpty()) {
                            viewModel.showToast("Please fill in all register details")
                        } else {
                            viewModel.registerSeller(businessName, sellerType, description, phone, location, logoUrl)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("seller_register_submit"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Register & Activate Store Panel", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- SUB SCREEN: PRODUCT DETAILS PAGE ---

@Composable
fun ProductDetailScreen(viewModel: DugamaViewModel) {
    val product by viewModel.selectedProduct.collectAsStateWithLifecycle()
    var qtySelected by remember { mutableStateOf(1) }

    product?.let { prod ->
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                AsyncImageWithFallback(
                    model = prod.productImage,
                    contentDescription = prod.productName,
                    modifier = Modifier.fillMaxSize()
                )
                // Back button overlay
                IconButton(
                    onClick = { viewModel.navigateBack() },
                    modifier = Modifier
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .align(Alignment.TopStart)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = prod.productName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "D${prod.price.toInt()} / ${prod.unit}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = prod.category,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Category Filter",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Seller Detail Block
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "Seller: ${prod.sellerName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Verified Status", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                }
                                Text(text = "Origin Garden: ${prod.location}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                item {
                    Text("Product Description", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = prod.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }

            // Bottom Sticky Quantity Selector & Add to Cart Broad Bar
            Surface(
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Select Quantity", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { if (qtySelected > 1) qtySelected-- },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrement")
                            }
                            Text(text = "$qtySelected ${prod.unit}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            IconButton(
                                onClick = { qtySelected++ },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increment")
                            }
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.addProductToCart(prod.productId, qtySelected)
                            viewModel.navigateBack()
                        },
                        modifier = Modifier
                            .height(46.dp)
                            .testTag("add_to_cart_detail_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add to Cart • D${(prod.price * qtySelected).toInt()}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- SUB SCREEN: BUNDLE DETAILS PAGE ---

@Composable
fun BundleDetailScreen(viewModel: DugamaViewModel) {
    val bundle by viewModel.selectedBundle.collectAsStateWithLifecycle()
    val bundleItems by viewModel.selectedBundleItems.collectAsStateWithLifecycle()

    bundle?.let { b ->
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImageWithFallback(
                    model = b.bundleImage,
                    contentDescription = b.bundleName,
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    onClick = { viewModel.navigateBack() },
                    modifier = Modifier
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .align(Alignment.TopStart)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    Column {
                        Text(
                            text = b.bundleName,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = b.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }

                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.List, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Included Fresh Ingredients:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                if (bundleItems.isEmpty()) {
                    item {
                        Text("Resolving package ingredients list...", fontSize = 11.sp, color = Color.Gray)
                    }
                } else {
                    items(bundleItems) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = item.productName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = "Standard required: ${item.quantity} packs", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(text = "Verified Fresh", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Bottom CTA broad button to buy entire food pack
            Surface(
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = { viewModel.addBundleToCart(b, bundleItems) },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("add_bundle_to_cart_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Add Full Bundle to Cart • D${b.bundlePrice.toInt()}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- SCREEN 5: SELLER/GARDENER DASHBOARD SCREEN ---

@Composable
fun SellerDashboardScreen(viewModel: DugamaViewModel) {
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val orders by viewModel.allOrdersForAdmin.collectAsStateWithLifecycle()
    val sellers by viewModel.allSellers.collectAsStateWithLifecycle()
    val sellerId by viewModel.loggedInSellerId.collectAsStateWithLifecycle()

    val currentSeller = remember(sellerId, sellers) { sellers.find { it.sellerId == sellerId } }
    val isPending = currentSeller?.verificationStatus == "Pending"

    var showUploadModal by remember { mutableStateOf(false) }

    // Forms states
    var prodName by remember { mutableStateOf("") }
    var prodCategory by remember { mutableStateOf("Vegetables") }
    var prodPrice by remember { mutableStateOf("") }
    var prodUnit by remember { mutableStateOf("kg") }
    var prodQty by remember { mutableStateOf("") }
    var prodDesc by remember { mutableStateOf("") }
    var prodLogoUrl by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(tonalElevation = 2.dp, color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.selectTab("Profile") }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Profile")
                }
                Text("Seller Portal Dashboard", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(start = 8.dp))
            }
        }

        if (showUploadModal) {
            // Dynamic item upload form inside
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text("Add Product to Market Catalog", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = MaterialTheme.colorScheme.secondary)
                }

                item {
                    OutlinedTextField(
                        value = prodName,
                        onValueChange = { prodName = it },
                        label = { Text("Product Name (e.g., Bitter Tomatoes)") },
                        modifier = Modifier.fillMaxWidth().testTag("add_product_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                item {
                    Text("Category Selection", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    val cats = listOf("Vegetables", "Fruits", "Fish", "Spices", "Rice & Grains", "Bread")
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        cats.forEach { cat ->
                            val selected = prodCategory == cat
                            Button(
                                onClick = { prodCategory = cat },
                                modifier = Modifier.height(34.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text(cat, fontSize = 11.sp)
                            }
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = prodPrice,
                            onValueChange = { prodPrice = it },
                            label = { Text("Price (Dalasi)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = prodUnit,
                            onValueChange = { prodUnit = it },
                            label = { Text("Sale Unit (e.g., kg, bag)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = prodQty,
                        onValueChange = { prodQty = it },
                        label = { Text("Quantity Available") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = prodDesc,
                        onValueChange = { prodDesc = it },
                        label = { Text("Product Long Description") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = prodLogoUrl,
                        onValueChange = { prodLogoUrl = it },
                        label = { Text("Photo Link URL (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                item {
                    Button(
                        onClick = {
                            if (prodName.isEmpty() || prodPrice.isEmpty() || prodQty.isEmpty() || prodDesc.isEmpty()) {
                                viewModel.showToast("Please fill all required upload fields")
                            } else {
                                viewModel.uploadProduct(
                                    prodName, prodCategory, prodPrice.toDoubleOrNull() ?: 1.0,
                                    prodUnit, prodQty.toIntOrNull() ?: 1, prodDesc, prodLogoUrl
                                )
                                showUploadModal = false
                                // Reset form
                                prodName = ""
                                prodPrice = ""
                                prodQty = ""
                                prodDesc = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_uploaded_product_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add Product to Market Live", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(onClick = { showUploadModal = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Cancel Upload", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Seller status analytics
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (isPending) {
                                Text("Store Status: Pending Admin Registry Approval 🟡", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Your farm profile is saved under review. An Admin must verify your profile before your items show up for public home shoppers.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer, lineHeight = 15.sp)
                            } else {
                                Text("Store Status: Verified & Live 🟢", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Ecosystem Earnings", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                    Text("D1,450", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.secondary)
                                }
                                Column {
                                    Text("Pending Orders", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                    Text("${orders.size} active", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.tertiary)
                                }
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = { showUploadModal = true },
                        modifier = Modifier.fillMaxWidth().height(46.dp).testTag("add_item_for_sale_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload New Product For Sale", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                item {
                    Text("My Public Inventory Products", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.secondary)
                }

                // Filter down products matching seller ID
                val sellerProds = products.filter { it.sellerId == sellerId }
                if (sellerProds.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(100.dp).border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Your public store inventory list is empty.", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                } else {
                    items(sellerProds) { prod ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImageWithFallback(
                                model = prod.productImage,
                                contentDescription = prod.productName,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prod.productName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Price: D${prod.price.toInt()} / ${prod.unit} • Qty available: ${prod.quantityAvailable}", fontSize = 11.sp, color = Color.Gray)
                            }
                            IconButton(onClick = { viewModel.showToast("Product edits available on next deployment.") }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Item", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 6: ADMIN DASHBOARD CONTROL SCREEN ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(viewModel: DugamaViewModel) {
    val orders by viewModel.allOrdersForAdmin.collectAsStateWithLifecycle()
    val sellers by viewModel.allSellers.collectAsStateWithLifecycle()
    val locations by viewModel.availableLocations.collectAsStateWithLifecycle()
    val activeAnnouncement by viewModel.activeAnnouncement.collectAsStateWithLifecycle()
    val terms by viewModel.sellerTerms.collectAsStateWithLifecycle()
    val donations by viewModel.supportDonations.collectAsStateWithLifecycle()

    var activeAdminTab by remember { mutableStateOf("Orders") }
    val tabs = listOf("Orders", "Gardeners", "Announcements", "Zones", "Add Product", "Bundles", "Donations Ledger")

    // --- FORM STATES FOR ADMIN PRODUCT ADDING ---
    var adminProdName by remember { mutableStateOf("") }
    var adminProdCat by remember { mutableStateOf("Vegetables") }
    var adminProdPrice by remember { mutableStateOf("") }
    var adminProdUnit by remember { mutableStateOf("kg") }
    var adminProdQty by remember { mutableStateOf("") }
    var adminProdDesc by remember { mutableStateOf("") }
    var adminProdImage by remember { mutableStateOf("") }
    var adminProdSellerId by remember { mutableStateOf("") }

    // --- FORM STATES FOR ADMIN BUNDLE ADDING ---
    var adminBundleName by remember { mutableStateOf("") }
    var adminBundlePrice by remember { mutableStateOf("") }
    var adminBundleDesc by remember { mutableStateOf("") }
    var adminBundleImage by remember { mutableStateOf("") }
    var adminBundleItemsText by remember { mutableStateOf("Bitter Tomato x1, Big Eggplant x2, Fresh Onion x3") }

    // --- OTHER ADMIN FORM STATES ---
    var announcementInput by remember { mutableStateOf(activeAnnouncement ?: "") }
    var termsInput by remember { mutableStateOf(terms) }
    var newLocationInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(tonalElevation = 2.dp, color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.selectTab("Profile") }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Profile")
                    }
                    Text("Central Admin Portal", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(start = 8.dp))
                }
                
                // Tabs Row
                ScrollableTabRow(
                    selectedTabIndex = tabs.indexOf(activeAdminTab),
                    edgePadding = 12.dp,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    tabs.forEach { tab ->
                        Tab(
                            selected = activeAdminTab == tab,
                            onClick = { activeAdminTab = tab },
                            text = { Text(tab, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Analytics block card is placed globally in Admin Portal
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdminStatCard(title = "Gross Sales", value = "D${orders.sumOf { it.totalAmount.toInt() }}", color = Color(0xFFE8F5E9), modifier = Modifier.weight(1f))
                    AdminStatCard(title = "Ecosystem Orders", value = "${orders.size} active", color = Color(0xFFE3F2FD), modifier = Modifier.weight(1f))
                    AdminStatCard(title = "Registered Stores", value = "${sellers.size} sellers", color = Color(0xFFFFF3E0), modifier = Modifier.weight(1f))
                }
            }

            when (activeAdminTab) {
                "Orders" -> {
                    item {
                        Text("Global Order Dispatch Controller", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                    if (orders.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                Text("No active orders in local database.", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    } else {
                        items(orders) { order ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(order.orderId, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                                        Box(modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                            Text(order.orderStatus, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Customer: ${order.buyerName} • Pay Method: ${order.paymentMethod}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Address: ${order.deliveryAddress}", fontSize = 11.sp, color = Color.Gray)
                                    Text("Total Amount: D${order.totalAmount.toInt() + 50}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { viewModel.updateOrderDispatchState(order.orderId, "Preparing", "In Transit") },
                                            modifier = Modifier.weight(1f).height(32.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("Mark Preparing", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = { viewModel.updateOrderDispatchState(order.orderId, "Delivered", "Completed") },
                                            modifier = Modifier.weight(1f).height(32.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("Mark Delivered", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "Gardeners" -> {
                    item {
                        Text("Verify & Approve Registered Gardeners", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                    if (sellers.isEmpty()) {
                        item {
                            Text("No suppliers registered yet.", fontSize = 12.sp, color = Color.Gray)
                        }
                    } else {
                        items(sellers) { seller ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text(seller.businessName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Type: ${seller.sellerType} • Location: ${seller.location}", fontSize = 11.sp, color = Color.Gray)
                                            Text("Phone: ${seller.phone}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(if (seller.verificationStatus == "Verified") Color(0xFFE8F5E9) else Color(0xFFFFFDE7), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(seller.verificationStatus, color = if (seller.verificationStatus == "Verified") Color(0xFF2E7D32) else Color(0xFFF57F17), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    if (seller.verificationStatus == "Pending") {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = { viewModel.approveSellerVerification(seller.sellerId) },
                                            modifier = Modifier.fillMaxWidth().height(36.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text("Approve & Open Gardener Registry", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Sellers Terms and Conditions registry text", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = termsInput,
                            onValueChange = { termsInput = it },
                            label = { Text("App Terms of becoming a Seller") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 5,
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.updateSellerTerms(termsInput) },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Save Terms & Conditions", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                "Announcements" -> {
                    item {
                        Text("Broadcast Notice / Announcements Banner Board", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Current Active Alert Announcement:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(activeAnnouncement ?: "No custom global announcement active.", fontSize = 11.sp, color = Color.DarkGray)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = announcementInput,
                            onValueChange = { announcementInput = it },
                            label = { Text("Broadcasting Announcement Text") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    if (announcementInput.isNotBlank()) {
                                        viewModel.postGlobalAnnouncement(announcementInput)
                                    } else {
                                        viewModel.showToast("Please write a custom statement to post")
                                    }
                                },
                                modifier = Modifier.weight(1f).height(42.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Deploy Banner Notice", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    viewModel.postGlobalAnnouncement(null)
                                    announcementInput = ""
                                },
                                modifier = Modifier.weight(1f).height(42.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) {
                                Text("Clear Banner Notice", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                "Zones" -> {
                    item {
                        Text("Operating Location / Delivery Hub Zones", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            locations.forEach { loc ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(loc, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    IconButton(
                                        onClick = {
                                            if (locations.size <= 1) {
                                                viewModel.showToast("Gambia must keep at least 1 central operating location zone!")
                                            } else {
                                                viewModel.removeLocation(loc)
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Hub", tint = Color.Red)
                                    }
                                }
                                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Inaugurate New Delivery Hub Zone", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = newLocationInput,
                            onValueChange = { newLocationInput = it },
                            label = { Text("Hub Name (e.g., Banjul, Serrekunda, Bakau)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (newLocationInput.isNotBlank()) {
                                    viewModel.addNewLocation(newLocationInput)
                                    newLocationInput = ""
                                } else {
                                    viewModel.showToast("Please enter a valid hub zone location name")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Launch Hub Zone", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                "Add Product" -> {
                    item {
                        Text("Upload Direct Product Live to Market", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                    item {
                        OutlinedTextField(
                            value = adminProdName,
                            onValueChange = { adminProdName = it },
                            label = { Text("Product Name (Bitter Tomatoes, Peppers)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    item {
                        Text("Select Category", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        val cats = listOf("Vegetables", "Fruits", "Fish", "Spices", "Rice & Grains", "Bread")
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            cats.forEach { cat ->
                                val selected = adminProdCat == cat
                                Button(
                                    onClick = { adminProdCat = cat },
                                    modifier = Modifier.height(34.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    Text(cat, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = adminProdPrice,
                                onValueChange = { adminProdPrice = it },
                                label = { Text("Price (D)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = adminProdUnit,
                                onValueChange = { adminProdUnit = it },
                                label = { Text("Unit (kg, bag)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = adminProdQty,
                            onValueChange = { adminProdQty = it },
                            label = { Text("Stock Quantity Available") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = adminProdDesc,
                            onValueChange = { adminProdDesc = it },
                            label = { Text("Product Description details") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = adminProdImage,
                            onValueChange = { adminProdImage = it },
                            label = { Text("Photo URL Link (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    item {
                        Text("Select Local Gardener / Seller Supplier", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        val activeSellers = sellers.filter { it.verificationStatus == "Verified" }
                        if (activeSellers.isEmpty()) {
                            Text("No verified local sellers currently. New sellers will register through profile.", fontSize = 11.sp, color = Color.Red)
                        } else {
                            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                activeSellers.forEach { sel ->
                                    val isSel = adminProdSellerId == sel.sellerId
                                    Button(
                                        onClick = { adminProdSellerId = sel.sellerId },
                                        modifier = Modifier.height(34.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSel) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    ) {
                                        Text(sel.businessName, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Button(
                            onClick = {
                                val sObj = sellers.find { it.sellerId == adminProdSellerId } ?: sellers.firstOrNull()
                                if (adminProdName.isBlank() || adminProdPrice.isBlank() || adminProdQty.isBlank()) {
                                    viewModel.showToast("Fill in all product fields to launch")
                                } else if (sObj == null) {
                                    viewModel.showToast("Verification requested: Need a local supplier to fulfill orders!")
                                } else {
                                    viewModel.adminAddProduct(
                                        adminProdName,
                                        adminProdCat,
                                        adminProdPrice.toDoubleOrNull() ?: 50.0,
                                        adminProdUnit,
                                        adminProdQty.toIntOrNull() ?: 10,
                                        adminProdDesc,
                                        adminProdImage,
                                        sObj.sellerId,
                                        sObj.businessName,
                                        sObj.location
                                    )
                                    // Reset fields
                                    adminProdName = ""
                                    adminProdPrice = ""
                                    adminProdQty = ""
                                    adminProdDesc = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Deploy Live Product catalog item", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                "Bundles" -> {
                    item {
                        Text("Create Custom Dynamic Food Bundles / Meal Kits", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                    item {
                        OutlinedTextField(
                            value = adminBundleName,
                            onValueChange = { adminBundleName = it },
                            label = { Text("Bundle/Meal Kit Name (e.g., Jollof Kit)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = adminBundlePrice,
                            onValueChange = { adminBundlePrice = it },
                            label = { Text("Bundle Price (D)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = adminBundleDesc,
                            onValueChange = { adminBundleDesc = it },
                            label = { Text("Description (What delicious food does this make?)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = adminBundleImage,
                            onValueChange = { adminBundleImage = it },
                            label = { Text("Photo URL Image (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = adminBundleItemsText,
                            onValueChange = { adminBundleItemsText = it },
                            label = { Text("Ingredients List (Comma-separated name x quantity)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Syntax format: Tomato x2, Pepper x1, Lemon x4", fontSize = 10.sp, color = Color.Gray)
                    }
                    item {
                        Button(
                            onClick = {
                                if (adminBundleName.isBlank() || adminBundlePrice.isBlank() || adminBundleItemsText.isBlank()) {
                                    viewModel.showToast("Ensure you fill all meal bundle fields")
                                } else {
                                    val parsedItems = adminBundleItemsText.split(",").mapNotNull { raw ->
                                        val parts = raw.split(" x")
                                        if (parts.size == 2) {
                                            val name = parts[0].trim()
                                            val qty = parts[1].trim().toIntOrNull() ?: 1
                                            Pair(name, qty)
                                        } else null
                                    }
                                    viewModel.adminAddBundle(
                                        adminBundleName,
                                        adminBundlePrice.toDoubleOrNull() ?: 200.0,
                                        adminBundleDesc,
                                        adminBundleImage,
                                        parsedItems
                                    )
                                    // Reset form
                                    adminBundleName = ""
                                    adminBundlePrice = ""
                                    adminBundleDesc = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Assemble & Publish Meal Kit Bundle", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                "Donations Ledger" -> {
                    item {
                        Text("Community Support Sponsoring Ledger", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Tracks agricultural sponsorship pledges received from Gambia ecosystem subscribers.", fontSize = 11.sp, color = Color.Gray, lineHeight = 15.sp)
                    }
                    if (donations.isEmpty()) {
                        item {
                            Text("No farmer pledges registered yet.", fontSize = 12.sp, color = Color.Gray)
                        }
                    } else {
                        items(donations) { don ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(don.donorName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Box(modifier = Modifier.background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                            Text("D${don.amount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Tier: ${don.tier}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    Text("Phone Payment Ref: ${don.phoneNumber}", fontSize = 11.sp, color = Color.DarkGray)
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Payment: ${don.paymentMethod}", fontSize = 10.sp, color = Color.Gray)
                                        Text(don.dateAdded.split(" ").firstOrNull() ?: "", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SUB SCREEN: COMMUNITY SUPPORT & FARMS DONATION ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunitySupportScreen(viewModel: DugamaViewModel) {
    var donorName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("+220 ") }
    var selectedTier by remember { mutableStateOf("Weekly Water Pump Fuel (D500)") }
    var selectedAmount by remember { mutableStateOf(500.0) }
    var paymentMethod by remember { mutableStateOf("Wave Mobile Money") }

    val tiers = listOf(
        Triple("Daily Seeds Bag", 100.0, "Provides fresh cabbage & pepper seeds to young gardeners."),
        Triple("Weekly Water Pump Fuel", 500.0, "Sponsors solar water pump running fuel and maintenance in Banjulinding."),
        Triple("Monthly Co-op Sponsoring", 2000.0, "Enables cooling storage stands to prevent tomato and vegetable loss.")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(tonalElevation = 2.dp, color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text("Support Local Farming", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(start = 8.dp))
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // How your support is used educational card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌱", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("How Your Support is Used", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "100% of community subscriptions and donations go directly to funding water infrastructure, organic fertilizers, seeds, and cooling storage for our smallholder gardeners and vendors in Banjulinding, Lamin, Yundum, and Busumbala.\n\nSponsoring helps secure food self-sufficiency in Gambia!",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Billing Form Header
            item {
                Text("Become an Agricultural Sponsor", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary)
            }

            // Sponsor Name input
            item {
                OutlinedTextField(
                    value = donorName,
                    onValueChange = { donorName = it },
                    label = { Text("Sponsor / Donor Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Sponsor Phone (for Wave confirmation check)
            item {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Wave / Mobile Money Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Select Support Level Tiers
            item {
                Text("Select Support Level Tier", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tiers.forEach { (name, amount, details) ->
                        val isSelected = selectedAmount == amount
                        Card(
                            onClick = {
                                selectedTier = "$name (D${amount.toInt()})"
                                selectedAmount = amount
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                            colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        selectedTier = "$name (D${amount.toInt()})"
                                        selectedAmount = amount
                                    }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("$name — D${amount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(details, fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }

            // Payment Option Select
            item {
                Text("Choose Payment Method", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                val payMethods = listOf("Wave Mobile Money", "QMoney", "Cash on Delivery")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    payMethods.forEach { method ->
                        val isSel = paymentMethod == method
                        Button(
                            onClick = { paymentMethod = method },
                            modifier = Modifier.weight(1f).height(38.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(method.replace(" Mobile Money", ""), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Submit Button
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (donorName.isBlank() || phoneNumber.isBlank()) {
                            viewModel.showToast("Please enter your name and transaction phone number")
                        } else {
                            viewModel.submitDonation(donorName, selectedTier, selectedAmount, paymentMethod, phoneNumber)
                            viewModel.navigateBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Process Support Pledge (D${selectedAmount.toInt()})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(84.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontSize = 9.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
        }
    }
}

// --- SUB SCREEN: SELLER PROFILE DETAILS ---

@Composable
fun SellerProfileScreen(viewModel: DugamaViewModel) {
    val activeId by viewModel.activeSellerIdForProfile.collectAsStateWithLifecycle()
    val allSellersFlow by viewModel.allSellers.collectAsStateWithLifecycle()
    val productsFlow by viewModel.allProducts.collectAsStateWithLifecycle()

    val sellerObj = remember(activeId, allSellersFlow) {
        allSellersFlow.find { it.sellerId == activeId }
    }

    val sellerProducts = remember(activeId, productsFlow) {
        productsFlow.filter { it.sellerId == activeId }
    }

    sellerObj?.let { seller ->
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(tonalElevation = 2.dp, color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text("Gardener Profile", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(start = 8.dp))
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with image
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImageWithFallback(
                            model = seller.logo,
                            contentDescription = seller.businessName,
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(seller.businessName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(seller.location, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }

                // Contact number & phone
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Bio & General Status", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(seller.description, fontSize = 12.sp, color = Color.DarkGray, lineHeight = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Contact Number: ${seller.phone}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Public products lists of this Seller
                item {
                    Text("Fresh Crops for Sale", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.secondary)
                }

                if (sellerProducts.isEmpty()) {
                    item {
                        Text("No crops for sale right now.", fontSize = 11.sp, color = Color.Gray)
                    }
                } else {
                    items(sellerProducts) { product ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.showProductDetails(product) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                AsyncImageWithFallback(
                                    model = product.productImage,
                                    contentDescription = product.productName,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(product.productName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("D${product.price.toInt()} / ${product.unit}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                        .clickable { viewModel.addProductToCart(product.productId, 1) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Item", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helpers for size constraints preventing compile warn
fun Modifier.spaceMinSize(dp: androidx.compose.ui.unit.Dp) = this.defaultMinSize(minWidth = dp, minHeight = dp)

@Composable
fun SystemConfigScreen(viewModel: DugamaViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var profilePhoto by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("••••••••") }

    // Initialize values when currentUser loads
    LaunchedEffect(currentUser) {
        currentUser?.let {
            name = it.fullName
            phone = it.phone
            location = it.address
            profilePhoto = it.profilePhoto
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(tonalElevation = 2.dp, color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text("System Configuration Settings", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(start = 8.dp))
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    AsyncImageWithFallback(
                        model = profilePhoto,
                        contentDescription = "Profile Photo",
                        modifier = Modifier.size(90.dp).clip(CircleShape).border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Change photo by entering an image URL below", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            item {
                OutlinedTextField(
                    value = profilePhoto,
                    onValueChange = { profilePhoto = it },
                    label = { Text("Profile Photo URL") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth().testTag("config_full_name"),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth().testTag("config_phone"),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Primary Location (Community / Address)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Change Password Settings") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (name.isBlank() || phone.isBlank() || location.isBlank()) {
                            viewModel.showToast("Please fill in essential profile fields.")
                        } else {
                            // Update user model securely
                            viewModel.updateUserProfile(name, phone, location, profilePhoto)
                            viewModel.showToast("Configuration saved successfully!")
                            viewModel.navigateBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_config_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Configuration", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

