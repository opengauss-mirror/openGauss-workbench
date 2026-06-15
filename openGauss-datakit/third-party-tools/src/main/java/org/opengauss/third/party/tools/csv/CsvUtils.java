/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package org.opengauss.third.party.tools.csv;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Csv utils class
 *
 * @since 2026/6/8
 */
public class CsvUtils {
    /**
     * Generate csv content
     *
     * @param csvExportableList csv exportable list
     * @return csv content
     */
    public static String generateCsvContent(List<? extends CsvExportable> csvExportableList) {
        if (csvExportableList == null || csvExportableList.isEmpty()) {
            return "";
        }

        StringBuilder csvContent = new StringBuilder();
        csvContent.append(csvExportableList.get(0).getCsvHeader()).append(System.lineSeparator());
        for (CsvExportable installInfo : csvExportableList) {
            csvContent.append(installInfo.getCsvData()).append(System.lineSeparator());
        }
        return csvContent.substring(0, csvContent.length() - System.lineSeparator().length());
    }

    /**
     * Export csv content to file
     *
     * @param csvFilePath csv file path
     * @param csvExportableList csv exportable list
     * @throws IOException io exception
     */
    public static void exportAsCsv(String csvFilePath, List<? extends CsvExportable> csvExportableList)
            throws IOException {
        if (csvExportableList == null || csvExportableList.isEmpty()) {
            return;
        }
        if (csvFilePath == null || csvFilePath.isBlank()) {
            throw new IllegalArgumentException("Argument 'csvFilePath' can not be null or blank");
        }

        Path path = Paths.get(csvFilePath);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(generateCsvContent(csvExportableList));
        }
    }

    /**
     * Add csv exportable row to file
     *
     * @param csvFilePath csv file path
     * @param csvExportable csv exportable
     * @throws IOException io exception
     */
    public static void addRowToCsv(String csvFilePath, CsvExportable csvExportable) throws IOException {
        if (csvExportable == null) {
            return;
        }
        if (csvFilePath == null || csvFilePath.isBlank()) {
            throw new IllegalArgumentException("Argument 'csvFilePath' can not be null or blank");
        }

        Path path = Paths.get(csvFilePath);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("Csv file not found: " + csvFilePath);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
            writer.newLine();
            writer.write(csvExportable.getCsvData());
        }
    }

    /**
     * Load csv lines from file
     *
     * @param filePath csv file path
     * @return csv lines
     * @throws IOException io exception
     */
    public static List<String> loadCsvLines(String filePath) throws IOException {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("Argument 'filePath' can not be null or blank");
        }

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        return Files.readAllLines(path, StandardCharsets.UTF_8);
    }

    /**
     * Write all csv lines to file
     *
     * @param filePath csv file path
     * @param csvLines csv lines
     * @throws IOException io exception
     */
    public static void writeAllCsvLines(String filePath, List<String> csvLines) throws IOException {
        if (csvLines == null || csvLines.isEmpty()) {
            return;
        }
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("Argument 'filePath' can not be null or blank");
        }

        Path path = Paths.get(filePath);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        StringBuilder csvContent = new StringBuilder();
        for (String line : csvLines) {
            csvContent.append(line).append(System.lineSeparator());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(csvContent.substring(0, csvContent.length() - System.lineSeparator().length()));
        }
    }
}
