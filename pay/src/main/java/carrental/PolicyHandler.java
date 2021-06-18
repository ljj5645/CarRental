package carrental;

import carrental.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired PayRepository payRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverRentCanceled_CancelPay(@Payload RentCanceled rentCanceled){

        if(!rentCanceled.validate()) return;

        System.out.println("\n\n##### listener CancelPay : " + rentCanceled.toJson() + "\n\n");

        Pay pay = payRepository.findByRentId(rentCanceled.getId());
        pay.setStatus("CANCELED");

        System.out.println("###### 결제 취소 확인 #######");
        // System.out.println("\n\n##### listener UpdateStatus : " + pay.toJson() + "\n\n");

        payRepository.save(pay);
            
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
