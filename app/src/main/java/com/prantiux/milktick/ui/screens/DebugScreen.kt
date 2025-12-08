package com.prantiux.milktick.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.prantiux.milktick.viewmodel.AuthViewModel
import com.prantiux.milktick.viewmodel.DebugViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    debugViewModel: DebugViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val debugState by debugViewModel.debugState.collectAsState()
    val context = LocalContext.current
    
    // Log debug state changes
    LaunchedEffect(debugState) {
        android.util.Log.d("DebugScreen", "Debug state changed: isLoading=${debugState.isLoading}, message=${debugState.message}")
    }
    
    // Auto-clear debug messages after 10 seconds
    LaunchedEffect(debugState.message) {
        debugState.message?.let {
            kotlinx.coroutines.delay(10000)
            debugViewModel.clearDebugMessage()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔧 Debug Options") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "🔧 Development Debug Tools",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Test database connectivity and functionality",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            // User Info Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "👤 Current User",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = "User ID: ${currentUser?.uid ?: "Not logged in"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Email: ${currentUser?.email ?: "No email"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Debug Tests Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "🧪 Database Tests",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    DebugTestItem(
                        icon = Icons.Default.CloudSync,
                        title = "Test Firebase Connection",
                        subtitle = "Check if Firebase is working properly",
                        isLoading = debugState.isLoading,
                        onClick = { 
                            android.util.Log.d("DebugScreen", "Firebase connection test clicked")
                            android.widget.Toast.makeText(context, "Testing Firebase Connection...", android.widget.Toast.LENGTH_SHORT).show()
                            debugViewModel.testFirestoreConnection() 
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    DebugTestItem(
                        icon = Icons.Default.AccountCircle,
                        title = "Check Authentication",
                        subtitle = "Verify user authentication status",
                        isLoading = debugState.isLoading,
                        onClick = { 
                            android.util.Log.d("DebugScreen", "Authentication test clicked")
                            android.widget.Toast.makeText(context, "Checking Authentication...", android.widget.Toast.LENGTH_SHORT).show()
                            debugViewModel.testAuthenticationStatus() 
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    DebugTestItem(
                        icon = Icons.Default.Save,
                        title = "Test Save Milk Entry",
                        subtitle = "Test saving daily milk data",
                        isLoading = debugState.isLoading,
                        onClick = { 
                            android.util.Log.d("DebugScreen", "Save milk entry test clicked")
                            android.widget.Toast.makeText(context, "Testing Milk Entry Save...", android.widget.Toast.LENGTH_SHORT).show()
                            currentUser?.uid?.let { userId ->
                                android.util.Log.d("DebugScreen", "User ID: $userId")
                                debugViewModel.testSaveMilkEntry(userId)
                            } ?: run {
                                android.util.Log.e("DebugScreen", "No user ID available")
                                android.widget.Toast.makeText(context, "No user logged in!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    DebugTestItem(
                        icon = Icons.Default.AttachMoney,
                        title = "Test Save Monthly Rate",
                        subtitle = "Test saving rate and default quantity",
                        isLoading = debugState.isLoading,
                        onClick = { 
                            android.widget.Toast.makeText(context, "Testing Monthly Rate Save...", android.widget.Toast.LENGTH_SHORT).show()
                            currentUser?.uid?.let { userId ->
                                debugViewModel.testSaveMonthlyRate(userId)
                            } ?: run {
                                android.widget.Toast.makeText(context, "No user logged in!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    DebugTestItem(
                        icon = Icons.Default.Download,
                        title = "Test Load Data",
                        subtitle = "Test loading existing data from database",
                        isLoading = debugState.isLoading,
                        onClick = { 
                            android.widget.Toast.makeText(context, "Testing Data Load...", android.widget.Toast.LENGTH_SHORT).show()
                            currentUser?.uid?.let { userId ->
                                debugViewModel.testLoadData(userId)
                            } ?: run {
                                android.widget.Toast.makeText(context, "No user logged in!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    DebugTestItem(
                        icon = Icons.Default.Delete,
                        title = "Test Delete Permissions",
                        subtitle = "Test if delete operations are allowed by Firestore rules",
                        isLoading = debugState.isLoading,
                        onClick = { 
                            android.widget.Toast.makeText(context, "Testing Delete Permissions...", android.widget.Toast.LENGTH_SHORT).show()
                            currentUser?.uid?.let { userId ->
                                android.util.Log.d("DebugScreen", "Testing delete permissions for user: $userId")
                                debugViewModel.testDeletePermissions(userId)
                            } ?: run {
                                android.widget.Toast.makeText(context, "No user logged in!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
            
            // Results Section
            debugState.message?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "📋 Test Result",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            // Instructions
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "💡 Instructions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "1. Start with 'Check Authentication'\n2. Then 'Test Firebase Connection'\n3. Test individual save functions\n4. Check results below each test\n5. Green ✅ = Success, Red ❌ = Error",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DebugTestItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Surface(
            onClick = if (!isLoading) {
                {
                    android.util.Log.d("DebugTestItem", "Clicked: $title")
                    onClick()
                }
            } else { {} },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = if (isLoading) MaterialTheme.colorScheme.onSurfaceVariant 
                           else MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
