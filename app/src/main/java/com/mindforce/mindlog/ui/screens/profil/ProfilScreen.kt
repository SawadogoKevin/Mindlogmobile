package com.mindforce.mindlog.ui.screens.profil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindforce.mindlog.data.local.SessionManager
import com.mindforce.mindlog.data.repository.DashboardRepository
import com.mindforce.mindlog.data.repository.UserRepository
import com.mindforce.mindlog.ui.components.MindForceTopBar
import com.mindforce.mindlog.ui.components.DangerButton
import com.mindforce.mindlog.ui.components.PrimaryButton
import com.mindforce.mindlog.ui.components.ErrorBanner
import com.mindforce.mindlog.ui.theme.MindBlack
import com.mindforce.mindlog.ui.theme.MindOrange
import com.mindforce.mindlog.ui.theme.MindWhite

@Composable
fun ProfilScreen(
    sessionManager: SessionManager,
    dashboardRepository: DashboardRepository,
    userRepository: UserRepository,
    onLogout: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val viewModel: ProfilViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ProfilViewModel(sessionManager, dashboardRepository, userRepository) as T
        }
    })
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.successMessage, state.passwordSuccessMessage) {
        if (state.successMessage != null || state.passwordSuccessMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MindWhite)) {
        MindForceTopBar(
            title = "Mon Profil",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Profil
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MindOrange),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = MindBlack
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${state.prenom} ${state.nom}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MindBlack
            )
            Text(
                text = state.role.replace("ROLE_", ""),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Error/Success messages
            state.errorMessage?.let { 
                ErrorBanner(message = it)
                Spacer(modifier = Modifier.height(12.dp))
            }
            state.successMessage?.let {
                InfoBanner(message = it)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Profile Information Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Informations personnelles",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MindBlack
                        )
                        if (!state.isEditing) {
                            TextButton(onClick = { viewModel.toggleEditMode() }) {
                                Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = MindOrange, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Modifier", color = MindOrange)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Nom field
                    ProfilEditField(
                        label = "Nom",
                        value = state.nom,
                        onValueChange = { viewModel.updateNom(it) },
                        enabled = state.isEditing,
                        icon = Icons.Default.Person
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Prénom field
                    ProfilEditField(
                        label = "Prénom",
                        value = state.prenom,
                        onValueChange = { viewModel.updatePrenom(it) },
                        enabled = state.isEditing,
                        icon = Icons.Default.Person
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Email field
                    ProfilEditField(
                        label = "Email",
                        value = state.email,
                        onValueChange = { viewModel.updateEmail(it) },
                        enabled = state.isEditing,
                        icon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Téléphone field
                    ProfilEditField(
                        label = "Téléphone",
                        value = state.telephone,
                        onValueChange = { viewModel.updateTelephone(it) },
                        enabled = state.isEditing,
                        icon = Icons.Default.Phone,
                        keyboardType = KeyboardType.Phone
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Read-only fields
                    ProfilReadOnlyField(
                        label = "Rôle",
                        value = state.role.replace("ROLE_", ""),
                        icon = Icons.Default.Badge
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (state.matricule.isNotBlank()) {
                        ProfilReadOnlyField(
                            label = "Matricule",
                            value = state.matricule,
                            icon = Icons.Default.Badge
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Save/Cancel buttons
                    if (state.isEditing) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.toggleEditMode() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Annuler")
                            }
                            PrimaryButton(
                                text = if (state.isSaving) "Enregistrement..." else "Enregistrer",
                                onClick = { viewModel.saveProfile() },
                                modifier = Modifier.weight(1f),
                                enabled = !state.isSaving,
                                isLoading = state.isSaving
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Password Change Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Modifier le mot de passe",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MindBlack
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password error/success messages
                    state.passwordErrorMessage?.let {
                        ErrorBanner(message = it)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    state.passwordSuccessMessage?.let {
                        InfoBanner(message = it)
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Current password
                    PasswordField(
                        label = "Mot de passe actuel",
                        value = state.currentPassword,
                        onValueChange = { viewModel.updateCurrentPassword(it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // New password
                    PasswordField(
                        label = "Nouveau mot de passe",
                        value = state.newPassword,
                        onValueChange = { viewModel.updateNewPassword(it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Confirm password
                    PasswordField(
                        label = "Confirmer le nouveau mot de passe",
                        value = state.confirmPassword,
                        onValueChange = { viewModel.updateConfirmPassword(it) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    PrimaryButton(
                        text = if (state.isChangingPassword) "Modification..." else "Modifier le mot de passe",
                        onClick = { viewModel.changePassword() },
                        enabled = !state.isChangingPassword,
                        isLoading = state.isChangingPassword
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            DangerButton(
                text = "Se déconnecter",
                onClick = onLogout
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilEditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MindOrange,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                disabledBorderColor = Color.Gray.copy(alpha = 0.2f),
                disabledTextColor = MindBlack
            ),
            leadingIcon = {
                Icon(icon, contentDescription = null, tint = MindOrange, modifier = Modifier.size(20.dp))
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilReadOnlyField(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                disabledBorderColor = Color.Gray.copy(alpha = 0.2f),
                disabledTextColor = MindBlack
            ),
            leadingIcon = {
                Icon(icon, contentDescription = null, tint = MindOrange, modifier = Modifier.size(20.dp))
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MindOrange,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
            ),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Masquer" else "Afficher",
                        tint = MindOrange
                    )
                }
            }
        )
    }
}

@Composable
fun InfoBanner(message: String) {
    Surface(
        color = MindOrange.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            color = MindBlack,
            modifier = Modifier.padding(12.dp)
        )
    }
}
