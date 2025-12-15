package com.gujju.thoughts

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gujju.thoughts.model.ImageItem
import com.unbelievable.justfacts.kotlinmodule.ADConstant

class MainActivity : AppCompatActivity() {
    var count = 2;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainActivityUI()
        }
        ADConstant.LoadAdmobInsterstitialAd(this@MainActivity)
    }


    private fun populateList(onSuccess: (List<ImageItem>) -> Unit, onError: () -> Unit) {

        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("data/0/details")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ImageItem>()
                for (child in snapshot.children) {
                    val item = child.getValue(ImageItem::class.java)
                    item?.let { list.add(it) }
                }

                list.shuffle()
                onSuccess(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error: ${error.message}")
                onError()
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()

    }

    @Composable
    fun MainActivityUI() {

        val context = LocalContext.current
        var isLoading by remember { mutableStateOf(true) }
        var hasInternet by remember { mutableStateOf(false) }
        var imageList by remember { mutableStateOf(listOf<ImageItem>()) }
        LaunchedEffect(Unit) {
            hasInternet = NetworkChecker.isInternetAvailable(context)
            if (hasInternet) {
                populateList(onSuccess = {
                    imageList = it
                    isLoading = false
                }, onError = {
                    isLoading = false
                })
            } else {
                isLoading = false
            }
        }

        Scaffold(
            topBar = {
                TopAppBar()
            }
        ) { paddingValues ->
            when {
                isLoading -> ProgressUI()
                !hasInternet -> RetryUI() {
                    isLoading = true
                    hasInternet = NetworkChecker.isInternetAvailable(context)
                }

                else -> ContentGrid(modifier = Modifier.padding(paddingValues), imageList)

            }

        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopAppBar() {
        TopAppBar(
            colors = topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ),
            title = {
                Text(stringResource(R.string.app_name))
            })
    }

    @Composable
    fun ImageItemUI(imageUrl: String, onClick: () -> Unit) {
        Card(
            modifier = Modifier
                .padding(6.dp)
                .fillMaxWidth()
                .height(180.dp)
                .clickable { onClick() },
            shape = RoundedCornerShape(12.dp)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        }
    }

    @Composable
    fun ContentGrid(modifier: Modifier = Modifier, images: List<ImageItem>) {

        val context = LocalContext.current
        var clickCount by remember { mutableStateOf(0) }

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(color = Color.Black)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(
                    images.size
                ) { index ->
                    ImageItemUI(imageUrl = images[index].background ?: "", onClick = {
                        clickCount++
                        moveNext(clickCount, index, images)
                    })
                }
            }
        }

    }

    fun moveNext(count: Int , index :Int, images: List<ImageItem>)
    {
        if (count % 3 == 0) {
            ADConstant.ShowAdmobInsterstitial(this@MainActivity, object : ADConstant.CallBack {
                override fun MoveToNext() {
                    val intent = Intent(this@MainActivity, GalleryActivity::class.java).apply {
                        putExtra("position", index)
                        putStringArrayListExtra(
                            "images",
                            ArrayList(images.mapNotNull { it.background })
                        )
                    }
                    startActivity(intent)
                }

            })

        } else {
            val intent = Intent(this@MainActivity, GalleryActivity::class.java).apply {
                putExtra("position", index)
                putStringArrayListExtra(
                    "images",
                    ArrayList(images.mapNotNull { it.background })
                )
            }
            startActivity(intent)
        }
    }



    @Composable
    fun ProgressUI() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                trackColor = Color.White,
                color = colorResource(R.color.teal_700)
            )
            Text(
                modifier = Modifier.padding(10.dp),
                text = "Please Wait..",
                fontSize = 18.sp,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }

    @Composable
    fun RetryUI(onRetry: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                modifier = Modifier.padding(10.dp),
                text = stringResource(R.string.connect_internet),
                fontSize = 18.sp,
                color = Color.White,
                style = MaterialTheme.typography.displaySmall
            )
            OutlinedButton(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.dark_grey),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = stringResource(R.string.retry), color = Color.White)
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        //  RetryUI()
    }

}





