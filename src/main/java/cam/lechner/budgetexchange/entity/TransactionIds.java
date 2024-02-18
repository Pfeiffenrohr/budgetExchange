package cam.lechner.budgetexchange.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "new_transaction_ids")
public class TransactionIds {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;
    private Integer budgetTransId;
    private Integer nextcloudBillId;
    private Integer isChecked;
}
