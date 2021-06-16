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
    @Autowired RentRepository rentRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverRepaired_UpdateStatus(@Payload Repaired repaired){

        if(!repaired.validate()) return;

        System.out.println("\n\n##### listener UpdateStatus : " + repaired.toJson() + "\n\n");

        // Sample Logic //
        Rent rent = new Rent();
        rentRepository.save(rent);
            
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaid_UpdateStatus(@Payload Paid paid){

        if(!paid.validate()) return;

        System.out.println("\n\n##### listener UpdateStatus : " + paid.toJson() + "\n\n");

        // Sample Logic //
        Rent rent = new Rent();
        rentRepository.save(rent);
            
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCanceled_UpdateStatus(@Payload PayCanceled payCanceled){

        if(!payCanceled.validate()) return;

        System.out.println("\n\n##### listener UpdateStatus : " + payCanceled.toJson() + "\n\n");

        // Sample Logic //
        Rent rent = new Rent();
        rentRepository.save(rent);
            
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
