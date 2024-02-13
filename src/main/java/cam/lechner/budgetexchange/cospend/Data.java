package cam.lechner.newcbudgetbatch.cospend;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Data {
    private Integer nb_bills;
    private Integer [] allBillIds;
    private Integer timestamp;
    private Bills [] bills;
}
