package com.rechargeweb.rechargeweb.Aeps;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.easypay.epmoney.epmoneylib.baseframework.model.PaisaNikalConfiguration;
import com.easypay.epmoney.epmoneylib.baseframework.model.PaisaNikalRequest;
import com.easypay.epmoney.epmoneylib.response_model.PaisaNikalTransactionResponse;
import com.easypay.epmoney.epmoneylib.ui.activity.IntermidiateActivity;
import com.easypay.epmoney.epmoneylib.utils.PaisaNikalConfig;
import com.rechargeweb.rechargeweb.Checksum;
import com.rechargeweb.rechargeweb.Keys;
import com.rechargeweb.rechargeweb.R;
import com.rechargeweb.rechargeweb.ViewModels.AllReportViewModel;
import com.rechargeweb.rechargeweb.databinding.FragmentIciciAepsBinding;

import org.apache.commons.codec.DecoderException;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 */
public class IciciAepsFragment extends Fragment {

    private static final String TAG = YblAepsFragment.class.getSimpleName();
    private final static String CONFIG_ENVIRONMENT = PaisaNikalConfig.Config.ENVIRONMENT_PRODUCTION;
    private String CONFIG_AGENT_ID_CODE = "FORMAX01";//RS00789
    private String CONFIG_AGENT_NMAE = "Ezazul Haque"; //Agent name
    private PaisaNikalConfiguration config = null;
    private PaisaNikalRequest apiRequest = null;

    private static final Integer CODE_AEPS_TRANSACTION = 1010;
    private static final Integer CODE_MICRO_TRANSACTION = 1011;

    String mobileNumber;
    String amount;
    private boolean isBalanceCheck;
    int position;

    String session_id, user_id;
    String auth;
    String orderId;

    StringBuilder balanceBuilder, withdrawalBuilder;

