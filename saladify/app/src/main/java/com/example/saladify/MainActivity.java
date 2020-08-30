package com.example.saladify;

import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLLocalModel;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions;
import com.muddzdev.styleabletoast.StyleableToast;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.Arrays;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.*;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity implements AddDialog.addDialogListener{

    //necessary global variable
    ImageView imageView;
    boolean isDisplayed;
    String warning;
    TextView detectedSalad;
    ArrayList<Allergy> listofAllergens;
    Button selectImage;
    ListView allergensList;
    Button adder;
    TextView typeSalad;
    String key;
    private String CompiledUrl;
    private String saladType;
    TextView textView;
    Uri uri;
    //firebase global variables
    private FirebaseAutoMLLocalModel model;
    FirebaseVisionImage image;
    FirebaseVisionImageLabeler imageLabeler;

    //keys
    String key1;
    String key2;
    String key3;
    String key4;
    String key5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //basic declarations for starting the program
        isDisplayed = false;
        listofAllergens = new ArrayList<>();
        imageView = findViewById(R.id.imageView);
        detectedSalad = findViewById(R.id.textView2);
        selectImage = findViewById(R.id.button);
        allergensList = findViewById(R.id.allergens);
        adder = findViewById(R.id.button2);
        textView = findViewById(R.id.textView);
        typeSalad = findViewById(R.id.textView5);
        key = "key";
        warning = "";
        key1 = "j";
        key2 = "jo";
        key3 = "joe";
        key4 = "joem";
        key5 = "joemoma";
        //declaring on click listeners
        //this is for selecting and having the ability to crop the image using the image cropper implemented library
        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("check", "funky");
                CropImage.activity().start(MainActivity.this);
            }
        });

        adder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("check", "running");
                openDialog();
            }
        });

        if( (savedInstanceState == null)){
            Allergy allergya = new Allergy("Enter an Allergy");
            listofAllergens.add(allergya);
        }else{
            Log.d("TEER", savedInstanceState.getParcelableArrayList(key1)+ "YYYY");
            listofAllergens = savedInstanceState.getParcelableArrayList(key1);
            detectedSalad.setText(savedInstanceState.getString(key3));
            typeSalad.setText(savedInstanceState.getString(key2));
            textView.setText(savedInstanceState.getString(key4));
            String temp = savedInstanceState.getString(key5);
            if(temp!= null) {
                Uri tempUri = Uri.parse(temp);
                uri = tempUri;
                imageView.setImageURI(tempUri);
            }
        }
        CustomAdapter customAdapter = new CustomAdapter(this, R.layout.adapter_custom ,listofAllergens);
        allergensList.setAdapter(customAdapter);
        customAdapter.notifyDataSetChanged();

    }

    public class CustomAdapter extends ArrayAdapter<Allergy> {
        Context parentConetext;
        List<Allergy> list;
        int xmlResources;


        public CustomAdapter(@NonNull Context context, int resource, @NonNull List objects) {
            super(context, resource, objects);
            parentConetext = context;
            xmlResources = resource;
            list = objects;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) parentConetext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            View adapterView = layoutInflater.inflate(xmlResources, null);
            ConstraintLayout constraintLayout = findViewById(R.id.constrain);
            TextView name = adapterView.findViewById(R.id.display);
            final Button remove = adapterView.findViewById(R.id.remove);
            name.setText("Name of Allergy: " + list.get(position).getName());
            //insert onclick listener
            if (remove != null)
                remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listofAllergens.remove(position);
                        allergensList.invalidateViews();
                        showRemoveText();
                    }
                });
            return adapterView;
        }
    }
    private void imageLabeler(Uri uri) {
        model = new FirebaseAutoMLLocalModel.Builder().setAssetFilePath("model/manifest.json").build();
        try{
            FirebaseVisionOnDeviceAutoMLImageLabelerOptions opts = new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(model).setConfidenceThreshold(0.0f).build();
            imageLabeler = FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(opts);
            image = FirebaseVisionImage.fromFilePath(MainActivity.this, uri);

            //image processing
            imageLabeler.processImage(image).addOnCompleteListener(new OnCompleteListener<List<FirebaseVisionImageLabel>>() {
                @Override
                public void onComplete(@NonNull Task<List<FirebaseVisionImageLabel>> task) {

                    //image labeling
                    int x = 0;
                    for(FirebaseVisionImageLabel firebaseVisionImageLabel : task.getResult()){
                      detectedSalad.append(firebaseVisionImageLabel.getText().toUpperCase() + ": " + (""+firebaseVisionImageLabel.getConfidence() * 100).subSequence(0, 8)+"%"+"\n");
                      if(x == 0){
                          saladType = firebaseVisionImageLabel.getText().toUpperCase();
                          typeSalad.setText(saladType+" Salad");
                          new Asyncthread().execute(saladType);
                      }
                      x++;
                  }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "FAILURE IN PROCESSING, TRY AGAIN!", Toast.LENGTH_SHORT);
                }
            });
        }catch(Exception e){
            Toast.makeText(MainActivity.this, "FAILURE, TRY AGAIN!", Toast.LENGTH_SHORT);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            Log.d("check", "stops");
            CropImage.ActivityResult activityResult = CropImage.getActivityResult(data);
            if((resultCode == RESULT_OK) && (activityResult != null)){

                //using uri in order to extract images easier as it is already used in the crop image library
                uri = activityResult.getUri();
                if(uri != null)
                imageView.setImageURI(uri);
                detectedSalad.setText("");
                imageLabeler(uri);
                Log.d("check","run");
            }
        }
    }

    public void openDialog(){
        AddDialog addDialog = new AddDialog();
        addDialog.show(getSupportFragmentManager(), "Add Dialog");
    }

    @Override
    public void applyTexts(String allergy) {
        String u = allergy;
        try {
            Allergy newAllergy = new Allergy(u);
            listofAllergens.add(newAllergy);
            allergensList.invalidateViews();
            showAddText();
        }catch (Exception e){
            StyleableToast.makeText(this, "Something went wrong", R.style.mytoast).show();
        }
    }

    public void showRemoveText(){
        StyleableToast.makeText(this, "Successfully Removed", R.style.mytoast).show();
    }
    public void showAddText(){
        StyleableToast.makeText(this, "Successfully Added", R.style.myaddtoast).show();
    }


    public class Asyncthread extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            Log.d("DOES", "str");
            String urle = "";
            try {
                System.out.println(strings[0]);
                System.out.println("http://www.recipepuppy.com/api/?i=&q=" + strings[0] + "&p=1");
                urle = " http://www.recipepuppy.com/api/?i=&q=" + strings[0] + "&p=1";
                try {
                    URL url = new URL(urle);
                    URLConnection urlConnection = url.openConnection();
                    InputStream inputStream = urlConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String mydata = "yoot";
                    if (bufferedReader != null)
                        mydata = bufferedReader.readLine() + " ";
                    System.out.println(mydata);
                    Log.d("DATA", ""+mydata);
                    return mydata;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    System.out.println("wrong UEL");
                    //     showRemoveText();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("wrong IO");
                    //   showRemoveText();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("wrong JSON");
                    //   showRemoveText();
                }
                return null;


            }catch (Exception e) {
                e.printStackTrace();
                Log.d("ERROR", "In this area");
            }
            return null;
        }

        
        @Override
        protected void onPostExecute(String s) {
            try{
                ArrayList<String> recipes = new ArrayList<>();
                ArrayList<String> detailedrecipe = new ArrayList<>();
                ArrayList<String> ingredients = new ArrayList<>();
                JSONObject hold = new JSONObject(s);
                JSONArray jsonArray = hold.getJSONArray("results");
                Log.d("JSONARRAY", ""+jsonArray);
                try {
                    JSONObject recipe = jsonArray.getJSONObject(1);
                    String guess = recipe.getString("ingredients");
                    Log.d("ingredients", ""+guess);
                    ingredients = new ArrayList<String>(Arrays.asList(guess.split(" , ")));
                    Log.d("ingredients", ""+ingredients);
                    int c = 0;
                    for(String ingredient: ingredients){
                        Log.d("CHECKKK", ingredient);
                        for(Allergy allergy : listofAllergens){
                            String x = allergy.getName().toLowerCase();
                            Log.d("CHECK", x);
                            if(ingredient.contains(x)){
                                c++;
                                warning += x+"\n";
                                textView.setText("Warning:\n"+warning);
                                Log.d("warnings", warning);
                                if(c == 1){
                                    //this makes a notification to tell the user that they are about to eat something they shouldn't
                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this)
                                            .setSmallIcon(R.drawable.ic_do_not_disturb_black)
                                            .setContentTitle("DO NOT EAT THAT!")
                                            .setContentText("This food may contain one or multiple allergens that you entered.")
                                            .setAutoCancel(true);
                                    Intent intent = new Intent(MainActivity.this, notification.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.putExtra("IMPORTANT", "This food may contain one or multiple allergens that you entered.");
                                    PendingIntent pend =  PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                    builder.setContentIntent(pend);
                                    NotificationManager noti = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                                    noti.notify(0,builder.build());

                                }
                            }
                        }
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }catch(Exception e){
                e.printStackTrace();
                Log.d("ERROR", "ONPOSTEXECUTE");
            }
        }
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
       // Log.d("run", "rin");
        TextView textView = findViewById(R.id.textView2);
        outState.putParcelableArrayList(key1, listofAllergens);
        TextView textView1 = findViewById(R.id.textView5);
        String f = textView1.getText().toString();
        outState.putString(key2, f);
         f = textView.getText().toString();
        outState.putString(key3, f);
        textView1 = findViewById(R.id.textView);
         f = textView1.getText().toString();
        outState.putString(key4, f);
        if(uri == null) {
            f = null;
        }else {
            f = uri.toString();
            uri = uri;
        }
        outState.putString(key5, f);
        super.onSaveInstanceState(outState);

    }

}
