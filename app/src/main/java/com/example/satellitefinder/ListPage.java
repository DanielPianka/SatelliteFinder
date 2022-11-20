package com.example.satellitefinder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class ListPage extends AppCompatActivity {
    public static String chosenObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_list);

        Button goToMainView = findViewById(R.id.go_to_main_view);
        goToMainView.setOnClickListener(view -> {
            openMainPage();
            chosenObject = whichRadioSelected();
        });
    }

    public void openMainPage() {
        Intent intent = new Intent(this, MainPage.class);
        startActivity(intent);
    }

    public String whichRadioSelected() {
        String selectedRadio;
        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        int radioButtonID = radioGroup.getCheckedRadioButtonId();
        if (radioButtonID == -1) {
            selectedRadio = null;
            return selectedRadio;
        }
        RadioButton radioButton = radioGroup.findViewById(radioButtonID);
        selectedRadio = (String) radioButton.getText();
        return selectedRadio;
    }

}