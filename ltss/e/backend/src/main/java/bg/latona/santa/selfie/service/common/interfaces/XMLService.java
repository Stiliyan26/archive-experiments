package bg.latona.santa.selfie.service.common.interfaces;

import bg.latona.santa.reports.ReportException;

public interface XMLService {

   <T> byte[] createXMLByteArray(T data) throws ReportException;

   <T> byte[] createXMLByteArray(T data, String includeStylesheet) throws ReportException;
}
