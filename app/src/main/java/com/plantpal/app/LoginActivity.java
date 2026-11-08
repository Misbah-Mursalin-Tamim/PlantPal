package com.plantpal.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.plantpal.app.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();

        // Already logged in from a previous session? Skip this screen.
        if (auth.getCurrentUser() != null) {
            goToMain();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnLogin.setOnClickListener(v -> attemptAuth(false));
        binding.btnSignUp.setOnClickListener(v -> attemptAuth(true));
    }

    // isSignUp = true  -> create a new account
    // isSignUp = false -> log in to an existing account
    private void attemptAuth(boolean isSignUp) {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError("Enter a valid email");
            return;
        }
        if (password.length() < 6) {
            binding.etPassword.setError("Password must be at least 6 characters");
            return;
        }

        setLoading(true);

        if (isSignUp) {
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        setLoading(false);
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
                            goToMain();
                        } else {
                            showError(task.getException());
                        }
                    });
        } else {
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        setLoading(false);
                        if (task.isSuccessful()) {
                            goToMain();
                        } else {
                            showError(task.getException());
                        }
                    });
        }
    }

    private void showError(Exception e) {
        String message = (e != null && e.getMessage() != null) ? e.getMessage() : "Something went wrong";
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // Disables the buttons and shows the spinner while waiting for Firebase
    private void setLoading(boolean loading) {
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!loading);
        binding.btnSignUp.setEnabled(!loading);
    }

    // Explicit Intent to MainActivity. The flags clear the back stack so the
    // Back button won't return to the login screen.
    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}