package cam.lechner.budgetexchange.compare;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class MapCategory {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int id;
    private int budgetCategory;
    private int cospendCategory;
    private int kind; // 0= Miete, 1= reperatur, 2 = umlegbar auf alle
    private int inout; //0= Einnahme , 2 = Ausgabe

}
