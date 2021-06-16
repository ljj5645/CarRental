package carrental;

import carrental.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerCenterViewHandler {


    @Autowired
    private CustomerCenterRepository customerCenterRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenRented_then_CREATE_1 (@Payload Rented rented) {
        try {

            if (!rented.validate()) return;

            // view 객체 생성
            CustomerCenter customerCenter = new CustomerCenter();
            // view 객체에 이벤트의 Value 를 set 함
            customerCenter.setRentId(rented.getId());
            customerCenter.setCarId(rented.getCarId());
            customerCenter.setStatus(rented.getStatus());
            // view 레파지 토리에 save
            customerCenterRepository.save(customerCenter);
        
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenRentCanceled_then_UPDATE_1(@Payload RentCanceled rentCanceled) {
        try {
            if (!rentCanceled.validate()) return;
                // view 객체 조회
            List<CustomerCenter> customerCenterList = customerCenterRepository.findByRentId(rentCanceled.getId());
            for(CustomerCenter customerCenter : customerCenterList){
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                customerCenter.setStatus(rentCanceled.getStatus());
                // view 레파지 토리에 save
                customerCenterRepository.save(customerCenter);
            }
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenPaid_then_UPDATE_2(@Payload Paid paid) {
        try {
            if (!paid.validate()) return;
                // view 객체 조회
            List<CustomerCenter> customerCenterList = customerCenterRepository.findByRentId(paid.getRentId());
            for(CustomerCenter customerCenter : customerCenterList){
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                customerCenter.setStatus(paid.getStatus());
                customerCenter.setPayId(paid.getId());
                // view 레파지 토리에 save
                customerCenterRepository.save(customerCenter);
            }
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenPayCanceled_then_UPDATE_3(@Payload PayCanceled payCanceled) {
        try {
            if (!payCanceled.validate()) return;
                // view 객체 조회
            List<CustomerCenter> customerCenterList = customerCenterRepository.findByRentId(payCanceled.getRentId());
            for(CustomerCenter customerCenter : customerCenterList){
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                customerCenter.setStatus(payCanceled.getStatus());
                // view 레파지 토리에 save
                customerCenterRepository.save(customerCenter);
            }
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenRepairApplied_then_UPDATE_4(@Payload RepairApplied repairApplied) {
        try {
            if (!repairApplied.validate()) return;
                // view 객체 조회
            List<CustomerCenter> customerCenterList = customerCenterRepository.findByRentId(repairApplied.getId());
            for(CustomerCenter customerCenter : customerCenterList){
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                customerCenter.setStatus(repairApplied.getStatus());
                // view 레파지 토리에 save
                customerCenterRepository.save(customerCenter);
            }
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenRepaired_then_UPDATE_5(@Payload Repaired repaired) {
        try {
            if (!repaired.validate()) return;
                // view 객체 조회
            List<CustomerCenter> customerCenterList = customerCenterRepository.findByCarId(repaired.getCarId());
            for(CustomerCenter customerCenter : customerCenterList){
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                customerCenter.setStatus(repaired.getStatus());
                // view 레파지 토리에 save
                customerCenterRepository.save(customerCenter);
            }
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}