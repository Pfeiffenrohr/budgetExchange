package cam.lechner.budgetexchange.application;


import cam.lechner.budgetexchange.apicall.ApiCall;
import cam.lechner.budgetexchange.cospend.BillRespond;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
@Disabled
public class CompareTest {
   /* @Mock
    private RestTemplate reestTemplate;
*/
    @InjectMocks
    private ApiCall apiCall = new ApiCall("true");

    @Test
    void compareCospendBudgetTest() {
        String project = "budgetall";
        BillRespond respond = new BillRespond();
        Mockito.when(apiCall.getAllBills(Mockito.anyString()))
                .thenReturn(respond);
        Mockito.when(apiCall.deleteBill(anyString(),anyString())).thenReturn(true);
        Compare compare = new Compare();
        compare.compareCospendBudget(project);
    }

}