    AllReportViewModel allReportViewModel;
    private FragmentIciciAepsBinding finoAepsBinding;
    public IciciAepsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        finoAepsBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_icici_aeps,container,false);

        AepsActivity aepsActivity = (AepsActivity) getActivity();
        session_id = aepsActivity.getSession_id();
        user_id = aepsActivity.getUser_id();

        //Initializing auth key
        auth = new Keys().apiKey();

        config = new PaisaNikalConfiguration();
        config.setAgentId(CONFIG_AGENT_ID_CODE);
        config.setAgentName(CONFIG_AGENT_NMAE);
        config.setEnvironment(CONFIG_ENVIRONMENT);

        allReportViewModel = new ViewModelProvider(this).get(AllReportViewModel.class);

        isBalanceCheck = true;

        //Setting Up Tool Bar
        ((AppCompatActivity) getActivity()).setSupportActionBar(finoAepsBinding.aepsToolbar);

        finoAepsBinding.icicBalanceInfoTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBalanceCheck = true;
                finoAepsBinding.icicBalanceInfoTv.setText(getResources().getString(R.string.balance_enquiry));

                if (finoAepsBinding.icicAmountTextInput.getText().length() > 0) {
                    finoAepsBinding.icicAmountTextInput.getText().clear();
                    finoAepsBinding.icicAmountTextInput.setEnabled(false);
                }

                String[] typeList = {getResources().getString(R.string.balance_enquiry), "Cash Withdrawal"};
                position = 0;
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("What you'd like to do?");
                builder.setSingleChoiceItems(typeList, position, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (which == position) {
                            isBalanceCheck = true;
                            finoAepsBinding.icicBalanceInfoTv.setText(typeList[0]);
                            finoAepsBinding.icicAmountTextInput.setEnabled(false);
                            dialog.dismiss();
                        } else {
                            isBalanceCheck = false;
                            finoAepsBinding.icicBalanceInfoTv.setText(typeList[1]);
                            finoAepsBinding.icicAmountTextInput.setEnabled(true);
                            dialog.dismiss();
                        }
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        finoAepsBinding.icicMobileNumberTextInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                finoAepsBinding.icicMobileNumberLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
                finoAepsBinding.icicMobileNumberLayout.setErrorEnabled(true);
                if (s.length() == 10) {
                    finoAepsBinding.icicSubmitButton.setEnabled(true);
                } else {
                    finoAepsBinding.icicSubmitButton.setEnabled(false);
                }
            }
        });

        finoAepsBinding.icicAmountTextInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                finoAepsBinding.icicAmountLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
                finoAepsBinding.icicAmountLayout.setErrorEnabled(true);
                if (s.length() > 0 && !isValidAmount(s.toString())) {
                    finoAepsBinding.icicAmountLayout.setError("Enter Amount between 100 to 10000");
                }
            }
        });

        finoAepsBinding.icicSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isBalanceCheck) {
                    mobileNumber = finoAepsBinding.icicMobileNumberTextInput.getText().toString().trim();
                    amount = finoAepsBinding.icicAmountTextInput.getText().toString().trim();

                    if (!amount.isEmpty() && isValidAmount(amount) && isValidMobile(mobileNumber)) {
                        finoAepsBinding.icicAmountTextInput.getText().clear();
                        finoAepsBinding.icicMobileNumberTextInput.getText().clear();

                        withdrawalBuilder = new StringBuilder();
                        withdrawalBuilder.append("IAW");
                        withdrawalBuilder.append(System.currentTimeMillis());
                        orderId = withdrawalBuilder.toString();

                        View view = getLayoutInflater().inflate(R.layout.loading_dialog, null);
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setCancelable(false);
                        builder.setView(view);

                        AlertDialog dialog = builder.create();
                        dialog.show();
                        allReportViewModel.sendAepsDetails(session_id, auth, "AW", amount, orderId, mobileNumber).observe(getViewLifecycleOwner(),
                                new Observer<String>() {
                                    @Override
                                    public void onChanged(String s) {
                                        dialog.dismiss();
                                        if (s.equals("Success")) {
                                            cashWithDrawal(mobileNumber, amount);
                                        } else {

                                            Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
                                            getActivity().finish();
                                        }
                                    }
                                });
                    } else if (!isValidAmount(amount) || amount.isEmpty()) {
                        finoAepsBinding.icicAmountLayout.setError("Enter Valid Amount");
                    } else if (!(isValidMobile(mobileNumber)) || mobileNumber.isEmpty()) {
                        finoAepsBinding.icicMobileNumberLayout.setError("Enter Valid Mobile Number");
                    }
                } else {
                    mobileNumber = finoAepsBinding.icicMobileNumberTextInput.getText().toString().trim();

                    if (isValidMobile(mobileNumber) && (!mobileNumber.isEmpty())) {

                        finoAepsBinding.icicMobileNumberTextInput.getText().clear();

                        balanceBuilder = new StringBuilder();
                        balanceBuilder.append("IAB");
                        balanceBuilder.append(System.currentTimeMillis());
                        orderId = balanceBuilder.toString();

                        Log.e(TAG, "Order Id: " + balanceBuilder.toString());

                        View view = getLayoutInflater().inflate(R.layout.loading_dialog, null);
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setCancelable(false);
                        builder.setView(view);

                        AlertDialog dialog = builder.create();
                        dialog.show();
                        allReportViewModel.sendAepsDetails(session_id, auth, "AB", "0.00", orderId, mobileNumber).observe(getViewLifecycleOwner(), new Observer<String>() {
                            @Override
                            public void onChanged(String s) {
                                dialog.dismiss();
                                if (s.equals("Success")) {
                                    checkBalance(mobileNumber);
                                } else {
                                    Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
                                    getActivity().finish();
                                }
                            }
                        });
                    } else {
                        finoAepsBinding.icicMobileNumberLayout.setError("Enter Valid Mobile Number");
                    }
                }
            }
        });
        return finoAepsBinding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_MICRO_TRANSACTION && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    PaisaNikalTransactionResponse apiResponse = bundle.getParcelable(PaisaNikalConfig.UI.FINO_TRANSACTION_RESPONSE);
                    Log.e(TAG, "Response: " + apiResponse);
                    if (apiResponse != null && apiResponse.getRESPCODE() == 200) {
                        //Success handler
                        Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
                    } else {
                        //Fail handler
                        Toast.makeText(getContext(), "Error: " + apiResponse.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else if (requestCode == CODE_MICRO_TRANSACTION && resultCode == Activity.RESULT_CANCELED) {
            //handler for user canceled
            Toast.makeText(getContext(), "Request has been canceled by user", Toast.LENGTH_SHORT).show();
        } else {
            getActivity().finish();
        }
    }

    private void checkBalance(String mobileNumber) {
        //Aeps balance check request
        apiRequest = new PaisaNikalRequest();
        //Transaction Amount, need to pass 0
        apiRequest.setAmount("0");
        try {
            apiRequest.setCheckSum(Checksum.getCheckSum(CONFIG_AGENT_ID_CODE));
        } catch (DecoderException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //User entered mobile number
        //Mobile number should be start with >= 6 of first later
        apiRequest.setMobileNumber(mobileNumber);
        apiRequest.setOrderId(balanceBuilder.toString());
        apiRequest.setTimeStemp(Checksum.currentTime.toString());
        apiRequest.setToken(Checksum.randomNumber);
        apiRequest.setServiceCode(String.valueOf(PaisaNikalConfig.ApiServices.YBL_AEPS__BALANCE_INQUIRY));


        Intent intent = new Intent(getContext(), IntermidiateActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(PaisaNikalConfig.Config.PAISA_NIKAL_CONFIG, config);
        bundle.putParcelable(PaisaNikalConfig.Config.PAISA_NIKAL_REQUEST, apiRequest);
        intent.putExtras(bundle);
        startActivityForResult(intent, CODE_AEPS_TRANSACTION);
    }

    //Fino AEPS withdrawal
    private void cashWithDrawal(String mobileNumber, String amount) {
        //Aeps withdrawal cash request
        apiRequest = new PaisaNikalRequest();
        apiRequest.setAmount(amount); //Transaction Amount
        try {
            apiRequest.setCheckSum(Checksum.getCheckSum(CONFIG_AGENT_ID_CODE));
        } catch (DecoderException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        apiRequest.setMobileNumber(mobileNumber);
        apiRequest.setOrderId(withdrawalBuilder.toString());
        apiRequest.setTimeStemp(Checksum.currentTime.toString());
        apiRequest.setToken(Checksum.randomNumber);
        apiRequest.setServiceCode(String.valueOf(PaisaNikalConfig.ApiServices.YBL_AEPS__CASH_WITHDRAW));


        Intent intent = new Intent(getContext(), IntermidiateActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(PaisaNikalConfig.Config.PAISA_NIKAL_CONFIG, config);
        bundle.putParcelable(PaisaNikalConfig.Config.PAISA_NIKAL_REQUEST, apiRequest);
        intent.putExtras(bundle);
        startActivityForResult(intent, CODE_AEPS_TRANSACTION);
    }

    //Check if the amount is valid
    private boolean isValidAmount(String amt) {
        int value = Integer.parseInt(amt);
        return value >= 100 && value <= 10000;
    }

    //Check if the mobile no. is correct
    private boolean isValidMobile(String s) {
        Pattern pattern = Pattern.compile("(0/91)?[6-9][0-9]{9}");

        Matcher matcher = pattern.matcher(s);
        return matcher.matches();
    }
}
