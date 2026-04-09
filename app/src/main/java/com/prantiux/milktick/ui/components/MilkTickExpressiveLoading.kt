package com.prantiux.milktick.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MilkTickExpressiveLoader(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ContainedLoadingIndicator(modifier = Modifier.size(56.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MilkTickSkeletonCardList(
    count: Int = 3,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(count) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp)
                            )
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.55f)
                                .height(14.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(7.dp)
                                )
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.35f)
                                .height(12.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SkeletonBlock(
    modifier: Modifier,
    alpha: Float = 0.6f
) {
    Box(
        modifier = modifier.background(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha),
            shape = RoundedCornerShape(8.dp)
        )
    )
}

@Composable
fun MilkTickHomeEntrySkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonBlock(modifier = Modifier.fillMaxWidth(0.46f).height(16.dp))
                SkeletonBlock(modifier = Modifier.fillMaxWidth(0.34f).height(12.dp), alpha = 0.45f)
            }
            SkeletonBlock(modifier = Modifier.size(44.dp))
        }
        SkeletonBlock(modifier = Modifier.fillMaxWidth().height(56.dp))
        SkeletonBlock(modifier = Modifier.fillMaxWidth().height(56.dp), alpha = 0.45f)
    }
}

@Composable
fun MilkTickRateEditorSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SkeletonBlock(modifier = Modifier.fillMaxWidth(0.4f).height(18.dp))
                    SkeletonBlock(modifier = Modifier.size(36.dp))
                }
                SkeletonBlock(modifier = Modifier.fillMaxWidth().height(48.dp), alpha = 0.45f)
                SkeletonBlock(modifier = Modifier.fillMaxWidth().height(56.dp))
                SkeletonBlock(modifier = Modifier.fillMaxWidth().height(56.dp), alpha = 0.45f)
                SkeletonBlock(modifier = Modifier.fillMaxWidth().height(50.dp), alpha = 0.5f)
            }
        }
    }
}

@Composable
fun MilkTickRecordsMonthListSkeleton(
    count: Int = 12,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(count) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SkeletonBlock(modifier = Modifier.fillMaxWidth(0.42f).height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SkeletonBlock(modifier = Modifier.size(width = 48.dp, height = 12.dp), alpha = 0.45f)
                            SkeletonBlock(modifier = Modifier.size(width = 4.dp, height = 4.dp), alpha = 0.35f)
                            SkeletonBlock(modifier = Modifier.size(width = 52.dp, height = 12.dp), alpha = 0.45f)
                        }
                    }
                    SkeletonBlock(modifier = Modifier.size(width = 68.dp, height = 32.dp), alpha = 0.5f)
                }
            }
        }
    }
}

@Composable
fun MilkTickSummaryContentSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(2) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SkeletonBlock(modifier = Modifier.fillMaxWidth(0.65f).height(12.dp))
                        SkeletonBlock(modifier = Modifier.fillMaxWidth(0.45f).height(20.dp))
                        SkeletonBlock(modifier = Modifier.fillMaxWidth(0.5f).height(10.dp), alpha = 0.45f)
                    }
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonBlock(modifier = Modifier.size(32.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkeletonBlock(modifier = Modifier.fillMaxWidth(0.4f).height(14.dp))
                    SkeletonBlock(modifier = Modifier.fillMaxWidth(0.55f).height(26.dp))
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(2) {
                SkeletonBlock(modifier = Modifier.weight(1f).height(84.dp), alpha = 0.5f)
            }
        }
        repeat(2) {
            SkeletonBlock(modifier = Modifier.fillMaxWidth().height(72.dp), alpha = 0.45f)
        }
    }
}

@Composable
fun MilkTickCalendarScreenSkeleton(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MilkTickExpressiveLoader(
            message = message,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 104.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(7) {
                        SkeletonBlock(modifier = Modifier.size(width = 28.dp, height = 10.dp), alpha = 0.45f)
                    }
                }
                repeat(6) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(7) {
                            SkeletonBlock(modifier = Modifier.size(34.dp), alpha = 0.55f)
                        }
                    }
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SkeletonBlock(modifier = Modifier.fillMaxWidth(0.4f).height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(3) {
                        SkeletonBlock(modifier = Modifier.size(width = 76.dp, height = 28.dp), alpha = 0.5f)
                    }
                }
            }
        }
        SkeletonBlock(modifier = Modifier.fillMaxWidth().height(188.dp), alpha = 0.45f)
    }
}

@Composable
fun MilkTickAnalyticsSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(2) {
                SkeletonBlock(modifier = Modifier.weight(1f).height(116.dp), alpha = 0.5f)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(2) {
                SkeletonBlock(modifier = Modifier.weight(1f).height(116.dp), alpha = 0.45f)
            }
        }
        repeat(3) {
            SkeletonBlock(modifier = Modifier.fillMaxWidth().height(236.dp), alpha = 0.45f)
        }
    }
}

@Composable
fun MilkTickExportPreviewSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SkeletonBlock(modifier = Modifier.fillMaxWidth(0.58f).height(16.dp))
        SkeletonBlock(modifier = Modifier.fillMaxWidth().height(1.dp), alpha = 0.3f)
        repeat(4) {
            SkeletonBlock(modifier = Modifier.fillMaxWidth().height(20.dp), alpha = 0.45f)
        }
        SkeletonBlock(modifier = Modifier.fillMaxWidth().height(76.dp), alpha = 0.4f)
    }
}
