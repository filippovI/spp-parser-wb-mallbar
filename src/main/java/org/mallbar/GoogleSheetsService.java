package org.mallbar;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;

import java.io.FileInputStream;
import java.util.*;

@Getter
@ToString
public class GoogleSheetsService {

    private static final String SPREADSHEET_ID = "1mVWGX2F2JIbrbUoSh2tEeniUFhiq6WdHrHMoG6dRDdE";
    private static Sheets service = null;
    private final String sheetName;


    @SneakyThrows
    public GoogleSheetsService(String sheetName) {
        this.sheetName = sheetName;
        FileInputStream credentialsStream = new FileInputStream("src\\main\\resources\\credentials.json");
        GoogleCredentials credentials = ServiceAccountCredentials.fromStream(credentialsStream)
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));
        service = new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("WB Parser")
                .build();
    }

    @SneakyThrows
    public void updatePercent(Map<String, String> dataToUpdate, String columnFroSearch, String columnForUpdate) {
        Map<String, Integer> articleAndPosition = getValuesFromColumn(columnFroSearch);
        List<ValueRange> updateList = new ArrayList<>();
        for (Map.Entry<String, String> entry : dataToUpdate.entrySet()) {
            String article = entry.getKey();
            String newValue = entry.getValue();
            if (articleAndPosition.containsKey(article)) {
                int rowIndex = articleAndPosition.get(article);
                updateList.add(new ValueRange()
                        .setRange("'WB unit БАЗА'!" + columnForUpdate + rowIndex)
                        .setValues(Collections.singletonList(Collections.singletonList(newValue))));
            }
        }
        System.out.println(updateList);
        if (updateList.isEmpty()) {
            System.out.println("Нет данных для обновления.");
            return;
        }
        batchUpdateColumn(updateList);
    }

    @SneakyThrows
    private Map<String, Integer> getValuesFromColumn(String column) {
        String rangeForValues = "'" + sheetName + "'!" + column + ":" + column;
        ValueRange response = service.spreadsheets().values()
                .get(SPREADSHEET_ID, rangeForValues)
                .execute();

        List<List<Object>> values = response.getValues();
        Map<String, Integer> articlesAndPosition = new HashMap<>();
        if (values != null) {
            for (int i = 0; i < values.size(); i++) {
                if (!values.get(i).isEmpty()) {
                    articlesAndPosition.put(values.get(i).get(0).toString().trim(), i + 1);
                }
            }
        }
        return articlesAndPosition;
    }


    @SneakyThrows
    private void batchUpdateColumn(List<ValueRange> updateList) {
        BatchUpdateValuesRequest batchBody = new BatchUpdateValuesRequest()
                .setValueInputOption("USER_ENTERED")
                .setData(updateList);
        service.spreadsheets().values()
                .batchUpdate(SPREADSHEET_ID, batchBody)
                .execute();
        System.out.println("✅ Успешно обновлено " + updateList.size() + " позиций одним запросом!");
    }
}
