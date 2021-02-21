package com.bose.legends;

import androidx.annotation.NonNull;
import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import javax.crypto.spec.SecretKeySpec;

public class SignIn extends AppCompatActivity
{
    private EditText email, password;
    private SignIn context;
    private CheckBox rememberMe;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); //hide the title bar

        setContentView(R.layout.activity_sign_in);

        context = this;
        email = findViewById(R.id.email); password = findViewById(R.id.password);
        rememberMe = findViewById(R.id.remember);

        email.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (!hasFocus)
                {
                    SignUp.validateEmail(email, email.getText().toString());
                }
            }
        });

        password.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                SignUp.validatePassword(password, password.getText().toString());
            }
        });
    }

    public void signIn(View view)
    {
        String sEmail = email.getText().toString();
        String sPassword = password.getText().toString();
        boolean remember = rememberMe.isChecked();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        if (SignUp.validateEmail(email, sEmail) || SignUp.validatePassword(password, sPassword))
            return;

        AlertDialog dialog = new BuildAlertMessage().buildAlertIndeterminateProgress(context, true);

        mAuth.signInWithEmailAndPassword(sEmail, sPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if (task.isComplete())
                {
                    Log.d("xyz", "Yay signed in");
                    Intent intent = new Intent(context, MainActivity.class);

                    if (mAuth.getUid() == null)
                    {
                        dialog.dismiss();
                        Toast.makeText(context, "Incorrect email/password", Toast.LENGTH_LONG).show();
                        return;
                    }

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference docRef = db.collection("users").document(mAuth.getUid());
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task)
                        {
                            if (task.isSuccessful())
                            {
                                DocumentSnapshot doc = task.getResult();

                                if (doc.exists())
                                {
                                    SharedPreferences pref = getSharedPreferences("com.bose.legends.user_details", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putString("username", doc.getString("username"));
                                    editor.putString("email", sEmail);
                                    editor.putBoolean("remember", remember);
                                    editor.apply();

                                    dialog.dismiss();

                                    startActivity(intent);
                                    SignUp.context.finish();
                                    finish();
                                }
                                else
                                {
                                    dialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "Something went wrong, try again.", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else
                            {
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Something went wrong, try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else
                {
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Something went wrong, try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}