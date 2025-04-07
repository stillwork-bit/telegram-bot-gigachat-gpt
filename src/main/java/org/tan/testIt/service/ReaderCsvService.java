package org.tan.testIt.service;

import lombok.extern.slf4j.Slf4j;
import org.tan.testIt.DTO.TestCreateRequestBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ReaderCsvService {
    private static final int EXPECTED_COLUMNS_COUNT = 7;
    private static final int DEFAULT_DURATION = 600000;
    private static final String EMPTY_FIELD = "";

    public List<TestCreateRequestBuilder> convertCsvToTestCreateRequestBuilder(String csv) {
        List<TestCreateRequestBuilder> result = new ArrayList<>();

        try {
            String[] rows = csv.split("\\R"); // Универсальное разделение для любых концов строк
            if (rows.length < 2) {
                log.warn("CSV содержит недостаточно строк");
                return result;
            }

            // Пропускаем заголовок и обрабатываем остальные строки
            List<String> dataRows = Stream.of(rows)
                    .skip(1)
                    .filter(row -> !row.trim().isEmpty())
                    .collect(Collectors.toList());

            for (String row : dataRows) {
                processRow(result, row);
            }
        } catch (Exception e) {
            log.error("Ошибка обработки CSV", e);
        }

        log.debug("Результаты преобразования: {}", result);
        return result;
    }

    private void processRow(List<TestCreateRequestBuilder> result, String row) {
        log.debug("Обработка строки: {}", row);
        String[] values = row.split(";", EXPECTED_COLUMNS_COUNT);

        if (values.length != EXPECTED_COLUMNS_COUNT) {
            log.warn("Некорректное количество параметров в строке: {}", row);
            return;
        }

        result.add(createRequestBuilder(values));
    }

    private TestCreateRequestBuilder createRequestBuilder(String[] values) {
        return new TestCreateRequestBuilder()
                .withName(values[0])
                .withDuration(DEFAULT_DURATION)
                .addPreconditionStep(values[1], values[2], EMPTY_FIELD, EMPTY_FIELD)
                .addStep(values[3], values[4], EMPTY_FIELD, EMPTY_FIELD)
                .addPostconditionStep(values[5], values[6], EMPTY_FIELD, EMPTY_FIELD);
    }

}


