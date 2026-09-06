package bg.latona.santa.selfie.domain.crm;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.List;


@AllArgsConstructor
public class SyncSummary<T> {

    private final int successCount;

    private final int failureCount;

    private  final List<T> results;


    public List<T> getResults() {
        return results;
    }


    public HttpStatus determineStatus() {
        if (failureCount == 0) {
            return HttpStatus.OK;
        } else if (successCount == 0) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        } else {
            return HttpStatus.OK;
        }
    }
}
