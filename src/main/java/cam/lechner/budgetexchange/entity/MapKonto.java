package cam.lechner.budgetexchange.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table (name = "new_map_Konto")
public class MapKonto {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int id;
    private int budgetKonto;
    private int cospendKonto;
    private String projectname;
}
