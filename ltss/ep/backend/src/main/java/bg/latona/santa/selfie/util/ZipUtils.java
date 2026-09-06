package bg.latona.santa.selfie.util;

import bg.latona.santa.reports.ReportException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class ZipUtils {

    public static void addToZip(ZipOutputStream zos, String fileName, byte[] fileBytes) throws IOException {
        if (fileBytes == null || fileBytes.length == 0) {
            log.error("Attempt to add empty file to zip: {}", fileName);
            throw new ReportException("File content cannot be empty.");
        }
        log.info("Adding file to zip: {}", fileName);

        putNextEntry(zos, fileName);

        writeZipEntry(zos, fileBytes);

        closeEntry(zos);
        log.info("File added successfully: {}", fileName);
    }

    public static void putNextEntry(ZipOutputStream zos, String fileName) throws IOException {
        log.trace("Creating new zip entry: {}", fileName);
        ZipEntry entry = new ZipEntry(fileName);

        zos.putNextEntry(entry);
    }

    public static void writeZipEntry(ZipOutputStream zos, byte[] fileBytes) throws IOException {
        log.trace("Writing {} bytes to zip entry", fileBytes.length);
        zos.write(fileBytes);
    }

    public static void closeEntry(ZipOutputStream zos) throws IOException {
        log.trace("Closing current zip entry");
        zos.closeEntry();
    }

    public static void closeZipOutputStream(ZipOutputStream zos) throws IOException {
        log.trace("Closing zip");
        zos.finish();
    }
}
