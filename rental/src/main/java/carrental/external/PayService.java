
package carrental.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="pay", url="${api.url.pay}", fallback = PayServiceFallback.class)
public interface PayService {

    @RequestMapping(method= RequestMethod.POST, path="/pays")
    public boolean pay(@RequestBody Pay pay);

}