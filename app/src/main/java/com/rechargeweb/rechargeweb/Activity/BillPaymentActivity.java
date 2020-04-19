package com.rechargeweb.rechargeweb.Activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.snackbar.Snackbar;
import com.rechargeweb.rechargeweb.BillPaymentViewModel;
import com.rechargeweb.rechargeweb.BottomSheetFrag.OperatorByStateSheet;
import com.rechargeweb.rechargeweb.Constant.Constants;
import com.rechargeweb.rechargeweb.Keys;
import com.rechargeweb.rechargeweb.Model.BillPay;
import com.rechargeweb.rechargeweb.Model.ElectricStatus;
import com.rechargeweb.rechargeweb.Model.Prepaid;
import com.rechargeweb.rechargeweb.R;
import com.rechargeweb.rechargeweb.BottomSheetFrag.StateListFragment;


import java.io.IOException;
import java.util.List;
import java.util.Locale;

import mehdi.sakout.fancybuttons.FancyButton;

public class BillPaymentActivity extends AppCompatActivity implements OperatorByStateSheet.OnOperatorByStateClickListener, StateListFragment.OnStateItemClickListener {


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final String TAG = BillPaymentActivity.class.getSimpleName();
    private static final String DEBUG_TAG = "NetworkStatusExample";
    double lat, lon;
    LocationManager locationManager;
    Location location;
    boolean gps;

    String session_id;
    String name;
    String code;
    boolean isWifi;
    String customer_id;
    String parameter1;
    int amount = 0;
    String ref_id;
    String customer_name;
    String board;

    String auth_key;

    //Show current state
    EditText currentState;

    //show current board
    TextView currentBoard;

    //Show other information
    EditText codeEditText;
    EditText codeEditTextTWo;

    private ImageView statePickerImage;

    //Button to fetch bill
    FancyButton billFetchButton;

