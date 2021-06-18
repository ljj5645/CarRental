package carrental;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="managements", path="managements")
public interface ManagementRepository extends PagingAndSortingRepository<Management, Long>{

    Management findByRentId(Long rentId);
    Management findByCarId(Long carId);
}
