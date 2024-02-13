package cam.lechner.newcbudgetbatch.apicall;

import cam.lechner.newcbudgetbatch.cospend.Ocs;
import cam.lechner.newcbudgetbatch.entity.ForecastWeights;
import cam.lechner.newcbudgetbatch.entity.Kategorie;
import cam.lechner.newcbudgetbatch.entity.Konto;
import cam.lechner.newcbudgetbatch.entity.Transaktion;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;


import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ApiCall {
    @Autowired
    private RestTemplate restTemplate;
    @Value("${budgetserver.host}")
    private String host;
    @Value("${budgetserver.port}")
    private String port;
    private static final Logger LOG = LoggerFactory.getLogger(ApiCall.class);

    public ApiCall() {
    }
    public List<Konto> getAllKonten() {
        LOG.info("Start Downloading all Kontos");
        if (host == null ) {host = "localhost";}
        if (port == null ) {port = "8092";}
        List<Konto> list = new ArrayList<Konto>();
        UriComponents uriComponents = UriComponentsBuilder.newInstance().scheme("http").host(host).port(port)
                .path("//kontos").build();
        String allKonten = uriComponents.toUriString();
        ResponseEntity<Konto[]> response = restTemplate.getForEntity(allKonten, Konto[].class);
        if (response.hasBody()) {

            Konto[] konto = response.getBody();
            Collections.addAll(list, konto);
        }
        return list;
    }

    public List<Kategorie> getAllCategories() {
        LOG.info("Start Downloading all Categories");
        if (host == null ) {host = "localhost";}
        if (port == null ) {port = "8092";}
        List<Kategorie> list = new ArrayList<>();
        UriComponents uriComponents = UriComponentsBuilder.newInstance().scheme("http").host(host).port(port)
                .path("//categories").build();
        String allCategories = uriComponents.toUriString();
        ResponseEntity<Kategorie[]> response = restTemplate.getForEntity(allCategories, Kategorie[].class);
        if (response.hasBody()) {
            Kategorie[] kategorie = response.getBody();
            Collections.addAll(list, kategorie);
        }
        return list;
    }
    public List<Transaktion> getTransactionWithCategory(int category) {
        LOG.info("Start Downloading all Categories");
        if (host == null ) {host = "localhost";}
        if (port == null ) {port = "8092";}
        List<Transaktion> list = new ArrayList<Transaktion>();
        UriComponents uriComponents = UriComponentsBuilder.newInstance().scheme("http").host(host).port(port)
                .path("//transaction_by_kategorie//"+category).build();
        String transactions = uriComponents.toUriString();
        ResponseEntity<Transaktion[]> response = restTemplate.getForEntity(transactions, Transaktion[].class);
        if (response.hasBody()) {
            Transaktion[] transaktions = response.getBody();
            Collections.addAll(list, transaktions);
        }
        return list;
    }

    public Ocs getAllBills(RestTemplate restTemplate) {
        LOG.info("Start getAllBillsFromCospend");
        if (host == null ) {host = "localhost";}
        if (port == null ) {port = "8092";}
        String plainCreds = "richard:Thierham123";
        byte[] encodedAuth = Base64.encodeBase64(
                plainCreds.getBytes(Charset.forName("US-ASCII")),false );
        String authHeader = "Basic " + new String( encodedAuth );
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authHeader);
        headers.add("OCS-APIRequest","true");


        HttpEntity<String> request = new HttpEntity<String>(headers);
        UriComponents uriComponents = UriComponentsBuilder.newInstance().scheme("https").host("richardlechner.spdns.de")
                .path("/ocs/v2.php/apps/cospend/api/v1/projects/test2/bills").build();
        String url = uriComponents.toUriString();
        ResponseEntity<Ocs> response = restTemplate.exchange(url, HttpMethod.GET, request, Ocs.class);
        Ocs ocs = response.getBody();
        return ocs;
    }
}
