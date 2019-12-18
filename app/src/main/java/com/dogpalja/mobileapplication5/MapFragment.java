package com.dogpalja.mobileapplication5;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;


public class MapFragment extends Fragment implements OnMapReadyCallback{
    public static View rootView;
    public static MapView mapView;
    public static GoogleMap mMap;

    //파일입출력
    File[] files;
    File storageDir;



    //경도위도 배열
    Vector<Double> latitudeV;
    Vector<Double> longitudeV;

    public MapFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //내위치 받아오는 부분 선언
        //https://vvh-avv.tistory.com/138 << 여기서 봤다.

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_story_map, container, false);

        //파일 불러오는 함수
        storageDir = getContext().getExternalFilesDir("PinPosition");
        files = storageDir.listFiles();


        latitudeV = new Vector<Double>(files.length);
        longitudeV = new Vector<Double>(files.length);

        try {
            readFile(getPositionFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        mapView = (MapView) rootView.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        for(int i = 0; i < latitudeV.size(); i++){
            Toast.makeText(MapFragment.this.getActivity(), latitudeV.elementAt(i) + " " + Integer.toString(i), Toast.LENGTH_SHORT).show();
        }

        return rootView;
    }

    public void readFile(File file) throws IOException {
        BufferedReader br=new BufferedReader(new FileReader(file));
        String line=null;
        int i = 0;
        while((line=br.readLine())!=null) {
            if(i%2 == 0)
                latitudeV.addElement(Double.parseDouble(line));
            else
                longitudeV.addElement(Double.parseDouble(line));
            i++;
        }
    }

    private File getPositionFile(){
        File storageDir = getContext().getExternalFilesDir("PinPosition");
        File Position = new File(storageDir, "location.txt");
        return Position;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();

    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {

        mMap = googleMap;
        final MarkerOptions markerOptions = new MarkerOptions();
        for(int i =0; i <latitudeV.size(); i++){
            markerOptions.position(new LatLng(latitudeV.get(i), longitudeV.get(i)));
            googleMap.addMarker(markerOptions);
        }
        markerOptions.position(new LatLng(32, 32));
        googleMap.addMarker(markerOptions);
        //맵 위치 찍기.
        MapsInitializer.initialize(this.getActivity());
        //    getAddress(getContext(), currentLocation.getLatitude(), currentLocation.getLongitude()); 위치받아오는 getadderess
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(35.89, 128.61), 10);
        googleMap.animateCamera(cameraUpdate);
        //현재위치 구현
        ////////////////////////////////////
        mMap = googleMap;
        // 맵 터치 이벤트 구현 //
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
            @Override
            public void onMapClick(LatLng point) {
                final MarkerOptions mOptions = new MarkerOptions();
                // 마커 타이틀
                mOptions.title("대표사진보기");
                mOptions.position(new LatLng(point.latitude, point.longitude));
                // 마커(핀) 추가 , 맵 클릭시 확인창 띄우기
                AlertDialog.Builder dialog = new AlertDialog.Builder(MapFragment.this.getActivity());
                dialog.setTitle("가봤던 곳").setMessage("핀을 추가 하시겠습니까?")
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i("clis", "마커 추가");
                                googleMap.addMarker(mOptions); // 마커 추가.
                                Toast.makeText(MapFragment.this.getActivity(), "추가 하였습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("clis", "클릭했습니다.");
                        Toast.makeText(MapFragment.this.getActivity(), "추가 하지 않았습니다.", Toast.LENGTH_SHORT).show();
                    }
                }).create().show();
            }
        });

//정보창 클릭 리스너
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                startActivity(new Intent(MapFragment.this.getActivity() , Map_infocli.class));
                Log.i("clis", "정 보창 클릭");
            }
        });

//마커 클릭 리스너
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                //삭제 할껀지... 여쭈어봄..
                final MarkerOptions mOptions = new MarkerOptions();
                // 마커 타이틀
                mOptions.title("삭제");
                // 마커(핀) 추가 , 맵 클릭시 확인창 띄우기
                AlertDialog.Builder dialog = new AlertDialog.Builder(MapFragment.this.getActivity());
                dialog.setTitle("여쭈어봄").setMessage("삭제하시겠습니까?")
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i("clis", "마커 추가");
                                marker.remove(); // 마커 삭제.
                                Toast.makeText(MapFragment.this.getActivity(), "삭제 하였습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("clis", "클릭했습니다.");
                        Toast.makeText(MapFragment.this.getActivity(), "삭제 하지 않았습니다.", Toast.LENGTH_SHORT).show();
                    }
                }).create().show();
                Log.i("clis", "마커 클릭");
                return false;
            }
        });
    }
}