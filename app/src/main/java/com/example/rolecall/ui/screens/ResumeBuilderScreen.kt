package com.example.rolecall.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.rolecall.data.remote.*
import com.example.rolecall.ui.components.RoleCallScaffold
import com.example.rolecall.ui.theme.*
import com.example.rolecall.ui.viewmodel.ResumeBuilderUiState
import com.example.rolecall.ui.viewmodel.ResumeBuilderViewModel
import com.example.rolecall.ui.viewmodel.ProfileViewModel
import com.example.rolecall.data.remote.ProfileRead

@Composable
fun ResumeBuilderScreen(navController: NavHostController) {
    val viewModel: ResumeBuilderViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val profile = profileViewModel.profile

    var currentStep by remember { mutableIntStateOf(0) }
    val steps = listOf("Contact", "Work", "Education", "Skills", "Projects", "Certifications", "Template")

    // Load the profile once
    LaunchedEffect(Unit) {
        if (profile == null) profileViewModel.loadProfile()
    }

    // Prefill only blank fields when the profile arrives
    LaunchedEffect(profile) {
        profile?.let {
            viewModel.prefillFromProfile(
                fullName = it.fullName,
                email = it.email ?: "",
                title = it.title,
                overwrite = false
            )
        }
    }

    // Watch for success and navigate to preview
    val uiState = viewModel.uiState
    LaunchedEffect(uiState) {
        if (uiState is ResumeBuilderUiState.Success) {
            navController.navigate("generated_resume/${uiState.resumeId}") {
                popUpTo("resume_builder") { inclusive = false }
            }
        }
    }
    RoleCallScaffold(
        navController = navController,
        title = "Resume Builder",
        showSearchBar = false
    ) { modifier ->
        Column(modifier = modifier.fillMaxSize()) {
            // Step indicator
            Text(
                "Step ${currentStep + 1} of ${steps.size}: ${steps[currentStep]}",
                style = MaterialTheme.typography.titleMedium,
                color = PrimaryText,
                modifier = Modifier.padding(16.dp)
            )

            // Content area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                when (currentStep) {
                    0 -> ContactInfoStep(viewModel, profile, onPrefill = {
                        profile?.let {
                            viewModel.prefillFromProfile(
                                fullName = it.fullName,
                                email = it.email ?: "",
                                title = it.title,
                                overwrite = true
                            )
                        }
                    })
                    1 -> WorkExperienceStep(viewModel)
                    2 -> EducationStep(viewModel)
                    3 -> SkillsStep(viewModel)
                    4 -> ProjectsStep(viewModel)
                    5 -> CertificationsStep(viewModel)
                    6 -> TemplateStep(viewModel)
                }
            }

            // Validation for the current step
            val canProceed = when (currentStep) {
                0 -> isContactValid(viewModel.contact)
                1 -> isWorkValid(viewModel.workExperience)
                2 -> isEducationValid(viewModel.education)
                3 -> isSkillsValid(viewModel.skills)
                else -> true
            }

            // Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 0) {
                    OutlinedButton(onClick = { currentStep-- }) {
                        Text("Previous")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (currentStep < steps.size - 1) {
                    Button(
                        onClick = { currentStep++ },
                        enabled = canProceed
                    ) {
                        Text("Next")
                    }
                } else {
                    Button(
                        onClick = { viewModel.generateResume() },
                        enabled = viewModel.uiState !is ResumeBuilderUiState.Loading
                    ) {
                        Text("Generate Resume")
                    }
                }
            }

            // Helper message when the current step is incomplete
            if (!canProceed) {
                val helper = when (currentStep) {
                    0 -> "Please fill in name and a valid email."
                    1 -> "Add at least one experience with all fields filled in."
                    2 -> "Add at least one education entry with institution and degree."
                    3 -> "Add at least one skill."
                    else -> null
                }
                helper?.let {
                    Text(
                        it,
                        color = AccentAlert,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            // Generation status
            when (val state = viewModel.uiState) {
                is ResumeBuilderUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
                is ResumeBuilderUiState.Success -> {
                    Text(
                        "Resume generated successfully!",
                        color = AccentSuccess,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is ResumeBuilderUiState.Error -> {
                    Text(
                        state.message,
                        color = AccentAlert,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is ResumeBuilderUiState.Idle -> {}
            }
        }
    }
}

// ── Validation helpers ──────────────────────────────────────────────────────

private fun isContactValid(contact: ContactInfo): Boolean {
    return contact.name.isNotBlank() &&
            contact.email.contains("@") &&
            contact.email.contains(".")
}

private fun isWorkValid(list: List<WorkExperience>): Boolean {
    return list.isNotEmpty() && list.all {
        it.company.isNotBlank() && it.jobTitle.isNotBlank() &&
                it.startDate.isNotBlank() && it.description.isNotBlank()
    }
}

private fun isEducationValid(list: List<Education>): Boolean {
    return list.isNotEmpty() && list.all {
        it.institution.isNotBlank() && it.degree.isNotBlank()
    }
}

private fun isSkillsValid(skills: List<String>): Boolean = skills.isNotEmpty()

// ── Step Composable Functions ──────────────────────────────────────────────

@Composable
private fun ContactInfoStep(
    viewModel: ResumeBuilderViewModel,
    profile: com.example.rolecall.data.remote.ProfileRead?,
    onPrefill: () -> Unit
) {
    var name by remember { mutableStateOf(viewModel.contact.name) }
    var email by remember { mutableStateOf(viewModel.contact.email) }
    var phone by remember { mutableStateOf(viewModel.contact.phone ?: "") }
    var location by remember { mutableStateOf(viewModel.contact.location ?: "") }
    var linkedin by remember { mutableStateOf(viewModel.contact.linkedin ?: "") }
    var github by remember { mutableStateOf(viewModel.contact.github ?: "") }

    //Sync local fields when the ViewModel updates (e.g., after prefill)
    LaunchedEffect(viewModel.contact) {
        name = viewModel.contact.name
        email = viewModel.contact.email
        phone = viewModel.contact.phone ?: ""
        location = viewModel.contact.location ?: ""
        linkedin = viewModel.contact.linkedin ?: ""
        github = viewModel.contact.github ?: ""
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        //Show the prefill button only when a profile is available
        if (profile != null) {
            TextButton(
                onClick = onPrefill,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Prefill from Profile", color = UiInteractive)
            }
        }

        OutlinedTextField(name,     { name = it },     label = { Text("Full Name") },  modifier = Modifier.fillMaxWidth())
        OutlinedTextField(email,    { email = it },    label = { Text("Email") },      modifier = Modifier.fillMaxWidth())
        OutlinedTextField(phone,    { phone = it },    label = { Text("Phone") },      modifier = Modifier.fillMaxWidth())
        OutlinedTextField(location, { location = it }, label = { Text("Location") },   modifier = Modifier.fillMaxWidth())
        OutlinedTextField(linkedin, { linkedin = it }, label = { Text("LinkedIn URL") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(github,   { github = it },   label = { Text("GitHub URL") },  modifier = Modifier.fillMaxWidth())

        Button(onClick = {
            viewModel.updateContact(
                ContactInfo(
                    name = name,
                    email = email,
                    phone = phone.ifBlank { null },
                    location = location.ifBlank { null },
                    linkedin = linkedin.ifBlank { null },
                    github = github.ifBlank { null }
                )
            )
        }) {
            Text("Save Contact")
        }
    }
}

@Composable
private fun WorkExperienceStep(viewModel: ResumeBuilderViewModel) {
    var company by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(company, { company = it }, label = { Text("Company") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(jobTitle, { jobTitle = it }, label = { Text("Job Title") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(startDate, { startDate = it }, label = { Text("Start Date (YYYY-MM)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(endDate, { endDate = it }, label = { Text("End Date (leave blank if current)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(description, { description = it }, label = { Text("Description") }, minLines = 3, modifier = Modifier.fillMaxWidth())

        Button(onClick = {
            viewModel.addWorkExperience(
                WorkExperience(
                    company = company,
                    jobTitle = jobTitle,
                    startDate = startDate,
                    endDate = endDate.ifBlank { null },
                    description = description
                )
            )
            company = ""; jobTitle = ""; startDate = ""; endDate = ""; description = ""
        }) {
            Text("Add Experience")
        }

        Spacer(Modifier.height(8.dp))
        viewModel.workExperience.forEachIndexed { index, exp ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${exp.jobTitle} at ${exp.company}", color = PrimaryText)
                TextButton(onClick = { viewModel.removeWorkExperience(index) }) {
                    Text("Remove")
                }
            }
        }
    }
}

@Composable
private fun EducationStep(viewModel: ResumeBuilderViewModel) {
    var institution by remember { mutableStateOf("") }
    var degree by remember { mutableStateOf("") }
    var field by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(institution, { institution = it }, label = { Text("Institution") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(degree, { degree = it }, label = { Text("Degree") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(field, { field = it }, label = { Text("Field of Study") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(startDate, { startDate = it }, label = { Text("Start Date") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(endDate, { endDate = it }, label = { Text("End Date") }, modifier = Modifier.fillMaxWidth())

        Button(onClick = {
            viewModel.addEducation(
                Education(
                    institution = institution,
                    degree = degree,
                    fieldOfStudy = field.ifBlank { null },
                    startDate = startDate.ifBlank { null },
                    endDate = endDate.ifBlank { null }
                )
            )
            institution = ""; degree = ""; field = ""; startDate = ""; endDate = ""
        }) {
            Text("Add Education")
        }

        Spacer(Modifier.height(8.dp))
        viewModel.education.forEachIndexed { index, edu ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${edu.degree}, ${edu.institution}", color = PrimaryText)
                TextButton(onClick = { viewModel.removeEducation(index) }) {
                    Text("Remove")
                }
            }
        }
    }
}

@Composable
private fun SkillsStep(viewModel: ResumeBuilderViewModel) {
    var skillInput by remember { mutableStateOf("") }
    val skills = viewModel.skills.toMutableStateList()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(skillInput, { skillInput = it }, label = { Text("Add Skill") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            if (skillInput.isNotBlank()) {
                skills.add(skillInput.trim())
                skillInput = ""
                viewModel.updateSkills(skills.toList())
            }
        }) {
            Text("Add")
        }
        Spacer(Modifier.height(8.dp))
        skills.forEach { skill ->
            Text("• $skill", color = PrimaryText)
        }
    }
}

@Composable
private fun ProjectsStep(viewModel: ResumeBuilderViewModel) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(name, { name = it }, label = { Text("Project Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(description, { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(url, { url = it }, label = { Text("Project URL") }, modifier = Modifier.fillMaxWidth())

        Button(onClick = {
            viewModel.addProject(Project(name = name, description = description, url = url.ifBlank { null }))
            name = ""; description = ""; url = ""
        }) {
            Text("Add Project")
        }

        viewModel.projects.forEachIndexed { index, proj ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(proj.name, color = PrimaryText)
                TextButton(onClick = { viewModel.removeProject(index) }) {
                    Text("Remove")
                }
            }
        }
    }
}

@Composable
private fun CertificationsStep(viewModel: ResumeBuilderViewModel) {
    var certInput by remember { mutableStateOf("") }
    val certs = viewModel.certifications.toMutableStateList()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(certInput, { certInput = it }, label = { Text("Certification") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            if (certInput.isNotBlank()) {
                certs.add(certInput.trim())
                certInput = ""
                viewModel.updateCertifications(certs.toList())
            }
        }) {
            Text("Add")
        }
        certs.forEach { cert -> Text("• $cert", color = PrimaryText) }
    }
}

@Composable
private fun TemplateStep(viewModel: ResumeBuilderViewModel) {
    val templates = listOf("classic", "modern", "compact")
    var availableTemplates by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        viewModel.loadTemplates { templates ->
            availableTemplates = templates
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        templates.forEach { t ->
            val selected = viewModel.template == t
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) FoundationSurface else FoundationSurface.copy(alpha = 0.6f)
                ),
                border = if (selected) BorderStroke(2.dp, UiInteractive) else null,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TemplatePreviewThumbnail(template = t, modifier = Modifier.size(72.dp, 96.dp))
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            t.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleMedium,
                            color = PrimaryText
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            when (t) {
                                "classic" -> "Traditional single-column serif layout. Best for ATS."
                                "modern" -> "Sans-serif with subtle accent colors. Great for tech roles."
                                else -> "Compact spacing to fit more content on one page."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText
                        )
                    }
                    RadioButton(
                        selected = selected,
                        onClick = { viewModel.selectTemplate(t) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplatePreviewThumbnail(template: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            when (template) {
                "classic" -> {
                    Box(Modifier.fillMaxWidth(0.7f).height(4.dp).background(Color(0xFF222222)))
                    Box(Modifier.fillMaxWidth(0.5f).height(3.dp).background(Color(0xFF888888)))
                    Spacer(Modifier.height(4.dp))
                    repeat(4) {
                        Box(Modifier.fillMaxWidth().height(2.dp).background(Color(0xFFAAAAAA)))
                    }
                    Spacer(Modifier.height(4.dp))
                    repeat(3) {
                        Box(Modifier.fillMaxWidth(0.9f).height(2.dp).background(Color(0xFFAAAAAA)))
                    }
                }
                "modern" -> {
                    Box(Modifier.fillMaxWidth().height(14.dp).background(UiInteractive))
                    Spacer(Modifier.height(4.dp))
                    Box(Modifier.fillMaxWidth(0.6f).height(3.dp).background(Color(0xFF333333)))
                    repeat(3) {
                        Box(Modifier.fillMaxWidth().height(2.dp).background(Color(0xFFAAAAAA)))
                    }
                    Spacer(Modifier.height(4.dp))
                    repeat(2) {
                        Box(Modifier.fillMaxWidth(0.9f).height(2.dp).background(Color(0xFFAAAAAA)))
                    }
                }
                else -> {
                    Box(Modifier.fillMaxWidth(0.6f).height(3.dp).background(Color(0xFF222222)))
                    repeat(6) {
                        Box(Modifier.fillMaxWidth().height(2.dp).background(Color(0xFFAAAAAA)))
                    }
                    Spacer(Modifier.height(2.dp))
                    repeat(4) {
                        Box(Modifier.fillMaxWidth(0.9f).height(2.dp).background(Color(0xFFAAAAAA)))
                    }
                    Spacer(Modifier.height(2.dp))
                    repeat(4) {
                        Box(Modifier.fillMaxWidth(0.9f).height(2.dp).background(Color(0xFFAAAAAA)))
                    }
                }
            }
        }
    }
}
