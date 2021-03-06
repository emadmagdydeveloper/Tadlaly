package com.semicolon.tadlaly.Fragments;

import android.app.AlertDialog;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.semicolon.tadlaly.Activities.HomeActivity;
import com.semicolon.tadlaly.Adapters.SubDeptAdsAdapter_Visitor;
import com.semicolon.tadlaly.Models.MyAdsModel;
import com.semicolon.tadlaly.Models.ResponseModel;
import com.semicolon.tadlaly.Models.UserModel;
import com.semicolon.tadlaly.R;
import com.semicolon.tadlaly.Services.Api;
import com.semicolon.tadlaly.Services.Services;
import com.semicolon.tadlaly.Services.Tags;
import com.semicolon.tadlaly.SingleTone.LatLngSingleTone;
import com.semicolon.tadlaly.SingleTone.UserSingleTone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SubDataFragment extends Fragment implements UserSingleTone.OnCompleteListener,LatLngSingleTone.onLatLngSuccess{
    private ProgressBar progressBar,progBarFollow;
    private RecyclerView recyclerView;
    private SubDeptAdsAdapter_Visitor adapter;
    private RecyclerView.LayoutManager manager;
    private TextView no_ads;
    private Button order_onNewBtn,order_onNearbyBtn,btn_follow;
    private List<MyAdsModel> myAdsModelList,myAdsModelList1,finalmyAdsModelList;
    private String sub_depId;
    private String depId;
    private UserSingleTone userSingleTone;
    private UserModel userModel;
    private static final String TAG1 ="sub_dept_id";
    private static final String TAG2="dept_id";
    private static final String TAG3="user_id";

    private double mylat=0.0,myLng=0.0;
    private List<Double> distList;
    private Map<String,Double> map;
    private List<String> idsList;
    private int page_index=1;
    private String button_type="nearby";
    private String user_id="";
    private Call<List<MyAdsModel>> call;
    private LatLngSingleTone latLngSingleTone;
    private String user_type;
    public ImageView image_top;
    private HomeActivity  activity;
    private boolean isFollow = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sub_dept_row,container,false);
        initView(view);

        return view;
    }

    private void initLoadMore() {
        adapter.setLoadListener(new SubDeptAdsAdapter_Visitor.OnLoadListener() {
            @Override
            public void onLoadMore() {
                if (button_type.equals("nearby"))
                {
                    myAdsModelList.add(null);
                    adapter.notifyItemInserted(myAdsModelList.size()-1);
                    int index = page_index;
                    onLoadgetData(sub_depId,index);
                }else
                {
                    myAdsModelList.add(null);
                    adapter.notifyItemInserted(myAdsModelList.size()-1);
                    int index = page_index;
                    onLoadgetOrderedData(sub_depId,index);
                }
            }
        });



    }

    private void initView(View view) {

        activity = (HomeActivity) getActivity();
        myAdsModelList = new ArrayList<>();
        finalmyAdsModelList = new ArrayList<>();
        myAdsModelList1 = new ArrayList<>();
        distList = new ArrayList<>();
        order_onNewBtn = view.findViewById(R.id.order_onNewBtn);
        order_onNearbyBtn = view.findViewById(R.id.order_onNearbyBtn);
        btn_follow = view.findViewById(R.id.btn_follow);

        map = new HashMap<>();
        idsList = new ArrayList<>();
        image_top = view.findViewById(R.id.image_top);
        no_ads = view.findViewById(R.id.no_ads);
        progressBar =view.findViewById(R.id.progBar);
        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getActivity(),R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        progBarFollow =view.findViewById(R.id.progBarFollow);
        progBarFollow.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getActivity(),R.color.colorPrimary), PorterDuff.Mode.SRC_IN);


        recyclerView = view.findViewById(R.id.recView);
        manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);


        Bundle bundle = getArguments();
        if (bundle!=null)
        {
            sub_depId = bundle.getString(TAG1);
            Log.e("sub_id",sub_depId);

            depId = bundle.getString(TAG2);
            user_id = bundle.getString(TAG3);
            if (!user_id.equals("0"))
            {
                userSingleTone = UserSingleTone.getInstance();
                userSingleTone.getUser(this);
                adapter = new SubDeptAdsAdapter_Visitor(recyclerView,getActivity(),myAdsModelList,this,true);
                recyclerView.setAdapter(adapter);
                getFollowDepartment();

            }else if (user_id.equals("0"))
            {
                latLngSingleTone = LatLngSingleTone.getInstance();
                latLngSingleTone.getLatLng(this);
                adapter = new SubDeptAdsAdapter_Visitor(recyclerView,getActivity(),myAdsModelList,this,false);
                recyclerView.setAdapter(adapter);
            }
        }

        order_onNewBtn.setOnClickListener(view1 -> {
            button_type="ordered";
            myAdsModelList1.clear();
            myAdsModelList.clear();
            finalmyAdsModelList.clear();
            idsList.clear();
            distList.clear();
            map.clear();
            order_onNewBtn.setBackgroundResource(R.drawable.btn_selected);
            order_onNewBtn.setTextColor(ContextCompat.getColor(getActivity(),R.color.white));

            order_onNearbyBtn.setBackgroundResource(R.drawable.btn_unselected);
            order_onNearbyBtn.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimary));

            getOrderedData(sub_depId,1);
        });

        order_onNearbyBtn.setOnClickListener(view1 -> {
            button_type="nearby";
            myAdsModelList1.clear();
            myAdsModelList.clear();
            finalmyAdsModelList.clear();
            idsList.clear();
            distList.clear();
            map.clear();
            order_onNewBtn.setBackgroundResource(R.drawable.btn_unselected);
            order_onNewBtn.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimary));
            order_onNearbyBtn.setBackgroundResource(R.drawable.btn_selected);
            order_onNearbyBtn.setTextColor(ContextCompat.getColor(getActivity(),R.color.white));


            getData(sub_depId,1);
            Log.e("id", sub_depId);
        });

        btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user_id.equals("0"))
                {
                    CreateServiceDialog();
                }else
                    {
                        if (isFollow)
                        {
                            follow_unFollow("unfollow");
                        }else
                            {
                                follow_unFollow("follow");
                            }
                    }
            }
        });

        image_top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.smoothScrollToPosition(0);
                image_top.setVisibility(View.GONE);
            }
        });
        getData(sub_depId, 1);
        initLoadMore();
    }

    private void follow_unFollow(String state) {

        progBarFollow.setVisibility(View.VISIBLE);
        btn_follow.setEnabled(false);
        Api.getRetrofit(Tags.Base_Url)
                .create(Services.class)
                .follow_unFollow(depId,userModel.getUser_id(),state)
                .enqueue(new Callback<ResponseModel>() {
                    @Override
                    public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                        if (response.isSuccessful()&&response.body()!=null&&response.body().getSuccess_follow() ==1)
                        {
                            if (state.equals("follow"))
                            {
                                updateFollowButton(true);
                            }else
                                {
                                    updateFollowButton(false);

                                }

                        }else
                        {
                            progBarFollow.setVisibility(View.GONE);
                            btn_follow.setEnabled(true);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseModel> call, Throwable t) {
                        try
                        {
                            progBarFollow.setVisibility(View.GONE);
                            btn_follow.setEnabled(true);
                            Log.e("Error",t.getMessage());
                        }catch (Exception e){}
                    }
                });
    }


    private void getFollowDepartment() {
        progBarFollow.setVisibility(View.VISIBLE);
        btn_follow.setEnabled(false);
        Api.getRetrofit(Tags.Base_Url)
                .create(Services.class)
                .isFollowDepartment(depId,userModel.getUser_id(),"checkfollow")
                .enqueue(new Callback<ResponseModel>() {
                    @Override
                    public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                        if (response.isSuccessful()&&response.body()!=null)
                        {
                            updateFollowButton(response.body().isStatus_follow());
                        }else
                            {
                                progBarFollow.setVisibility(View.GONE);
                                btn_follow.setEnabled(true);
                            }
                    }

                    @Override
                    public void onFailure(Call<ResponseModel> call, Throwable t) {
                        try
                        {
                            progBarFollow.setVisibility(View.GONE);
                            btn_follow.setEnabled(true);
                            Log.e("Error",t.getMessage());
                        }catch (Exception e){}
                    }
                });
    }

    private void updateFollowButton(boolean status_follow) {
        progBarFollow.setVisibility(View.GONE);
        btn_follow.setEnabled(true);
        isFollow = status_follow;

        if (status_follow)
        {
            btn_follow.setText(getString(R.string.unfollow));
        }else
            {
                btn_follow.setText(getString(R.string.follow));

            }
    }

    public static SubDataFragment newInstance(String deptId, String sub_dept_id, String user_id)
    {
        SubDataFragment dataFragment = new SubDataFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TAG1,sub_dept_id);
        bundle.putString(TAG2,deptId);
        bundle.putString(TAG3,user_id);

        dataFragment.setArguments(bundle);
        return dataFragment;
    }

    private void getData(String supDept_id, int page_index)
    {

        Retrofit retrofit = Api.getRetrofit(Tags.Base_Url);
        if (user_id.equals("0"))
        {
            call = retrofit.create(Services.class).visitor_getSubDeptAds(Tags.display_new, supDept_id,page_index,String.valueOf(mylat),String.valueOf(myLng));

        }else if (!user_id.equals("0"))
            {
                call = retrofit.create(Services.class).getSubDept_Ads(Tags.display_new, user_id, supDept_id, page_index);

            }
                call.enqueue(new Callback<List<MyAdsModel>>() {
                    @Override
                    public void onResponse(Call<List<MyAdsModel>> call, Response<List<MyAdsModel>> response) {

                        if (response.isSuccessful())
                        {
                            progressBar.setVisibility(View.GONE);

                            myAdsModelList.clear();

                            if (response.body().size()>0)
                            {
                                no_ads.setVisibility(View.GONE);
                                myAdsModelList.addAll(response.body());
                                adapter.notifyDataSetChanged();
                            }else
                            {
                                no_ads.setVisibility(View.VISIBLE);

                            }

                        }



                    }

                    @Override
                    public void onFailure(Call<List<MyAdsModel>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getActivity(),R.string.something, Toast.LENGTH_SHORT).show();
                    }
                });



    }

    private void onLoadgetData(String supDept_id, int page_index)
    {

        Retrofit retrofit = Api.getRetrofit(Tags.Base_Url);
        if (user_id.equals("0"))
        {
            call = retrofit.create(Services.class).visitor_getSubDeptAds(Tags.display_nearby, supDept_id,page_index,String.valueOf(mylat),String.valueOf(myLng));

        }else if (!user_id.equals("0"))
        {
            call = retrofit.create(Services.class).getSubDept_Ads(Tags.display_nearby, user_id, supDept_id, page_index);

        }
        call.enqueue(new Callback<List<MyAdsModel>>() {
            @Override
            public void onResponse(Call<List<MyAdsModel>> call, Response<List<MyAdsModel>> response) {

                if (response.isSuccessful())
                {
                    if (response.body().size()>0)
                    {
                        SubDataFragment.this.page_index = SubDataFragment.this.page_index+1;
                        int lastpos = myAdsModelList.size()-1;
                        myAdsModelList.remove(myAdsModelList.size()-1);
                        adapter.notifyItemRemoved(myAdsModelList.size());
                        adapter.setLoaded();
                        myAdsModelList.addAll(response.body());
                        adapter.notifyItemRangeChanged(lastpos,myAdsModelList.size()-1);
                    }else
                    {
                        myAdsModelList.remove(myAdsModelList.size()-1);
                        adapter.notifyItemRemoved(myAdsModelList.size());
                        adapter.setLoaded();
                    }
                }

                      /*  if (response.isSuccessful())
                        {



                            if (myAdsModelList.size()>0)
                            {
                                no_ads.setVisibility(View.GONE);

                                myAdsModelList.remove(myAdsModelList.size()-1);
                                adapter.notifyItemRemoved(myAdsModelList.size());
                                adapter.setLoaded();
                                myAdsModelList.addAll(response.body());
                                adapter.notifyDataSetChanged();
                                adapter.setLoaded();


                            }else
                            {
                                if (response.body().size()>0)
                                {
                                    progressBar.setVisibility(View.GONE);
                                    no_ads.setVisibility(View.GONE);
                                    myAdsModelList.addAll(response.body());
                                    adapter.notifyDataSetChanged();
                                    adapter.setLoaded();

                                }else
                                {
                                    progressBar.setVisibility(View.GONE);
                                    no_ads.setVisibility(View.VISIBLE);
                                    adapter.setLoaded();

                                }
                            }
                        }*/

            }

            @Override
            public void onFailure(Call<List<MyAdsModel>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getActivity(),R.string.something, Toast.LENGTH_SHORT).show();
            }
        });
        /*myAdsModelList1.clear();
        Retrofit retrofit = Api.getRetrofit2(Tags.Base_Url);
        Observable<List<MyAdsModel>> observable = retrofit.create(Services.class).getSubDept_Ads(Tags.display_nearby,user_id, supDept_id, page_index);
        observable.flatMap(ads->Observable.fromIterable(ads))
                .flatMap(dis -> retrofit.create(Services.class).getDistance("https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + mylat + "," + myLng + "&destinations=" + dis.getGoogle_lat() + "," + dis.getGoogle_long() + "&key=" + getString(R.string.Api_key)), (myAdsModel, placesDistanceModel) -> {
                    myAdsModel.setM_distance(String.valueOf(placesDistanceModel.getRows().get(0).getElements().get(0).getDistanceObject().getValue()/1000));
                    Log.e("distanccccccccc",myAdsModel.getM_distance());
                    return myAdsModel;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<MyAdsModel>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(MyAdsModel myAdsModel) {
                        myAdsModelList1.add(myAdsModel);

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("error",e.getMessage());

                    }

                    @Override
                    public void onComplete() {
                        if (page_index==1&&myAdsModelList1.size()==0)
                        {
                            progressBar.setVisibility(View.GONE);
                            no_ads.setVisibility(View.VISIBLE);
                        }else
                        {
                            no_ads.setVisibility(View.GONE);

                            if (myAdsModelList1.size()>0)
                            {
                                SortData(myAdsModelList1);

                            }else
                            {
                                if (myAdsModelList.size()>0)
                                {
                                    myAdsModelList.remove(myAdsModelList.size()-1);
                                    adapter.notifyItemRemoved(myAdsModelList.size());
                                }

                            }

                        }
                    }
                });*/


    }

    private void getOrderedData(String depId, int page_index)
    {
        Retrofit retrofit = Api.getRetrofit(Tags.Base_Url);
        if (user_id.equals("0"))
        {
            call = retrofit.create(Services.class).visitor_getSubDeptAds(Tags.display_new,depId,page_index,String.valueOf(myLng),String.valueOf(myLng));

        }else
            {
                call = retrofit.create(Services.class).getSubDept_Ads(Tags.display_new, user_id, depId, page_index);

            }
                call.enqueue(new Callback<List<MyAdsModel>>() {
                    @Override
                    public void onResponse(Call<List<MyAdsModel>> call, Response<List<MyAdsModel>> response) {

                        if (response.isSuccessful())
                        {
                            progressBar.setVisibility(View.GONE);

                            myAdsModelList.clear();

                            if (response.body().size()>0)
                            {
                                no_ads.setVisibility(View.GONE);
                                myAdsModelList.addAll(response.body());
                                adapter.notifyDataSetChanged();
                            }else
                            {
                                no_ads.setVisibility(View.VISIBLE);

                            }

                        }


                        /*if (response.isSuccessful())
                        {
                            if (myAdsModelList.size()>0)
                            {
                                no_ads.setVisibility(View.GONE);

                                myAdsModelList.remove(myAdsModelList.size()-1);
                                adapter.notifyItemRemoved(myAdsModelList.size());
                                adapter.setLoaded();
                                myAdsModelList.addAll(response.body());
                                adapter.notifyDataSetChanged();
                                adapter.setLoaded();


                            }else
                            {
                                if (response.body().size()>0)
                                {
                                    progressBar.setVisibility(View.GONE);
                                    no_ads.setVisibility(View.GONE);
                                    myAdsModelList.addAll(response.body());
                                    adapter.notifyDataSetChanged();
                                    adapter.setLoaded();

                                }else
                                {
                                    progressBar.setVisibility(View.GONE);
                                    no_ads.setVisibility(View.VISIBLE);
                                    adapter.setLoaded();

                                }
                            }

                        }
*/
                    }

                    @Override
                    public void onFailure(Call<List<MyAdsModel>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getActivity(),R.string.something, Toast.LENGTH_SHORT).show();
                        Log.e("Error",t.getMessage());
                    }
                });
        /*Retrofit retrofit = Api.getRetrofit2(Tags.Base_Url);
        Observable<List<MyAdsModel>> observable = retrofit.create(Services.class).getSubDept_Ads(Tags.display_new,user_id, sub_depId, page_index);
        observable.flatMap(ads->Observable.fromIterable(ads))
                .flatMap(dis -> retrofit.create(Services.class).getDistance("https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + mylat + "," + myLng + "&destinations=" + dis.getGoogle_lat() + "," + dis.getGoogle_long() + "&key=" + getString(R.string.Api_key)), new BiFunction<MyAdsModel, PlacesDistanceModel, MyAdsModel>() {
                    @Override
                    public MyAdsModel apply(MyAdsModel myAdsModel, PlacesDistanceModel placesDistanceModel) throws Exception {
                        myAdsModel.setM_distance(String.valueOf(placesDistanceModel.getRows().get(0).getElements().get(0).getDistanceObject().getValue()/1000));
                        Log.e("distanccccccccc",myAdsModel.getM_distance());
                        return myAdsModel;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<MyAdsModel>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(MyAdsModel myAdsModel) {
                        myAdsModelList1.add(myAdsModel);

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("error",e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        if (page_index==1&&myAdsModelList1.size()==0)
                        {
                            progressBar.setVisibility(View.GONE);
                            no_ads.setVisibility(View.VISIBLE);
                        }else
                        {
                            no_ads.setVisibility(View.GONE);

                            if (myAdsModelList1.size()>0)
                            {
                                myAdsModelList.remove(myAdsModelList.size()-1);
                                adapter.notifyItemRemoved(myAdsModelList.size());
                                myAdsModelList.addAll(myAdsModelList1);
                                adapter.notifyDataSetChanged();
                            }else
                            {
                                myAdsModelList.remove(myAdsModelList.size()-1);
                                adapter.notifyItemRemoved(myAdsModelList.size());
                            }

                        }
                    }
                });
*/

    }

    private void onLoadgetOrderedData(String depId, int page_index)
    {
        Retrofit retrofit = Api.getRetrofit(Tags.Base_Url);
        if (user_id.equals("0"))
        {
            call = retrofit.create(Services.class).visitor_getSubDeptAds(Tags.display_new,depId,page_index,String.valueOf(myLng),String.valueOf(myLng));

        }else
        {
            call = retrofit.create(Services.class).getSubDept_Ads(Tags.display_new, user_id, depId, page_index);

        }
        call.enqueue(new Callback<List<MyAdsModel>>() {
            @Override
            public void onResponse(Call<List<MyAdsModel>> call, Response<List<MyAdsModel>> response) {

                if (response.isSuccessful())
                {
                    if (response.body().size()>0)
                    {
                        SubDataFragment.this.page_index = SubDataFragment.this.page_index+1;
                        int lastpos = myAdsModelList.size()-1;
                        myAdsModelList.remove(myAdsModelList.size()-1);
                        adapter.notifyItemRemoved(myAdsModelList.size());
                        adapter.setLoaded();
                        myAdsModelList.addAll(response.body());
                        adapter.notifyItemRangeChanged(lastpos,myAdsModelList.size()-1);
                    }else
                    {
                        myAdsModelList.remove(myAdsModelList.size()-1);
                        adapter.notifyItemRemoved(myAdsModelList.size());
                        adapter.setLoaded();
                    }
                }


            }

            @Override
            public void onFailure(Call<List<MyAdsModel>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getActivity(),R.string.something, Toast.LENGTH_SHORT).show();
                Log.e("Error",t.getMessage());
            }
        });


    }

    private void CreateServiceDialog() {
        AlertDialog.Builder serviceBuilder = new AlertDialog.Builder(getActivity());
        serviceBuilder.setMessage(R.string.ser_not_ava);
        serviceBuilder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });

        AlertDialog alertDialog = serviceBuilder.create();
        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

    }


    @Override
    public void onSuccess(UserModel userModel) {
        this.userModel =userModel;
        if (this.userModel!=null)
        {
            mylat = Double.parseDouble(userModel.getUser_google_lat());
            myLng = Double.parseDouble(userModel.getUser_google_long());
        }


    }

    @Override
    public void onSuccess(double lat, double lng) {
        mylat = lat;
        myLng = lng;
    }

    public void ItemData(MyAdsModel myAdsModel, int adapterPosition) {
        if (!user_id.equals("0")) {
            if (!myAdsModel.isRead_status()) {

                myAdsModel.setRead_status(true);
                myAdsModel.setReaded(false);
                myAdsModelList.set(adapterPosition, myAdsModel);
                adapter.notifyItemChanged(adapterPosition, myAdsModel);
            }
        }

        activity.SetMyadsData(myAdsModel);


    }
}
