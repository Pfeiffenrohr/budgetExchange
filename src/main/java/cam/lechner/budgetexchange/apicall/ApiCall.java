package cam.lechner.budgetexchange.apicall;

import cam.lechner.budgetexchange.cospend.Ocs;
import cam.lechner.budgetexchange.cospend.BillRespond;
import cam.lechner.budgetexchange.cospend.SendBill;
import cam.lechner.budgetexchange.entity.Kategorie;
import cam.lechner.budgetexchange.entity.Konto;
import cam.lechner.budgetexchange.entity.Transaktion;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
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
        if (host == null) {
            host = "localhost";
        }
        if (port == null) {
            port = "8092";
        }
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
        //LOG.info("Start Downloading all Categories");
        if (host == null) {
            host = "localhost";
        }
        if (port == null) {
            port = "8092";
        }
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

    public List<Transaktion> getTransactionWithCategoryAndDate(String category, String startdate, String enddate) {
        if (host == null) {
            host = "localhost";
        }
        if (port == null) {
            port = "8092";
        }
        List<Transaktion> list = new ArrayList<Transaktion>();
        UriComponents uriComponents = UriComponentsBuilder.newInstance().scheme("http").host(host).port(port)
                .path("//transactionWithDateAndCategory").query("startdate=" + startdate).query("enddate=" + enddate).query("category=" + category).build();
        String transactions = uriComponents.toUriString();
        ResponseEntity<Transaktion[]> response = restTemplate.getForEntity(transactions, Transaktion[].class);
        if (response.hasBody()) {
            Transaktion[] transaktions = response.getBody();
            Collections.addAll(list, transaktions);
        }
        return list;
    }

    public BillRespond getAllBills(RestTemplate restTemplate,String projectId) {
        LOG.info("Start getAllBillsFromCospend");
        if (host == null) {
            host = "localhost";
        }
        if (port == null) {
            port = "8092";
        }
        String plainCreds = "richard:Thierham123";
        byte[] encodedAuth = Base64.encodeBase64(
                plainCreds.getBytes(Charset.forName("US-ASCII")), false);
        String authHeader = "Basic " + new String(encodedAuth);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authHeader);
        headers.add("OCS-APIRequest", "true");
        HttpEntity<String> request = new HttpEntity<String>(headers);
        UriComponents uriComponents = UriComponentsBuilder.newInstance().scheme("https").host("h3070648.stratoserver.net")
                .path("/ocs/v2.php/apps/cospend/api/v1/projects/"+projectId+"/bills").build();
        String url = uriComponents.toUriString();
        ResponseEntity<BillRespond> response = restTemplate.exchange(url, HttpMethod.GET, request, BillRespond.class);
        BillRespond billRespond = response.getBody();
        return billRespond;
    }

    public Integer sendBill(MultiValueMap map,String projectId) {
        LOG.info("Start sendBillToCospend with bill" + map.get("what"));
        String plainCreds = "richard:Thierham123";
        byte[] encodedAuth = Base64.encodeBase64(
                plainCreds.getBytes(Charset.forName("US-ASCII")), false);
        String authHeader = "Basic " + new String(encodedAuth);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authHeader);
        headers.add("OCS-APIRequest", "true");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        UriComponents uriComponents = UriComponentsBuilder.newInstance().scheme("https").host("h3070648.stratoserver.net")
                .path("/ocs/v2.php/apps/cospend/api/v1/projects/"+projectId+"/bills").build();
        String url = uriComponents.toUriString();
        ResponseEntity<String> response = restTemplate.postForEntity(
                url, request, String.class);
        String result = response.getBody();
        String[] chunks = result.split(":");
        String[] resp = chunks[6].split("}");
        LOG.info("Bill " + map.get("what") + " succesfully sent");
        //sendMessageToTalk("[Cospend]Neue Rechnung zu Cosepend: " + map.get("what"));
        return Integer.parseInt(resp[0]);
    }

    public Integer updateBill(MultiValueMap map, String billId,String projectId) {
        LOG.info("Update bill" + map.get("what"));
        String plainCreds = "richard:Thierham123";
        byte[] encodedAuth = Base64.encodeBase64(
                plainCreds.getBytes(Charset.forName("US-ASCII")), false);
        String authHeader = "Basic " + new String(encodedAuth);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authHeader);
        headers.add("OCS-APIRequest", "true");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        UriComponents uriComponents = UriComponentsBuilder.newInstance().scheme("https").host("h3070648.stratoserver.net")
                .path("/ocs/v2.php/apps/cospend/api/v1/projects/"+projectId+"/bills" + billId).build();
        String url = uriComponents.toUriString();
        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.PUT, request, String.class);
        String result = response.getBody();
        String[] chunks = result.split(":");
        String[] resp = chunks[6].split("}");
        LOG.info("Bill " + map.get("what") + " succesfully sent");
        //sendMessageToTalk("[Cospend] Update Rechnung zu Cosepend: " + map.get("what"));
        return Integer.parseInt(resp[0]);
    }

    public Boolean deleteBill(String billId, String projectId) {
        LOG.info("Delete bill in Cospend " + billId);
        String plainCreds = "richard:Thierham123";
        byte[] encodedAuth = Base64.encodeBase64(
                plainCreds.getBytes(Charset.forName("US-ASCII")), false);
        String authHeader = "Basic " + new String(encodedAuth);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authHeader);
        headers.add("OCS-APIRequest", "true");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);
        UriComponents uriComponents = UriComponentsBuilder.newInstance().scheme("https").host("h3070648.stratoserver.net")
                .path("/ocs/v2.php/apps/cospend/api/v1/projects/"+projectId+" /bills/" + billId).build();
        String url = uriComponents.toUriString();
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.DELETE, request, String.class);
            String result = response.getBody();
            LOG.info("Bill deleteted " + billId);
            //sendMessageToTalk("[Cospend] Rechnung gelöscht: " + billId);
        } catch (final HttpClientErrorException e) {
            LOG.error("!!!Fehler " + e.getStatusCode().toString());
            // System.out.println(e.getStatusCode());
            // System.out.println(e.getResponseBodyAsString());
            LOG.error(e.getResponseBodyAsString());
            return false;
        }
        return true;
    }

    public void sendMessageToTalk(String msg) {
        try {
            LOG.info("Start sendmessage to talk");
            String plainCreds = "richard:Thierham123";
            byte[] encodedAuth = Base64.encodeBase64(
                    plainCreds.getBytes(Charset.forName("US-ASCII")), false);
            String authHeader = "Basic " + new String(encodedAuth);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", authHeader);
            headers.add("OCS-APIRequest", "true");
            MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
            map.add("token", "i28sw2gn");
            map.add("message", msg);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            UriComponents uriComponents = UriComponentsBuilder.newInstance().scheme("https").host("h3070648.stratoserver.net")
                    .path("/ocs/v2.php/apps/spreed/api/v1/chat/i28sw2gn").build();
            String url = uriComponents.toUriString();
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url, request, String.class);
            String result = response.getBody();
        } catch ( Exception ex ) {
            LOG.error("Can not send message to talk " +ex);
        }
    }
    public Integer sendCategory(MultiValueMap map,String projectId) {
        LOG.info("Start send Category " + map.get("name"));
        String plainCreds = "richard:Thierham123";
        byte[] encodedAuth = Base64.encodeBase64(
                plainCreds.getBytes(Charset.forName("US-ASCII")), false);
        String authHeader = "Basic " + new String(encodedAuth);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authHeader);
        headers.add("OCS-APIRequest", "true");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        UriComponents uriComponents = UriComponentsBuilder.newInstance().scheme("https").host("h3070648.stratoserver.net")
                .path("/ocs/v2.php/apps/cospend/api/v1/projects/"+projectId+"/category").build();
        String url = uriComponents.toUriString();
        ResponseEntity<String> response = restTemplate.postForEntity(
                url, request, String.class);
        String result = response.getBody();
        String[] chunks = result.split(":");
        String[] resp = chunks[6].split("}");
        LOG.info("Category " + map.get("name") + " succesfully sent");
        //sendMessageToTalk("[Cospend]Neue Rechnung zu Cosepend: " + map.get("what"));
        return Integer.parseInt(resp[0]);
    }
}
