package com.discoveries.jeremy.sunrisesunsetapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.discoveries.jeremy.sunrisesunsetapp.fragments.NavigatorFragment;
import com.discoveries.jeremy.sunrisesunsetapp.fragments.SunriseSunsetFragment;
import com.discoveries.jeremy.sunrisesunsetapp.fragments.WeatherFragment;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

public class LauncherActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    Geocoder geocoder;

    SQLiteDatabase sqLiteDatabase;

    TextView countryView, cityView, regionView, streetView,
            latitudeView, longitudeView, distanceView, GPSView;

    Button run, reset;

    private String startLatitude, startLongitude, endLatitude, endLongitude,
            country, city, region, street, distance, speed, date, format, lifeStatus;

    Geocoder geocoder;

    Handler handler;

    Runnable runFirstTimeRunnable, runSecondTimeRunnable;

    BroadcastReceiver broadcastReceiver;

    Switch GPS;

    Spinner distanceFormatSpinner;

    ListView dataStorageInfoListView;

    private Chronometer chronometer;
    private boolean isRunning, runFirstTime;

    private String[] distanceFormats = {"m", "km", "cm"};

    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        mViewPager = (ViewPager) findViewById(R.id.container);
        if (!runtime_permissions()) initialize();
    }

    public void initialize() {
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        iAdapter(mSectionsPagerAdapter);
        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        startLatitude = latitudeView.getText().toString();
        startLongitude = longitudeView.getText().toString();
        distance = distanceView.getText().toString();
        handler = new Handler();
        runFirstTimeRunnable = new Runnable() {
            @Override
            public void run() {
                latitudeView.setText(startLatitude);
                longitudeView.setText(startLongitude);
                countryView.setText(country);
                cityView.setText(city);
                regionView.setText(region);
                streetView.setText(street);
                distanceView.setText(distance);
            }
        };
        runSecondTimeRunnable = new Runnable() {
            @Override
            public void run() {
                latitudeView.setText(endLatitude);
                longitudeView.setText(endLongitude);
                countryView.setText(country);
                cityView.setText(city);
                regionView.setText(region);
                streetView.setText(street);
                distanceView.setText(distance);
            }
        };
        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        intent = new Intent(getApplicationContext(), GPS.class);
        isRunning = false;
        runFirstTime = true;

        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, distanceFormats);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distanceFormatSpinner.setAdapter(arrayAdapter);

        date = DateFormat.getDateInstance(DateFormat.FULL).format(Calendar.getInstance().getTime());

    }

    public void setGeocoder(Geocoder geocoder) {
        this.geocoder = geocoder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
            getActivity().unregisterReceiver(broadcastReceiver);
        }
    }

    public void identify(View v) {

        countryView = (TextView) v.findViewById(R.id.country);
        cityView = (TextView) v.findViewById(R.id.city);
        regionView = (TextView) v.findViewById(R.id.region);
        streetView = (TextView) v.findViewById(R.id.street);
        distanceView = (TextView) v.findViewById(R.id.metres);
        latitudeView = (TextView) v.findViewById(R.id.latitudeTextView);
        longitudeView = (TextView) v.findViewById(R.id.longitudeTextView);
        run = (Button) v.findViewById(R.id.runBtn);
        reset = (Button) v.findViewById(R.id.resetBtn);
        chronometer = (Chronometer) v.findViewById(R.id.time);
        GPS = (Switch) v.findViewById(R.id.GPS);
        GPSView = (TextView) v.findViewById(R.id.GPSView);
        distanceFormatSpinner = (Spinner) v.findViewById(R.id.spinnerDistance);
        dataStorageInfoListView = (ListView) v.findViewById(R.id.dataStorageInfo);

    }

    public void runNavigationFirstTime() {
        Thread navigator = new Thread(new Runnable() {
            @Override
            public void run() {
                runCheckAddress(Float.valueOf(startLatitude), Float.valueOf(startLongitude));
                distance = getString(R.string.unknown);
                handler.post(runFirstTimeRunnable);
            }
        });
        navigator.start();
        runFirstTime = false;
    }

    public void runNavigationSecondTime() {
        Thread navigator = new Thread(new Runnable() {
            @Override
            public void run() {
                runCheckAddress(Float.valueOf(endLatitude), Float.valueOf(endLongitude));
                calculateDistance();
                handler.post(runSecondTimeRunnable);
            }
        });
        navigator.start();
    }

    public void runCheckAddress(float latitude, float longitude) {

        CheckAddress checkAddress = new CheckAddress(longitude, latitude, geocoder);
        city = checkAddress.getCity();
        country = checkAddress.getCountry();
        region = checkAddress.getArea();
        street = checkAddress.getAddressLine();

    }

    public void calculateDistance() {
        float results[] = new float[10];

        Location.distanceBetween(Double.valueOf(startLatitude), Double.valueOf(startLongitude), Double.valueOf(endLatitude), Double.valueOf(endLongitude), results);
        distance = String.valueOf(results[0]);
    }

    public void calculateSpeed() {
        // calculate speed
    }

    public void setEmptyFields() {

        latitudeView.setText(R.string.unknown);
        longitudeView.setText(R.string.unknown);
        countryView.setText(R.string.country);
        cityView.setText(R.string.city);
        regionView.setText(R.string.region);
        streetView.setText(R.string.street);
        distanceView.setText("0.0");

        startLatitude = getString(R.string.unknown);
        startLongitude = getString(R.string.unknown);
        distance = distanceView.getText().toString();

        stopTimer();

        Toast.makeText(getActivity().getApplicationContext(), "All fields were reseted!", Toast.LENGTH_SHORT).show();

    }

    public void setGPSListener() {
        GPS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    GPSView.setTextColor(getResources().getColor(R.color.colorAccent));
                    getActivity().startService(intent);
                } else {
                    GPSView.setTextColor(getResources().getColor(android.R.color.white));
                    removeService();
                }
            }
        });
    }

    private void setListeners() {

        setSpinnerListener();
        setGPSListener();
        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (startLatitude.equals("unknown") || startLongitude.equals("unknown")) {
                    Toast.makeText(getActivity().getApplicationContext(), "Please , run GPS service!", Toast.LENGTH_SHORT).show();
                } else {
                    startTimer();
                }
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEmptyFields();
                handler.removeCallbacks(runFirstTimeRunnable);
                handler.removeCallbacks(runSecondTimeRunnable);
            }
        });

    }

    public void startTimer() {
        if (!isRunning) {
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            isRunning = true;
        }
    }

    public void stopTimer() {
        if (isRunning) {
            chronometer.stop();
            isRunning = false;
        }
    }

    public void removeService() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("geoData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("firstLongitude", startLongitude);
        editor.putString("firstLatitude", startLatitude);
        editor.apply();
        getActivity().stopService(intent);
    }

    public void setSpinnerListener() {
        distanceFormatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
                    case 0:
                        double metres = Double.valueOf(distance);
                        distance = String.valueOf(metres);
                        format = "m";
                        break;
                    case 1:
                        double kilometres = (double) (Integer.valueOf(distance) / 1000);
                        distance = String.valueOf(kilometres);
                        format = "km";
                        break;
                    case 2:
                        double centimetres = Double.valueOf(distance) * 10;
                        distance = String.valueOf(centimetres);
                        format = "cm";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void saveInDataStorage() {
        DataStorage dataStorage = new DataStorage(getActivity().getApplicationContext());
        dataStorage.addAllData(date, distance, speed, lifeStatus, sqLiteDatabase);
    }

    public void getAllFromDataStorage() {
        DataStorage.getAllData(sqLiteDatabase);
    }

    public void runNavigatorHelper() {
        NavigatorHelper navigatorHelper = new NavigatorHelper(chronometer, distance, date, format);
        speed = navigatorHelper.getSpeed();
        lifeStatus = navigatorHelper.getLifeStatus();
    }

    private boolean runtime_permissions() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);

            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                initialize();
            } else {
                LauncherActivity.this.finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.launcher_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.exit:
                LauncherActivity.this.finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    NavigatorFragment navigatorFragment = new NavigatorFragment();
                    navigatorFragment.setGeocoder(geocoder);
                    return navigatorFragment;
                case 1:
                    SunriseSunsetFragment sunriseSunsetFragment = new SunriseSunsetFragment();
                    sunriseSunsetFragment.setGeocoder(geocoder);
                    return sunriseSunsetFragment;
                case 2:
                    WeatherFragment weatherFragment = new WeatherFragment();
                    weatherFragment.setGeocoder(geocoder);
                    return weatherFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }
}

