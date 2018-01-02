package uber.com.uber.views;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import uber.com.uber.R;
import uber.com.uber.model.User;
import uber.com.uber.utils.CommonUtils;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    private Button btnSignIn, btnRegister;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase db;
    private DatabaseReference users;
    private RelativeLayout mRootLayout;
    private MaterialEditText mMaterialEdtName, mMaterialEdtPassword, mMaterialEdtPhone, mMaterialEdtEmail;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        setContentView(R.layout.activity_main);

        initUI();
        initFirebase();

    }

    private void initUI() {
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        mRootLayout = (RelativeLayout) findViewById(R.id.relRootlayout);

        btnRegister.setOnClickListener(onClickListener);
        btnSignIn.setOnClickListener(onClickListener);
    }

    private void initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
                case R.id.btnRegister:
                    showRegisterUserDialog();
                    break;
                case R.id.btnSignIn:
                    showSignInUserDialog();
                    break;
            }
        }
    };

    private void showRegisterUserDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("REGISTER");
        dialog.setMessage("Please use email to register");
        LayoutInflater inflater = LayoutInflater.from(this);
        View register_layout = inflater.inflate(R.layout.layout_register, null);

        mMaterialEdtEmail = register_layout.findViewById(R.id.edtEmail);
        mMaterialEdtPassword = register_layout.findViewById(R.id.edtPassword);
        mMaterialEdtName = register_layout.findViewById(R.id.edtName);
        mMaterialEdtPhone = register_layout.findViewById(R.id.edtPhone);


        dialog.setView(register_layout);

        dialog.setPositiveButton("Register", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        validateRegisterInput();
                    }
                }
        );

        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();
    }
    private void showSignInUserDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("SIGN IN");
        LayoutInflater inflater = LayoutInflater.from(this);
        View signin_layout = inflater.inflate(R.layout.layout_singin, null);

        mMaterialEdtEmail = signin_layout.findViewById(R.id.edtEmail);
        mMaterialEdtPassword = signin_layout.findViewById(R.id.edtPassword);



        dialog.setView(signin_layout);

        dialog.setPositiveButton("Sign In", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        validateSignInInput();
                    }
                }
        );

        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();
    }

    private void validateRegisterInput() {

        if (!validateEmail()) {

        }
        if (!validateName()) {

        }
        if (!validatePassword()) {

        }
        if (!validatePhone()) {

        } else {
            registerUser();
        }

    }
    private void validateSignInInput() {

        if (!validateEmail()) {

        }

        if (!validatePassword()) {

        }
       else {
            signInUser();
        }

    }
    private boolean validateEmail() {
        if (TextUtils.isEmpty(mMaterialEdtEmail.getText().toString())) {

            CommonUtils.showSnackBarNoAction(mRootLayout, "Please Enter Email");
            return false;
        } else {
            return true;
        }
    }

    private boolean validateName() {
        if (TextUtils.isEmpty(mMaterialEdtName.getText().toString())) {

            CommonUtils.showSnackBarNoAction(mRootLayout, "Please Enter Name");
            return false;
        } else {
            return true;
        }
    }

    private boolean validatePassword() {
        if (TextUtils.isEmpty(mMaterialEdtPassword.getText().toString())) {

            CommonUtils.showSnackBarNoAction(mRootLayout, "Please Enter Password");
            return false;
        } else if (mMaterialEdtPassword.getText().toString().length() < 6) {
            CommonUtils.showSnackBarNoAction(mRootLayout, "Password too short");
            return false;
        } else {
            return true;
        }
    }

    private boolean validatePhone() {
        if (TextUtils.isEmpty(mMaterialEdtPhone.getText().toString())) {

            CommonUtils.showSnackBarNoAction(mRootLayout, "Please Enter Phone");
            return false;
        } else {
            return true;
        }
    }

    private void registerUser() {

        firebaseAuth.createUserWithEmailAndPassword(mMaterialEdtEmail.getText().toString(), mMaterialEdtPassword.getText().toString())
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        User userObj = new User();
                        userObj.setEmail(mMaterialEdtEmail.getText().toString());
                        userObj.setPassword(mMaterialEdtPassword.getText().toString());
                        userObj.setName(mMaterialEdtName.getText().toString());
                        userObj.setPhone(mMaterialEdtPhone.getText().toString());


                        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(userObj)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        CommonUtils.showSnackBarNoAction(mRootLayout, "Registered Successfully");

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {

                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        CommonUtils.showSnackBarNoAction(mRootLayout, "Registration Failed" + e.getMessage());

                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        CommonUtils.showSnackBarNoAction(mRootLayout, "Registration Failed" + e.getMessage());

                    }
                });

    }

    private void signInUser() {

        firebaseAuth.signInWithEmailAndPassword(mMaterialEdtEmail.getText().toString(),mMaterialEdtPassword.getText().toString())
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        startActivity(new Intent(MainActivity.this,WelcomeActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        CommonUtils.showSnackBarNoAction(mRootLayout, "Sign In Failed" + e.getMessage());

                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        CommonUtils.showSnackBarNoAction(mRootLayout, "Sign In Failed" + e.getMessage());

                    }
                });

    }
}
