package cam.lechner.budgetexchange.cospend;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SendBill {

    private String what;
    private Integer payer;
    private Integer payed_for;
    private Double ammount;
    private String repeat;
    private String date;
    private Integer categoryid;
}
