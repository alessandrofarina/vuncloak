package main;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import vulnerability.VulnRestAPI;
import vulnerability.Vulnerability;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.util.Collection;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class Statistics {

    private static final String FILENAME = "report.csv";

    public static void main(String[] args) throws IOException, InterruptedException, ParseException {
        BufferedReader reader = new BufferedReader(new FileReader(FILENAME));

        TreeMap<String, Integer> yearCountMap = new TreeMap<>();
        TreeMap<Double, Integer> scoreCountMap = new TreeMap<>();

        for (String line = reader.readLine(); line != null; line = reader.readLine()) {

            String cve = line.split(",")[4];
            String year = cve.split("-")[1];

            //YEAR
            yearCountMap.put(year, yearCountMap.containsKey(year) ? yearCountMap.get(year) + 1 : 1);

            //SCORE API REQUEST
            String url = "https://services.nvd.nist.gov/rest/json/cve/1.0/" + cve;
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(url)).header("accept", "application/json").build();
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            try
            {
                JSONObject jsonObject = new JSONObject(httpResponse.body());
                Double score = jsonObject.getJSONObject("result").getJSONArray("CVE_Items").getJSONObject(0).getJSONObject("impact").getJSONObject("baseMetricV3").getJSONObject("cvssV3").getDouble("baseScore");
                scoreCountMap.put(score, scoreCountMap.containsKey(score) ? scoreCountMap.get(score) + 1 : 1);
            }
            catch (JSONException e) { }
            TimeUnit.SECONDS.sleep(7); //REQUEST TIME DELAY REQUIRED BY FIREWALL
        }

        System.out.println("\n■ YEAR");
        yearCountMap.forEach((y, c) -> System.out.println("Year " + y + ": " + c));

        System.out.println("\n■ SCORE");
        scoreCountMap.forEach((s, c) -> System.out.println("Score " + s + ": " + c));

        reader.close();
    }

}
