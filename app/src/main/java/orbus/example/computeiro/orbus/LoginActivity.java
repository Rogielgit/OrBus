package orbus.example.computeiro.orbus;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends BaseActivity implements
	View.OnClickListener {

	private static final String TAG = "EmailPassword";
    private Toolbar toolbar;

	private TextView mStatusTextView;
	private TextView mDetailTextView;
	private EditText mEmailField;
	private EditText mPasswordField;

	// [START declare_auth]
	private FirebaseAuth mAuth;
	// [END declare_auth]
	// [START declare_auth_listener]
	private FirebaseAuth.AuthStateListener mAuthListener;
	// [END declare_auth_listener]

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_emailpassword);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


		// Views
		mStatusTextView = (TextView) findViewById(R.id.status);
		mDetailTextView = (TextView) findViewById(R.id.detail);
		mEmailField = (EditText) findViewById(R.id.field_email);
		mPasswordField = (EditText) findViewById(R.id.field_password);

		// Buttons
		findViewById(R.id.email_sign_in_button).setOnClickListener(this);
		findViewById(R.id.email_create_account_button).setOnClickListener(this);
		findViewById(R.id.sign_out_button).setOnClickListener(this);



		//[START initialize_auth]
		mAuth = FirebaseAuth.getInstance();
		// [END initialize_auth]

		// [START auth_state_listener]

		mAuthListener = new FirebaseAuth.AuthStateListener() {
			@Override
			public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
				FirebaseUser user = firebaseAuth.getCurrentUser();
				if (user != null) {
					// User is signed in
					Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
				} else {
					// User is signed out
					Log.d(TAG, "onAuthStateChanged:signed_out");
				}
				// [START_EXCLUDE]
				updateUI(user);
				// [END_EXCLUDE]
			}
		};
		// [END auth_state_listener]9

	}

	// [START on_start_add_listener]
	@Override
	public void onStart() {
		super.onStart();
		mAuth.addAuthStateListener(mAuthListener);
	}
	// [END on_start_add_listener]

	// [START on_stop_remove_listener]
	@Override
	public void onStop() {
		super.onStop();
		if (mAuthListener != null) {
			mAuth.removeAuthStateListener(mAuthListener);
		}
	}
	// [END on_stop_remove_listener]

	private void createAccount(String email, String password) {
		Log.d(TAG, "createAccount:" + email);
		if (!validateForm()) {
			return;
		}
		showProgressDialog();

		// [START create_user_with_email]
		mAuth.createUserWithEmailAndPassword(email, password)
			.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
				@Override
				public void onComplete(@NonNull Task<AuthResult> task) {
					Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

					// If sign in fails, display a message to the user. If sign in succeeds
					// the auth state listener will be notified and logic to handle the
					// signed in user can be handled in the listener.
					if (!task.isSuccessful()) {
						Toast.makeText(LoginActivity.this, R.string.auth_failed,
							Toast.LENGTH_SHORT).show();
					}

					// [START_EXCLUDE]
					hideProgressDialog();
					// [END_EXCLUDE]
				}
			});
		// [END create_user_with_email]
	}

	private void signIn(String email, String password) {
		Log.d(TAG, "signIn:" + email);
		if (!validateForm()) {
			return;
		}

		showProgressDialog();

		// [START sign_in_with_email]
		mAuth.signInWithEmailAndPassword(email, password)
			.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
				@Override
				public void onComplete(@NonNull Task<AuthResult> task) {
					Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

					// If sign in fails, display a message to the user. If sign in succeeds
					// the auth state listener will be notified and logic to handle the
					// signed in user can be handled in the listener.
					if (!task.isSuccessful()) {
						Log.w(TAG, "signInWithEmail:failed", task.getException());
						Toast.makeText(LoginActivity.this, R.string.auth_failed,
							Toast.LENGTH_SHORT).show();
					}

					// [START_EXCLUDE]
					if (!task.isSuccessful()) {
						mStatusTextView.setText(R.string.auth_failed);
					}
					hideProgressDialog();
					// [END_EXCLUDE]
				}
			});
		// [END sign_in_with_email]

	}

	private void signOut() {
		mAuth.signOut();
		updateUI(null);
	}

	public FirebaseAuth getAuth() {
		return mAuth;
	}

	public void setmAuth(FirebaseAuth mAuth) {
		this.mAuth = mAuth;
	}

	private boolean validateForm() {
		boolean valid = true;

		String email = mEmailField.getText().toString();
		if (TextUtils.isEmpty(email)) {
			mEmailField.setError("Required.");
			valid = false;
		} else {
			mEmailField.setError(null);
		}

		String password = mPasswordField.getText().toString();
		if (TextUtils.isEmpty(password)) {
			mPasswordField.setError("Required.");
			valid = false;
		} else {
			mPasswordField.setError(null);
		}

		return valid;
	}

	private void updateUI(FirebaseUser user) {
		hideProgressDialog();
		if (user != null) {
			// mStatusTextView.setText(getString(R.string.emailpassword_status_fmt, user.getEmail()));
			//mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));
			showProgressDialog();
						/*findViewById(R.id.email_password_buttons).setVisibility(View.GONE);
            findViewById(R.id.email_password_fields).setVisibility(View.GONE);
            findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);*/

			Intent it = new Intent(LoginActivity.this, PesquisarActivity.class);
			startActivity(it);
		}
	}

	@Override
	public void onClick(View v) {
		int i = v.getId();
		if (i == R.id.email_create_account_button) {
			createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
		} else if (i == R.id.email_sign_in_button) {
			signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
		}
	}
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
		if(id ==R.id.aboutProjet)
		{
			setContentView(R.layout.activity_sobre);
			//return true;
		}
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menulogin, menu);
        return true;
    }
}


