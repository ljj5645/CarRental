package carrental;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

 @RestController
 public class PayController {

    @Autowired PayRepository payRepository;

    @PostMapping("/pays")
    public boolean pay(@RequestBody Rented rented){
        Pay pay = new Pay();
        boolean result = false;
        pay.setRentId(rented.getId());
        pay.setCarId(rented.getCarId());
        pay.setStatus("PAID");

        try {
                pay = payRepository.save(pay);
                result = true;
                    
        } catch (Exception e) {
                e.printStackTrace();
        }
        return result;
    }
 }
