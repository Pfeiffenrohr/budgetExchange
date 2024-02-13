package cam.lechner.newcbudgetbatch.apicall;

import cam.lechner.newcbudgetbatch.entity.Kategorie;
import cam.lechner.newcbudgetbatch.entity.Konto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
