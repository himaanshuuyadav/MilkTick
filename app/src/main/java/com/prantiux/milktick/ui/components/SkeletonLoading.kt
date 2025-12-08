package com.prantiux.milktick.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SkeletonLoadingBox(
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 20.dp,
    cornerRadius: androidx.compose.ui.unit.Dp = 8.dp
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 300f, 0f),
        end = Offset(translateAnim, 0f)
    )

    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush)
    )
}

// Home Screen Skeleton Components
@Composable
fun SkeletonHomeWelcomeCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SkeletonLoadingBox(
                modifier = Modifier.fillMaxWidth(0.6f),
                height = 28.dp,
                cornerRadius = 12.dp
            )
            SkeletonLoadingBox(
                modifier = Modifier.fillMaxWidth(0.4f),
                height = 16.dp,
                cornerRadius = 8.dp
            )
        }
    }
}

@Composable
fun SkeletonHomeMilkCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonLoadingBox(
                    modifier = Modifier.fillMaxWidth(0.6f),
                    height = 20.dp
                )
                SkeletonLoadingBox(
                    modifier = Modifier.size(40.dp),
                    height = 40.dp,
                    cornerRadius = 20.dp
                )
            }
            
            // Toggle row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SkeletonLoadingBox(
                        modifier = Modifier.width(150.dp),
                        height = 16.dp
                    )
                    SkeletonLoadingBox(
                        modifier = Modifier.width(120.dp),
                        height = 14.dp
                    )
                }
                SkeletonLoadingBox(
                    modifier = Modifier.size(48.dp, 24.dp),
                    height = 24.dp,
                    cornerRadius = 12.dp
                )
            }
            
            // Input fields area
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SkeletonLoadingBox(
                    modifier = Modifier.fillMaxWidth(),
                    height = 56.dp,
                    cornerRadius = 8.dp
                )
                SkeletonLoadingBox(
                    modifier = Modifier.fillMaxWidth(),
                    height = 80.dp,
                    cornerRadius = 8.dp
                )
            }
            
            // Save button
            SkeletonLoadingBox(
                modifier = Modifier.fillMaxWidth(),
                height = 56.dp,
                cornerRadius = 16.dp
            )
        }
    }
}

// Rate Screen Skeleton Components
@Composable
fun SkeletonRateHeaderCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SkeletonLoadingBox(
                modifier = Modifier.size(48.dp),
                height = 48.dp,
                cornerRadius = 12.dp
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonLoadingBox(
                    modifier = Modifier.width(140.dp),
                    height = 20.dp
                )
                SkeletonLoadingBox(
                    modifier = Modifier.width(180.dp),
                    height = 16.dp
                )
            }
        }
    }
}

@Composable
fun SkeletonRateFormCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonLoadingBox(
                    modifier = Modifier.width(100.dp),
                    height = 20.dp
                )
                SkeletonLoadingBox(
                    modifier = Modifier.size(40.dp),
                    height = 40.dp,
                    cornerRadius = 20.dp
                )
            }
            
            // Month/Year card
            SkeletonLoadingBox(
                modifier = Modifier.fillMaxWidth(),
                height = 56.dp,
                cornerRadius = 12.dp
            )
            
            // Form fields
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                repeat(2) {
                    SkeletonLoadingBox(
                        modifier = Modifier.fillMaxWidth(),
                        height = 56.dp,
                        cornerRadius = 8.dp
                    )
                }
            }
            
            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(2) {
                    SkeletonLoadingBox(
                        modifier = Modifier.weight(1f),
                        height = 56.dp,
                        cornerRadius = 16.dp
                    )
                }
            }
        }
    }
}

// Records Screen Skeleton (Updated)
@Composable
fun SkeletonRecordsMonthCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonLoadingBox(
                    modifier = Modifier.width(80.dp),
                    height = 16.dp
                )
                SkeletonLoadingBox(
                    modifier = Modifier.width(120.dp),
                    height = 14.dp
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonLoadingBox(
                    modifier = Modifier.width(60.dp),
                    height = 16.dp
                )
                SkeletonLoadingBox(
                    modifier = Modifier.size(16.dp),
                    height = 16.dp,
                    cornerRadius = 8.dp
                )
            }
        }
    }
}

// Summary Screen Skeleton Components (Updated to match layout)
@Composable
fun SkeletonSummaryMonthCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SkeletonLoadingBox(
                modifier = Modifier.size(48.dp),
                height = 48.dp,
                cornerRadius = 12.dp
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonLoadingBox(
                    modifier = Modifier.width(120.dp),
                    height = 16.dp
                )
                SkeletonLoadingBox(
                    modifier = Modifier.width(100.dp),
                    height = 24.dp
                )
            }
        }
    }
}

@Composable
fun SkeletonSummaryStatsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Days card
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonLoadingBox(
                    modifier = Modifier.width(40.dp),
                    height = 16.dp
                )
                SkeletonLoadingBox(
                    modifier = Modifier.width(20.dp),
                    height = 32.dp
                )
                SkeletonLoadingBox(
                    modifier = Modifier.width(80.dp),
                    height = 14.dp
                )
            }
        }
        
        // Total card
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonLoadingBox(
                    modifier = Modifier.width(40.dp),
                    height = 16.dp
                )
                SkeletonLoadingBox(
                    modifier = Modifier.width(50.dp),
                    height = 32.dp
                )
                SkeletonLoadingBox(
                    modifier = Modifier.width(80.dp),
                    height = 14.dp
                )
            }
        }
    }
}

@Composable
fun SkeletonSummaryTotalAmountCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SkeletonLoadingBox(
                modifier = Modifier.size(32.dp),
                height = 32.dp,
                cornerRadius = 16.dp
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonLoadingBox(
                    modifier = Modifier.width(100.dp),
                    height = 16.dp
                )
                SkeletonLoadingBox(
                    modifier = Modifier.width(120.dp),
                    height = 32.dp
                )
            }
        }
    }
}

@Composable
fun SkeletonSummaryQuickStats() {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SkeletonLoadingBox(
            modifier = Modifier.width(120.dp),
            height = 20.dp
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(2) { index ->
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SkeletonLoadingBox(
                            modifier = Modifier.size(24.dp),
                            height = 24.dp,
                            cornerRadius = 12.dp
                        )
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            SkeletonLoadingBox(
                                modifier = Modifier.width(if (index == 0) 70.dp else 60.dp),
                                height = 14.dp
                            )
                            SkeletonLoadingBox(
                                modifier = Modifier.width(if (index == 0) 40.dp else 30.dp),
                                height = 18.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SkeletonSummaryListItem() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonLoadingBox(
                    modifier = Modifier.size(40.dp),
                    height = 40.dp,
                    cornerRadius = 20.dp
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SkeletonLoadingBox(
                        modifier = Modifier.width(80.dp),
                        height = 16.dp
                    )
                    SkeletonLoadingBox(
                        modifier = Modifier.width(60.dp),
                        height = 14.dp
                    )
                }
            }
            
            SkeletonLoadingBox(
                modifier = Modifier.size(24.dp),
                height = 24.dp,
                cornerRadius = 12.dp
            )
        }
    }
}
