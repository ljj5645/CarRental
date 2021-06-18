package carrental;

import carrental.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class PolicyHandler{
    @Autowired RentRepository rentRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverRepaired_UpdateStatus(@Payload Repaired repaired){

        if(!repaired.validate()) return;

        System.out.println("\n\n##### listener UpdateStatus : " + repaired.toJson() + "\n\n");
        Optional<Rent> optionalRent = rentRepository.findById(repaired.getRentId());
        
        Rent rent = optionalRent.get();
        rent.setStatus("REPAIR APPLIED");
        
        System.out.println("###### 수리 접수 완료 확인 #######");
        // System.out.println("\n\n##### listener UpdateStatus : " + rent.toJson() + "\n\n");

        rentRepository.save(rent);
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaid_UpdateStatus(@Payload Paid paid){

        if(!paid.validate()) return;

        System.out.println("\n\n##### listener UpdateStatus : " + paid.toJson() + "\n\n");

        System.out.println("###### 결제 완료 확인 #######");
        // System.out.println("\n\n##### listener UpdateStatus : " + rent.toJson() + "\n\n");
            
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
