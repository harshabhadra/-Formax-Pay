package com.rechargeweb.rechargeweb.Activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.rechargeweb.rechargeweb.BottomSheetFrag.BottomSheetFragment;
import com.rechargeweb.rechargeweb.BottomSheetFrag.DTHSheetFragment;
import com.rechargeweb.rechargeweb.BottomSheetFrag.DthCustomerInfoBottomSheet;
import com.rechargeweb.rechargeweb.BottomSheetFrag.PostPaidSheet;
import com.rechargeweb.rechargeweb.Constant.Constants;
import com.rechargeweb.rechargeweb.Keys;
import com.rechargeweb.rechargeweb.Model.FetchOperator;
import com.rechargeweb.rechargeweb.Model.Items;
import com.rechargeweb.rechargeweb.Model.Prepaid;
import com.rechargeweb.rechargeweb.Model.Recharge;
import com.rechargeweb.rechargeweb.R;
import com.rechargeweb.rechargeweb.ViewModels.PlanViewModel;
import com.rechargeweb.rechargeweb.ViewModels.RechargeViewModel;
import com.squareup.picasso.Picasso;

import mehdi.sakout.fancybuttons.FancyButton;

public class RechargeActivity extends AppCompatActivity implements BottomSheetFragment.OnPrepaidListClickLietener,
        PostPaidSheet.OnPostPaidClickListener, DTHSheetFragment.OnDthSheetItemClickListener {

    private static final int RC_PLAN_DETAILS = 123;
    public static final int PICK_CONTACT = 1337;

    private static final String TAG = RechargeActivity.class.getSimpleName();
    public static final int PERMISSION_REQUEST_CONTACT = 1338;
    String name;
    RechargeViewModel rechargeViewModel;
    private PlanViewModel planViewModel;

    String session_id;

    EditText numberEditText;
    EditText amountEditText;
    MaterialButton rechargeButton;
    TextView providerText;
    ImageView providerImage;
    TextView selectType;

    ImageView contactImage;
    TextView browseTextView;
    private Toolbar rechargeToolbar;
    private AlertDialog loadingDialog;

    String auth;
    String providerName;
    String providerId;
    String mobileNumber;
    String number;
    private String circleName;

    String amount;

    private boolean isPostPaid = false;
    String layoutName;

    @Override
    protected void onResume() {
        super.onResume();

        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move_from_left);

        rechargeButton.setAnimation(animation);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recharge);

        auth = new Keys().apiKey();

        numberEditText = findViewById(R.id.mobile_number);
        amountEditText = findViewById(R.id.amount);
        rechargeButton = findViewById(R.id.recharge_button);
        providerText = findViewById(R.id.provider_name);
        providerImage = findViewById(R.id.provider_logo);

        contactImage = findViewById(R.id.contact_image);
        browseTextView = findViewById(R.id.browse_plans_text);
        selectType = findViewById(R.id.select_wallet);

        rechargeToolbar = findViewById(R.id.recharge_toolbar);
        setSupportActionBar(rechargeToolbar);
        rechargeToolbar.setTitleTextColor(Color.WHITE);

        //Initializing RechargeViewModel Class
        rechargeViewModel = ViewModelProviders.of(this).get(RechargeViewModel.class);

        //Initializing PlanViewModel Class
        planViewModel = ViewModelProviders.of(this).get(PlanViewModel.class);

        //Getting intent
        final Intent intent = getIntent();
        if (intent.hasExtra(Constants.RECHARGE)) {

            layoutName = intent.getStringExtra(Constants.LAYOUT_NAME);

            //Getting the session id from intent
            session_id = intent.getStringExtra(Constants.SESSION_ID);

            if (layoutName.equals("Mobile")) {
                selectType.setVisibility(View.VISIBLE);
            } else if (layoutName.equals("DTH")) {
                selectType.setVisibility(View.GONE);
                browseTextView.setText("Customer Info");
            }
            if (intent.hasExtra(Constants.ITEM_POSITION)) {
                Items items = intent.getParcelableExtra(Constants.ITEM_POSITION);
                name = items.getName();
                setTitle(name + " " + "Recharge");
            }
        }

        //Open alert dialog on select edit
        selectType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                browseTextView.setVisibility(View.VISIBLE);
                selectType.setText("Prepaid");
                final String[] typeList = {"Prepaid", "Postpaid"};
                final int position = 0;
                AlertDialog.Builder builder = new AlertDialog.Builder(RechargeActivity.this);
                builder.setTitle("Choose Type");
                builder.setSingleChoiceItems(typeList, position, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if ((which == position)) {
                            isPostPaid = false;
                            selectType.setText(typeList[0]);
                            browseTextView.setVisibility(View.VISIBLE);
                            dialog.dismiss();
                        } else {
                            isPostPaid = true;
                            selectType.setText(typeList[1]);
                            browseTextView.setVisibility(View.INVISIBLE);
                            dialog.dismiss();
                        }
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


        //Set Text Watcher to Mobile number Input Edit Text
        numberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (!isPostPaid) {
                    if (layoutName.equals("Mobile") && s.length() > 10) {

                        number = numberEditText.getText().toString().trim();
                        mobileNumber = number.substring(3);
                        numberEditText.setText(mobileNumber);
                    } else if (layoutName.equals("Mobile") && s.length() == 10) {
                        mobileNumber = s.toString();
                        getNumberInformation(mobileNumber);
                    } else if (layoutName.equals("Mobile") && s.length() < 10) {
                        providerText.setText(null);
                        rechargeButton.setEnabled(false);
                        providerText.setHint(getResources().getString(R.string.select_operator));
                        Picasso.get().load(R.mipmap.formax_icon).into(providerImage);
                    } else {
                        mobileNumber = s.toString().trim();
                    }
                }else {
                    mobileNumber = s.toString().trim();
                }
            }
        });

        //On click browse plans text view
        browseTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (layoutName.equals("Mobile")) {
                    if (mobileNumber != null) {
                        browsePlan(circleName, providerName);
                    } else {
                        Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator_layout), "Please Enter Number", Snackbar.LENGTH_SHORT);
                        View view = snackbar.getView();
                        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackBarRed));
                        snackbar.show();
                    }
                }else {
//                    if (mobileNumber == null){
//                        Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator_layout),"Enter DTH Number",Snackbar.LENGTH_SHORT);
//                        snackbar.getView().setBackgroundColor(Color.RED);
//                        snackbar.show();
//                    }else if (providerName == null){
//                        Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator_layout),"Select An Operator",Snackbar.LENGTH_SHORT);
//                        snackbar.getView().setBackgroundColor(Color.RED);
//                        snackbar.show();
//                    }else {
//                        //Show the BottomSheet
//                        String dthOpertorCode = getDTHOperatorCode(providerName);
//                        DthCustomerInfoBottomSheet dthCustomerInfoBottomSheet = new DthCustomerInfoBottomSheet(mobileNumber,dthOpertorCode);
//                        dthCustomerInfoBottomSheet.show(getSupportFragmentManager(),dthCustomerInfoBottomSheet.getTag());
//                    }
                    Toast.makeText(getApplicationContext(),"Coming Soon",Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Process recharge request on Recharge button click
        rechargeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                assert inputManager != null;
                inputManager.hideSoftInputFromWindow(rechargeButton.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

                final String amount = amountEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(mobileNumber) && !TextUtils.isEmpty(amount) && !providerId.isEmpty()) {

                    LayoutInflater inflater = getLayoutInflater();
                    final View confirmLayout = inflater.inflate(R.layout.confirmation_layout, null);

                    final TextView numTextView = confirmLayout.findViewById(R.id.confirm_number);
                    final TextView amountTextView = confirmLayout.findViewById(R.id.confirm_amount);
                    final TextView retailer = confirmLayout.findViewById(R.id.confirm_retailer);
                    final FancyButton cancelButton = confirmLayout.findViewById(R.id.cancel_confirm);
                    final FancyButton confirmButton = confirmLayout.findViewById(R.id.cancel_button);
                    //Getting number and amount from edit text

                    AlertDialog.Builder builder = new AlertDialog.Builder(RechargeActivity.this, R.style.CustomDialog);
                    builder.setView(confirmLayout);
                    builder.setCancelable(false);
                    final AlertDialog dialog = builder.create();
                    dialog.show();

                    numTextView.setText(mobileNumber.trim());
                    amountTextView.setText(amount.trim());
                    retailer.setText(providerName);
                    confirmButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.e(TAG, mobileNumber);
                            Log.e(TAG, amount);

                            //Do recharge
                            doRecharge(mobileNumber, amount);
                            dialog.dismiss();
                        }
                    });

                    cancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent1 = new Intent(RechargeActivity.this, RechargeActivity.class);
                            intent1.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                            intent1.putExtra(Constants.RECHARGE, "recharge");
                            intent1.putExtra(Constants.LAYOUT_NAME, layoutName);
                            intent1.putExtra(Constants.SESSION_ID, session_id);
                            startActivity(intent1);
                            finish();
                        }
                    });
                } else if (mobileNumber == null) {
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator_layout), "Please Enter Number", Snackbar.LENGTH_LONG);
                    View view = snackbar.getView();
                    view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackBarRed));
                    snackbar.show();
                } else if (providerName == null) {
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator_layout), "Select an Operator", Snackbar.LENGTH_LONG);
                    View view = snackbar.getView();
                    view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackBarRed));
                    snackbar.show();
                } else if (TextUtils.isEmpty(amount)) {
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator_layout), "Please Enter Amount", Snackbar.LENGTH_LONG);
                    View view = snackbar.getView();
                    view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackBarRed));
                    snackbar.show();
                }
            }
        });

        //On click select operator text view
        providerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutName.equals("Mobile")) {
                    if (isPostPaid) {
                        showPostPidBottomSheet();
                    } else {
                        showBottomSheetFragment();
                    }
                } else {
                    showDthBottomSheet();
                }
            }
        });

    }


    //Get information about the number user entered
    private void getNumberInformation(String number) {

        Toast.makeText(getApplicationContext(), "Wait we are fetching your operator", Toast.LENGTH_SHORT).show();
        planViewModel.getOperatorDetails(number).observe(this, new Observer<FetchOperator>() {
            @Override
            public void onChanged(FetchOperator fetchOperator) {

                rechargeButton.setEnabled(true);
                //Check if FetchOperator is not null
                if (fetchOperator != null) {

                    //Check if circle and operator value is not null
                    if (fetchOperator.getCircle() == null || fetchOperator.getOperator() == null) {
                        Log.e(TAG, "Circle: " + fetchOperator.getCircle() + " Operator: " + fetchOperator.getOperator());
                    } else {
                        circleName = fetchOperator.getCircle().toUpperCase();
                        providerName = fetchOperator.getOperator().toUpperCase();
                        Log.e(TAG, "Circle: " + fetchOperator.getCircle() + " Operator: " + fetchOperator.getOperator());
                        providerText.setText(providerName + ", " + circleName);
                        if (!providerName.isEmpty()) {
                            setOperatorLogoAndId(providerName);
                        }
                    }
                }
            }
        });
    }

    //Method to set provider logo to ImageView and set provider Id for Recharge
    private void setOperatorLogoAndId(String operatorName) {

        providerImage.setVisibility(View.VISIBLE);
        if ("AIRTEL".equals(operatorName)) {
            providerId = "1";
            Picasso.get().load(R.drawable.airtel).into(providerImage);
        } else if ("IDEA".equals(operatorName)) {
            providerId = "5";
            Picasso.get().load(R.drawable.idea).into(providerImage);
        } else if ("VODAFONE".equals(operatorName)) {
            providerId = "2";
            Picasso.get().load(R.drawable.vodafone).into(providerImage);
        } else if (operatorName.contains("BSNL")) {
            providerId = "4";
            Picasso.get().load(R.drawable.bsnl).into(providerImage);
        } else if ("RELIANCE JIO INFOCOMM LIMITED".equals(operatorName)) {
            providerId = "8";
            Picasso.get().load(R.drawable.jio).into(providerImage);
        }
    }

    //Fetch Operator details
    private void browsePlan(String circle, final String operator) {
        if (circle != null && operator != null) {
            Intent plansIntent = new Intent(RechargeActivity.this, PlansActivity.class);
            plansIntent.putExtra(Constants.OPERATOR_NAME, operator);
            plansIntent.putExtra(Constants.LAYOUT_NAME, layoutName);
            plansIntent.putExtra(Constants.CIRCLE_NAME, circle);
            plansIntent.putExtra(Constants.MOBILE_NUMBER,mobileNumber);
            startActivityForResult(plansIntent, RC_PLAN_DETAILS);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        } else if (circle == null) {
            Toast.makeText(getApplicationContext(), "circle empty", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "operator empty", Toast.LENGTH_SHORT).show();
        }
    }

    //Make recharge status dialog method
    private void makeRechargeStatusDialog(Recharge recharge) {

        View statusLayout = getLayoutInflater().inflate(R.layout.recharge_status, null);

        ImageView imageView = statusLayout.findViewById(R.id.status_image);
        TextView textView = statusLayout.findViewById(R.id.process_text);

        TextView num = statusLayout.findViewById(R.id.recharge_number);
        TextView amount = statusLayout.findViewById(R.id.recharge_amount);

        TextView date = statusLayout.findViewById(R.id.date_time_recharge);
        TextView status = statusLayout.findViewById(R.id.recharge_status);
        TextView txnId = statusLayout.findViewById(R.id.txn_id);
        TextView optTxnId = statusLayout.findViewById(R.id.opt_txn_id);
        FancyButton button = statusLayout.findViewById(R.id.close_status);
        LottieAnimationView logoImage = statusLayout.findViewById(R.id.recharge_confirm_logo);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        builder.setView(statusLayout);
        builder.setCancelable(false);
        final AlertDialog dialog = builder.create();
        dialog.show();


        textView.setText(recharge.getMessage());
        num.setText(":    " + recharge.getNumber());
        amount.setText("INR" + recharge.getAmount());
        date.setText(":    " + recharge.getCreated_on());
        status.setText(":    " + recharge.getStatus());
        txnId.setText(":    " + recharge.getOperator_name());
        String opt = recharge.getOpt_txn_id();
        if (opt.isEmpty()) {
            optTxnId.setText(":    " + "N/A");
        } else {
            optTxnId.setText(":    " + opt);
        }
        String stat = recharge.getStatus();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RechargeActivity.this, RechargeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                intent.putExtra(Constants.RECHARGE, "recharge");
                intent.putExtra(Constants.LAYOUT_NAME, layoutName);
                intent.putExtra(Constants.SESSION_ID, session_id);
                startActivity(intent);
                finish();
            }
        });

        if (stat.equals("PENDING")) {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_processing_24dp));
            textView.setTextColor(getResources().getColor(R.color.processing));
            status.setTextColor(getResources().getColor(R.color.processing));
            logoImage.setAnimation(R.raw.force_update);
        } else if (stat.equals("SUCCESS")) {
            textView.setTextColor(Color.parseColor("#009624"));
            status.setTextColor(Color.parseColor("#009624"));
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_recharge_done));
            logoImage.setAnimation(R.raw.done);
        } else {
            textView.setTextColor(Color.RED);
            status.setTextColor(Color.RED);
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_recharge_failed));
            logoImage.setAnimation(R.raw.failed);
        }
    }

    //Method to mobile recharge on Recharge button clicked
    private void doRecharge(String mNum, String rAmount) {

        //If number and amount is not null
        View layout = getLayoutInflater().inflate(R.layout.loading_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(layout);
        builder.setCancelable(false);
        final AlertDialog dialog = builder.create();
        dialog.show();

        if (mNum != null && rAmount != null && providerId != null) {
            rechargeButton.setVisibility(View.GONE);

            Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator_layout), "Processing", Snackbar.LENGTH_LONG);
            View view = snackbar.getView();
            view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackBarGreen));
            snackbar.show();

            rechargeViewModel.getRechargeStatus(session_id, auth, mNum, providerId, rAmount).observe(RechargeActivity.this, new Observer<Recharge>() {
                @Override
                public void onChanged(Recharge recharge) {
                    dialog.dismiss();

                    if (recharge != null) {
                        Log.e(TAG, "Recharge is not null");

                        if (recharge.getAmount() == null) {
                            Toast.makeText(getApplicationContext(), recharge.getNumber(), Toast.LENGTH_LONG).show();
                            providerImage.setVisibility(View.INVISIBLE);
                            rechargeButton.setVisibility(View.VISIBLE);

                            Intent intent = new Intent(RechargeActivity.this, RechargeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                            intent.putExtra(Constants.RECHARGE, "recharge");
                            intent.putExtra(Constants.LAYOUT_NAME, layoutName);
                            intent.putExtra(Constants.SESSION_ID, session_id);
                            startActivity(intent);
                            finish();
                        } else {
                            providerImage.setVisibility(View.INVISIBLE);
                            rechargeButton.setVisibility(View.VISIBLE);
                            makeRechargeStatusDialog(recharge);
                        }
                    } else {
                        Log.e(TAG, "Recharge is null");
                    }
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), mNum + ", amount: " + rAmount + ", provider: " + providerId, Toast.LENGTH_SHORT).show();
        }
    }

    public void showBottomSheetFragment() {
        BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
    }

    public void showPostPidBottomSheet() {
        PostPaidSheet postPaidSheet = new PostPaidSheet();
        postPaidSheet.show(getSupportFragmentManager(), postPaidSheet.getTag());
    }

    public void showDthBottomSheet() {
        DTHSheetFragment dthSheetFragment = new DTHSheetFragment();
        dthSheetFragment.show(getSupportFragmentManager(), dthSheetFragment.getTag());
    }

    @Override
    public void onPrepaidClick(Prepaid prepaid) {

        providerId = prepaid.getId();
        providerText.setText(prepaid.getOperatorName());
        setProviderName(prepaid.getOperatorName());
        Picasso.get().load(prepaid.getImage()).placeholder(R.mipmap.formax_round_icon).into(providerImage);

    }

    @Override
    public void onPostPaidClick(Prepaid prepaid) {

        providerId = prepaid.getId();
        Picasso.get().load(prepaid.getImage()).placeholder(R.mipmap.formax_round_icon).into(providerImage);
        setProviderName(prepaid.getOperatorName());
        providerText.setText(prepaid.getOperatorName());
    }


    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    @Override
    public void OnDthItemClick(Prepaid prepaid) {

        providerId = prepaid.getId();
        providerText.setText(prepaid.getOperatorName());
        setProviderName(prepaid.getOperatorName());
        Picasso.get().load(prepaid.getImage()).placeholder(R.mipmap.formax_round_icon).into(providerImage);
    }

    public void onContactImageClick(View view) {
        askForContactPermission();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CONTACT) {
            if (resultCode == RESULT_OK) {
                Uri contactData = data.getData();
                String number = "";
                Cursor cursor = getContentResolver().query(contactData, null, null, null, null);
                cursor.moveToFirst();
                String hasPhone = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                String contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                if (hasPhone.equals("1")) {
                    Cursor phones = getContentResolver().query
                            (ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                            + " = " + contactId, null, null);
                    while (phones.moveToNext()) {
                        number = phones.getString(phones.getColumnIndex
                                (ContactsContract.CommonDataKinds.Phone.NUMBER)).replaceAll("[-() ]", "");
                        numberEditText.setText(number);
                    }
                    phones.close();
                    //Do something with number
                } else {
                    Toast.makeText(getApplicationContext(), "This contact has no phone number", Toast.LENGTH_LONG).show();
                }
                cursor.close();
            }
        }else if (requestCode == RC_PLAN_DETAILS && resultCode == RESULT_OK){

            amount = data.getStringExtra(Constants.RECHARGE_AMOUNT);
            amountEditText.setText(amount);
        }
    }

    public void askForContactPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_CONTACTS)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Need Contact Permission to read Contacts");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setMessage("please confirm Contacts access");//TODO put real question
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            requestPermissions(
                                    new String[]
                                            {Manifest.permission.READ_CONTACTS}
                                    , PERMISSION_REQUEST_CONTACT);
                        }
                    });
                    builder.show();
                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            PERMISSION_REQUEST_CONTACT);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {
                getContact();
            }
        } else {
            getContact();
        }
    }

    //Get DTH operator code
    private String getDTHOperatorCode(String name) {

        String dName = "";
        if (name.equals("DISH TV DTH")) {
            dName = "25";
        } else if (name.equals("SUN TV DTH")) {
            dName = "27";
        } else if (name.equals("TATA SKY DTH")) {
            dName = "28";
        } else if (name.equals("VIDEOCON DTH")) {
            dName = "29";
        } else if (name.equals("AIRTEL DTH")) {
            dName = "24";
        }else if (name.equals("BIG TV DTH")){
            dName = "26";
        }
        return dName;
    }

    //Open Contacts to get Mobile Number
    private void getContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    //Create Loading Dialog
    private AlertDialog createLoadingDialog(Context context) {

        View layout = getLayoutInflater().inflate(R.layout.loading_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(layout);
        builder.setCancelable(false);
        return builder.create();
    }
}

