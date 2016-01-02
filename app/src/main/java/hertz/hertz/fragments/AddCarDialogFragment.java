package hertz.hertz.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hertz.hertz.R;
import hertz.hertz.activities.CarManagementActivity;
import hertz.hertz.helpers.AppConstants;

/**
 * Created by rsbulanon on 11/17/15.
 */
public class AddCarDialogFragment extends DialogFragment {

    @Bind(R.id.ivAddCarImage) ImageView ivAddCarImage;
    @Bind(R.id.tvAddCarImage) TextView tvAddCarImage;
    @Bind(R.id.rlAddImage) RelativeLayout rlAddImage;
    @Bind(R.id.ivDeleteImage) ImageView ivDeleteImage;
    @Bind(R.id.etCarModel) EditText etCarModel;
    @Bind(R.id.etPlateNo) EditText etPlateNo;
    @Bind(R.id.etDescripton) EditText etDescripton;
    @Bind(R.id.etRatePer3Hours) EditText etRatePer3Hours;
    @Bind(R.id.etRatePer10Hours) EditText etRatePer10Hours;
    @Bind(R.id.etExcess) EditText etExcess;
    @Bind(R.id.tvHeader) TextView tvHeader;
    @Bind(R.id.pbLoadImage) ProgressBar pbLoadImage;
    private static final int SELECT_IMAGE = 1;
    private View view;
    private CarManagementActivity activity;
    private Bitmap bitmap;
    private OnAddCarListener onAddCarListener;
    private ParseObject car;
    private boolean hasImage;
    private String prevCarModel;
    private String prevCarDescription;
    private String prevPlateNo;
    private String prevRatePer3Hours;
    private String prevRatePer10Hours;
    private String prevExcessRate;

    public static AddCarDialogFragment newInstance(ParseObject car) {
        AddCarDialogFragment frag = new AddCarDialogFragment();
        frag.car = car;
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
        return super.onCreateView(inflater, container, savedInstanceState);
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_add_car, null);
        ButterKnife.bind(this, view);
        activity = (CarManagementActivity)getActivity();

