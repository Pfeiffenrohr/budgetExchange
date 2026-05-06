package cam.lechner.budgetexchange.cospend;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
@NoArgsConstructor
public class Data {
    private Bill[] bills;

    public Bill[] getBills() {
        return bills;
    }

    public void setBills(Bill[] bills) {
        this.bills = bills;
    }

    public Integer[] getAllBillIds() {
        if (bills == null) return new Integer[0];
        return Arrays.stream(bills)
                .map(Bill::getId)
                .toArray(Integer[]::new);
    }
}
