package com.rechargeweb.rechargeweb.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.rechargeweb.rechargeweb.Adapters.PlanPagerAdapter;
import com.rechargeweb.rechargeweb.Constant.Constants;
import com.rechargeweb.rechargeweb.PlanFragment.ComboFragment;
import com.rechargeweb.rechargeweb.PlanFragment.FullTTFragment;
import com.rechargeweb.rechargeweb.PlanFragment.RateCutterFragment;
import com.rechargeweb.rechargeweb.PlanFragment.RoamingFragment;
import com.rechargeweb.rechargeweb.PlanFragment.SmsFragment;
import com.rechargeweb.rechargeweb.PlanFragment.SpecialOfferFragment;
import com.rechargeweb.rechargeweb.PlanFragment.ThreeGFourGFragment;
import com.rechargeweb.rechargeweb.PlanFragment.TopUpFragment;
import com.rechargeweb.rechargeweb.PlanFragment.TwoGFragment;
import com.rechargeweb.rechargeweb.Plans;
import com.rechargeweb.rechargeweb.R;
import com.rechargeweb.rechargeweb.Roffer;

public class PlansActivity extends AppCompatActivity implements ComboFragment.OnComBoItemClickListener,
        FullTTFragment.OnFullTTItemClickListener,RateCutterFragment.OnRateCutterItemClickListener,
        RoamingFragment.OnRoamingItemClikcListener,SmsFragment.OnSmsItemClickListener, SpecialOfferFragment.OnSpecialOfferItemClickListener
        ,ThreeGFourGFragment.OnThreeGForuGItemClickListener,TopUpFragment.OnTopupItemClickListener,TwoGFragment.OnTwoGFragmentClickListener{

    private static final String TAG = PlansActivity.class.getSimpleName();
    private String opertor;
    private String operatorCode;
    private String amount;

    private String mobileNumber;
    private String rechargeType;
    private String circleCode;
    private String circleName;
    private boolean isSelected;

    PlanPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plans);

        ViewPager viewPager = findViewById(R.id.view_pager);
        pagerAdapter = new PlanPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,this);
        setUpPlansPagerAdapter(viewPager);
        TabLayout tabLayout = findViewById(R.id.tab);
        tabLayout.setupWithViewPager(viewPager,true);

        //Getting intent
        Intent intent = getIntent();

        //Get operator name
        opertor = intent.getStringExtra(Constants.OPERATOR_NAME);

        if (opertor != null) {
            Log.e(TAG , opertor);
            if (opertor.contains("BSNL")){
                String ope = "BSNL";
                operatorCode = getOperatorCode(ope);
                Log.e(TAG,operatorCode);
            }else {
                operatorCode = getOperatorCode(opertor);
                Log.e(TAG,operatorCode);
            }
        }
        //Get Circle id
        circleName = intent.getStringExtra(Constants.CIRCLE_NAME);
        if (circleName != null) {
            Log.e(TAG,circleName);
            circleCode = getCircleCode(circleName);
            Log.e(TAG,circleCode);
        }

        //Get Mobile Number
        mobileNumber = intent.getStringExtra(Constants.MOBILE_NUMBER);
        Log.e(TAG,"Mobile Number " + mobileNumber);
    }

    //Set up ViewPager With Fragment
    private void setUpPlansPagerAdapter(ViewPager viewPager){

        pagerAdapter.addFragment(new SpecialOfferFragment(),"Only For You",0);
        pagerAdapter.addFragment(new ComboFragment(),"Combo Plans",1);
        pagerAdapter.addFragment(new ThreeGFourGFragment(),"3G/4G Plans",2);
        pagerAdapter.addFragment(new TwoGFragment(),"2G Plans",3);
        pagerAdapter.addFragment(new FullTTFragment(),"Full Talk time Plans",4);
        pagerAdapter.addFragment(new TopUpFragment(),"Top UP Plans",5);
        pagerAdapter.addFragment(new RateCutterFragment(),"Rate Cutter Plans",6);
        pagerAdapter.addFragment(new RoamingFragment(),"Roaming Plans",7);
        pagerAdapter.addFragment(new SmsFragment(),"SMS Plans",8);
        pagerAdapter.notifyDataSetChanged();
        viewPager.setAdapter(pagerAdapter);
    }
    //Get circle code
    private String getCircleCode(String circleName){

        String circleCode ="";
        switch (circleName) {
            case "DELHI":
                circleCode = "10";
                break;
            case "UP(WEST)":
                circleCode = "97";
                break;
            case "PUNJAB":
                circleCode = "02";
                break;
            case "HP":
                circleCode = "03";
                break;
            case "HARYANA":
                circleCode = "96";
                break;
            case "J&K":
                circleCode = "55";
                break;
            case "UP(EAST)":
                circleCode = "54";
                break;
            case "MUMBAI":
                circleCode = "92";
                break;
            case "MAHARASHTRA":
                circleCode = "90";
                break;
            case "GUJARAT":
                circleCode = "98";
                break;
            case "MP":
                circleCode = "93";
                break;
            case "RAJASTHAN":
                circleCode = "70";
                break;
            case "KOLKATA":
                circleCode = "31";
                break;
            case "WEST BENGAL":
                circleCode = "51";
                break;
            case "ORISSA":
                circleCode = "53";
                break;
            case "ASSAM":
                circleCode = "56";
                break;
            case "NESA":
                circleCode = "16";
                break;
            case "BIHAR":
                circleCode = "52";
                break;
            case "KARNATAKA":
                circleCode = "06";
                break;
            case "CHENNAI":
                circleCode = "40";
                break;
            case "TAMIL NADU":
                circleCode = "94";
                break;
            case "KERALA":
                circleCode = "95";
                break;
            case "AP":
                circleCode = "49";
                break;
        }
        return circleCode;
    }

    //Get Operator Code
    private String getOperatorCode(String operatorName){

        String operatorCode = "";
        if ("AIRTEL".equals(operatorName)) {
            operatorCode = "2";
        } else if ("IDEA".equals(operatorName)) {
            operatorCode = "6";
        } else if ("VODAFONE".equals(operatorName)) {
            operatorCode = "23";
        } else if ("BSNL".equals(operatorName)) {
            operatorCode = "4";
        } else if ("RELIANCE JIO INFOCOMM LIMITED".equals(operatorName)) {
            operatorCode = "11";
        }
        return operatorCode;
    }

    public String getOperatorCode() {
        return operatorCode;
    }

    public String getCircleCode() {
        return circleCode;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    @Override
    public void onComBoItemClick(Plans plans) {

        isSelected = true;
        amount = plans.getRs();
        Intent intent = new Intent();
        intent.putExtra(Constants.RECHARGE_AMOUNT, amount);
        setResult(RESULT_OK,intent);
        finish();
    }

    @Override
    public void onFullTTItemClick(Plans plans) {
        amount = plans.getRs();
        Intent intent = new Intent();
        intent.putExtra(Constants.RECHARGE_AMOUNT, amount);
        setResult(RESULT_OK,intent);
        finish();
    }

    @Override
    public void onRateCutterItemclick(Plans plans) {
        amount = plans.getRs();
        Intent intent = new Intent();
        intent.putExtra(Constants.RECHARGE_AMOUNT, amount);
        setResult(RESULT_OK,intent);
        finish();
    }

    @Override
    public void onRoamingItemclick(Plans plans) {
        amount = plans.getRs();
        Intent intent = new Intent();
        intent.putExtra(Constants.RECHARGE_AMOUNT, amount);
        setResult(RESULT_OK,intent);
        finish();
    }

    @Override
    public void onSmsItemClick(Plans plans) {
        amount = plans.getRs();
        Intent intent = new Intent();
        intent.putExtra(Constants.RECHARGE_AMOUNT, amount);
        setResult(RESULT_OK,intent);
        finish();
    }

    @Override
    public void onSpecialOfferItemClikc(Roffer roffer) {
        amount = roffer.getRs();
        Intent intent = new Intent();
        intent.putExtra(Constants.RECHARGE_AMOUNT, amount);
        setResult(RESULT_OK,intent);
        finish();
    }

    @Override
    public void onThreeGForuGItemClick(Plans plans) {
        amount = plans.getRs();
        Intent intent = new Intent();
        intent.putExtra(Constants.RECHARGE_AMOUNT, amount);
        setResult(RESULT_OK,intent);
        finish();
    }

    @Override
    public void onTopUpItemClikc(Plans plans) {
        amount = plans.getRs();
        Intent intent = new Intent();
        intent.putExtra(Constants.RECHARGE_AMOUNT, amount);
        setResult(RESULT_OK,intent);
        finish();
    }

    @Override
    public void onTwoGItemClick(Plans plans) {
        amount = plans.getRs();
        Intent intent = new Intent();
        intent.putExtra(Constants.RECHARGE_AMOUNT, amount);
        setResult(RESULT_OK,intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (!isSelected){
            Toast.makeText(getApplicationContext(),"Select A Plan or Press Back again to Exit",Toast.LENGTH_SHORT).show();
            isSelected = true;
        }else {
            super.onBackPressed();
        }
    }
}
