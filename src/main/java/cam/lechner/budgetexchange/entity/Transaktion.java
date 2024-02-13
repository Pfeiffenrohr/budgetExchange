package cam.lechner.newcbudgetbatch.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
public class Transaktion {
    private String name;
    private String user;
    private int konto;
    private int kategorie;
    private String partner;
    private String beschreibung;
    private int kor_id;
    private int cycle;
    private String planed;
    private String datum;
    private double wert;
}
