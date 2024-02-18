package cam.lechner.budgetexchange.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "new_map_member")
public class MapMember {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;
    private Integer cospendMemberId;
    private String name;
    private Date begindate;
    private Date enddate;

}
