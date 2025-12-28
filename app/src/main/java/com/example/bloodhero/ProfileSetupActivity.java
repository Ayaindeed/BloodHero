package com.example.bloodhero;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.AutoCompleteTextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.bloodhero.utils.EnhancedDialogHelper;import com.example.bloodhero.models.User;
import com.example.bloodhero.repository.UserRepository;
import com.example.bloodhero.utils.UserHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.widget.ArrayAdapter;

public class ProfileSetupActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CAMERA = 100;

    private UserRepository userRepository;
    private User currentUser;

    private TextInputLayout tilName, tilPhone, tilLocation, tilEmail, tilDateOfBirth, tilWeight;
    private TextInputEditText etName, etPhone, etLocation, etEmail, etDateOfBirth, etWeight;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale;
    private MaterialButton btnSaveProfile, btnSkip;
    private ImageView ivProfilePhoto, ivAddPhoto;
    
    // Security Question Fields
    private TextInputLayout tilSecurityQuestion, tilSecurityAnswer;
    private AutoCompleteTextView etSecurityQuestion;
    private TextInputEditText etSecurityAnswer;
    
    private MaterialButton[] bloodTypeButtons;
    private String selectedBloodType = null;
    private MaterialButton selectedButton = null;
    private Calendar selectedDateOfBirth = Calendar.getInstance();
    private String selectedSecurityQuestion = null;
    
    private String currentPhotoPath;
    private Uri photoUri;
    
    // Activity Result Launchers for photo selection
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        userRepository = UserRepository.getInstance(this);
        currentUser = UserHelper.getCurrentUser(this);

        setupPhotoLaunchers();
        initViews();
        loadExistingData();
        setupBloodTypeButtons();
        setupListeners();
    }
    
    private void setupPhotoLaunchers() {
        // Camera launcher
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Photo was taken, load from file
                    if (currentPhotoPath != null) {
                        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                        if (bitmap != null) {
                            // Compress and save
                            Bitmap compressed = compressImage(bitmap, 500);
                            ivProfilePhoto.setImageBitmap(compressed);
                            savePhotoToPrefs(compressed);
                        }
                    }
                }
            }
        );
        
        // Gallery launcher
        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            if (bitmap != null) {
                                Bitmap compressed = compressImage(bitmap, 500);
                                ivProfilePhoto.setImageBitmap(compressed);
                                savePhotoToPrefs(compressed);
                            }
                        } catch (IOException e) {
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        );
    }

    private void initViews() {
        tilName = findViewById(R.id.tilName);
        tilPhone = findViewById(R.id.tilPhone);
        tilLocation = findViewById(R.id.tilLocation);
        tilEmail = findViewById(R.id.tilEmail);
        tilDateOfBirth = findViewById(R.id.tilDateOfBirth);
        tilWeight = findViewById(R.id.tilWeight);
        
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etLocation = findViewById(R.id.etLocation);
        etEmail = findViewById(R.id.etEmail);
        etDateOfBirth = findViewById(R.id.etDateOfBirth);
        etWeight = findViewById(R.id.etWeight);
        
        rgGender = findViewById(R.id.rgGender);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        
        // Security Question Fields
        tilSecurityQuestion = findViewById(R.id.tilSecurityQuestion);
        tilSecurityAnswer = findViewById(R.id.tilSecurityAnswer);
        etSecurityQuestion = findViewById(R.id.etSecurityQuestion);
        etSecurityAnswer = findViewById(R.id.etSecurityAnswer);
        
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnSkip = findViewById(R.id.btnSkip);
        
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        ivAddPhoto = findViewById(R.id.ivAddPhoto);
        
        // Blood type buttons
        bloodTypeButtons = new MaterialButton[]{
                findViewById(R.id.btnBloodAPos),
                findViewById(R.id.btnBloodANeg),
                findViewById(R.id.btnBloodBPos),
                findViewById(R.id.btnBloodBNeg),
                findViewById(R.id.btnBloodABPos),
                findViewById(R.id.btnBloodABNeg),
                findViewById(R.id.btnBloodOPos),
                findViewById(R.id.btnBloodONeg)
        };
    }

    private void loadExistingData() {
        if (currentUser == null) return;
        
        // Load saved photo
        loadSavedPhoto();
        
        // Populate fields from SQLite
        if (!TextUtils.isEmpty(currentUser.getName())) {
            etName.setText(currentUser.getName());
        }
        if (!TextUtils.isEmpty(currentUser.getPhoneNumber())) {
            etPhone.setText(currentUser.getPhoneNumber());
        }
        if (!TextUtils.isEmpty(currentUser.getLocation())) {
            etLocation.setText(currentUser.getLocation());
        }
        if (!TextUtils.isEmpty(currentUser.getEmail())) {
            etEmail.setText(currentUser.getEmail());
        }
        if (!TextUtils.isEmpty(currentUser.getDateOfBirth())) {
            etDateOfBirth.setText(currentUser.getDateOfBirth());
        }
        if (!TextUtils.isEmpty(currentUser.getGender())) {
            if ("Male".equals(currentUser.getGender())) {
                rbMale.setChecked(true);
            } else if ("Female".equals(currentUser.getGender())) {
                rbFemale.setChecked(true);
            }
        }
        if (currentUser.getWeight() != null && currentUser.getWeight() > 0) {
            etWeight.setText(String.valueOf(currentUser.getWeight()));
        }

        // Load security questions/answers if present
        if (!TextUtils.isEmpty(currentUser.getSecurityQuestion())) {
            etSecurityQuestion.setText(currentUser.getSecurityQuestion(), false);
            selectedSecurityQuestion = currentUser.getSecurityQuestion();
        }
        if (!TextUtils.isEmpty(currentUser.getSecurityAnswer())) {
            etSecurityAnswer.setText(currentUser.getSecurityAnswer());
        }
        // Clear any legacy second question data to avoid mismatched expectations
        if (currentUser.getSecurityQuestion2() != null || currentUser.getSecurityAnswer2() != null) {
            currentUser.setSecurityQuestion2(null);
            currentUser.setSecurityAnswer2(null);
            userRepository.updateUser(currentUser);
        }
        
        // Pre-select blood type button
        String bloodType = currentUser.getBloodType();
        if (!TextUtils.isEmpty(bloodType) && !"Unknown".equals(bloodType)) {
            String[] bloodTypes = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
            for (int i = 0; i < bloodTypes.length; i++) {
                if (bloodTypes[i].equals(bloodType)) {
                    selectBloodType(bloodTypeButtons[i], bloodType);
                    break;
                }
            }
        }
    }

    private void setupBloodTypeButtons() {
        String[] bloodTypes = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        
        for (int i = 0; i < bloodTypeButtons.length; i++) {
            final String bloodType = bloodTypes[i];
            final MaterialButton button = bloodTypeButtons[i];
            
            button.setOnClickListener(v -> selectBloodType(button, bloodType));
        }
    }

    private void selectBloodType(MaterialButton button, String bloodType) {
        // Reset previous selection
        if (selectedButton != null) {
            selectedButton.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.transparent));
            selectedButton.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            selectedButton.setStrokeColorResource(R.color.divider);
        }
        
        // Set new selection
        selectedButton = button;
        selectedBloodType = bloodType;
        
        button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary));
        button.setTextColor(ContextCompat.getColor(this, R.color.text_on_primary));
        button.setStrokeColorResource(R.color.primary);
    }

    private void setupListeners() {
        ivAddPhoto.setOnClickListener(v -> showPhotoSelectionDialog());
        ivProfilePhoto.setOnClickListener(v -> showPhotoSelectionDialog());

        // Date of Birth picker
        etDateOfBirth.setOnClickListener(v -> showDatePickerDialog());
        
        // Setup security questions dropdown
        setupSecurityQuestionsDropdown();

        btnSaveProfile.setOnClickListener(v -> saveProfile());

        btnSkip.setOnClickListener(v -> navigateToHome());
    }
    
    private void setupSecurityQuestionsDropdown() {
        String[] securityQuestions = {
            "What is your favorite color?",
            "What is the name of your first pet?"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            securityQuestions
        );
        etSecurityQuestion.setAdapter(adapter);
        etSecurityQuestion.setOnItemClickListener((parent, view, position, id) -> {
            selectedSecurityQuestion = securityQuestions[position];
        });
        etSecurityQuestion.setOnClickListener(v -> etSecurityQuestion.showDropDown());
        etSecurityQuestion.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                etSecurityQuestion.showDropDown();
            }
        });
    }
    
    private void showPhotoSelectionDialog() {
        String[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        
        new AlertDialog.Builder(this)
            .setTitle("Select Profile Photo")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // Take Photo
                        if (checkCameraPermission()) {
                            openCamera();
                        }
                        break;
                    case 1: // Choose from Gallery
                        openGallery();
                        break;
                    case 2: // Cancel
                        dialog.dismiss();
                        break;
                }
            })
            .show();
    }
    
    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            return false;
        }
        return true;
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to take photos", 
                    Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                cameraLauncher.launch(takePictureIntent);
            }
        }
    }
    
    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(new java.util.Date());
        String imageFileName = "PROFILE_" + timeStamp;
        File storageDir = getFilesDir();
        try {
            File image = File.createTempFile(imageFileName, ".jpg", storageDir);
            currentPhotoPath = image.getAbsolutePath();
            return image;
        } catch (IOException e) {
            Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
    
    private void openGallery() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, 
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhotoIntent.setType("image/*");
        galleryLauncher.launch(pickPhotoIntent);
    }
    
    private Bitmap compressImage(Bitmap original, int maxSize) {
        int width = original.getWidth();
        int height = original.getHeight();
        
        float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
        if (ratio >= 1) return original;
        
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);
        
        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
    }
    
    private void savePhotoToPrefs(Bitmap bitmap) {
        try {
            // Save to internal storage
            String fileName = "profile_photo_" + currentUser.getId() + ".jpg";
            FileOutputStream fos = openFileOutput(fileName, MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            
            // Store the path in user profile
            String photoPath = getFilesDir() + "/" + fileName;
            currentUser.setProfileImageUrl(photoPath);
            userRepository.updateUser(currentUser);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void loadSavedPhoto() {
        if (currentUser != null && currentUser.getProfileImageUrl() != null) {
            String photoPath = currentUser.getProfileImageUrl();
            File photoFile = new File(photoPath);
            if (photoFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
                if (bitmap != null) {
                    ivProfilePhoto.setImageBitmap(bitmap);
                }
            }
        }
    }

    private void showDatePickerDialog() {
        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        
        // Create year range (18 to 65 years old)
        int maxYear = currentYear - 18; // Minimum age
        int minYear = currentYear - 65; // Maximum age
        int defaultYear = currentYear - 25; // Default to 25 years old
        
        // Create year array
        String[] years = new String[maxYear - minYear + 1];
        for (int i = 0; i < years.length; i++) {
            years[i] = String.valueOf(maxYear - i);
        }
        
        // Show AlertDialog with year picker using builder
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Select Birth Year")
                .setItems(years, (dialog, which) -> {
                    int selectedYear = maxYear - which;
                    selectedDateOfBirth.set(selectedYear, 0, 1); // Set to January 1st of selected year
                    etDateOfBirth.setText(String.valueOf(selectedYear));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveProfile() {
        // Clear errors
        tilName.setError(null);
        tilPhone.setError(null);
        tilLocation.setError(null);
        tilWeight.setError(null);
        tilSecurityQuestion.setError(null);
        tilSecurityAnswer.setError(null);

        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String location = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String dob = etDateOfBirth.getText() != null ? etDateOfBirth.getText().toString().trim() : "";
        String weightStr = etWeight.getText() != null ? etWeight.getText().toString().trim() : "";
        String securityAnswer = etSecurityAnswer.getText() != null ? etSecurityAnswer.getText().toString().trim() : "";

        // If user typed instead of selecting, sync selected values from text
        if (TextUtils.isEmpty(selectedSecurityQuestion) && !TextUtils.isEmpty(etSecurityQuestion.getText())) {
            selectedSecurityQuestion = etSecurityQuestion.getText().toString();
        }
        // Only one security question supported
        
        String gender = "";
        if (rbMale.isChecked()) gender = "Male";
        else if (rbFemale.isChecked()) gender = "Female";

        boolean isValid = true;

        if (TextUtils.isEmpty(name)) {
            tilName.setError("Name is required");
            isValid = false;
        }

        if (selectedBloodType == null) {
            Toast.makeText(this, "Please select your blood type", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        // Validate first security question
        if (TextUtils.isEmpty(selectedSecurityQuestion)) {
            tilSecurityQuestion.setError("Please select a security question");
            isValid = false;
        }
        
        // Validate first security answer
        if (TextUtils.isEmpty(securityAnswer)) {
            tilSecurityAnswer.setError("Please provide an answer");
            isValid = false;
        } else if (securityAnswer.length() < 3) {
            tilSecurityAnswer.setError("Answer must be at least 3 characters");
            isValid = false;
        }

        // Validate weight - REQUIRED for blood donation safety
        Double weight = null;
        if (TextUtils.isEmpty(weightStr)) {
            tilWeight.setError("Weight is required for blood donation eligibility");
            isValid = false;
        } else {
            try {
                weight = Double.parseDouble(weightStr);
                if (weight < 50) {
                    tilWeight.setError("Sorry, minimum weight is 50kg for safe blood donation");
                    Toast.makeText(this, "Blood donors must weigh at least 50kg for safety reasons", Toast.LENGTH_LONG).show();
                    isValid = false;
                } else if (weight > 200) {
                    tilWeight.setError("Please enter a valid weight");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                tilWeight.setError("Please enter a valid weight in kg");
                isValid = false;
            }
        }

        if (isValid) {
            if (currentUser == null) {
                Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update user object with all profile fields
            currentUser.setName(name);
            currentUser.setPhoneNumber(phone);
            currentUser.setLocation(location);
            currentUser.setBloodType(selectedBloodType);
            currentUser.setDateOfBirth(dob);
            currentUser.setGender(gender);
            currentUser.setWeight(weight);
            currentUser.setSecurityQuestion(selectedSecurityQuestion);
            currentUser.setSecurityAnswer(securityAnswer); // Store answer (in production, hash this)
            currentUser.setSecurityQuestion2(null);
            currentUser.setSecurityAnswer2(null);

            // Save to SQLite
            boolean success = userRepository.updateUser(currentUser);
            
            if (success) {
                Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                navigateToHome();
            } else {
                Toast.makeText(this, "Failed to save profile", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(ProfileSetupActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}