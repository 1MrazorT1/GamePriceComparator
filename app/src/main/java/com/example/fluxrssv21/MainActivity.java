package com.example.fluxrssv21;

import static com.example.fluxrssv21.NetworkUtils.fetchGamePrice;

import com.squareup.picasso.Picasso;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        executor = Executors.newSingleThreadExecutor();
        EditText gameInput = findViewById(R.id.gameInput);
        Button compareButton = findViewById(R.id.compareButton);
        TextView steamTextView = findViewById(R.id.steamPrice);
        TextView dealTextView = findViewById(R.id.dealPrice);
        TextView minTextView = findViewById(R.id.minPrice);
        TextView descriptionTextView = findViewById(R.id.gameDescription);
        ImageView imageView = findViewById(R.id.imageView);
        compareButton.setOnClickListener(view -> {
            String gameName = gameInput.getText().toString().trim();
            if (!gameName.isEmpty()) {
                fetchGamePrices(gameName, steamTextView, dealTextView, minTextView, imageView, descriptionTextView);
            } else {
                minTextView.setText("Please enter a valid game name");
            }
        });
    }

    public String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String[] words = input.toLowerCase().split("\\s+");
        StringBuilder capitalized = new StringBuilder();

        for (String word : words) {
            capitalized.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1))
                    .append(" ");
        }

        return capitalized.toString().trim();
    }

    private void fetchGamePrices(String gameName, TextView steamTextView, TextView dealTextView, TextView minTextView, ImageView imageView, TextView descriptionTextView) {
        executor.execute(() -> {
            try {
                ((TextView) findViewById(R.id.gameTitle)).setText("Game Title: " + capitalizeWords(gameName));
                final String steamPrice;
                final String dealPrice;
                String steamAppId = NetworkUtils.fetchSteamAppId(gameName);
                if (steamAppId == null || steamAppId.isEmpty()) {
                    runOnUiThread(() -> steamTextView.setText("Game not found on Steam"));
                    steamPrice = "N/A";
                } else {
                    String steamAPIUrl = "https://store.steampowered.com/api/appdetails?appids=" + steamAppId;
                    String steamResponse = NetworkUtils.fetchAPIData(steamAPIUrl);

                    steamPrice = (steamResponse == null || steamResponse.isEmpty())
                            ? "Error fetching Steam price"
                            : NetworkUtils.parseSteamResponse(steamResponse, steamAppId);

                    String finalSteamPrice = steamPrice;
                    runOnUiThread(() -> steamTextView.setText("Steam Price: " + finalSteamPrice));
                    runOnUiThread(() -> Picasso.get().load(NetworkUtils.fetchGameCover(steamResponse, steamAppId)).into(imageView));
                    runOnUiThread(() -> descriptionTextView.setText(NetworkUtils.fetchGameDescription(steamResponse, steamAppId)));
                }

                String plainId = NetworkUtils.fetchPlainId(gameName);
                if (plainId == null || plainId.isEmpty()) {
                    runOnUiThread(() -> {
                        dealTextView.setText("Price not available on IsThereAnyDeal");
                        minTextView.setText("Minimum Price: N/A");
                    });
                    dealPrice = "N/A";
                } else {
                    String bestPrice = fetchGamePrice(plainId);

                    dealPrice = (bestPrice == null || bestPrice.isEmpty() || bestPrice.contains("No current price found"))
                            ? "Price not available on IsThereAnyDeal"
                            : bestPrice;

                    String finalDealPrice = dealPrice;
                    runOnUiThread(() -> dealTextView.setText("IsThereAnyDeal Price: " + finalDealPrice));
                }
                String minPrice;
                try {
                    double steamPriceValue = Double.parseDouble(steamPrice.replaceAll("[^\\d.]", ""));
                    double dealPriceValue = Double.parseDouble(dealPrice.replaceAll("[^\\d.]", ""));
                    minPrice = String.valueOf(Math.min(steamPriceValue, dealPriceValue));
                } catch (NumberFormatException e) {
                    minPrice = "N/A";
                }
                String finalMinPrice = minPrice;
                runOnUiThread(() -> minTextView.setText("Minimum Price: " + finalMinPrice));
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    steamTextView.setText("Error fetching data");
                    dealTextView.setText("Error fetching data");
                    minTextView.setText("N/A");
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