    BillPaymentViewModel billPaymentViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_payment);

        Intent intent = getIntent();
        if (intent.hasExtra(Constants.SESSION_ID)) {
            session_id = intent.getStringExtra(Constants.SESSION_ID);
            name = intent.getStringExtra(Constants.STATE);
            if (name != null) {
                Log.e(TAG, name);
            } else {
                Log.e(TAG, "null");
            }
        }
        auth_key = new Keys().apiKey();
        checkLocationPermission();

        //Check which network is connected
        whichConnected();

        currentState = findViewById(R.id.current_state);
        currentBoard = findViewById(R.id.current_board);
        codeEditText = findViewById(R.id.code_edit);
        codeEditTextTWo = findViewById(R.id.code_edit_two);
        billFetchButton = findViewById(R.id.cancel_button);
        statePickerImage = findViewById(R.id.state_picker_iv);

        //Initializing ViewModel class
        billPaymentViewModel = ViewModelProviders.of(this).get(BillPaymentViewModel.class);

        //Set onClickListener to statePickerImageView
        statePickerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StateListFragment stateListFragment = new StateListFragment();
                stateListFragment.show(getSupportFragmentManager(),stateListFragment.getTag());
            }
        });

        //Set onClickListener to currentBoardTextView
        currentBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOperatorByState();
            }
        });

        //Fetch bill details on bill fetch button click
        billFetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                if (inputManager != null) {
                    inputManager.hideSoftInputFromWindow(billFetchButton.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
                customer_id = codeEditText.getText().toString();
                parameter1 = codeEditTextTWo.getText().toString();

                if (!customer_id.isEmpty() && parameter1.isEmpty() && board != null) {

                    View layout = getLayoutInflater().inflate(R.layout.loading_dialog, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(BillPaymentActivity.this);
                    builder.setCancelable(false);
                    builder.setView(layout);
                    final AlertDialog mDialog = builder.create();
                    mDialog.show();

                    Log.e(TAG,"Custoemr Id: " + customer_id + ", code: " + code);
                    billPaymentViewModel.getElectricBillStatus(auth_key, customer_id, code).observe(BillPaymentActivity.this, new Observer<ElectricStatus>() {
                        @Override
                        public void onChanged(ElectricStatus electricStatus) {

                            mDialog.dismiss();
                            if (electricStatus != null) {

                                Log.e(TAG, "electric status is not null");
                                if (electricStatus.getStatus().equals("FAILED")) {
                                    Log.e(TAG, "Bill amount is null");
                                    Toast.makeText(getApplicationContext(), electricStatus.getMessage(), Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(BillPaymentActivity.this, BillPaymentActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.putExtra(Constants.SESSION_ID, session_id);
                                    intent.putExtra(Constants.STATE, name);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Log.e(TAG, "Bill amount is not null");
                                    currentBoard.setText("");
                                    currentBoard.setHint("Select Board");
                                    codeEditText.setText("");
                                    codeEditText.setVisibility(View.GONE);
                                    codeEditTextTWo.setText("");
                                    codeEditText.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(), electricStatus.getMessage(), Toast.LENGTH_LONG).show();
                                    createBillFetchDialog(electricStatus);
                                }

                            } else {
                                Toast.makeText(getApplicationContext(), "Status is null", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else if (!customer_id.isEmpty() && !parameter1.isEmpty() && board != null) {
                    View layout = getLayoutInflater().inflate(R.layout.loading_dialog, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(BillPaymentActivity.this);
                    builder.setView(layout);
                    builder.setCancelable(false);
                    final AlertDialog mDialog = builder.create();
                    mDialog.show();

                    billPaymentViewModel.getElectricBillStatusTWo(auth_key, customer_id, code, parameter1).observe(BillPaymentActivity.this, new Observer<ElectricStatus>() {
                        @Override
                        public void onChanged(ElectricStatus electricStatus) {
                            mDialog.dismiss();
                            if (electricStatus != null) {
                                if (electricStatus.getCustomerId() == null) {
                                    Toast.makeText(getApplicationContext(), electricStatus.getStatus(), Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(BillPaymentActivity.this, BillPaymentActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.putExtra(Constants.SESSION_ID, session_id);
                                    intent.putExtra(Constants.STATE, name);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    currentBoard.setText("");
                                    currentBoard.setHint("Select Board");
                                    codeEditText.setText("");
                                    codeEditText.setVisibility(View.GONE);
                                    codeEditTextTWo.setText("");
                                    codeEditText.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(), electricStatus.getMessage(), Toast.LENGTH_LONG).show();
                                    createBillFetchDialog(electricStatus);
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Status is null", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else if (board == null) {
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator_layout), "Please Select a Board", Snackbar.LENGTH_LONG);
                    View view = snackbar.getView();
                    view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackBarRed));
                    snackbar.show();
                } else {
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator_layout), "Please Enter Consumer Id", Snackbar.LENGTH_LONG);
                    View view = snackbar.getView();
                    view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackBarRed));
                    snackbar.show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (name == null) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    gps = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                    if (location != null) {
                        if (gps) {
                            name = getStateNme(location);
                            currentState.setText(name);
                        } else {
                            Toast.makeText(getApplicationContext(), "turn on location", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }
            }
        } else {
            currentState.setText(name);
        }
    }

    //Get the state name from location
    private String getStateNme(Location location) {
        lon = location.getLongitude();
        lat = location.getLatitude();

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> list = geocoder.getFromLocation(lat, lon, 1);
            name = list.get(0).getAdminArea();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return name;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setCancelable(false);
                    builder.setMessage("We need to access your Location, Please allow location Permission");
                    builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(BillPaymentActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    MY_PERMISSIONS_REQUEST_LOCATION);
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        }
    }

    //Check if the device is connected to wifi or mobile network
    private boolean whichConnected() {

        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean isWifiConn = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (Network network : connMgr.getAllNetworks()) {
                NetworkInfo networkInfo = connMgr.getNetworkInfo(network);
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    isWifiConn |= networkInfo.isConnected();
                }
            }
        } else {
            NetworkInfo mWifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            isWifiConn |= mWifi.isConnected();
        }
        Log.e(DEBUG_TAG, "Wifi connected: " + isWifiConn);
        return isWifiConn;
    }

    //Getting Runtime Permission
    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                builder.setMessage("We need to access your Location, Please allow location Permission");
                builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Prompt the user once explanation has been shown
                        ActivityCompat.requestPermissions(BillPaymentActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_LOCATION);
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    //Create Bill fetch Dialog
    private void createBillFetchDialog(ElectricStatus electricStatus) {


        View layout = getLayoutInflater().inflate(R.layout.electric_bill_details_dialog, null);

        final TextView customerId = layout.findViewById(R.id.customer_id);
        TextView customerName = layout.findViewById(R.id.customer_name);
        TextView billNumber = layout.findViewById(R.id.bill_number);
        TextView billDate = layout.findViewById(R.id.bill_date);
        TextView billDueDate = layout.findViewById(R.id.bill_due_date);
        TextView billPeriod = layout.findViewById(R.id.bill_period);
        TextView billAmount = layout.findViewById(R.id.billamount);
        FancyButton button = layout.findViewById(R.id.cancel_button);
        FancyButton payButton = layout.findViewById(R.id.close);
        AlertDialog.Builder builder = new AlertDialog.Builder(BillPaymentActivity.this, R.style.CustomDialog);
        builder.setView(layout);

        final AlertDialog dialog = builder.create();
        dialog.show();
        if (electricStatus != null) {
            customerId.setText(electricStatus.getCustomerId());
            customer_name = electricStatus.getCustomerName();
            customerName.setText(customer_name);
            billNumber.setText(electricStatus.getBillNumber());
            billDate.setText(electricStatus.getBillDate());
            billDueDate.setText(electricStatus.getBillDueDate());
            billPeriod.setText(electricStatus.getBillPeriod());
            if (electricStatus.getBillAmount() != null) {
                amount = electricStatus.getBillAmount();
                billAmount.setText(String.valueOf(amount));
                Log.e(TAG, "Pay amount: " + amount);
            } else {
                billAmount.setText("N/A");
            }
            ref_id = electricStatus.getRefId();
        } else {
            Toast.makeText(getApplicationContext(), "Failed to get bill ", Toast.LENGTH_SHORT).show();
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(BillPaymentActivity.this, BillPaymentActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra(Constants.SESSION_ID, session_id);
                intent.putExtra(Constants.STATE, name);
                startActivity(intent);
                finish();
            }
        });

        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (customer_id != null && code != null && amount != 0 && ref_id != null) {
                    dialog.dismiss();
                    View layout = getLayoutInflater().inflate(R.layout.loading_dialog, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(BillPaymentActivity.this);
                    builder.setView(layout);
                    builder.setCancelable(false);
                    final AlertDialog mDialog = builder.create();
                    mDialog.show();

                    billPaymentViewModel.getElectricbillPaymentDetials(auth_key, session_id, customer_id, code, amount, ref_id).observe(BillPaymentActivity.this, new Observer<BillPay>() {
                        @Override
                        public void onChanged(BillPay billPay) {
                            mDialog.dismiss();
                            if (billPay != null) {
                                if (billPay.getCreatedOn() == null) {
                                    Log.e(TAG, "amount is null");
                                    Toast.makeText(getApplicationContext(), billPay.getNumber(), Toast.LENGTH_LONG).show();
                                } else {
                                    Log.e(TAG, "amount is not null");
                                    Toast.makeText(getApplicationContext(), billPay.getResponse(), Toast.LENGTH_LONG).show();
                                    createConfrimPayDialog(billPay, customer_name);
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "NO response", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                } else {
                    Toast.makeText(getApplicationContext(), "Please provide needed information to Pay bill", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(BillPaymentActivity.this, BillPaymentActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra(Constants.SESSION_ID, session_id);
                    intent.putExtra(Constants.STATE, name);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    //Show bill payment confirm dialog
    private void createConfrimPayDialog(BillPay billPay, String customer) {

        View layout = getLayoutInflater().inflate(R.layout.confirm_bill_payment, null);

        TextView operatorName = layout.findViewById(R.id.operator_name_tv);
        TextView customerName = layout.findViewById(R.id.customer_name_tv);
        TextView counsumerId = layout.findViewById(R.id.cousumer_id_tv);
        TextView amount = layout.findViewById(R.id.amount_tv);
        TextView status = layout.findViewById(R.id.status_tv);
        TextView txbId = layout.findViewById(R.id.txn_id_tv);
        TextView opt_txn_id = layout.findViewById(R.id.opt_txn_id_tv);
        TextView date = layout.findViewById(R.id.date_tv);
        TextView response = layout.findViewById(R.id.response_tv);

        FancyButton closeButton = layout.findViewById(R.id.close);

        AlertDialog.Builder builder = new AlertDialog.Builder(BillPaymentActivity.this, R.style.CustomDialog);
        builder.setView(layout);

        final AlertDialog dialog = builder.create();
        dialog.show();

        if (billPay != null) {

            operatorName.setText(billPay.getOperatorName());
            customerName.setText(customer);
            counsumerId.setText(billPay.getNumber());
            amount.setText(billPay.getAmount());
            String stat = billPay.getStatus();
            status.setText(stat);
            if (stat.equals("SUCCESS")) {
                status.setTextColor(Color.GREEN);
            } else if (stat.equals("PENDING")) {
                status.setTextColor(getResources().getColor(R.color.processing));
            } else {
                status.setTextColor(Color.RED);
            }
            txbId.setText(billPay.getTxnId());
            String opt = billPay.getOptTxnId();
            opt_txn_id.setText(billPay.getOptTxnId());
            date.setText(billPay.getCreatedOn());
            response.setText(billPay.getResponse());
        }

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
              finish();
            }
        });
    }

    //Show BottomSheet of Electricity operators by state
    public void showOperatorByState() {
        String state = currentState.getText().toString();
        OperatorByStateSheet operatorByStateSheet = new OperatorByStateSheet(state);
        operatorByStateSheet.show(getSupportFragmentManager(), operatorByStateSheet.getTag());
    }

    //Set Code EditText according to code
    private void setCodeEditText(String code) {

        String CONSUMER_ID = "Consumer Id";
        String K_NUMBER = "K Number";
        String SERVICE_NO = "Service Number";
        String ACCOUNT_NO = "Account Number";
        String CUSTOMRE_ID = "Customer Id";
        String CA_NO = "CA Number";
        String CUSTOMER_ACCOUNT = "Customer ID / Account ID";
        String BUSINESS_PARTNER_NUMBER = "Business Partner Number";
        String CONSUMER_NUMBER = "Consumer Number";
        String SERVICE_CONNECTION_NUMBER = "Service Connection Number";
        String BILLING_UNIT = "Billing Unit";
        String MOBILE_NUMBER = "Mobile Number";
        String SUBDIVISION_NUMBER = "Subdivision Number";
        String CYCLE_NUMBER = "Cycle NUmber";

        switch (code) {
            case "WBE":
            case "WOE":
            case "TNE":
            case "GEE":
            case "CSE":
            case "CRE":
            case "TTE":
            case "ANE":
            case "BET":
            case "HNE":
            case "MHE":
                codeEditText.setHint(CONSUMER_ID);
                break;
            case "AJE":
            case "HPE":
            case "BEE":
            case "BKE":
            case "AVE":
            case "JDE":
            case "KTE":
            case "JIE":
                codeEditText.setHint(K_NUMBER);
                break;
            case "TBE":
            case "APE":
                codeEditText.setHint(SERVICE_NO);
                break;
            case "DDE":
                codeEditText.setHint(ACCOUNT_NO);
            case "CWE":
            case "STO":
            case "SDE":
                codeEditText.setHint(CUSTOMRE_ID);
                break;
            case "SBE":
            case "BRE":
            case "BSY":
            case "NDE":
            case "NBE":
            case "ASE":
                codeEditText.setHint(CA_NO);
                break;
            case "BBE":
                codeEditText.setHint(CUSTOMER_ACCOUNT);
                break;
            case "CCE":
            case "JUE":
                codeEditText.setHint(BUSINESS_PARTNER_NUMBER);
                break;
            case "DGE":
            case "IBE":
            case "IWE":
            case "TWE":
            case "MZE":
            case "NSE":
            case "MGE":
            case "MPE":
            case "NUE":
            case "PGE":
            case "UGE":
            case "UBE":
            case "URE":
                codeEditText.setHint(CONSUMER_NUMBER);
                break;
            case "DNE":
                codeEditText.setHint(SERVICE_CONNECTION_NUMBER);
                break;
            case "MDE":
                codeEditText.setHint(CONSUMER_NUMBER);
                codeEditTextTWo.setVisibility(View.VISIBLE);
                codeEditTextTWo.setHint(BILLING_UNIT);
                break;
            case "DHB":
            case "UHB":
                codeEditText.setHint(ACCOUNT_NO);
                codeEditTextTWo.setVisibility(View.VISIBLE);
                codeEditTextTWo.setHint(MOBILE_NUMBER);
                break;
            case "TSE":
                codeEditText.setHint(SERVICE_NO);
                codeEditTextTWo.setVisibility(View.VISIBLE);
                codeEditTextTWo.setText("Surat");
                break;
            case "THE":
                codeEditText.setHint(SERVICE_NO);
                codeEditTextTWo.setVisibility(View.VISIBLE);
                codeEditTextTWo.setText("Ahmedabad");
                break;
            case "TAE":
                codeEditText.setHint(SERVICE_NO);
                codeEditTextTWo.setVisibility(View.VISIBLE);
                codeEditTextTWo.setText("Agra");
                break;
            case "JBE":
                codeEditText.setHint(CONSUMER_NUMBER);
                codeEditTextTWo.setVisibility(View.VISIBLE);
                codeEditTextTWo.setHint(SUBDIVISION_NUMBER);
                break;
            case "IRG":
                codeEditText.setHint(CONSUMER_NUMBER);
                codeEditTextTWo.setVisibility(View.VISIBLE);
                codeEditTextTWo.setHint(CYCLE_NUMBER);
                break;
        }
    }

    @Override
    public void onBoardItemClick(Prepaid prepaid) {

        board = prepaid.getOperatorName();
        currentBoard.setText(board);
        code = prepaid.getId();
        setCodeEditText(code);
        codeEditText.setVisibility(View.VISIBLE);

        Log.e(TAG, "Code is : " + code);
    }

    @Override
    public void onStateItemClick(String state) {

        currentState.setText(state);
    }
}
