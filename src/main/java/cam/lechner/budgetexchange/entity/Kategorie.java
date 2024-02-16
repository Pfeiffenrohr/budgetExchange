package cam.lechner.budgetexchange.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;


import java.util.Date;

@Getter
@Setter
@Configuration
public class Kategorie {

    Integer id;
    String name;
    Integer konto_id;
    Double wert;
    Date datum;
    String partner;
    String beschreibung;
    Integer kategorie;
    Integer kor_id;
    Integer cycle;
    String planed;
}
