package com.example.windows10.ltd_learning.mFragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.windows10.ltd_learning.MyAPI;
import com.example.windows10.ltd_learning.mInterface.ElearningAPI;
import com.example.windows10.ltd_learning.mModel.Course;
import com.example.windows10.ltd_learning.R;
import com.example.windows10.ltd_learning.mRecycler.CustomAdapter;
import com.google.gson.Gson;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by Windows10 on 10/10/2017.
 */

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Windows10 on 10/11/2017.
 */

public class SearchFragment extends Fragment {

    private EditText textSearch;
    private Button searchButton;
    private Button byCourseName;
    private Button byTeacherName;
    private String searchBy="name";
    private ImageView loadingImage;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerViewReadyCallback recyclerViewReadyCallback;

    private ElearningAPI elearningAPI;

    public interface RecyclerViewReadyCallback {
        void onLayoutReady();
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment,container,false);
        bindView(view);
        loadingImage =  view.findViewById(R.id.imageView);

        Glide.with(getContext()).load(R.drawable.loading).into(loadingImage);

        getCourse();
        byCourseName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchBy="name";
                textSearch.setHint("course name");
            }
        });
        byTeacherName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchBy="teacherName";
                textSearch.setHint("teacher name");
            }
        });
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //getCourse(URL_GET_COURSE+"?"+searchBy+"="+textSearch.getText().toString());
                if (searchBy.equals("name"))
                {
                    getCourseByName(textSearch.getText().toString());
                }
                else
                {
                    getCourseByTeacherName(textSearch.getText().toString());
                }
            }
        });

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(getContext(),"refresh",Toast.LENGTH_LONG).show();
                //getCourse(URL_GET_COURSE+"?"+searchBy+"="+textSearch.getText().toString());
                getCourse();

            }
        });

        return view;
    }
    public void getCourse()
    {



            /*new AsyncTask<String, Void, String>() {
                @Override
                protected String doInBackground(String... url) {
                    String result = "";
                    try {

                        HttpGet httpGet = new HttpGet(url[0]);
                        HttpClient client = new DefaultHttpClient();

                        HttpResponse response = client.execute(httpGet);

                        int statusCode = response.getStatusLine().getStatusCode();

                        if (statusCode == 200) {
                            InputStream inputStream = response.getEntity().getContent();
                            BufferedReader reader = new BufferedReader
                                    (new InputStreamReader(inputStream));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                result += line;
                            }
                        }

                    } catch (ClientProtocolException e) {
                        Log.d("ClientProtocolException", e.getMessage());

                    } catch (IOException e) {
                        Log.d("IOException", e.getMessage());
                    }
                    return result;
                }

                protected void onPostExecute(String jsonString) {
                    showData(jsonString);
                    swipeRefreshLayout.setRefreshing(false);
                }
            }.execute(url);*/

        Call<ResponseBody> responseBody = elearningAPI.getAllCourses();
        responseBody.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> rawResponse) {
                if (rawResponse.code()==200) {
                    try {
                        String jsonString = rawResponse.body().string();

                        if (!jsonString.equals("")) {
                            Gson gson = new Gson();
                            Course data = gson.fromJson(jsonString, Course.class);

                            Course.StatusBean response = data.getStatus();
                            if (data.getCourses() != null)
                                if (response.isStatus()) {
                                    List<Course.CoursesBean> courses = data.getCourses();

                                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                    recyclerView.setAdapter(new CustomAdapter(getContext(), courses));
                                    recyclerView.setHasFixedSize(true);
                                    recyclerView.setVisibility(View.INVISIBLE);
                                    recyclerViewReadyCallback = new RecyclerViewReadyCallback() {
                                        @Override
                                        public void onLayoutReady() {
                                            //
                                            //here comes your code that will be executed after all items are laid down
                                            //
                                            loadingImage.setVisibility(View.GONE);
                                            recyclerView.setVisibility(View.VISIBLE);
                                        }
                                    };
                                    recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                        @Override
                                        public void onGlobalLayout() {
                                            if (recyclerViewReadyCallback != null) {
                                                recyclerViewReadyCallback.onLayoutReady();
                                            }
                                            recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                        }

                                    });

                                } else {

                                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                    recyclerView.setAdapter(null);
                                }
                        } else {

                            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            recyclerView.setAdapter(null);
                        }
                        swipeRefreshLayout.setRefreshing(false);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setAdapter(null);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }

    public void getCourseByName(String name)
    {
        Call<ResponseBody> responseBody = elearningAPI.getCourseByName(name);
        responseBody.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> rawResponse) {
                if (rawResponse.code()==200) {
                    try {
                        String jsonString = rawResponse.body().string();

                        if (!jsonString.equals("")) {
                            Gson gson = new Gson();
                            Course data = gson.fromJson(jsonString, Course.class);

                            Course.StatusBean response = data.getStatus();

                            if (response.isStatus()) {
                                List<Course.CoursesBean> courses = data.getCourses();

                                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                recyclerView.setAdapter(new CustomAdapter(getContext(), courses));
                                recyclerView.setHasFixedSize(true);
                                recyclerView.setVisibility(View.INVISIBLE);
                                recyclerViewReadyCallback = new RecyclerViewReadyCallback() {
                                    @Override
                                    public void onLayoutReady() {
                                        //
                                        //here comes your code that will be executed after all items are laid down
                                        //
                                        loadingImage.setVisibility(View.GONE);
                                        recyclerView.setVisibility(View.VISIBLE);
                                    }
                                };
                                recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                    @Override
                                    public void onGlobalLayout() {
                                        if (recyclerViewReadyCallback != null) {
                                            recyclerViewReadyCallback.onLayoutReady();
                                        }
                                        recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                    }

                                });

                            } else {

                                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                recyclerView.setAdapter(null);
                            }
                        } else {

                            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            recyclerView.setAdapter(null);
                        }
                        swipeRefreshLayout.setRefreshing(false);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setAdapter(null);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public void getCourseByTeacherName(String teacherName)
    {
        Call<ResponseBody> responseBody = elearningAPI.getCourseByTeacherName(teacherName);
        responseBody.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> rawResponse) {
                if (rawResponse.code()==200) {
                    try {
                        String jsonString = rawResponse.body().string();

                        if (!jsonString.equals("")) {
                            Gson gson = new Gson();
                            Course data = gson.fromJson(jsonString, Course.class);

                            Course.StatusBean response = data.getStatus();

                            if (response.isStatus()) {
                                List<Course.CoursesBean> courses = data.getCourses();

                                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                recyclerView.setAdapter(new CustomAdapter(getContext(), courses));
                                recyclerView.setHasFixedSize(true);
                                recyclerView.setVisibility(View.INVISIBLE);
                                recyclerViewReadyCallback = new RecyclerViewReadyCallback() {
                                    @Override
                                    public void onLayoutReady() {
                                        //
                                        //here comes your code that will be executed after all items are laid down
                                        //
                                        loadingImage.setVisibility(View.GONE);
                                        recyclerView.setVisibility(View.VISIBLE);
                                    }
                                };
                                recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                    @Override
                                    public void onGlobalLayout() {
                                        if (recyclerViewReadyCallback != null) {
                                            recyclerViewReadyCallback.onLayoutReady();
                                        }
                                        recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                    }

                                });

                            } else {

                                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                recyclerView.setAdapter(null);
                            }
                        } else {

                            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            recyclerView.setAdapter(null);
                        }
                        swipeRefreshLayout.setRefreshing(false);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setAdapter(null);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }
    /*public void showData(String jsonString)
    {
        if (!jsonString.equals(""))
        {
            Gson gson = new Gson();
            Course data = gson.fromJson(jsonString,Course.class);

            Course.StatusBean response = data.getStatus();

            if (response.isStatus())
            {
                List<Course.CoursesBean> courses = data.getCourses();
                recyclerView = (RecyclerView) getView().findViewById(R.id.rv_search);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(new CustomAdapter(getContext(), courses));
                recyclerView.setHasFixedSize(true);
                recyclerView.setVisibility(View.INVISIBLE);
                recyclerViewReadyCallback = new RecyclerViewReadyCallback() {
                    @Override
                    public void onLayoutReady() {
                        //
                        //here comes your code that will be executed after all items are laid down
                        //
                        loadingImage.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                };
                recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (recyclerViewReadyCallback != null) {
                            recyclerViewReadyCallback.onLayoutReady();
                        }
                        recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }

                });

            }
            else
            {
                RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.rv_search);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(null);
            }
        }
        else
        {
            RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.rv_search);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(null);
        }
    }*/
    public void bindView(View view)
    {
        elearningAPI = MyAPI.getAPI();
        textSearch = view.findViewById(R.id.editText_search);
        searchButton = view.findViewById(R.id.search_button);
        byCourseName = view.findViewById(R.id.byCName);
        byTeacherName = view.findViewById(R.id.byTName);
        recyclerView =  view.findViewById(R.id.rv_search);
    }

}

