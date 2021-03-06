package com.cloudeducate.redtick.Activities;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cloudeducate.redtick.R;
import com.cloudeducate.redtick.Utils.Constants;
import com.cloudeducate.redtick.Utils.URL;
import com.cloudeducate.redtick.Volley.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DashBoard extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    String jsondata;
    private static final int REQUEST_WRITE_STORAGE = 112;

    private TextView totalAM, completedAM, remainingAM, attendanceRM;

    SharedPreferences sharedpref;
    String metadata;
    private VolleySingleton volleySingleton;
    private RequestQueue requestQueue;
    private final String TAG="yahoo";
    private ProgressDialog progressDialog;
    private TextView nameview,classview;
    private ImageView userimage;
    private CardView messageCardView, assignmentCardView,resultCardView,attendanceview,performanceview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedpref = this.getSharedPreferences(getString(R.string.preference), Context.MODE_PRIVATE);
        metadata = sharedpref.getString(getString(R.string.metavalue), "null");

        boolean hasPermission = (ContextCompat.checkSelfPermission(DashBoard.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(DashBoard.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        }

        totalAM = (TextView) findViewById(R.id.total_assgnment);
        completedAM = (TextView) findViewById(R.id.completed_assgnement);
        remainingAM = (TextView) findViewById(R.id.remaining_assgnment);
        attendanceRM = (TextView) findViewById(R.id.attendance_value);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        messageCardView = (CardView) findViewById(R.id.message_card);
        assignmentCardView = (CardView) findViewById(R.id.assignmet_card);
        resultCardView= (CardView) findViewById(R.id.resultcard);
        attendanceview=(CardView) findViewById(R.id.cardattendance);
        performanceview=(CardView) findViewById(R.id.card_viewperformance);
        View view=navigationView.inflateHeaderView(R.layout.nav_header_dash_board);
        nameview=(TextView)view.findViewById(R.id.username);
        classview=(TextView) view.findViewById(R.id.classid);
        userimage=(ImageView) view.findViewById(R.id.imageView);
        userimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashBoard.this,DashBoard.class));
            }
        });

        performanceview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performanceview.setCardElevation(25);
                startActivity(new Intent(DashBoard.this, PerformanceActivity.class));
            }
        });

        attendanceview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attendanceview.setCardElevation(25);
                startActivity(new Intent(DashBoard.this, AttendanceActivity.class));
            }
        });
        resultCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resultCardView.setCardElevation(25);
                startActivity(new Intent(DashBoard.this, ResultActivity.class));
            }
        });

        messageCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageCardView.setCardElevation(25);
                startActivity(new Intent(DashBoard.this, MessageActivity.class));
            }
        });
        assignmentCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assignmentCardView.setCardElevation(25);
                startActivity(new Intent(DashBoard.this, AssignmentActivity.class));
            }
        });

        fetchNotificationData();
        fetchDashBoardData();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dash_board, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent=new Intent(DashBoard.this,SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_assignment) {
            Intent intent = new Intent(DashBoard.this, AssignmentActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_messages) {
            Intent intent = new Intent(DashBoard.this, MessageActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_result) {
            Intent intent = new Intent(DashBoard.this, ResultActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_performance) {
            Intent intent = new Intent(DashBoard.this, PerformanceActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_attendance) {
            Intent intent = new Intent(DashBoard.this, AttendanceActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(DashBoard.this, ProfileActivity.class);
            intent.putExtra(Constants.PROFILE, jsondata);
            //Log.v("json", jsondata);
            startActivity(intent);
        }
        else if (id == R.id.nav_courses) {
            Intent intent = new Intent(DashBoard.this, CourseActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_logout) {
            sharedpref = this.getSharedPreferences(getString(R.string.preference), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpref.edit();
            editor.clear();
            editor.commit();
            DashBoard.this.finish();
            Intent login=new Intent(this,LoginActivity.class);
            startActivity(login);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    //reload my activity with permission granted or use the features what required the permission
                } else
                {
                    Toast.makeText(DashBoard.this, "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    void fetchDashBoardData(){

        Log.v(TAG, "fetchData fo Courses is called");
        volleySingleton = VolleySingleton.getMyInstance();
        requestQueue = volleySingleton.getRequestQueue();
        showProgressDialog();
        StringRequest jsonObjectRequest = new StringRequest(Request.Method.GET, URL.getCoursesURL(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                jsondata=response;
                try {
                    UpdateDashboard(response.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (response == null) {
                    Log.v(TAG, "fetchData is not giving a fuck");
                }
                Log.v(TAG, "response = " + response);
                progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Log.v(TAG, "Response = " + "timeOut");
                } else if (error instanceof AuthFailureError) {
                    Log.v(TAG, "Response = " + "AuthFail");
                } else if (error instanceof ServerError) {
                    Log.v(TAG, "Response = " + "ServerError");
                } else if (error instanceof NetworkError) {
                    Log.v(TAG, "Response = " + "NetworkError");
                } else if (error instanceof ParseError) {
                    Log.v(TAG, "Response = " + "ParseError");
                }
            }
        })
        {
            @Override
            public Map<String,String> getHeaders() throws com.android.volley.AuthFailureError{
                Map<String,String> params=new HashMap<String,String>();
                params.put("X-App","student");
                params.put("X-Access-Token",metadata);
                return params;
            };
        };

        requestQueue.add(jsonObjectRequest);
    }

    private void UpdateDashboard(String jsonString) throws JSONException {

        JSONObject jsonobj = new JSONObject(jsonString);
        JSONObject assignmentObject = jsonobj.getJSONObject(Constants.ASSIGNMENTS);
        totalAM.setText("Total assignment : " + assignmentObject.getString(Constants.ASSIGNMENT_TOTAL));
        completedAM.setText("Completed assignment : " + assignmentObject.getString(Constants.ASSIGNMENT_SUBMITTED));
        int total = Integer.parseInt(assignmentObject.getString(Constants.ASSIGNMENT_TOTAL));
        int completed = Integer.parseInt(assignmentObject.getString(Constants.ASSIGNMENT_SUBMITTED));
        int remaining = total - completed;
        remainingAM.setText("Remaining assignment : " + String.valueOf(remaining));
        String attendance = jsonobj.getString(Constants.ATTENDANCE);
        attendanceRM.setText("Your percentage attendance for this month is " + attendance + " %");
        JSONObject user=jsonobj.getJSONObject(Constants.USER);
        nameview.setText("Welcome "+user.getString(Constants.NAME));
        JSONObject org=jsonobj.getJSONObject(Constants.ORGANIZATION);
        classview.setText("School Studying in: "+org.getString(Constants.NAME));


    }

    public void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Getting Data");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
    }

    void fetchNotificationData(){

        Log.v(TAG, "fetchData fo Courses is called");
        volleySingleton = VolleySingleton.getMyInstance();
        requestQueue = volleySingleton.getRequestQueue();
        StringRequest jsonObjectRequest = new StringRequest(Request.Method.GET, URL.getNotificationURL(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    UpdateDashboard(response.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (response == null) {
                    Log.v(TAG, "fetchData is not giving a fuck");
                }
                Log.v(TAG, "response = " + response);
                parsejson(response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Log.v(TAG, "Response = " + "timeOut");
                } else if (error instanceof AuthFailureError) {
                    Log.v(TAG, "Response = " + "AuthFail");
                } else if (error instanceof ServerError) {
                    Log.v(TAG, "Response = " + "ServerError");
                } else if (error instanceof NetworkError) {
                    Log.v(TAG, "Response = " + "NetworkError");
                } else if (error instanceof ParseError) {
                    Log.v(TAG, "Response = " + "ParseError");
                }
            }
        })
        {
            @Override
            public Map<String,String> getHeaders() throws com.android.volley.AuthFailureError{
                Map<String,String> params=new HashMap<String,String>();
                params.put("X-App","student");
                params.put("X-Access-Token",metadata);
                return params;
            };
        };

        requestQueue.add(jsonObjectRequest);
    }
    void parsejson(String jsonString)
    {
        String content="Content of Notification",type="assignment",link,id="null",read="false";
        String[] split={"title","content"};
        Intent intent;
        try {
            JSONObject jsonobj =new JSONObject(jsonString);
            JSONArray noitfyObject = jsonobj.getJSONArray("notifications");
            for(int i=0;i<noitfyObject.length();i++)
            {
                JSONObject jsonObject = noitfyObject.getJSONObject(i);
                content=jsonObject.getString("content");
                type=jsonObject.getString("type");
                link=jsonObject.getString("url");
                read=jsonObject.getString("read");
                id=jsonObject.getString("id");
                split=content.split(":");

            }

            if(type.equals("assignment")) {
                intent = new Intent(DashBoard.this, AssignmentActivity.class);
            }
            else {
                intent = new Intent(DashBoard.this, AssignmentActivity.class);
            }
            if(read.equals("false")) {
                final PendingIntent pendingIntent = PendingIntent.getActivity(DashBoard.this, 0, intent, 0);
                final NotificationManager mNotifyManager =
                        (NotificationManager) DashBoard.this.getSystemService(Context.NOTIFICATION_SERVICE);
                final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(DashBoard.this);
                mBuilder.setContentTitle(split[0])
                        .setSmallIcon(R.drawable.ic_assessment_grey);
                mBuilder.setContentText(split[1]);
                mBuilder.setContentIntent(pendingIntent);
                mNotifyManager.notify(1, mBuilder.build());
                sendNotificationData(id);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    void sendNotificationData(String id_in){

        Log.v(TAG, "fetchData fo Courses is called");
        volleySingleton = VolleySingleton.getMyInstance();
        requestQueue = volleySingleton.getRequestQueue();

        StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST, URL.sendNotificationURL(id_in), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                jsondata=response;
                try {
                    UpdateDashboard(response.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (response == null) {
                    Log.v(TAG, "fetchData is not giving a fuck");
                }
                Log.v(TAG, "response = " + response);
                Toast.makeText(DashBoard.this, "Notification Read", Toast.LENGTH_SHORT);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Log.v(TAG, "Response = " + "timeOut");
                } else if (error instanceof AuthFailureError) {
                    Log.v(TAG, "Response = " + "AuthFail");
                } else if (error instanceof ServerError) {
                    Log.v(TAG, "Response = " + "ServerError");
                } else if (error instanceof NetworkError) {
                    Log.v(TAG, "Response = " + "NetworkError");
                } else if (error instanceof ParseError) {
                    Log.v(TAG, "Response = " + "ParseError");
                }
            }
        })
        {
            @Override
            public Map<String,String> getHeaders() throws com.android.volley.AuthFailureError{
                Map<String,String> params=new HashMap<String,String>();
                params.put("X-App","student");
                params.put("X-Access-Token",metadata);
                return params;
            };
            @Override
            public Map<String,String> getParams() throws com.android.volley.AuthFailureError{
                Map<String,String> params=new HashMap<String,String>();
                params.put("read","true");
                return params;
            };

        };

        requestQueue.add(jsonObjectRequest);
    }

}
