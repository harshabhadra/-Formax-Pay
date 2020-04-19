package com.rechargeweb.rechargeweb.Aeps;


import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.rechargeweb.rechargeweb.Constant.Constants;
import com.rechargeweb.rechargeweb.FileUtils;
import com.rechargeweb.rechargeweb.Keys;
import com.rechargeweb.rechargeweb.Model.AepsLogIn;
import com.rechargeweb.rechargeweb.R;
import com.rechargeweb.rechargeweb.ViewModels.UploadKycViewModel;
import com.rechargeweb.rechargeweb.databinding.ActivityUploadKycBinding;
import com.squareup.picasso.Picasso;
import com.tsongkha.spinnerdatepicker.DatePicker;
import com.tsongkha.spinnerdatepicker.DatePickerDialog;
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class UploadKycFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = UploadKycFragment.class.getSimpleName();
    private ActivityUploadKycBinding uploadKycBinding;
    private static final int RC_ADHAR_IMAGE = 1;
    private static final int RC_PAN_IMAGE = 2;
    Uri adharUri,panUri;
    RequestBody aadharImageBody, panImageBody;

    String name,shopName,dob,email,address,pincode,state,mobile,city,adharNo,panNo;
    UploadKycViewModel uploadKycViewModel;
    String session_id;
    String auth_key;
    String user_id;
    String service_type;

    private SimpleDateFormat simpleDateFormat;
    private int day,month,year;

    private AepsLogIn aepsLogIn;

    public UploadKycFragment(AepsLogIn aepsLogIn) {
        this.aepsLogIn = aepsLogIn;
    }

    //Storage Permission array
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 99;

    public UploadKycFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Initializing DataBinding
        uploadKycBinding = DataBindingUtil.inflate(inflater, R.layout.activity_upload_kyc,container,false);

        //Verify storage permission
        verifyStoragePermissions(getActivity());

        //Getting today's date
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        String today = simpleDateFormat.format(calendar.getTime());
        String[]parts = today.split("/");
        day = Integer.parseInt(parts[0]);
        month = Integer.parseInt(parts[1])-1;
        year = Integer.parseInt(parts[2]);

        //Initializing Auth key
        auth_key = new Keys().apiKey();

        AepsActivity aepsActivity = (AepsActivity)getActivity();
        session_id = aepsActivity.getSession_id();
        service_type = Constants.AEPS_TYPE;

        //Initializing ViewModel
        uploadKycViewModel = new ViewModelProvider(this).get(UploadKycViewModel.class);

        //Setting up toolbar as Support Action Bar
        ((AppCompatActivity)getActivity()).setSupportActionBar(uploadKycBinding.kycToolbar);

        if (aepsLogIn != null) {
            checkUserStatus(aepsLogIn);
        }

        //Adding text watcher to Text input layouts
        uploadKycBinding.kycNameTextInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                uploadKycBinding.kycNameLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

                uploadKycBinding.kycNameLayout.setErrorEnabled(true);
            }
        });

        uploadKycBinding.kycShopNameTextInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                uploadKycBinding.kycShopNameLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

                uploadKycBinding.kycShopNameLayout.setErrorEnabled(true);
            }
        });

        uploadKycBinding.kycEmailTextInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                uploadKycBinding.kycEmailLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

                uploadKycBinding.kycEmailLayout.setErrorEnabled(true);
            }
        });

        uploadKycBinding.kycAddressInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                uploadKycBinding.kycAddressLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
                uploadKycBinding.kycAddressLayout.setErrorEnabled(true);
            }
        });
        uploadKycBinding.kycPincodeInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                uploadKycBinding.kycPincodeLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
                uploadKycBinding.kycPincodeLayout.setErrorEnabled(true);
            }
        });

        uploadKycBinding.kycStateInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                uploadKycBinding.kycStateLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
                uploadKycBinding.kycStateLayout.setErrorEnabled(true);
            }
        });

        uploadKycBinding.kycMobileInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                uploadKycBinding.kycMobileNumberLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
                uploadKycBinding.kycMobileNumberLayout.setErrorEnabled(true);
            }
        });

        uploadKycBinding.kycCityInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                uploadKycBinding.kycCityLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

                uploadKycBinding.kycCityLayout.setErrorEnabled(true);
            }
        });

        uploadKycBinding.kycAadhaarInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                uploadKycBinding.kycAdharnumberLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
                uploadKycBinding.kycAdharnumberLayout.setErrorEnabled(true);
            }
        });

        uploadKycBinding.kycPanNumberInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                uploadKycBinding.kycPanNumberLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

                uploadKycBinding.kycPanNumberLayout.setErrorEnabled(true);
            }
        });

        //Upload aadhar image
        uploadKycBinding.aadharImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_ADHAR_IMAGE);
            }
        });

        //Upload pan image
        uploadKycBinding.panImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(Intent.createChooser(intent,"Complete action using"),RC_PAN_IMAGE);
            }
        });

        //Set onClickListener to saveImage button
        uploadKycBinding.kycSaveImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (aadharImageBody != null && panImageBody != null) {
                    uploadKycBinding.kycImageGroup.setVisibility(View.GONE);
                    uploadKycBinding.kycInputGroup.setVisibility(View.VISIBLE);
                    uploadKycBinding.kycSaveButton.setVisibility(View.VISIBLE);
                    uploadKycBinding.kycSaveImagesButton.setVisibility(View.GONE);
                }else {
                    Toast.makeText(getContext(),"Please Upload Required Images", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Add onClickListener to Dob textView
        uploadKycBinding.kycDobLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showDate(year,month,day,R.style.DatePickerSpinner);
            }
        });

        //On save button clicked
        uploadKycBinding.kycSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uploadKycBinding.kycNameTextInput.getText().toString().isEmpty()) {
                    uploadKycBinding.kycNameLayout.setError("Enter Name");
                } else if (uploadKycBinding.kycShopNameTextInput.getText().toString().isEmpty()) {
                    uploadKycBinding.kycShopNameLayout.setError("Enter Shop Name");
                } else if (uploadKycBinding.kycDobLayout.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Enter Date Of Birth", Toast.LENGTH_SHORT).show();
                } else if (uploadKycBinding.kycEmailTextInput.getText().toString().isEmpty()) {
                    uploadKycBinding.kycEmailLayout.setError("Enter Email Address");
                } else if (uploadKycBinding.kycAddressInputText.getText().toString().isEmpty()) {
                    uploadKycBinding.kycAddressLayout.setError("Enter Address");
                } else if (uploadKycBinding.kycPincodeInputText.getText().toString().isEmpty()) {
                    uploadKycBinding.kycPincodeLayout.setError("Enter Pincode");
                } else {
                    uploadKycBinding.kycInputGroup.setVisibility(View.GONE);
                    uploadKycBinding.kycSaveButton.setVisibility(View.GONE);
                    uploadKycBinding.kycInputGroup2.setVisibility(View.VISIBLE);
                    uploadKycBinding.submitKycButton.setVisibility(View.VISIBLE);
                }
            }
        });

        //Submit button click
        uploadKycBinding.submitKycButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uploadKycBinding.kycInputGroup.setVisibility(View.VISIBLE);
                uploadKycBinding.kycImageGroup.setVisibility(View.VISIBLE);
                uploadKycBinding.kycInputGroup2.setVisibility(View.VISIBLE);

                if (uploadKycBinding.kycNameTextInput.getText().toString().isEmpty()) {
                    uploadKycBinding.kycNameLayout.setError("Enter Name");
                } else if (uploadKycBinding.kycShopNameTextInput.getText().toString().isEmpty()) {
                    uploadKycBinding.kycShopNameLayout.setError("Enter Shop Name");
                } else if (uploadKycBinding.kycDobLayout.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Enter Date Of Birth", Toast.LENGTH_SHORT).show();
                } else if (uploadKycBinding.kycEmailTextInput.getText().toString().isEmpty()) {
                    uploadKycBinding.kycEmailLayout.setError("Enter Email Address");
                } else if (uploadKycBinding.kycAddressInputText.getText().toString().isEmpty()) {
                    uploadKycBinding.kycAddressLayout.setError("Enter Address");
                } else if (uploadKycBinding.kycPincodeInputText.getText().toString().isEmpty()) {
                    uploadKycBinding.kycPincodeLayout.setError("Enter Pincode");
                } else if (uploadKycBinding.kycStateInputText.getText().toString().isEmpty()){
                    uploadKycBinding.kycStateLayout.setError("Enter State Name");
                }else if (uploadKycBinding.kycMobileInputText.getText().toString().isEmpty() || uploadKycBinding.kycMobileInputText.getText().toString().length()<10){
                    uploadKycBinding.kycMobileNumberLayout.setError("Enter Valid Mobile Number");
                }else if (uploadKycBinding.kycCityInputText.getText().toString().isEmpty()){
                    uploadKycBinding.kycCityLayout.setError("Enter City Name");
                }else if (uploadKycBinding.kycAadhaarInputText.getText().toString().isEmpty() || uploadKycBinding.kycAadhaarInputText.getText().toString().length()<12){
                    uploadKycBinding.kycAdharnumberLayout.setError("Enter Valid Aadhaar Number");
                }else if (uploadKycBinding.kycPanNumberInputText.getText().toString().isEmpty()){
                    uploadKycBinding.kycPanNumberLayout.setError("Enter Pan Number");
                }else if (adharUri == null){
                    Toast.makeText(getContext(),"Upload Aadhar Image",Toast.LENGTH_SHORT).show();
                }else if (panUri == null){
                    Toast.makeText(getContext(),"Upload Pan Image", Toast.LENGTH_SHORT).show();
                }else {

                    //Make loading Dialog
                    View layout = getLayoutInflater().inflate(R.layout.loading_dialog, null);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setCancelable(false);
                    builder.setView(layout);
                    final AlertDialog dialog = builder.create();
                    dialog.show();

                    name = uploadKycBinding.kycNameTextInput.getText().toString();
                    shopName = uploadKycBinding.kycShopNameTextInput.getText().toString();
                    dob = uploadKycBinding.kycDobLayout.getText().toString();
                    email = uploadKycBinding.kycEmailTextInput.getText().toString();
                    address = uploadKycBinding.kycAddressInputText.getText().toString();
                    pincode = uploadKycBinding.kycPincodeInputText.getText().toString();
                    state = uploadKycBinding.kycStateInputText.getText().toString();
                    mobile = uploadKycBinding.kycMobileInputText.getText().toString();
                    city = uploadKycBinding.kycCityInputText.getText().toString();
                    adharNo = uploadKycBinding.kycAadhaarInputText.getText().toString();
                    panNo = uploadKycBinding.kycPanNumberInputText.getText().toString();

                    uploadKycViewModel.submitKyc(session_id,auth_key,name,shopName,dob,email,address,pincode
                            ,state,mobile,city,adharNo,panNo, aadharImageBody, panImageBody,service_type)
                            .observe(getViewLifecycleOwner(), new Observer<String>() {
                                @Override
                                public void onChanged(String s) {

                                    dialog.dismiss();
                                    Toast.makeText(getContext(),s,Toast.LENGTH_SHORT).show();
                                    Log.e(TAG,"Status: " + s);
                                    getActivity().finish();
                                }
                            });
                }
            }
        });

       return uploadKycBinding.getRoot();
    }

    //Check User Status
    private void  checkUserStatus(AepsLogIn aepsLogIn){
        Log.e(TAG,"aeps status:" + aepsLogIn.getStatus());
        if (aepsLogIn.getStatus().equals("Rejected") || aepsLogIn.getStatus().equals("REJECTED")){

            String reject = "ApplicationTest Rejected : " + aepsLogIn.getRemark();
            uploadKycBinding.remarkTv.setText(reject);

        }else if (aepsLogIn.getStatus().equals("Processing") || aepsLogIn.getStatus().equals("PROCESSING")){
            AlertDialog.Builder successBuilder = new AlertDialog.Builder(getContext());
            successBuilder.setCancelable(false);
            successBuilder.setMessage("You've successfully submitted your KYC details, Please wait for approval.");
            successBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                    getActivity().finish();
                }
            });

            AlertDialog alertDialog = successBuilder.create();
            alertDialog.show();
        }
    }

    //Setting up the date picker
    void showDate(int year, int monthOfYear, int dayOfMonth, int spinnerTheme) {
        new SpinnerDatePickerDialogBuilder()
                .context(getContext())
                .callback(this)
                .spinnerTheme(spinnerTheme)
                .defaultDate(year, monthOfYear, dayOfMonth)
                .build()
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Get Aadhaar Image
        if (requestCode == RC_ADHAR_IMAGE && resultCode == RESULT_OK){
            adharUri = data.getData();
            Picasso.get().load(adharUri).placeholder(R.drawable.add_cred_img2).into(uploadKycBinding.kycAadhaarImageView);
            //creating a file
            File file = new File(FileUtils.getPath(adharUri,getContext()));
            aadharImageBody = RequestBody.create(MediaType.parse(getActivity().getContentResolver().getType(adharUri)), file);

            //Get Pan Image
        }else if (requestCode == RC_PAN_IMAGE && resultCode == RESULT_OK){
            panUri = data.getData();
            //creating a file
            if (panUri != null) {
                Picasso.get().load(panUri).placeholder(R.drawable.add_cred_img2).into(uploadKycBinding.kycPanImageView);
                File file = new File(FileUtils.getPath(panUri, getContext()));
                panImageBody = RequestBody.create(MediaType.parse(getActivity().getContentResolver().getType(panUri)), file);
            }
        }else if (resultCode == RESULT_CANCELED){
            Log.e(TAG,"No image selected");
        }
    }

    //Method to verify storage permission
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    MY_PERMISSIONS_REQUEST_READ_STORAGE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_STORAGE){
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // location-related task you need to do.
                if (ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {

                    //Request location updates:
                }

            } else {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(false);
                builder.setMessage("We need to access your Location, Please allow location Permission");
                builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Prompt the user once explanation has been shown
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_STORAGE);
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }


    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = new GregorianCalendar(year,monthOfYear,dayOfMonth);
        String today = simpleDateFormat.format(calendar.getTime());
        uploadKycBinding.kycDobLayout.setText(today);
    }
}
