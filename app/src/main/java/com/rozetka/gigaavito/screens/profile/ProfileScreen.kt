
package com.rozetka.gigaavito.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ModeEdit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.rozetka.gigaavito.R
import com.rozetka.gigaavito.ui.theme.ThemeViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinViewModel(),
    themeViewModel: ThemeViewModel = koinViewModel(),
    onOpenDrawer: () -> Unit,
    onLogout: () -> Unit
) {
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
    var isEditing by remember { mutableStateOf(false) }

    val userName by viewModel.userName.collectAsState()
    val email by viewModel.userEmail.collectAsState()
    val phone by viewModel.userPhone.collectAsState()
    val photoUrl by viewModel.userPhotoUrl.collectAsState()
    val tokens by viewModel.tokensCount.collectAsState()

    val displayName = userName ?: stringResource(R.string.default_name)
    var editableName by remember(displayName) { mutableStateOf(displayName) }

    val mimeTypeImage = stringResource(R.string.mime_type_image)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateProfile(editableName, it) }
    }

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text(stringResource(R.string.profile_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) { Icon(Icons.Default.Menu, null) }
                },
                actions = {
                    TextButton(onClick = {
                        if (isEditing) viewModel.updateProfile(editableName)
                        isEditing = !isEditing
                    }) {
                        Text(if (isEditing) stringResource(R.string.btn_save) else stringResource(R.string.btn_edit))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clickable(enabled = isEditing) { photoPickerLauncher.launch(mimeTypeImage) },
                contentAlignment = Alignment.Center
            ) {
                if (photoUrl != null) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(70.dp))
                }
                if (isEditing) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(0.4f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.ModeEdit, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = editableName,
                onValueChange = { editableName = it },
                label = { Text(stringResource(R.string.label_username)) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = !isEditing,
                shape = MaterialTheme.shapes.extraLarge
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = email ?: stringResource(R.string.default_email),
                onValueChange = {},
                label = { Text(stringResource(R.string.label_email)) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                enabled = false,
                shape = MaterialTheme.shapes.extraLarge
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = phone ?: stringResource(R.string.default_phone),
                onValueChange = {},
                label = { Text(stringResource(R.string.label_phone)) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                enabled = false,
                shape = MaterialTheme.shapes.extraLarge
            )

            Spacer(Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Token, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(R.string.label_tokens), modifier = Modifier.weight(1f))
                    Text(tokens.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                }
            }

            Spacer(Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.label_dark_theme), style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = isDarkTheme, onCheckedChange = { themeViewModel.setTheme(it) })
                }
            }

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = { viewModel.logout(onLogout) },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.btn_logout), style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}