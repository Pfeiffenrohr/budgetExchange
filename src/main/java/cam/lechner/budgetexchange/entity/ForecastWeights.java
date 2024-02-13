package cam.lechner.newcbudgetbatch.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ForecastWeights {
    private Integer id;
    private Integer category;
    private Integer konto;
    private Double y1;
    private Double y2;
    private Double y3;
    private Double precision;
}
