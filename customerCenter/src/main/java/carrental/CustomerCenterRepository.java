package carrental;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerCenterRepository extends CrudRepository<CustomerCenter, Long> {

    List<CustomerCenter> findByRentId(Long rentId);
    List<CustomerCenter> findByCarId(Long carId);

}