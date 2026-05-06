package cam.lechner.budgetexchange.cospend;

public class Bill {
    private Integer id;

    public Bill() {}

    public Bill(Integer id) {
        this.id = id;
    }
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
}