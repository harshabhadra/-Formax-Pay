package com.rechargeweb.rechargeweb.Aeps;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.rechargeweb.rechargeweb.Constant.Constants;
import com.rechargeweb.rechargeweb.Keys;
import com.rechargeweb.rechargeweb.Model.AepsLogIn;
import com.rechargeweb.rechargeweb.R;
import com.rechargeweb.rechargeweb.ViewModels.AepsViewModel;

public class AepsActivity extends AppCompatActivity {

    private String session_id, user_id, auth_key;
    private AepsViewModel aepsViewModel;
    private String bank;

    private FragmentTransaction fragmentTransaction;
    private AlertDialog alertDialog;
    private static final String TAG = AepsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aeps);

        //Getting values from Intent
        Intent intent = getIntent();
        session_id = intent.getStringExtra(Constants.SESSION_ID);
        user_id = intent.getStringExtra(Constants.USER_ID);
        bank = intent.getStringExtra(Constants.BANK);

        //Initializing Auth Key
        auth_key = new Keys().apiKey();
        String service_type = "YBL_AEPS";

        fragmentTransaction = getSupportFragmentManager().beginTransaction();

        //Initializing AepsViewModel
        aepsViewModel = new ViewModelProvider(this).get(AepsViewModel.class);

        //Create Loading Dialog
        alertDialog = createAlertDialog(this);

        aepsLogIn(session_id, service_type, auth_key);
    }

    //Log in aeps
    private void aepsLogIn(String session_id, String serviceType, String auth_key) {
        aepsViewModel.aepsLogIn(session_id, serviceType, auth_key).observe(this, new Observer<AepsLogIn>() {
            @Override
            public void onChanged(AepsLogIn aepsLogIn) {

                alertDialog.dismiss();
                if (aepsLogIn != null) {
                    Log.e(TAG,"aeps is not null");
                    if (aepsLogIn.getStatus().equals("Approved") || aepsLogIn.getStatus().equals("APPROVED")) {
                        if (bank.equals(Constants.YES_BANK)) {
                            YblAepsFragment yblAepsFragment = new YblAepsFragment();
                            fragmentTransaction.replace(R.id.aeps_container, yblAepsFragment).commit();
                        }else {
                            IciciAepsFragment iciciAepsFragment = new IciciAepsFragment();
                            fragmentTransaction.replace(R.id.aeps_container,iciciAepsFragment).commit();
                        }
                    } else {
                        UploadKycFragment uploadKycFragment = new UploadKycFragment(aepsLogIn);
                        fragmentTransaction.replace(R.id.aeps_container, uploadKycFragment).commit();
                    }
                }else {
                    Log.e(TAG,"aeps is null");
                }
            }
        });
    }

    //Create Loading Dialog
    private AlertDialog createAlertDialog(Context context) {

        View view = getLayoutInflater().inflate(R.layout.loading_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setView(view);
        return builder.create();
    }

    public String getSession_id() {
        return session_id;
    }

    public String getUser_id() {
        return user_id;
    }
}
