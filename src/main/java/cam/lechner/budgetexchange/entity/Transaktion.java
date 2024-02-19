package cam.lechner.budgetexchange.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@Entity
@Table(name = "transaktion")
public class Transaktion {
    @Id
    private int id;
    private String name;
    private int konto_id;
    private int kategorie;
    private String partner;
    private String beschreibung;
    private int kor_id;
    private int cycle;
    private String planed;
    private String datum;
    private double wert;
}
