package carrental;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Rent_table")
public class Rent {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String status;
    private Long carId;

    @PostPersist
    public void onPostPersist(){
        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        carrental.external.Pay pay = new carrental.external.Pay();
        // mappings goes here
        pay.setRentId(this.getId());
        pay.setStatus("PAID");
        pay.setCarId(this.getCarId());
        RentalApplication.applicationContext.getBean(carrental.external.PayService.class)
            .pay(pay);

        Rented rented = new Rented();
        BeanUtils.copyProperties(this, rented);
        rented.publishAfterCommit();
    }

    @PostUpdate
    public void onPostUpdate(){
        RepairApplied repairApplied = new RepairApplied();
        BeanUtils.copyProperties(this, repairApplied);
        repairApplied.publishAfterCommit();
    }

    @PreRemove
    public void onPreRemove(){
        RentCanceled rentCanceled = new RentCanceled();
        BeanUtils.copyProperties(this, rentCanceled);
        this.setStatus("RENTAL CANCELED");
        rentCanceled.publishAfterCommit();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }




}
