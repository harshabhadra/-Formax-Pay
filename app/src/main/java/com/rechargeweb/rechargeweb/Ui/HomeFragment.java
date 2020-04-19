package com.rechargeweb.rechargeweb.Ui;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rechargeweb.rechargeweb.Aeps.AepsActivity;
import com.rechargeweb.rechargeweb.Activity.DmtActivity;
import com.rechargeweb.rechargeweb.Activity.HomeActivity;
import com.rechargeweb.rechargeweb.Adapters.AepsSliderAdapter;
import com.rechargeweb.rechargeweb.Adapters.AllReportAdapter;
import com.rechargeweb.rechargeweb.Adapters.DmtSliderAdapter;
import com.rechargeweb.rechargeweb.Adapters.ItemAdapter;
import com.rechargeweb.rechargeweb.Adapters.SliderAdapter;
import com.rechargeweb.rechargeweb.Constant.Constants;
import com.rechargeweb.rechargeweb.Constant.DummyData;
import com.rechargeweb.rechargeweb.Gist.StatefulRecyclerView;
import com.rechargeweb.rechargeweb.Keys;
import com.rechargeweb.rechargeweb.Model.Details;
import com.rechargeweb.rechargeweb.Model.Items;
import com.rechargeweb.rechargeweb.Network.ApiService;
import com.rechargeweb.rechargeweb.Network.ApiUtills;
import com.rechargeweb.rechargeweb.R;
import com.rechargeweb.rechargeweb.ViewModels.AllReportViewModel;
import com.smarteist.autoimageslider.SliderView;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements ItemAdapter.OnItemclickListener, AllReportAdapter.OnReportItemClickListener {


    private static final String TAG = HomeFragment.class.getSimpleName();

    private StatefulRecyclerView itemRecyclerView;
    private RecyclerView reportRecyclerView;

    private ItemAdapter itemAdapter;
    private AllReportAdapter allReportAdapter;

    private TextView walletOneTv;
    private TextView walletTwoTv;
    private ImageView addMoneyImgOne, addMoneyImgTwo;
    private TextView transactionReportLabelTv;
    private Button dmtSendtButton;
    private Button aepsSendButton;

    private String walletOne, walletTWo;
    private ApiService apiService;
    private String id;
    private String authKey;
    private String user_id;
    private boolean isLoading;
    private AllReportViewModel allReportViewModel;

    private AlertDialog loadingDialog;

    private OnHomeItemClickLisetener homeItemClickLisetener;
    private OnReportItemClickListener reportItemClickListener;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        isLoading = false;
        authKey = new Keys().apiKey();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //Initializing AllReportViewModel
        allReportViewModel = ViewModelProviders.of(this).get(AllReportViewModel.class);

        //Initializing wallet one and wallet two text view
        walletOneTv = view.findViewById(R.id.home_walllet_1_tv);
        walletTwoTv = view.findViewById(R.id.home_wallet_2_tv);

        //Initializing Add Money img one and two
        addMoneyImgOne = view.findViewById(R.id.add_money_img_1);
        addMoneyImgTwo = view.findViewById(R.id.add_money_img_2);

        //Initializing Aeps and dmt send button
        dmtSendtButton = view.findViewById(R.id.dmt_home_button);
        aepsSendButton = view.findViewById(R.id.aeps_home_button);

        //Initializing Transaction report label text view
        transactionReportLabelTv = view.findViewById(R.id.transaction_report_label);

        HomeActivity activity = (HomeActivity) getActivity();
        if (activity != null) {
            activity.getSupportActionBar().setTitle(getResources().getString(R.string.my_app_name));
            id = activity.getSession_id();
            user_id = activity.getUser_id();
        }
        apiService = ApiUtills.getApiService();

        DummyData dummyData = new DummyData();

        //Setting up Slider View
        SliderView sliderView = view.findViewById(R.id.imageSlider);
        SliderAdapter sliderAdapter = new SliderAdapter(getContext());
        sliderView.setSliderAdapter(sliderAdapter);

        //Setting Dmt Slider View
        SliderView dmtSlider = view.findViewById(R.id.dmt_slider_view);
        DmtSliderAdapter dmtSliderAdapter = new DmtSliderAdapter(getContext());
        dmtSlider.setSliderAdapter(dmtSliderAdapter);

        //Setting up Aeps Slider View
        SliderView aepsSlider = view.findViewById(R.id.aeps_slider_view);
        AepsSliderAdapter aepsSliderAdapter = new AepsSliderAdapter(getContext());
        aepsSlider.setSliderAdapter(aepsSliderAdapter);

        //Initializing RecyclerView
        itemRecyclerView = view.findViewById(R.id.item_recycler);
        itemRecyclerView.setHasFixedSize(true);
        itemRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        itemAdapter = new ItemAdapter(getContext(), HomeFragment.this, dummyData.getItemsList());
        itemRecyclerView.setAdapter(itemAdapter);

        //Setting Report Recycler
        reportRecyclerView = view.findViewById(R.id.home_report_recycler);
        reportRecyclerView.setHasFixedSize(true);
        reportRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        allReportAdapter = new AllReportAdapter(getContext(), HomeFragment.this, dummyData.getReportList());
        reportRecyclerView.setAdapter(allReportAdapter);

        //On Click add money one ImageView
        addMoneyImgOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
//                Fragment fragment = new AddMoneyFragment();
//                fragmentTransaction.replace(R.id.main_container, fragment);
//                fragmentTransaction.commit();
//                activity.setFragment(fragment);
            }
        });

        //On Click AddMoneyTwo ImageView
        addMoneyImgTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
