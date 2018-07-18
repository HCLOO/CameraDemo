package com.example.huangcl.camerademo;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ShowPictureFragment extends Fragment {

    ImageView mPicture;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view=inflater.inflate(R.layout.fragment_show_picture,container,false);
        mPicture=(ImageView)view.findViewById(R.id.image_view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String picPath = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath()+"/picture.jpg";
        Bitmap picture=PhotoUtil.getBitmapFromSd(picPath);
//        int degree=PhotoUtil.readPictureDegree(picPath);
        int degree=0;
        if(MainActivity.cameraPosition==0)
            degree=-90;
        else
            degree=90;
        Bitmap imgPicture=PhotoUtil.rotateToDegrees(degree,picture);
        mPicture.setImageBitmap(imgPicture);
    }
}