        if (car != null) {
            prevCarModel = car.getString("carModel");
            prevPlateNo = car.getString("plateNo") == null ? "Not Set" : car.getString("plateNo");
            prevCarDescription = car.getString("description");
            prevRatePer3Hours = car.getNumber("ratePer3Hours").toString();
            prevRatePer10Hours = car.getNumber("ratePer10Hours").toString();
            prevExcessRate = car.getNumber("excessRate").toString();

            tvHeader.setText("Update car");
            etCarModel.setText(prevCarModel);
            etDescripton.setText(prevCarDescription);
            etPlateNo.setText(prevPlateNo);
            etRatePer3Hours.setText(prevRatePer3Hours);
            etRatePer10Hours.setText(prevRatePer10Hours);
            etExcess.setText(prevExcessRate);

            if (car.getParseFile("carImage") != null) {
                ImageLoader.getInstance().loadImage(car.getParseFile("carImage").getUrl(), new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        pbLoadImage.setVisibility(View.VISIBLE);
                        tvAddCarImage.setText("Fetching car image, Please wait...");
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        tvAddCarImage.setText("Failed to load car image");

                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        bitmap = loadedImage;
                        hasImage = true;
                        pbLoadImage.setVisibility(View.GONE);
                        tvAddCarImage.setVisibility(View.GONE);
                        ivDeleteImage.setVisibility(View.VISIBLE);
                        ivAddCarImage.setImageBitmap(loadedImage);
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {

                    }
                });
            } else {
                pbLoadImage.setVisibility(View.GONE);
                tvAddCarImage.setText("Upload the car image");
                ivDeleteImage.setVisibility(View.GONE);
            }
        }

        final Dialog mDialog = new Dialog(getActivity());
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(view);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        return mDialog;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE) {
            if (resultCode == getActivity().RESULT_OK) {
                bitmap = BitmapFactory.decodeFile(getPath(getActivity(), data.getData()));
                hasImage = true;
                ivAddCarImage.setImageBitmap(bitmap);
                ivDeleteImage.setVisibility(View.VISIBLE);
                tvAddCarImage.setVisibility(View.GONE);
            }
        }
    }

    public static String getPath(Context context, Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s=cursor.getString(column_index);
        cursor.close();
        return s;
    }

    @OnClick(R.id.ivDeleteImage)
    public void deleteImage() {
        bitmap = null;
        hasImage = false;
        ivAddCarImage.setImageDrawable(null);
        ivDeleteImage.setVisibility(View.GONE);
        tvAddCarImage.setVisibility(View.VISIBLE);
        tvAddCarImage.setText("Add car the car image");
    }

    @OnClick({R.id.rlAddImage, R.id.tvAddCarImage})
    public void addCarImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);
    }

    @OnClick(R.id.btnAdd)
    public void addCar() {
        if (!activity.isNetworkAvailable()) {
            activity.showToast(AppConstants.ERR_CONNECTION);
        } else {
            final String carModel = etCarModel.getText().toString();
            final String plateNo = etPlateNo.getText().toString();
            final String desc = etDescripton.getText().toString();
            final String ratePer3Hours = etRatePer3Hours.getText().toString();
            final String ratePer10Hours = etRatePer10Hours.getText().toString();
            final String excessRate = etExcess.getText().toString();

             if (carModel.isEmpty()) {
                activity.setError(etCarModel, AppConstants.WARN_FIELD_REQUIRED);
            } else if (plateNo.isEmpty()) {
                activity.setError(etPlateNo, AppConstants.WARN_FIELD_REQUIRED);
            } else if (ratePer3Hours.isEmpty()) {
                activity.setError(etRatePer3Hours, AppConstants.WARN_FIELD_REQUIRED);
            } else if (ratePer10Hours.isEmpty()) {
                activity.setError(etRatePer10Hours, AppConstants.WARN_FIELD_REQUIRED);
            } else if (excessRate.isEmpty()) {
                activity.setError(etExcess, AppConstants.WARN_FIELD_REQUIRED);
            } else {
                if (car != null) {
                    if (prevCarModel.equals(carModel) && prevPlateNo.equals(plateNo)
                        && prevCarDescription.equals(desc) && prevRatePer3Hours.equals(ratePer3Hours)
                        && prevRatePer10Hours.equals(prevRatePer10Hours) && prevExcessRate.equals(excessRate)) {
                        activity.showSweetDialog(AppConstants.WARN_NO_CHANGES_DETECTED,"warning");
                    } else {
                        new ConvertImage(carModel,plateNo,desc,ratePer3Hours,ratePer10Hours,excessRate).execute();
                    }
                } else {
                    new ConvertImage(carModel,plateNo,desc,ratePer3Hours,ratePer10Hours,excessRate).execute();
                }
            }
        }
    }

    private class ConvertImage extends AsyncTask<Void,Void,byte[]> {

        private String carModel;
        private String plateNo;
        private String desc;
        private String ratePer3Hours;
        private String ratePer10Hours;
        private String excessRate;

        public ConvertImage(String carModel, String plateNo, String desc, String ratePer3Hours,
                            String ratePer10Hours, String excessRate) {
            this.carModel = carModel;
            this.plateNo = plateNo;
            this.desc = desc;
            this.ratePer3Hours = ratePer3Hours;
            this.ratePer10Hours = ratePer10Hours;
            this.excessRate = excessRate;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (car == null) {
                activity.showCustomProgress(AppConstants.LOAD_CREATE_CAR);
            } else {
                activity.showCustomProgress(AppConstants.LOAD_UPDATE_CAR_RECORD);
            }
        }

        @Override
        protected byte[] doInBackground(Void... params) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            if (hasImage) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                return stream.toByteArray();
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            if (car == null) {
               car = new ParseObject("Car");
                car.put("status","available");
            }
            if (bytes != null) {
                ParseFile pf = new ParseFile("img.png", bytes);
                car.put("carImage", pf);
            }
            car.put("carModel", carModel);
            car.put("plateNo",plateNo);
            car.put("description", desc.isEmpty() ? "No descriptions provided" : desc);
            car.put("ratePer3Hours",Double.parseDouble(ratePer3Hours));
            car.put("ratePer10Hours",Double.parseDouble(ratePer10Hours));
            car.put("excessRate", Double.parseDouble(excessRate));
            car.put("markedAsDeleted",false);
            car.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    activity.dismissCustomProgress();
                    if (e == null) {
                        onAddCarListener.onSuccessful();
                    } else {
                        onAddCarListener.onFailed(e);
                    }
                }
            });
        }
    }

    public interface OnAddCarListener {
        void onSuccessful();
        void onFailed(ParseException e);
    }

    public void setOnAddCarListener(OnAddCarListener onAddCarListener) {
        this.onAddCarListener = onAddCarListener;
    }
}
