package cam.lechner.budgetexchange.cospend;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Bills {
    private Integer id;
    private Float amount;
    private String what;
    private String comment;
    private Integer timestamp;
    private String date;
    private Integer payer_id;
    private Owers [] owers;
    private Integer [] owerIds;
    private String repeat;
    private String paymentmode;
    private Integer paymentmodeid;
    private Integer categoryid;
    private Integer lastchanged;
    private Integer repeatallactive;
    private String repeatuntil;
    private Integer repeatfreq;
    private Integer deleted;
}
