package com.rechargeweb.rechargeweb.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.rechargeweb.rechargeweb.Adapters.BeneficiaryAdapter;
import com.rechargeweb.rechargeweb.Constant.Constants;
import com.rechargeweb.rechargeweb.Keys;
import com.rechargeweb.rechargeweb.Model.AddBeneficiary;
import com.rechargeweb.rechargeweb.Model.Beneficiary;
import com.rechargeweb.rechargeweb.Model.Remitter;
import com.rechargeweb.rechargeweb.R;
import com.rechargeweb.rechargeweb.ViewModels.BeneficiaryViewModel;
import com.rechargeweb.rechargeweb.ViewModels.DmtViewModel;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RemitterActivity extends AppCompatActivity {

    TextView remitterName, remitterNumber, consumedLimit, remaninngLimit;
    RecyclerView recyclerView;

    TextView beneficiaryTextView;
    ProgressBar beneficiaryLoading;
    private String auth;
    private String session_id;

    private String mobile;
    String userName;
    String remitter_id;

    private boolean isValidIfsc;

    boolean getName;

    DmtViewModel dmtViewModel;
    BeneficiaryAdapter beneficiaryAdapter;

    AlertDialog dialog;
    FloatingActionButton addBeneficiaryButton;

    BeneficiaryViewModel beneficiaryViewModel;

    private static final String TAG = RemitterActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remitter);

        auth = new Keys().apiKey();

        //Initializing ViewModel classes
        beneficiaryViewModel = new ViewModelProvider(this).get(BeneficiaryViewModel.class);
        dmtViewModel = new ViewModelProvider(this).get(DmtViewModel.class);

        remitterName = findViewById(R.id.remitter_name);
        remitterNumber = findViewById(R.id.remitter_phone);

        consumedLimit = findViewById(R.id.consumed_limit_tv);
        remaninngLimit = findViewById(R.id.remaining_limit_tv);

        beneficiaryTextView = findViewById(R.id.beneficiary_error_tv);
        beneficiaryLoading = findViewById(R.id.beneficiary_loading);

        addBeneficiaryButton = findViewById(R.id.add_beneficiary_fab);

        //Get Intent
        Intent intent = getIntent();
        if (intent.hasExtra(Constants.REMITTER_MOBILE)) {
            session_id = intent.getStringExtra(Constants.SESSION_ID);

            View layout = getLayoutInflater().inflate(R.layout.loading_dialog, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(layout);
            builder.setCancelable(false);
            dialog = builder.create();
            dialog.show();

            mobile = intent.getStringExtra(Constants.REMITTER_MOBILE);

            //Initializing recyclerView
            recyclerView = findViewById(R.id.remitter_recyclerView);
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(linearLayoutManager);
            beneficiaryAdapter = new BeneficiaryAdapter(this, auth,session_id,mobile);
            recyclerView.setAdapter(beneficiaryAdapter);
            getRemitterDetails(auth, mobile);
            getBeneficiaryList(auth, mobile);
        }


        //Add new beneficiary
        addBeneficiaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBeneficiary();
            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    //Get remitter details
    private void getRemitterDetails(String auth, String number) {

        dmtViewModel.getRemitterDetails(auth, number).observe(RemitterActivity.this, new Observer<Remitter>() {
            @Override
            public void onChanged(Remitter remitter) {
                dialog.dismiss();
                if (remitter != null) {

                    if (remitter.getName() != null) {
                        Log.e(TAG, "Remitter details received");
                        remitter_id = remitter.getBeneficiary_id();
                        beneficiaryAdapter.setRemId(remitter_id);
                        String name = remitter.getName();
                        String parts[] = name.split("\\s+");
                        userName = parts[0];
                        String number = remitter.getMobile();
                        remitterName.setText(userName);
                        remitterNumber.setText(number);
                        String conLim = "Rs/- " + String.valueOf(remitter.getConsumed_limit());
                        String remLim = "Rs/- " + String.valueOf(remitter.getRemaining_limit());
                        consumedLimit.setText(conLim);
                        remaninngLimit.setText(remLim);
                    } else {
                        Toast.makeText(getApplicationContext(), remitter.getBeneficiary_id(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Error Fetching Details", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Get beneficiary list
    private void getBeneficiaryList(String auth, String number) {

        beneficiaryLoading.setVisibility(View.VISIBLE);
        dmtViewModel.getBeneficiaryDetails(auth, number).observe(RemitterActivity.this, new Observer<List<Beneficiary>>() {
            @Override
            public void onChanged(List<Beneficiary> beneficiaries) {
                beneficiaryLoading.setVisibility(View.GONE);
                if (beneficiaries != null) {
                    Log.e(TAG, "Beneficiary details received");
                    if (beneficiaries.get(0).getId().equals("Add Beneficiary")) {
                        Toast.makeText(getApplicationContext(), "Add Beneficiaries", Toast.LENGTH_SHORT).show();
                        beneficiaryTextView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        beneficiaryTextView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        beneficiaryAdapter.setBeneficiaryList(beneficiaries);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Error Fetching Beneficiary Details", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Add beneficiary
    private void addBeneficiary() {

        View layout = getLayoutInflater().inflate(R.layout.add_beneficiary, null);

        final TextInputLayout nameLayout = layout.findViewById(R.id.enter_name_ben_layout);
        final TextInputLayout ifscLayout = layout.findViewById(R.id.ifsc_code_ben_layout);
        final TextInputLayout accountLayout = layout.findViewById(R.id.account_number_ben_layout);
        final TextInputLayout confirmAccountLayout = layout.findViewById(R.id.confirm_account_ben_layout);

        final TextInputEditText name = layout.findViewById(R.id.enter_name_ben_text_input);
        final TextInputEditText ifsc = layout.findViewById(R.id.ifsc_code_text_input);
        final TextInputEditText account = layout.findViewById(R.id.account_number_input_text);
        final TextInputEditText confirmAccont = layout.findViewById(R.id.confirm_account_input_text);
        final ProgressBar progressBar = layout.findViewById(R.id.add_benificiary_loading);
        final TextView getNameTv = layout.findViewById(R.id.get_name_tv);

        final Button submitBeneficiaryButton = layout.findViewById(R.id.submit_beneficiary_button);
        ImageView closeDialog = layout.findViewById(R.id.close_add_ben_dialog);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AddBeneficiaryDialog);
        builder.setView(layout);

        final AlertDialog dialog = builder.create();
        dialog.show();


        //Set onClickListener to close image
        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        //Adding Text Watcher to all text input Edit text
        confirmAccont.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                confirmAccountLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

                confirmAccountLayout.setErrorEnabled(true);
            }
        });
        account.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                accountLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

                accountLayout.setErrorEnabled(true);
            }
        });
        ifsc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                ifscLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
                ifscLayout.setErrorEnabled(true);
                if (s.length()>0){
                    getNameTv.setEnabled(true);
                }else {
                    getNameTv.setEnabled(true);
                }
            }
        });

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                nameLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
                nameLayout.setErrorEnabled(true);
            }
        });

        //On Get Name button clicked
        getNameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String ifscS = ifsc.getText().toString().toUpperCase();
                String accountS = account.getText().toString();
                String conAccount = confirmAccont.getText().toString();
                isValidIfsc = validateIfsc(ifscS);

                if (ifscS.isEmpty() || (!isValidIfsc)) {
                    ifscLayout.setError("Enter IFSC code");
                } else if (accountS.isEmpty()) {
                    accountLayout.setError("Enter Account Number");
                } else if (conAccount.isEmpty()) {
                    confirmAccountLayout.setError("Confirm Account Number");
                } else if (!accountS.equals(conAccount)) {
                    accountLayout.setError("Account Number doesn't match");
                    confirmAccountLayout.setError("Account Number doesn't match");
                }else {
                    getNameTv.setEnabled(false);
                    submitBeneficiaryButton.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    beneficiaryViewModel.validateAccount(session_id, auth, accountS, ifscS, mobile).observe(RemitterActivity.this, new Observer<AddBeneficiary>() {
                        @Override
                        public void onChanged(AddBeneficiary addBeneficiary) {
                            getNameTv.setEnabled(true);
                            submitBeneficiaryButton.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            if (addBeneficiary != null) {
                                Log.e(TAG, "Beneficiary name is Here");
                                String message = addBeneficiary.getMessage();
                                if (message.equals("Transaction Successful")) {
                                    String n = addBeneficiary.getStatus();

                                    if (!n.isEmpty()) {
                                        name.setText(n);
                                    } else {
                                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                Log.e(TAG, "Beneficiary name is null");
                            }
                        }
                    });
                }

            }
        });

        //On Submit button click
        submitBeneficiaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nameS = name.getText().toString();
                String ifscS = ifsc.getText().toString().toUpperCase();
                String accountS = account.getText().toString();
                String conAccount = confirmAccont.getText().toString();
                isValidIfsc = validateIfsc(ifscS);

                if (ifscS.isEmpty()) {
                    ifscLayout.setError("Enter IFSC code");
                } else if (accountS.isEmpty()) {
                    accountLayout.setError("Enter Account Number");
                } else if (conAccount.isEmpty()) {
                    confirmAccountLayout.setError("Confirm Account Number");
                } else if (nameS.isEmpty()) {
                    nameLayout.setError("Enter Name");
                } else if (!accountS.equals(conAccount)) {
                    accountLayout.setError("Account Number doesn't match");
                    confirmAccountLayout.setError("Account Number doesn't match");
                } else if (!isValidIfsc){
                    ifscLayout.setError("Enter Valid IFSC Code");
                }else {
                    InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputMethodManager != null) {
                        inputMethodManager.hideSoftInputFromWindow(submitBeneficiaryButton.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                    progressBar.setVisibility(View.VISIBLE);
                    submitBeneficiaryButton.setVisibility(View.GONE);

                    beneficiaryViewModel.addBeneficiary(auth, mobile, remitter_id, nameS, ifscS, accountS).observe(RemitterActivity.this, new Observer<AddBeneficiary>() {
                        @Override
                        public void onChanged(AddBeneficiary addBeneficiary) {
                            progressBar.setVisibility(View.GONE);
                            submitBeneficiaryButton.setVisibility(View.VISIBLE);
                            if (addBeneficiary != null) {

                                Log.e(TAG, "Beneficiary is not null");
                                String message = addBeneficiary.getMessage();
                                if (message.equals("Transaction Successful")) {
                                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(RemitterActivity.this,RemitterActivity.class);
                                    intent.putExtra(Constants.REMITTER_MOBILE,mobile);
                                    intent.putExtra(Constants.SESSION_ID,session_id);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(RemitterActivity.this,RemitterActivity.class);
                                    intent.putExtra(Constants.REMITTER_MOBILE,mobile);
                                    intent.putExtra(Constants.SESSION_ID,session_id);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                                    startActivity(intent);
                                    finish();
                                }
                            } else {
                                Log.e(TAG, "Beneficiary not added");
                            }
                        }
                    });
                }
            }
        });
    }

    //Check if IFSC code is valid
    private boolean validateIfsc(String s){
        Pattern pattern=Pattern.compile("^[A-Za-z]{4}0[A-Z0-9a-z]{6}$");
        Matcher matcher=pattern.matcher(s);
        return matcher.matches();
    }

}
