package com.example.moneytracker.fragment_activity;


import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.moneytracker.AddItem_On_Spinner;
import com.example.moneytracker.ArrayListClass;
import com.example.moneytracker.DB.DBHelper;
import com.example.moneytracker.ModelClass.Model;
import com.example.moneytracker.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class ExpensesFragment extends Fragment implements View.OnClickListener {

    EditText amount,note;
    Spinner category;;
    ImageButton newCategory;
    TextView currentDate;
    ImageView image;
    Button save,cancel;
    final Calendar myCalendar = Calendar.getInstance();
    String date;
    Bitmap imageData;
    private int GALLERY = 1, CAMERA = 2;
    private static final String Type="Expenses";
    private DBHelper dbHelper;


    public ExpensesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_expenses, container, false);


        amount = v.findViewById(R.id.expenses_fragment_amount_id);
        category = v.findViewById(R.id.expenses_fragment_category_id);
        newCategory = v.findViewById(R.id.expenses_fragment_addCategory_id);
        currentDate = v.findViewById(R.id.expenses_fragment_currentDate_id);
        image = v.findViewById(R.id.expenses_fragment_image_id);
        note = v.findViewById(R.id.expenses_fragment_note_id);
        save = v.findViewById(R.id.expenses_fragment_save_id);
        cancel = v.findViewById(R.id.expenses_fragment_cancel_id);
        dbHelper = new DBHelper(getContext());

        SQLiteDatabase db = dbHelper.getWritableDatabase();


        newCategory.setOnClickListener(this);
        currentDate.setOnClickListener(this);
        image.setOnClickListener(this);
        save.setOnClickListener(this);
        cancel.setOnClickListener(this);

        ArrayListClass arrayListClass = new ArrayListClass();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.showspinneritem_sample_lay, R.id.ShowSpinnerTxtId, arrayListClass.getArrayList());
        category.setAdapter(adapter);
        currentDate.setText(updateLabel());

        return v;
    }

    final DatePickerDialog.OnDateSetListener datepicker = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {

            myCalendar.set(java.util.Calendar.YEAR, year);
            myCalendar.set(java.util.Calendar.MONTH, monthOfYear);
            myCalendar.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }

    };
    private String updateLabel() {
        String myFormat = "dd/MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        date=sdf.format(myCalendar.getTime());
        currentDate.setText(date);
        return date;
    }

    @Override
    public void onClick(View v) {
        if (v==image){
            showPictureDialog();
        }
        if (v==newCategory){
            Intent intent = new Intent(getActivity(), AddItem_On_Spinner.class);
            intent.putExtra("name", "Deposit");
            startActivity(intent);
        }
        if (v==currentDate){
            new DatePickerDialog(getContext(), datepicker, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        }
        if (v==save){
            if (validation()){
                String Amount=amount.getText().toString();
                String Category=category.getSelectedItem().toString();
                String CurrentDate=currentDate.getText().toString();
                byte[] image=getBitmapAsByteArray(imageData);
                String Note=note.getText().toString();
                Model data=new Model(String.valueOf(Amount),Category,CurrentDate,null,Note,image,Type);
                long row=dbHelper.insertData(data);

                if (row == -1){
                    Toast.makeText(getContext(),"Data Inserted Failed",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getContext(),"Data Inserted Success",Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(getContext(),"Check All Data ",Toast.LENGTH_SHORT).show();
            }
        }
        if (v==cancel){

        }
    }

    private void showPictureDialog(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(getActivity());
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {"Select photo from gallery", "Capture photo from camera" };
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallary();
                                break;
                            case 1:
                                takePhotoFromCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    public boolean validation(){

        String Amount=amount.getText().toString();
        String Category=category.getSelectedItem().toString();
        String CurrentDate=currentDate.getText().toString();
        String Note=note.getText().toString();

        boolean val=true;
        if (Amount.isEmpty()){
            val=false;
            amount.setError("Enter Amount");
        }
        if (Category.isEmpty() || Category.startsWith(" ")){
            val=false;
            Toast.makeText(getContext(),"Category Not Selected ",Toast.LENGTH_LONG).show();
        }
        if (CurrentDate.isEmpty() || CurrentDate.startsWith(" ")){
            val=false;
            currentDate.setError("Enter Current Date");
        }
        if (Note.isEmpty()){
            val=false;
            note.setError("Enter Note");
        }
        if (imageData==null){
            val=false;
            Toast.makeText(getContext(),"Image Not Selected ",Toast.LENGTH_LONG).show();
        }
        return val;
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    public void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY);
    }

    private void takePhotoFromCamera() {

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    0);

        }else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, CAMERA);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA && resultCode == RESULT_OK) {
            Bitmap thumbnail = data.getParcelableExtra("data");
            imageData=thumbnail;
            image.setImageBitmap(thumbnail);
        }
        if (requestCode == GALLERY && resultCode == RESULT_OK) {
            Uri thumbnail=data.getData();
            Bitmap  mBitmap = null;
            try {
                mBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), thumbnail);
                imageData=mBitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
            image.setImageBitmap(mBitmap);
        }
    }


}
