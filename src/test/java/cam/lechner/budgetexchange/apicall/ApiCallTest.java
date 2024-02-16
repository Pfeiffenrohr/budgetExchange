package cam.lechner.budgetexchange.apicall;

import cam.lechner.budgetexchange.cospend.SendBill;
import cam.lechner.budgetexchange.entity.Kategorie;
import cam.lechner.budgetexchange.entity.Konto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
public class ApiCallTest {

    @Mock
   private RestTemplate restTemplate;

    @InjectMocks
    private ApiCall apiCall = new ApiCall();


    @Test
    void getAllKontenTest() {
     //   RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        Konto konto = new Konto();
        konto.setId(1);
        konto.setKontoname("Name");

        Konto[] allKonto = new Konto[1];
        allKonto[0] = konto;
        Mockito.when(restTemplate.getForEntity(anyString(), any()))
                .thenReturn(new ResponseEntity(allKonto, HttpStatus.OK));
        List<Konto> returnedKonto = apiCall.getAllKonten();
        assertThat(returnedKonto.get(0).getId()).isEqualTo(1);
        assertThat(returnedKonto.get(0).getKontoname()).isEqualTo("Name");
    }
    @Test
    void getAllKategorienTest() {
        //   RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        Kategorie kategorie = new Kategorie();
        kategorie.setId(1);
        kategorie.setName("Name");

        Kategorie[] allKategorie = new Kategorie[1];
        allKategorie[0] = kategorie;
        Mockito.when(restTemplate.getForEntity(anyString(), any()))
                .thenReturn(new ResponseEntity(allKategorie, HttpStatus.OK));
        List<Kategorie> returnedKategories = apiCall.getAllCategories();
        assertThat(returnedKategories.get(0).getId()).isEqualTo(1);
        assertThat(returnedKategories.get(0).getName()).isEqualTo("Name");
    }

    @Test
    void getAllBills() {
        //   RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        RestTemplate restTemplate1 = new RestTemplate();
        ApiCall apiCall = new ApiCall();
        apiCall.getAllBills(restTemplate1);
    }

   /* @Test
    void sendBillToCospend() {
        //   RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        RestTemplate restTemplate1 = new RestTemplate();
        MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
        ApiCall apiCall = new ApiCall();

        map.add("amount","200.4");
        map.add("what","APITEST");
        map.add("payer","11");
        map.add("repeat","n");
        map.add("payed_for","12");
        map.add("date","2024-02-10");
        SendBill sendBill = new SendBill();
        sendBill.setAmmount(200.4);
        sendBill.setPayer(11);
        sendBill.setWhat("APITEST");
        sendBill.setRepeat("n");
        sendBill.setPayed_for(12);
        sendBill.setDate("2024-02-10");
        apiCall.sendBill(restTemplate1,map);
    }*/

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
