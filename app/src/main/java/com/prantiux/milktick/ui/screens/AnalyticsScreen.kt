package com.prantiux.milktick.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.prantiux.milktick.R
import com.prantiux.milktick.viewmodel.AnalyticsViewModel
import com.prantiux.milktick.viewmodel.AppViewModel
import com.prantiux.milktick.ui.components.MilkTickSubpageFloatingHeader
import com.prantiux.milktick.ui.components.MilkTickSubpageSystemBarsGradient
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController: NavController,
    appViewModel: AppViewModel = viewModel(),
    analyticsViewModel: AnalyticsViewModel = viewModel()
) {
    val currentUserId by appViewModel.currentUserId.collectAsState()
    val analyticsData by analyticsViewModel.analyticsData.collectAsState()
    val isLoading by analyticsViewModel.isLoading.collectAsState()
    
    LaunchedEffect(currentUserId) {
        currentUserId?.let { userId ->
            analyticsViewModel.loadAnalytics(userId)
        }
    }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            MilkTickSubpageSystemBarsGradient()
            MilkTickSubpageFloatingHeader(
                title = "Data Analytics",
                onBackClick = { navController.navigateUp() }
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 144.dp, bottom = 16.dp)
                ) {
                    // Summary Cards
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricCard(
                                title = "This Month",
                                value = "${analyticsData.currentMonthQuantity}L",
                                subtitle = "Total Quantity",
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.primary
                            )
                            MetricCard(
                                title = "Avg Daily",
                                value = "${analyticsData.averageDailyConsumption}L",
                                subtitle = "Consumption",
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricCard(
                                title = "Cost/Liter",
                                value = "₹${analyticsData.costPerLiter}",
                                subtitle = "Average Rate",
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            MetricCard(
                                title = "Consistency",
                                value = "${analyticsData.deliveryConsistency}%",
                                subtitle = "Delivery Rate",
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    // Monthly Comparison Chart
                    item {
                        ChartCard(title = "Monthly Comparison") {
                            MonthlyComparisonChart(
                                currentMonth = analyticsData.currentMonthQuantity,
                                previousMonth = analyticsData.previousMonthQuantity,
                                currentMonthName = YearMonth.now().month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                previousMonthName = YearMonth.now().minusMonths(1).month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                            )
                        }
                    }
                    
                    // Yearly Trend Chart
                    item {
                        ChartCard(title = "Yearly Consumption Trend") {
                            YearlyTrendChart(monthlyData = analyticsData.yearlyMonthlyData)
                        }
                    }
                    
                    // Cost Trend Chart
                    item {
                        ChartCard(title = "Cost per Liter Trend") {
                            CostTrendChart(costData = analyticsData.monthlyCostData)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    color: Color
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun ChartCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun MonthlyComparisonChart(
    currentMonth: Float,
    previousMonth: Float,
    currentMonthName: String,
    previousMonthName: String
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                setDrawGridBackground(false)
                setDrawBarShadow(false)
                legend.isEnabled = true
                setScaleEnabled(false)
                setPinchZoom(false)
                
                val entries = listOf(
                    BarEntry(0f, previousMonth),
                    BarEntry(1f, currentMonth)
                )
                
                val dataSet = BarDataSet(entries, "Quantity (Liters)").apply {
                    colors = listOf(secondaryColor.toArgb(), primaryColor.toArgb())
                    valueTextSize = 12f
                    valueTextColor = android.graphics.Color.BLACK
                }
                
                val barData = BarData(dataSet).apply {
                    barWidth = 0.5f
                }
                
                data = barData
                
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    textSize = 12f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return when (value.toInt()) {
                                0 -> previousMonthName
                                1 -> currentMonthName
                                else -> ""
                            }
                        }
                    }
                }
                
                axisLeft.apply {
                    setDrawGridLines(true)
                    gridColor = android.graphics.Color.LTGRAY
                    gridLineWidth = 0.5f
                    textSize = 12f
                }
                
                axisRight.isEnabled = false
                
                animateY(800)
                invalidate()
            }
        }
    )
}

@Composable
fun YearlyTrendChart(monthlyData: List<Float>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setDrawGridBackground(false)
                legend.isEnabled = false
                setScaleEnabled(false)
                setPinchZoom(false)
                
                val entries = monthlyData.mapIndexed { index, value ->
                    Entry(index.toFloat(), value)
                }
                
                val dataSet = LineDataSet(entries, "Monthly Consumption").apply {
                    color = primaryColor.toArgb()
                    lineWidth = 3f
                    setCircleColor(primaryColor.toArgb())
                    circleRadius = 5f
                    setDrawCircleHole(false)
                    valueTextSize = 10f
                    valueTextColor = android.graphics.Color.BLACK
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    setDrawFilled(true)
                    fillColor = primaryColor.toArgb()
                    fillAlpha = 50
                }
                
                data = LineData(dataSet)
                
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    textSize = 10f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                                              "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                            val index = value.toInt()
                            return if (index in months.indices) months[index] else ""
                        }
                    }
                }
                
                axisLeft.apply {
                    setDrawGridLines(true)
                    gridColor = android.graphics.Color.LTGRAY
                    gridLineWidth = 0.5f
                    textSize = 10f
                }
                
                axisRight.isEnabled = false
                
                animateX(1000)
                invalidate()
            }
        }
    )
}

@Composable
fun CostTrendChart(costData: List<Float>) {
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setDrawGridBackground(false)
                legend.isEnabled = false
                setScaleEnabled(false)
                setPinchZoom(false)
                
                val entries = costData.mapIndexed { index, value ->
                    Entry(index.toFloat(), value)
                }
                
                val dataSet = LineDataSet(entries, "Cost/Liter").apply {
                    color = tertiaryColor.toArgb()
                    lineWidth = 3f
                    setCircleColor(tertiaryColor.toArgb())
                    circleRadius = 5f
                    setDrawCircleHole(false)
                    valueTextSize = 10f
                    valueTextColor = android.graphics.Color.BLACK
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    setDrawFilled(true)
                    fillColor = tertiaryColor.toArgb()
                    fillAlpha = 50
                }
                
                data = LineData(dataSet)
                
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    textSize = 10f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                                              "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                            val index = value.toInt()
                            return if (index in months.indices) months[index] else ""
                        }
                    }
                }
                
                axisLeft.apply {
                    setDrawGridLines(true)
                    gridColor = android.graphics.Color.LTGRAY
                    gridLineWidth = 0.5f
                    textSize = 10f
                }
                
                axisRight.isEnabled = false
                
                animateX(1000)
                invalidate()
            }
        }
    )
}
