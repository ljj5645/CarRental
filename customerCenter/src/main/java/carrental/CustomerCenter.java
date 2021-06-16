package carrental;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="CustomerCenter_table")
public class CustomerCenter {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;
        private Long rentId;
        private Long carId;
        private String carName;
        private String status;
        private Long payId;


        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
        public Long getRentId() {
            return rentId;
        }

        public void setRentId(Long rentId) {
            this.rentId = rentId;
        }
        public Long getCarId() {
            return carId;
        }

        public void setCarId(Long carId) {
            this.carId = carId;
        }
        public String getCarName() {
            return carName;
        }

        public void setCarName(String carName) {
            this.carName = carName;
        }
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
        public Long getPayId() {
            return payId;
        }

        public void setPayId(Long payId) {
            this.payId = payId;
        }

}
