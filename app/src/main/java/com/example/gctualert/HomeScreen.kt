package com.example.gctualert

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(loggedInIndexNumber: String) {
    var profile by remember { mutableStateOf<StudentProfile?>(null) }
    var announcements by remember { mutableStateOf<List<NewsletterAnnouncement>>(emptyList()) }
    var commentsMap by remember { mutableStateOf<Map<Long, List<Map<String, Any>>>>(emptyMap()) }
    var newCommentText by remember { mutableStateOf(emptyMap<Long, String>()) }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var isLoading by remember { mutableStateOf(true) }

    val darkBg = Color(0xFF141A23)
    val darkSlate = Color(0xFF1F2633)
    val cardBg = Color(0xFF1F2633)
    val primaryRed = Color(0xFFD91A1A)
    val yellowAccent = Color(0xFFFFC107)

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val greetingMessage = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "GOOD MORNING,"
            hour < 17 -> "GOOD AFTERNOON,"
            else -> "GOOD EVENING,"
        }
    }

    val formattedDate = remember {
        SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date()).uppercase()
    }

    fun loadComments(announcementId: Long) {
        scope.launch {
            try {
                val res = SupabaseManager.client.from("COMMENTS")
                    .select {
                        filter { eq("announcement_id", announcementId) }
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<Map<String, Any>>()
                commentsMap = commentsMap + (announcementId to res)
            } catch (e: Exception) {
                Log.e("HomeScreen", "Error loading comments for $announcementId: ${e.localizedMessage}")
            }
        }
    }

    fun loadData() {
        isLoading = true
        scope.launch {
            try {
                val numericIndex = loggedInIndexNumber.toLongOrNull() ?: 0L
                profile = SupabaseManager.client.from("STUDENT").select {
                    filter { eq("Index Number", numericIndex) }
                }.decodeSingleOrNull<StudentProfile>()
            } catch (e: Exception) {
                Log.e("HomeScreen", "Error loading profile: ${e.localizedMessage}")
            }

            try {
                announcements = SupabaseManager.client.from("ANNOUNCEMENTS").select {
                    order("created_at", Order.DESCENDING)
                }.decodeList<NewsletterAnnouncement>()

                announcements.forEach { post ->
                    loadComments(post.id)
                }
            } catch (e: Exception) {
                Log.e("HomeScreen", "Error loading announcements: ${e.localizedMessage}")
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadData()
        try {
            val channel = SupabaseManager.client.channel("public:ANNOUNCEMENTS")
            channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "ANNOUNCEMENTS"
            }.onEach { loadData() }.launchIn(scope)
            channel.subscribe()
        } catch (e: Exception) {
            Log.e("HomeScreen", "Realtime error: ${e.localizedMessage}")
        }
    }

    val filteredAnnouncements = if (selectedCategory == "ALL") {
        announcements
    } else {
        announcements.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBg)
    ) {
        // News Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(darkSlate)
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("GCTU NEWSROOM", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                Text(formattedDate, color = Color(0xFFB3C0CC), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(
                onClick = { loadData() },
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Student Identity Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(yellowAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile?.name?.firstOrNull()?.uppercase() ?: "S",
                            color = darkBg,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(greetingMessage, color = yellowAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(profile?.name?.uppercase() ?: "STUDENT", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
                        Text("${profile?.course ?: "COMPUTER ENGINEERING"} • ${profile?.indexNumber ?: loggedInIndexNumber}", color = Color(0xFFBFCCD8), fontSize = 9.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Campus Bulletins & Announcements", color = yellowAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Live campus notifications and discussions.", color = Color(0xFFA0AAB8), fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categories = listOf("ALL", "EXAM", "GENERAL", "EVENT", "URGENT")
                categories.forEach { cat ->
                    val isSelected = selectedCategory.equals(cat, ignoreCase = true)
                    Button(
                        onClick = { selectedCategory = cat },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) primaryRed else cardBg
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text(cat, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = yellowAccent)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredAnnouncements) { item ->
                    val postId = item.id

                    Card(
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = primaryRed,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = item.category?.uppercase() ?: "GENERAL",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Text("Target: ${item.department ?: "All Departments"}", color = Color(0xFFA0AAB8), fontSize = 9.sp)
                                    Text("Expires in 11h 59m", color = yellowAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                item.author?.let {
                                    if (it.isNotBlank()) {
                                        Text("By $it", fontSize = 10.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }

                                Text(item.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(item.content, fontSize = 13.sp, color = Color(0xFFD0D7DE))

                                val imageUrl = item.activeImageUrl
                                if (!imageUrl.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    val sanitizedUrl = remember(imageUrl) { imageUrl.trim().replace(" ", "%20") }
                                    SubcomposeAsyncImage(
                                        model = ImageRequest.Builder(context).data(sanitizedUrl).crossfade(true).build(),
                                        contentDescription = "Bulletin Attachment",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop,
                                        error = {
                                            Box(
                                                modifier = Modifier.fillMaxWidth().height(100.dp).background(Color.Red.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("Failed to load image attachment", color = Color.Red, fontSize = 10.sp)
                                            }
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = Color(0xFF2E3846))
                                Spacer(modifier = Modifier.height(10.dp))

                                Text("DISCUSSION & FEEDBACK", color = yellowAccent, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                Spacer(modifier = Modifier.height(6.dp))

                                val postComments = commentsMap[postId] ?: emptyList()
                                if (postComments.isEmpty()) {
                                    Text("No feedback yet. Be the first!", color = Color(0xFFA0AAB8), fontSize = 11.sp)
                                } else {
                                    postComments.take(3).forEach { com ->
                                        Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                            Text(
                                                text = "${com["student_username"] ?: "Student"}:",
                                                color = yellowAccent,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = com["comment"]?.toString() ?: "",
                                                color = Color.White,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = newCommentText[postId] ?: "",
                                        onValueChange = { typed ->
                                            newCommentText = newCommentText + (postId to typed)
                                        },
                                        placeholder = { Text("Write a comment...", fontSize = 12.sp, color = Color(0xFFA0AAB8)) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF2E3846),
                                            unfocusedBorderColor = Color(0xFF2E3846),
                                            focusedContainerColor = Color(0xFF141A23),
                                            unfocusedContainerColor = Color(0xFF141A23),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            val text = newCommentText[postId] ?: ""
                                            if (text.isNotBlank()) {
                                                scope.launch {
                                                    try {
                                                        SupabaseManager.client.from("COMMENTS").insert(
                                                            mapOf(
                                                                "announcement_id" to postId,
                                                                "student_username" to (profile?.name ?: loggedInIndexNumber),
                                                                "comment" to text
                                                            )
                                                        )
                                                        newCommentText = newCommentText + (postId to "")
                                                        loadComments(postId)
                                                    } catch (e: Exception) {
                                                        Log.e("HomeScreen", "Failed to insert comment: ${e.localizedMessage}")
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .background(yellowAccent, CircleShape)
                                            .size(38.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = darkBg, modifier = Modifier.size(16.dp))
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
