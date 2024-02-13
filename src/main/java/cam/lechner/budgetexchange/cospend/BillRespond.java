package cam.lechner.budgetexchange.cospend;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Resp {
    @JsonProperty("ocs")
    private Ocs ocs;
}
