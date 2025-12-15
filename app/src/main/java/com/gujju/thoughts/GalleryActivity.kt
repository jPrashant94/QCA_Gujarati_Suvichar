package com.gujju.thoughts

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.gujju.thoughts.ui.theme.AppBarWithBackButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GalleryActivity : AppCompatActivity() {
    private lateinit var images: List<String>
    private val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GalleryUI()
        }

    }

    var startPosition: Int = 0

    @Composable
    fun GalleryUI() {
        images = intent.getStringArrayListExtra("images") ?: emptyList()
        startPosition = intent.getIntExtra("position", 0)

        MyScreen(navController = rememberNavController())
    }

    @Composable
    fun MyScreen(navController: NavController) {
        Scaffold(
            topBar = {
                AppBarWithBackButton(
                    navController = navController,
                    title = stringResource(R.string.app_name)
                )
            }
        ) { innerPadding ->
            // Screen content goes here
            PagerUI(modifier = Modifier.padding(innerPadding))
        }
    }

    @Composable
    fun PagerUI(modifier: Modifier) {

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Column(
                modifier = modifier
                    .weight(1.0f)
            ) {
                val pagerState = rememberPagerState(initialPage = startPosition, pageCount = {
                    images.size
                })
                HorizontalPager(state = pagerState, modifier = Modifier.weight(1.0f)) { page ->
                    // Our page content
                    AsyncImage(
                        model = images[page].toUri(),
                        contentDescription = "Image $page",
                        modifier = Modifier
                            .fillMaxSize()
                        // You can add contentScale, alignment, etc., here
                    )
                }
                OutlinedButton(
                    onClick = {
                        val currentPos = pagerState.currentPage

                        coroutineScope.launch {
                            shareImageFromUrl(
                                this@GalleryActivity,
                                images[currentPos],
                                "my_image.jpg"
                            )
                        }

                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 15.dp)
                        .background(Color.Black)
                ) {

                    Text(text = "Share", color = Color.White, textAlign = TextAlign.Center)
                }

            }
            AdaptiveAdMobBanner(
                adUnitId = BuildConfig.ADMOB_BANNER_ID
            )
        }

    }

    @Composable
    fun AdaptiveAdMobBanner(adUnitId: String) {
        val context = LocalContext.current
        val displayMetrics = context.resources.displayMetrics
        val adWidth = (displayMetrics.widthPixels / displayMetrics.density).toInt()

        val adView = remember {
            AdView(context).apply {
                setAdSize(
                    AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                        context, adWidth
                    )
                )
                this.adUnitId = adUnitId
                loadAd(AdRequest.Builder().build())
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                adView.destroy()   // ✅ AUTO CLEANUP
            }
        }

        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { adView }
        )
    }

//    @Preview(showBackground = true)
//    @Composable
//    fun DefaultPreview() {
//        PagerUI(modifier = Modifier)
//    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()

    }

}