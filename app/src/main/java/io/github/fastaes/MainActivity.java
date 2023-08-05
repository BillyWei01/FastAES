package io.github.fastaes;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import io.github.fastaes.databinding.ActivityMainBinding;
import io.github.fastaes.test.AESTest;
import io.github.fastaes.test.Benchmark;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AsyncTask.SERIAL_EXECUTOR.execute(this::test);
    }

    @SuppressLint("SetTextI18n")
    private void test() {
        try {
            TextView tv = binding.sampleText;

            StringBuilder builder = new StringBuilder();

            // Verify the correctness of FastAES
            boolean success;
            try {
                success = AESTest.test();
            } catch (Exception e) {
                success = false;
                Log.e("MyTag", e.getMessage(), e);
            }
            builder.append("Test ").append((success ? "success" : "failed")).append("\n\n");

            tv.post(() -> tv.setText(builder.toString()));

            if (success) {
                System.gc();
                Thread.sleep(100);

                Benchmark.start(result -> {
                    builder.append(result);
                    tv.post(() -> tv.setText(builder.toString()));
                });
            }
        } catch (Exception e) {
            Log.e("MyTag", e.getMessage(), e);
        }
    }
}