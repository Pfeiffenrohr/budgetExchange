package cam.lechner.newcbudgetbatch.cospend;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Ocs {
    @JsonProperty("meta")
    private Meta meta;
    @JsonProperty("data")
    private Data data;

}
