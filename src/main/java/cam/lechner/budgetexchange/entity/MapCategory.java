package cam.lechner.budgetexchange.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table (name = "new_map_category")
public class MapCategory {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int id;
    private int budgetCategory;
    private int cospendCategory;
    private int kind; // 0= Miete, 1= reperatur, 2 = umlegbar auf alle
    private int inout; //0= Einnahme , 2 = Ausgabe
    private String projectname;
}
