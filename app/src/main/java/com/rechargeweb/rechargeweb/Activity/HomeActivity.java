package com.rechargeweb.rechargeweb.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.infideap.drawerbehavior.Advance3DDrawerLayout;
import com.rechargeweb.rechargeweb.Aeps.AepsActivity;
import com.rechargeweb.rechargeweb.BottomSheetFrag.BankNameBottomFragment;
import com.rechargeweb.rechargeweb.Constant.Constants;
import com.rechargeweb.rechargeweb.Keys;
import com.rechargeweb.rechargeweb.Model.Bank;
import com.rechargeweb.rechargeweb.Model.Details;
import com.rechargeweb.rechargeweb.Model.Items;
import com.rechargeweb.rechargeweb.Ui.MoveMoneyToBankFragment;
import com.rechargeweb.rechargeweb.Network.ApiService;
import com.rechargeweb.rechargeweb.Network.ApiUtills;
import com.rechargeweb.rechargeweb.R;
import com.rechargeweb.rechargeweb.Ui.AddMoneyFragment;
import com.rechargeweb.rechargeweb.Ui.HomeFragment;
import com.rechargeweb.rechargeweb.Ui.ProfileFragment;
import com.rechargeweb.rechargeweb.ViewModels.AllReportViewModel;
import com.shreyaspatil.MaterialDialog.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity implements HomeFragment.OnHomeItemClickLisetener,
        LocationListener,
        NavigationView.OnNavigationItemSelectedListener, HomeFragment.OnReportItemClickListener ,BankNameBottomFragment.OnClickListener{

    //Remote config values
    private static final String VERSION_NAME_KEY = "version_name";
    private static final String APPLY_FORCE_UPDATE_KEY = "apply_force_update";
    private static final String SHOW_UPDATE_DIALOG_KEY = "show_update_dialog";

    String bank, banReferenceNo, service, stanNo, transactionAmount, transtionId, transactionNo, uidNo;
    private boolean showUpdateDialog;

    //App url
    private String MY_APP_URL = "https://play.google.com/store/apps/details?id=com.rechargeweb.rechargeweb";

    String auth;

    //version name variable
    private String version;

    private FirebaseRemoteConfig firebaseRemoteConfig;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final String TAG = HomeActivity.class.getSimpleName();
    LocationManager locationManager;
    LocationListener locationListener;
    Location location;

    String locality, pinCode, state;
    boolean isGpsEnable;

    String session_id, user_id;

    String agentCode;

    AllReportViewModel allReportViewModel;

    Advance3DDrawerLayout drawerLayout;
    private Fragment fragment;
    private HomeFragment homeFragment = new HomeFragment();
    private ProfileFragment profileFragment = new ProfileFragment();

    private ApiService apiService;

    private View headerView;
    private TextView navTextView;

    ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        showUpdateDialog = true;

        apiService = ApiUtills.getApiService();

        //Checking the location permission
        checkLocationPermission();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.home_drawer);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        drawerLayout.setViewRotation(Gravity.START, 15);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.colorPrimaryDark));
        toolbar.setVisibility(View.VISIBLE);

        NavigationView sideNav = findViewById(R.id.home_nav_view);
        sideNav.bringToFront();
        sideNav.setNavigationItemSelectedListener(this);

        headerView = sideNav.inflateHeaderView(R.layout.nav_header_main);
        navTextView = headerView.findViewById(R.id.nav_header_name);
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment = profileFragment;
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_container, fragment);
                fragmentTransaction.commit();
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        //Initializing the ViewModel Class
        allReportViewModel = ViewModelProviders.of(this).get(AllReportViewModel.class);

        //Initialzing auth key
        auth = new Keys().apiKey();

        Intent intent = getIntent();
        if (intent.hasExtra(Constants.SESSION_ID)) {
            session_id = intent.getStringExtra(Constants.SESSION_ID);
            user_id = intent.getStringExtra(Constants.USER_ID);
        }

        //Get remote config instance
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        //Create a remote config setting
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(60)
                .build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        //Set default remote config parameter values
        firebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

        //Getting the version name
        version = getVersionName(this);

        disPlaySelectedScreen(R.id.nav_home);
    }

    public String getSession_id() {

        return session_id;
    }

    public String getUser_id() {
        return user_id;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (fragment != homeFragment ) {
            disPlaySelectedScreen(R.id.nav_home);
        }
        else {

            MaterialDialog materialDialog = new MaterialDialog.Builder(HomeActivity.this)
                    .setTitle("Do You want to exit?")
                    .setCancelable(false)
                    .setAnimation(R.raw.confirmation)
                    .setPositiveButton("Yes", new MaterialDialog.OnClickListener() {
                        @Override
                        public void onClick(com.shreyaspatil.MaterialDialog.interfaces.DialogInterface dialogInterface, int which) {

                            dialogInterface.dismiss();
                            HomeActivity.super.onBackPressed();
                        }
                    }).setNegativeButton("No", new MaterialDialog.OnClickListener() {
                        @Override
                        public void onClick(com.shreyaspatil.MaterialDialog.interfaces.DialogInterface dialogInterface, int which) {

                            dialogInterface.dismiss();
                        }
                    })
                    .build();

            materialDialog.show();
            }
        }
    @Override
    public void onHomeItemclick(Items items) {

        String itemName = items.getName();
        switch (itemName) {
            case "Mobile":
            case "DTH":
                Intent rechargeIntent = new Intent(HomeActivity.this, RechargeActivity.class);
                rechargeIntent.putExtra(Constants.RECHARGE, "recharge");
                rechargeIntent.putExtra(Constants.ITEM_POSITION, items);
                rechargeIntent.putExtra(Constants.LAYOUT_NAME, items.getName());
                rechargeIntent.putExtra(Constants.SESSION_ID, session_id);
                startActivity(rechargeIntent);
                break;
            case "Electricity": {
                Intent intent = new Intent(HomeActivity.this, BillPaymentActivity.class);
                intent.putExtra(Constants.SESSION_ID, session_id);
                intent.putExtra(Constants.ITEM_POSITION, items);
                intent.putExtra(Constants.STATE, state);
                startActivity(intent);
                break;
            }
            case "PAN Coupon": {
                Bundle bundle = new Bundle();
                bundle.putString(Constants.LOCALITY, locality);
                bundle.putString(Constants.PINCODE, pinCode);
                bundle.putString(Constants.STATE, state);
                Intent intent = new Intent(HomeActivity.this, PanActivity.class);
                intent.putExtra(Constants.SESSION_ID, session_id);
                intent.putExtra(Constants.ITEM_POSITION, items);
                intent.putExtra(Constants.LOCATION_BUNDLE, bundle);
                startActivity(intent);
                break;
            }
            case "DMT":
                Intent intent = new Intent(HomeActivity.this, DmtActivity.class);
                intent.putExtra(Constants.SESSION_ID, session_id);
                startActivity(intent);
                break;
            case "ICICI AEPS":
                Intent iaepsIntent = new Intent(HomeActivity.this, AepsActivity.class);
                iaepsIntent.putExtra(Constants.SESSION_ID,session_id);
                iaepsIntent.putExtra(Constants.USER_ID,user_id);
                iaepsIntent.putExtra(Constants.BANK,Constants.ICICI_BANK);
                startActivity(iaepsIntent);
                break;
            case "YBL AEPS":
                Intent aepsIntent = new Intent(HomeActivity.this, AepsActivity.class);
                aepsIntent.putExtra(Constants.SESSION_ID,session_id);
                aepsIntent.putExtra(Constants.USER_ID,user_id);
                aepsIntent.putExtra(Constants.BANK,Constants.YES_BANK);
                startActivity(aepsIntent);
                break;
            default:
                Toast.makeText(getApplicationContext(), "Coming Soon", Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 300 && resultCode == RESULT_OK) {
            if (data != null) {
                Log.e(TAG, "Data is full: " + data.toString());
                String message = data.getStringExtra("message");

                String status = data.getStringExtra("statusCode");

                Log.e(TAG, "message: " + message + ", Status: " + status);
                try {
                    JSONObject jsonTransactionJsonObject = new JSONObject(data.getStringExtra("data"));
                    bank = message + jsonTransactionJsonObject.getString("bankName");
                    banReferenceNo = jsonTransactionJsonObject.getString("bankrefrenceNo");
                    service = jsonTransactionJsonObject.getString("service");
                    stanNo = jsonTransactionJsonObject.getString("stanNo");
                    transactionAmount = jsonTransactionJsonObject.getString("transactionAmount");
                    transtionId = jsonTransactionJsonObject.getString("transactionId");
                    transactionNo = jsonTransactionJsonObject.getString("transactionNO");
                    uidNo = jsonTransactionJsonObject.getString("uidNo");

                    if (!bank.isEmpty() && !banReferenceNo.isEmpty() && !service.isEmpty() && !transactionAmount.isEmpty()) {
                        showAepsDialog();
                    } else {
                        Toast.makeText(getApplicationContext(), "Empty Response", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                        intent.putExtra(Constants.USER_ID, user_id);
                        intent.putExtra(Constants.SESSION_ID, session_id);
                        startActivity(intent);
                        finish();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e(TAG, "Data is null");
            }
        } else {
            Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            intent.putExtra(Constants.USER_ID, user_id);
            intent.putExtra(Constants.SESSION_ID, session_id);
            startActivity(intent);
        }
    }

    //Create Aeps success dialog
    private void showAepsDialog() {

        View layout = getLayoutInflater().inflate(R.layout.aeps_dialog, null);

        Button closeButton = layout.findViewById(R.id.aeps_close_button);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AddBeneficiaryDialog);
        builder.setView(layout);
        builder.setCancelable(false);
        final AlertDialog dialog = builder.create();
        dialog.show();

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                intent.putExtra(Constants.USER_ID, user_id);
                intent.putExtra(Constants.SESSION_ID, session_id);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
                            ActivityCompat.requestPermissions(HomeActivity.this,
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

    @Override
    protected void onResume() {
        super.onResume();

        sendRequest(session_id, auth);
        if (showUpdateDialog) {
            fetchValues();
        }

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SAVE_LOCATION, MODE_PRIVATE);
        locality = sharedPreferences.getString(Constants.LOCALITY, null);
        pinCode = sharedPreferences.getString(Constants.PINCODE, null);
        state = sharedPreferences.getString(Constants.STATE, null);

        if (locality == null || pinCode == null || state == null) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                if (locationManager != null) {
                    isGpsEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                    if (isGpsEnable) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        if (location != null) {
                            Geocoder geocoder = new Geocoder(this, Locale.getDefault());

                            try {
                                List<Address> list = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                locality = list.get(0).getLocality();
                                pinCode = list.get(0).getPostalCode();
                                state = list.get(0).getAdminArea();

                                SharedPreferences.Editor editor = getSharedPreferences(Constants.SAVE_LOCATION, MODE_PRIVATE).edit();
                                editor.putString(Constants.LOCALITY, locality);
                                editor.putString(Constants.PINCODE, pinCode);
                                editor.putString(Constants.STATE, state);
                                editor.apply();

                                Log.e(TAG, locality + ", " + pinCode + ", " + state);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Unable to get Location", Toast.LENGTH_SHORT).show();
                        }
                    } else {

                        //Create an alert dialog if the location is off on device
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);

                        builder.setCancelable(false);
                        builder.setMessage("Your Location is disabled. Turn on your location");
                        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();

                    }
                } else {

                    Toast.makeText(getApplicationContext(), "Unable to fetch location Service", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Log.e(TAG, locality + state + pinCode);
        }
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
                        ActivityCompat.requestPermissions(HomeActivity.this,
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

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //Fetch values from firebase
    private void fetchValues() {

        firebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {

                        if (task.isSuccessful()) {
                            boolean updated = task.getResult();
                            Log.e(TAG, "Remote config values updated: " + updated);

                        } else {
                            Log.e(TAG, "Remote values not updated");
                        }
                        displayDialog();
                    }
                });
    }

    //Display Alert dialog according to remote values
    private void displayDialog() {

        String version_name = firebaseRemoteConfig.getString(VERSION_NAME_KEY);
        Log.e(TAG, "config: " + version_name + "app: " + version);

        if (version_name.equals(version) && firebaseRemoteConfig.getBoolean(SHOW_UPDATE_DIALOG_KEY) && firebaseRemoteConfig.getBoolean(APPLY_FORCE_UPDATE_KEY)) {

            Log.e(TAG, "Show Alert dialog");

            View layout = getLayoutInflater().inflate(R.layout.force_update_dialog, null);

            Button update = layout.findViewById(R.id.force_update);
            Button later = layout.findViewById(R.id.no_thanks);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(layout);
            builder.setCancelable(false);

            AlertDialog dialog = builder.create();
            dialog.show();

            update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MY_APP_URL));
                    startActivity(intent);
                    showUpdateDialog = false;
                }
            });

            later.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    finish();
                }
            });
        } else if (version_name.equals(version) && firebaseRemoteConfig.getBoolean(SHOW_UPDATE_DIALOG_KEY) &&
                !(firebaseRemoteConfig.getBoolean(APPLY_FORCE_UPDATE_KEY))) {

            View layout = getLayoutInflater().inflate(R.layout.update_dilog, null);

            Button updateButton = layout.findViewById(R.id.update_now);
            Button laterButton = layout.findViewById(R.id.update_later);

            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
            builder.setView(layout);
            builder.setCancelable(false);

            final AlertDialog dialog = builder.create();
            dialog.show();

            updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MY_APP_URL));
                    startActivity(intent);
                    showUpdateDialog = false;
                }
            });

            laterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    dialog.dismiss();
                    showUpdateDialog = false;
                }
            });
        }
    }

    //Get the version name
    private String getVersionName(Context context) {
        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pInfo.versionName;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        disPlaySelectedScreen(id);
        return true;
    }

    @SuppressLint("RestrictedApi")
    private void disPlaySelectedScreen(int itemId) {

        fragment = null;

        switch (itemId) {

            case R.id.nav_home:
                fragment = homeFragment;
                toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.colorPrimaryDark));
                break;
            case R.id.nav_support:
                Intent intent = new Intent(HomeActivity.this, SupportActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_changePassword:
                Intent changePassIntent = new Intent(HomeActivity.this, ChangePassActivity.class);
                changePassIntent.putExtra(Constants.SESSION_ID, session_id);
                startActivity(changePassIntent);
                break;
            case R.id.nav_transaction_report:
                Intent reportIntent = new Intent(HomeActivity.this, ReportActivity.class);
                reportIntent.putExtra(Constants.SESSION_ID, session_id);
                startActivity(reportIntent);
                break;
            case R.id.nav_fund_request:
                Intent intent1 = new Intent(HomeActivity.this, FundRequestActivity.class);
                intent1.putExtra(Constants.SESSION_ID, session_id);
                startActivity(intent1);
                break;
            case R.id.nav_passbook:
                Intent passbookIntent = new Intent(HomeActivity.this, ReportActivity.class);
                passbookIntent.putExtra(Constants.SESSION_ID, session_id);
                passbookIntent.putExtra(Constants.PASSBOOK, "Passbook");
                startActivity(passbookIntent);
                break;
            case R.id.nav_rate_us:
                openAppRating(this);
                break;
            case R.id.nav_shareapp:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Hey Check out this awesome app on: https://play.google.com/store/apps/details?id=com.rechargeweb.rechargeweb");
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, "Share Via"));
                break;
            case R.id.nav_logout:

                MaterialDialog materialDialog = new MaterialDialog.Builder(this)
                        .setTitle(" Log Out?")
                        .setMessage("Are you sure want to Log out?")
                        .setCancelable(false)
                        .setAnimation(R.raw.confirmation)
                        .setPositiveButton("Log Out", new MaterialDialog.OnClickListener() {
                            @Override
                            public void onClick(com.shreyaspatil.MaterialDialog.interfaces.DialogInterface dialogInterface, int which) {

                                dialogInterface.dismiss();
                                logOutUser();
                            }
                        })
                        .setNegativeButton("Cancel", new MaterialDialog.OnClickListener() {
                            @Override
                            public void onClick(com.shreyaspatil.MaterialDialog.interfaces.DialogInterface dialogInterface, int which) {

                                dialogInterface.dismiss();
                            }
                        }).build();
                materialDialog.show();
                break;
            case R.id.nav_bank_detials:
                BankNameBottomFragment bankNameBottomFragment = new BankNameBottomFragment();
                bankNameBottomFragment.show(getSupportFragmentManager(),bankNameBottomFragment.getTag());
                break;
            case R.id.nav_move_money_to_bank:
                fragment = new MoveMoneyToBankFragment();
                break;
        }

        if (fragment != null) {

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.main_container, fragment);
            fragmentTransaction.commit();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
    }

    //Log out user
    private void logOutUser(){
        Intent intent = new Intent(HomeActivity.this, SignUpActivity.class);
        intent.putExtra("logout", true);
        startActivity(intent);
        finish();
    }

    //Method to open PlayStore  for rating
    public static void openAppRating(Context context) {
        // you can also use BuildConfig.APPLICATION_ID
        String appId = context.getPackageName();
        Intent rateIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + appId));
        boolean marketFound = false;

        // find all applications able to handle our rateIntent
        final List<ResolveInfo> otherApps = context.getPackageManager()
                .queryIntentActivities(rateIntent, 0);
        for (ResolveInfo otherApp : otherApps) {
            // look for Google Play application
            if (otherApp.activityInfo.applicationInfo.packageName
                    .equals("com.android.vending")) {

                ActivityInfo otherAppActivity = otherApp.activityInfo;
                ComponentName componentName = new ComponentName(
                        otherAppActivity.applicationInfo.packageName,
                        otherAppActivity.name
                );
                // make sure it does NOT open in the stack of your activity
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // task reparenting if needed
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                // if the Google Play was already open in a search result
                //  this make sure it still go to the app page you requested
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                // this make sure only the Google Play app is allowed to
                // intercept the intent
                rateIntent.setComponent(componentName);
                context.startActivity(rateIntent);
                marketFound = true;
                break;

            }
        }

        // if GP not present on device, open web browser
        if (!marketFound) {
            Intent webIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + appId));
            context.startActivity(webIntent);
        }
    }

    @Override
    public void onReportItemClick(Items items) {
        Intent reportIntent = new Intent(HomeActivity.this, ReportActivity.class);
        reportIntent.putExtra(Constants.SESSION_ID, session_id);
        reportIntent.putExtra(Constants.REPORT, items);
        startActivity(reportIntent);
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    //Get user details
    private void sendRequest(String id, String key) {
        apiService.setDetailsPost(id, key).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new io.reactivex.Observer<Details>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Details details) {
                        Log.e(TAG, "success: " + details.toString());
                        navTextView.setText(details.getBusiness_name());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "error");
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public void onItemClick(Bank bank) {

    }
}
