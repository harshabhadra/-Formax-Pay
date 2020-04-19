package com.rechargeweb.rechargeweb.Constant;

import com.rechargeweb.rechargeweb.Model.Items;
import com.rechargeweb.rechargeweb.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DummyData {

    public DummyData() {
    }


    //Get Utility service items
    public List<Items> getItemsList() {

        List<Items> dummyList = new ArrayList<>();
        dummyList.add(new Items(R.drawable.mobile_icon, "Mobile"));
        dummyList.add(new Items(R.drawable.dth_icon, "DTH"));
        dummyList.add(new Items(R.drawable.landline_icon, "Telephone"));
        dummyList.add(new Items(R.drawable.electricity_icon, "Electricity"));
        dummyList.add(new Items(R.drawable.pan_icon,"PAN Coupon"));
        dummyList.add(new Items(R.drawable.dmt_icon,"DMT"));
        dummyList.add(new Items(R.drawable.aeps_icon,"ICICI AEPS"));
        dummyList.add(new Items(R.drawable.aeps_icon,"YBL AEPS"));
        return dummyList;
    }

    //Get report items
    public List<Items> getReportList() {
        List<Items> itemsList = new ArrayList<>();
        itemsList.add(new Items(R.drawable.mobile_icon, "Recharge"));
        itemsList.add(new Items(R.drawable.aeps_icon,"AEPS"));
        itemsList.add(new Items(R.drawable.coupon_icon, "Coupon"));
        itemsList.add(new Items(R.drawable.credit_icon, "Credit"));
        itemsList.add(new Items(R.drawable.debit_icon, "Debit"));

        return itemsList;
    }

    //Get balance items
    public List<Items>getAddMoneyTerms(){
        List<Items>itemsList = new ArrayList<>();
        itemsList.add(new Items(R.drawable.ic_play_arrow_black_24dp, "Load money via Debit Card amount below Rs. 2000/- transaction charges is FREE."));
        itemsList.add(new Items(R.drawable.ic_play_arrow_black_24dp, "Load money via Debit Card amount above Rs. 2000/- transaction charges is 1%"));
        itemsList.add(new Items(R.drawable.ic_play_arrow_black_24dp, "Load money via Credit Card transaction charges is 1.99%"));
        itemsList.add(new Items(R.drawable.ic_play_arrow_black_24dp, "Load money via Internet Banking transaction charges is 1.80%"));
        itemsList.add(new Items(R.drawable.ic_play_arrow_black_24dp, "The money will be automatically loaded in your account."));
        itemsList.add(new Items(R.drawable.ic_play_arrow_black_24dp, "Load money via Debit Card amount below Rs. 2000/- transaction charges is FREE."));

        return itemsList;
    }

    //Get Payment mode
    public List<String>getPaymentMode(){
        List<String>paymentList = new ArrayList<>();
        paymentList.add("NEFT");
        paymentList.add("IMPS");
        paymentList.add("UPI/BHIM");
        paymentList.add("RTGS");
        paymentList.add("CASH DEPOSIT");
        paymentList.add("BANK TRANSFER");
        return paymentList;
    }

    //List of States
    public List<String>getStateList(){
        List<String>stateList = new ArrayList<>();
        stateList.add("Maharashtra");
        stateList.add("Rajasthan");
        stateList.add("Karnataka");
        stateList.add("Delhi");
        stateList.add("West Bengal");
        stateList.add("Odisha");
        stateList.add("Chhattisgarh");
        stateList.add("Daman and Diu");
        stateList.add("Gujarat");
        stateList.add("Nagaland");
        stateList.add("Dadra and Nagar Haveli");
        stateList.add("Andhra Pradesh");
        stateList.add("Goa");
        stateList.add("Himachal Pradesh");
        stateList.add("Bihar");
        stateList.add("Jharkhand");
        stateList.add("Uttar Pradesh");
        stateList.add("Kerala");
        stateList.add("Meghalaya");
        stateList.add("Madhya Pradesh");
        stateList.add("Mizoram");
        stateList.add("Punjab");
        stateList.add("Sikkim");
        stateList.add("Tamil Nadu");
        stateList.add("Tripura");
        stateList.add("Haryana");
        stateList.add("Uttarakhand");
        Collections.sort(stateList,String.CASE_INSENSITIVE_ORDER);
        return stateList;
    }
}
