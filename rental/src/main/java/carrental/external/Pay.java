package carrental.external;

public class Pay {

    private Long id;
    private Long rentId;
    private Long carId;
    private String status;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }
    public Long getRentId() {
        return rentId;
    }
    public void setRentId(Long rentId) {
        this.rentId = rentId;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

}