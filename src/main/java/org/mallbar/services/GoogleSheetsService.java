package org.mallbar.services;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
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
    private final Sheets service;
    private final String searchColumn;
    private final String updateColumn;
    private final String sheetName;

    //WB unit БАЗА


    @SneakyThrows
    public GoogleSheetsService(String sheetName, String searchColumn, String updateColumn) {
        this.searchColumn = searchColumn;
        this.updateColumn = updateColumn;
        this.sheetName = sheetName;
        FileInputStream credentialsStream = new FileInputStream("src\\main\\resources\\credentials.json");
        GoogleCredentials credentials = ServiceAccountCredentials.fromStream(credentialsStream)
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));
        this.service = new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("WB Parser")
                .build();

    }

    @SneakyThrows
    public boolean updateColumn(Map<String, String> dataToUpdate) {
        Map<String, Integer> articleAndPosition = getValuesFromColumn(this.searchColumn);
        List<ValueRange> updateList = new ArrayList<>();
        for (Map.Entry<String, String> entry : dataToUpdate.entrySet()) {
            String article = entry.getKey();
            String newValue = entry.getValue();
            if (articleAndPosition.containsKey(article)) {
                int rowIndex = articleAndPosition.get(article);
                updateList.add(new ValueRange()
                        .setRange(this.sheetName + "!" + this.updateColumn + rowIndex)
                        .setValues(Collections.singletonList(Collections.singletonList(newValue))));
            }
        }
        System.out.println(updateList);
        if (updateList.isEmpty()) {
            System.out.println("Нет данных для обновления.");
            return false;
        }
        return batchUpdateColumn(updateList);
    }

    @SneakyThrows
    private Map<String, Integer> getValuesFromColumn(String searchColumn) {
        String rangeForValues = "'" + this.sheetName + "'!" + searchColumn + ":" + searchColumn;
        Map<String, Integer> articlesAndPosition = new HashMap<>();
        try {
            ValueRange response = service.spreadsheets().values()
                    .get(SPREADSHEET_ID, rangeForValues)
                    .execute();
            List<List<Object>> values = response.getValues();
            if (values != null) {
                for (int i = 0; i < values.size(); i++) {
                    if (!values.get(i).isEmpty()) {
                        articlesAndPosition.put(values.get(i).get(0).toString().trim(), i + 1);
                    }
                }
            }
        } catch (GoogleJsonResponseException ex) {
            //log
            System.out.println("Не удалось получить данные из таблицы " + searchColumn + "\n" + ex);
            return Map.of();
        }
        return articlesAndPosition;
    }


    @SneakyThrows
    private boolean batchUpdateColumn(List<ValueRange> updateList) {
        try {
            BatchUpdateValuesRequest batchBody = new BatchUpdateValuesRequest()
                    .setValueInputOption("USER_ENTERED")
                    .setData(updateList);
            service.spreadsheets().values()
                    .batchUpdate(SPREADSHEET_ID, batchBody)
                    .execute();
        } catch (GoogleJsonResponseException ex) {
            //log
            System.out.println("Не удалось обновить данные " + updateList + '\n' + ex);
            return false;
        }
        return true;
    }
}
