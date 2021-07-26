package com.example.feedforward;

import androidx.appcompat.app.AppCompatActivity;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    EditText number1;
    EditText number2;
    Button Add_button;
    TextView result;
    TextView message;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        number1 = (EditText) findViewById(R.id.editText_first_no);
        number2 = (EditText) findViewById(R.id.editText_second_no);
        Add_button = (Button) findViewById(R.id.add_button);
        result = (TextView) findViewById(R.id.textView_answer);
        message = (TextView) findViewById(R.id.textView_message);
        Add_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new AsyncTaskRunner(number1,number2,message,result).execute();
                /*int num1 = Integer.parseInt(number1.getText().toString());
                int num2 = Integer.parseInt(number2.getText().toString());
                message.setText("your execution started, please wait");
                double startTime = System.currentTimeMillis();
                Main.main(num2, num1);
                double endTime = System.currentTimeMillis();
                result.setText(Double.toString(endTime - startTime) + " ms");
                message.setText("finished");*/
            }
        });
    }
}
class AsyncTaskRunner extends AsyncTask<Void,Void,Double> {
    public EditText number1;
    public EditText number2;
    public TextView result;
    public TextView message;

    public AsyncTaskRunner(EditText number1 , EditText number2 , TextView message,
                           TextView result) {
        this.number1 = number1;
        this.number2 = number2;
        this.message = message;
        this.result = result;
    }

    @Override
    protected Double doInBackground(Void... params) {
        int num1 = Integer.parseInt(number1.getText().toString());
        int num2 = Integer.parseInt(number2.getText().toString());
        message.setText("your execution started, please wait");
        double startTime = System.currentTimeMillis();
        Main.main(num2, num1);
        double endTime = System.currentTimeMillis();
        result.setText(Double.toString(endTime - startTime) + " ms");
        message.setText("finished");
        return endTime;
    }

}