//                Fragment fragment = new AddMoneyFragment();
//                fragmentTransaction.replace(R.id.main_container, fragment);
//                fragmentTransaction.commit();
//                activity.setFragment(fragment);
            }
        });

        //On Click TransactionReportLabel TextView label
        transactionReportLabelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportRecyclerView.smoothScrollToPosition(100);
            }
        });

        //On Dmt home button click
        dmtSendtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dmtIntent = new Intent(getActivity(), DmtActivity.class);
                dmtIntent.putExtra(Constants.SESSION_ID, id);
                startActivity(dmtIntent);
            }
        });

        //On Aeps home button click
        aepsSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), AepsActivity.class);
                intent.putExtra(Constants.SESSION_ID, id);
                intent.putExtra(Constants.USER_ID, user_id);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e(TAG, "onActivityCreated");
    }

    @Override
    public void onResume() {
        super.onResume();
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.move_from_right);
        itemRecyclerView.startAnimation(animation);
        sendRequest(id, authKey);
    }

    private void sendRequest(String id, String key) {
        apiService.setDetailsPost(id, key).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Details>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Details details) {
                        Log.e(TAG, "success: " + details.toString());
                        walletOne = details.getWallet_1();
                        walletTWo = details.getWallet_2();
                        walletOneTv.setText(walletOne);
                        walletTwoTv.setText(walletTWo);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "error");
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();

                        isLoading = false;
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        homeItemClickLisetener = (OnHomeItemClickLisetener) context;
        reportItemClickListener = (OnReportItemClickListener) getActivity();
    }

    @Override
    public void onItemClick(int position) {

        if (!isLoading) {
            Items rItem = itemAdapter.getItem(position);
            homeItemClickLisetener.onHomeItemclick(rItem);
        }
    }

    @Override
    public void onReportItemClick(int position) {

        Items items = allReportAdapter.getReportItem(position);
        reportItemClickListener.onReportItemClick(items);
    }

    public interface OnReportItemClickListener {
        void onReportItemClick(Items items);
    }

    public interface OnHomeItemClickLisetener {
        void onHomeItemclick(Items items);
    }
}
