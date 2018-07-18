package com.example.huangcl.camerademo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity implements SensorEventListener,SurfaceHolder.Callback,View.OnClickListener {

    private RelativeLayout relativeLayout;
    private FrameLayout frameLayout;
    private RelativeLayout flash;
    private ImageView position;//切换前后置摄像头
    private ImageView flashAuto,flashOn,flashOff;
    private SurfaceView surface;
    private FloatingActionButton shutter;//快门
    private SurfaceHolder holder;
    private Camera camera;//声明相机
    private String filepath = "";//照片保存路径
    public static int cameraPosition = 1;//0代表前置摄像头，1代表后置摄像头
    int flag=0;

    //加速度传感器
    public static final String TAG = "SensorControler";
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private int mX, mY, mZ;
    private long lastStaticStamp = 0;
    Calendar mCalendar;
    boolean isFocusing = false;
    boolean canFocusIn = false;  //内部是否能够对焦控制机制
    boolean canFocus = false;
    public static final int DELEY_DURATION = 500;
    public static final int STATUS_NONE = 0;
    public static final int STATUS_STATIC = 1;
    public static final int STATUS_MOVE = 2;
    private int STATUE = STATUS_NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);//没有标题
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//拍照过程屏幕一直处于高亮
        //设置手机屏幕朝向，一共有7种
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //SCREEN_ORIENTATION_BEHIND： 继承Activity堆栈中当前Activity下面的那个Activity的方向
        //SCREEN_ORIENTATION_LANDSCAPE： 横屏(风景照) ，显示时宽度大于高度
        //SCREEN_ORIENTATION_PORTRAIT： 竖屏 (肖像照) ， 显示时高度大于宽度
        //SCREEN_ORIENTATION_SENSOR  由重力感应器来决定屏幕的朝向,它取决于用户如何持有设备,当设备被旋转时方向会随之在横屏与竖屏之间变化
        //SCREEN_ORIENTATION_NOSENSOR： 忽略物理感应器——即显示方向与物理感应器无关，不管用户如何旋转设备显示方向都不会随着改变("unspecified"设置除外)
        //SCREEN_ORIENTATION_UNSPECIFIED： 未指定，此为默认值，由Android系统自己选择适当的方向，选择策略视具体设备的配置情况而定，因此不同的设备会有不同的方向选择
        //SCREEN_ORIENTATION_USER： 用户当前的首选方向

        setContentView(R.layout.activity_main);

        relativeLayout=(RelativeLayout) findViewById(R.id.relative_layout);
        frameLayout=(FrameLayout)findViewById(R.id.frame_layout);
        flash = (RelativeLayout) findViewById(R.id.camera_flash);
        flashAuto=(ImageView)findViewById(R.id.camera_flash_auto);
        flashOn=(ImageView)findViewById(R.id.camera_flash_on);
        flashOff=(ImageView)findViewById(R.id.camera_flash_off);
        position = (ImageView) findViewById(R.id.camera_position);
        surface = (SurfaceView) findViewById(R.id.camera_surface);
        shutter = (FloatingActionButton) findViewById(R.id.camera_shutter);
        holder = surface.getHolder();//获得句柄
        holder.addCallback(this);//添加回调
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//surfaceview不维护自己的缓冲区，等待屏幕渲染引擎将内容推送到用户面前

        relativeLayout.setVisibility(View.VISIBLE);
        frameLayout.setVisibility(View.GONE);
        //设置监听
        flash.setOnClickListener(this);
        position.setOnClickListener(this);
        shutter.setOnClickListener(this);
        surface.setOnClickListener(this);

        mSensorManager = (SensorManager) this.getSystemService(Activity.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);// TYPE_GRAVITY
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.camera_surface:
                cameraAutoFocus(camera);
                break;
            case R.id.camera_flash:
                //闪光灯
                flag=(flag+1)%3;
                if(flag==1) {
                    flashAuto.setVisibility(View.GONE);
                    flashOn.setVisibility(View.VISIBLE);
                    flashOff.setVisibility(View.GONE);
                }
                else if(flag==2) {
                    flashAuto.setVisibility(View.GONE);
                    flashOn.setVisibility(View.GONE);
                    flashOff.setVisibility(View.VISIBLE);
                }
                else {
                    flashAuto.setVisibility(View.VISIBLE);
                    flashOn.setVisibility(View.GONE);
                    flashOff.setVisibility(View.GONE);
                }
                break;
            case R.id.camera_position:
                //切换前后摄像头
                int cameraCount = 0;
                CameraInfo cameraInfo = new CameraInfo();
                cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数
                for (int i = 0; i < cameraCount; ++i) {
                    Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
                    if (cameraPosition == 1) {
                        //现在是后置，变更为前置
                        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                            camera.stopPreview();//停掉原来摄像头的预览
                            camera.release();//释放资源
                            camera = null;//取消原来摄像头
                            camera = Camera.open(i);//打开当前选中的摄像头
                            try {
                                camera.setDisplayOrientation(90);
                                camera.setPreviewDisplay(holder);//通过surfaceview显示取景画面
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            camera.startPreview();//开始预览
                            cameraPosition = 0;
                            break;
                        }
                    } else {
                        //现在是前置， 变更为后置
                        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                            camera.stopPreview();//停掉原来摄像头的预览
                            camera.release();//释放资源
                            camera = null;//取消原来摄像头
                            camera = Camera.open(i);//打开当前选中的摄像头
                            try {
                                camera.setDisplayOrientation(90);
                                camera.setPreviewDisplay(holder);//通过surfaceview显示取景画面
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            camera.startPreview();//开始预览
                            cameraPosition = 1;
                            break;
                        }
                    }

                }
                break;
            case R.id.camera_shutter:
                //快门
                if(cameraPosition==1)
                    isEnableFlashLight(flag);
                unRegisterSensorListener();
                camera.takePicture(null, null, jpeg);//将拍摄到的照片给自定义的对象
                break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        //当surfaceview创建时开启相机
        if(camera == null) {
            camera = Camera.open();
            try {
                camera.setDisplayOrientation(90);
                camera.setPreviewDisplay(holder);//通过surfaceview显示取景画面
                camera.startPreview();//开始预览
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        //当surfaceview关闭时，关闭预览并释放资源
        camera.stopPreview();
        camera.release();
        camera = null;
        holder = null;
        surface = null;
    }

    //创建jpeg图片回调数据对象
    PictureCallback jpeg = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
        Log.e("onPictureTaken: ","okokok!");
            try {
                File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),"picture.jpg");
                Log.d("picturePath:", getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath()+"/picture.jpg");
                OutputStream os = null;
                os = new FileOutputStream(file);
                os.write(data);
                os.close();
                camera.stopPreview();//关闭预览 处理数据
                getFragmentManager().beginTransaction().replace(R.id.frame_layout,new ShowPictureFragment()).commit();
                relativeLayout.setVisibility(View.GONE);
                frameLayout.setVisibility(View.VISIBLE);

//                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//                File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),"picture.jpg");
//                Log.d("picturePath:", getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath()+"/picture.jpg");
//                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);//将图片压缩的流里面
//                bos.flush();// 刷新此缓冲区的输出流
//                bos.close();// 关闭此输出流并释放与此流有关的所有系统资源
//                camera.stopPreview();//关闭预览 处理数据
//                bitmap.recycle();//回收bitmap空间
//                linearLayout.setVisibility(View.GONE);
//                frameLayout.setVisibility(View.VISIBLE);
//                getFragmentManager().beginTransaction().replace(R.id.frame_layout,new ShowPictureFragment()).commit();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };

    public void cameraAutoFocus(Camera camera) {
        if(camera!=null) {
            camera.autoFocus(new AutoFocusCallback() {//自动对焦
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    // TODO Auto-generated method stub
                    if (success) {
                        Log.e("autoFocus: ","success!"+camera.getParameters().getFocusMode());
                    } else {
                        Log.e("autoFocus: ","fail!");
                    }
                }
            });
        }
    }

    public void isEnableFlashLight(int flag) {
        Camera.Parameters parameters = camera.getParameters();
        if(flag==1)
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);//开启
        else if(flag==2)
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);//关闭
        else
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);//自动
        camera.setParameters(parameters);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == null) {
            return;
        }

        if (isFocusing) {
            restParams();
            return;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            int x = (int) event.values[0];
            int y = (int) event.values[1];
            int z = (int) event.values[2];
            mCalendar = Calendar.getInstance();
            long stamp = mCalendar.getTimeInMillis();// 1393844912

            int second = mCalendar.get(Calendar.SECOND);// 53

            if (STATUE != STATUS_NONE) {
                int px = Math.abs(mX - x);
                int py = Math.abs(mY - y);
                int pz = Math.abs(mZ - z);
                double value = Math.sqrt(px * px + py * py + pz * pz);
                if (value > 1.4) {
                    STATUE = STATUS_MOVE;
                } else {
                    if (STATUE == STATUS_MOVE) {
                        lastStaticStamp = stamp;
                        canFocusIn = true;
                    }

                    if (canFocusIn) {
                        if (stamp - lastStaticStamp > DELEY_DURATION) {
                            //移动后静止一段时间，可以发生对焦行为
                            if (!isFocusing) {
                                canFocusIn = false;
                                if(camera!=null)
                                    cameraAutoFocus(camera);
                            }
                        }
                    }

                    STATUE = STATUS_STATIC;
                }
            } else {
                lastStaticStamp = stamp;
                STATUE = STATUS_STATIC;
            }

            mX = x;
            mY = y;
            mZ = z;
        }
    }

    private void restParams() {
        STATUE = STATUS_NONE;
        canFocusIn = false;
        mX = 0;
        mY = 0;
        mZ = 0;
    }

    public void unRegisterSensorListener() {
        if(mSensorManager!=null) {
            mSensorManager.unregisterListener(this, mSensor);
            canFocus = false;
            mSensorManager=null;
            mSensor=null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mSensorManager==null) {
            mSensorManager = (SensorManager) this.getSystemService(Activity.SENSOR_SERVICE);
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);// TYPE_GRAVITY
        }
        restParams();
        canFocus = true;
        mSensorManager.registerListener(this, mSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mSensorManager!=null) {
            mSensorManager.unregisterListener(this, mSensor);
            canFocus = false;
            mSensorManager=null;
            mSensor=null;
        }
    }
}
