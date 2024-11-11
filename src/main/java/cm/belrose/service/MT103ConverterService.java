package cm.belrose.service;

import cm.belrose.model.MT103Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class MT103ConverterService {

    private final ObjectMapper objectMapper;

    public MT103ConverterService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public MT103Message parseMT103(String mt103Message) {
        MT103Message mtMsg103 = new MT103Message();
        MT103Message.Header header = new MT103Message.Header();
        MT103Message.Text text = new MT103Message.Text();

        // Parse Block 1
        //Pattern block1Pattern = Pattern.compile("\\{1:F01[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}[A-Z]{1}XXX\\d{10}\\}");
       // Pattern block1Pattern = Pattern.compile("\\{1:F(\\d{2})([A-Z]{4})([A-Z]{2})([A-Z0-9]{2})([A-Z]{1})XXX(\\d{10})\\}");
        Pattern block1Pattern = Pattern.compile("\\{1:F01([A-Z]{4})([A-Z]{2})([A-Z0-9]{2})([A-Z]{1})XXX(\\d{10})\\}");
        Matcher matcher = block1Pattern.matcher(mt103Message);
        if (matcher.find()) {
            MT103Message.Block1 block1 = new MT103Message.Block1();
            block1.setApplicationId("1:F01"); // Group 1: Application ID (two digits)
            block1.setSenderBIC(matcher.group(1) + matcher.group(2) + matcher.group(3) + matcher.group(4)+ "XXX"); // Full BIC code
            block1.setSessionNumber(matcher.group(5)); // Group 6: Session Number (ten digits)
            header.setBlock1(block1);
            mtMsg103.setHeader(header);
           // log.info(">>>>> block1 {}",block1);
        }

        // Parse Block 2
        Pattern block2Pattern = Pattern.compile("\\{2:O103(\\d{10})([A-Z]{4})([A-Z]{2})([A-Z0-9]{3})(\\d{10})(\\d{10})N\\}");
        matcher = block2Pattern.matcher(mt103Message);
        if (matcher.find()) {
            MT103Message.Block2 block2 = new MT103Message.Block2();
            block2.setOutputType("2:O103");
            block2.setTimeOfInput(matcher.group(1));
            block2.setDateOfMessage(matcher.group(2));
            block2.setReceiverBIC(matcher.group(3));
            block2.setMessagePriority(matcher.group(4));
            block2.setDeliveryNotification(matcher.group(5));
            block2.setDateTimeIndicator(matcher.group(5));
            block2.setPriorityIndicator(matcher.group(6)+"N");
            mtMsg103.getHeader().setBlock2(block2);
        }

        // Parse Block 3
        Pattern block3Pattern = Pattern.compile("\\{3:\\{103:([A-Z]{3})\\}\\{108:(\\d{10,15})-(\\d{2})\\}\\{121:([0-9a-fA-F]{8})-([0-9a-fA-F]{4})-([0-9a-fA-F]{4})-([0-9a-fA-F]{4})-([0-9a-fA-F]{12})\\}\\}");
        matcher = block3Pattern.matcher(mt103Message);
        if (matcher.find()) {
            MT103Message.Block3 block3 = new MT103Message.Block3();
            MT103Message.Block3.Block31 block31 = new MT103Message.Block3.Block31();
            MT103Message.Block3.Block32 block32 = new MT103Message.Block3.Block32();
            MT103Message.Block3.Block33 block33 = new MT103Message.Block3.Block33();
            block3.setTransactionReference("3:");
            block31.setCode1("103:");
            block31.setExpression1(matcher.group(1));
            block3.setBlock31(block31);
            block32.setCode2("108:");
            block32.setExpression2(matcher.group(2)+"-"+matcher.group(3));
            block3.setBlock32(block32);
            block33.setCode3("121:");
            block33.setExpression3(matcher.group(4)+"-"+matcher.group(5)+"-"+matcher.group(6)+"-"+matcher.group(7)+"-"+matcher.group(8));
            block3.setBlock33(block33);
            mtMsg103.getHeader().setBlock3(block3);
        }

        // Parse Text block fields
        Pattern transactionRefPattern = Pattern.compile("\\{4::20:([A-Z0-9]+)");
        matcher = transactionRefPattern.matcher(mt103Message);
        if (matcher.find()) {
            text.setTransactionReferenceNumber("4::20:"+matcher.group(1));
            mtMsg103.setText(text);
        }

        Pattern bankOperationCodePattern = Pattern.compile(":23B:(\\w+)");
        matcher = bankOperationCodePattern.matcher(mt103Message);
        if (matcher.find()) {
            mtMsg103.getText().setBankOperationCode(":23B:"+matcher.group(1));
        }

        Pattern valueDatePattern = Pattern.compile(":32A:(\\d{6})([A-Z]{3})(\\d+),(\\d{2})");
        matcher = valueDatePattern.matcher(mt103Message);
        if (matcher.find()) {
            MT103Message.Text.ValueDateCurrencyInterbankSettledAmount valueDateCurrency = new MT103Message.Text.ValueDateCurrencyInterbankSettledAmount();
            valueDateCurrency.setDate(":32A:"+matcher.group(1));
            valueDateCurrency.setCurrency(matcher.group(2));
            valueDateCurrency.setAmount(matcher.group(3)+","+matcher.group(4));
            mtMsg103.getText().setValueDateCurrencyInterbankSettledAmount(valueDateCurrency);
        }

        Pattern orderingCustomerPattern = Pattern.compile(":50K:([A-Z0-9\\s]+),(\\s[A-Z]{2}\\s)(\\d{5})-(\\d{4})?", Pattern.DOTALL);
        matcher = orderingCustomerPattern.matcher(mt103Message);
        if (matcher.find()) {
            MT103Message.Text.OrderingCustomer orderingCustomer = new MT103Message.Text.OrderingCustomer();
            orderingCustomer.setName(":50K:"+matcher.group(1).trim());
            orderingCustomer.setAddress(matcher.group(2).trim() + ", " + matcher.group(3).trim()+"-"+matcher.group(4).trim());
            mtMsg103.getText().setOrderingCustomer(orderingCustomer);
        }

        Pattern orderingInstitutionPattern = Pattern.compile(":52A:([A-Z0-9]{8,11})", Pattern.DOTALL);
        matcher = orderingInstitutionPattern.matcher(mt103Message);
        if (matcher.find()) {
            MT103Message.Text.OrderingInstitution orderingInstitution = new MT103Message.Text.OrderingInstitution();
            orderingInstitution.setName(":52A:");
            orderingInstitution.setAddress(matcher.group(1).trim());
            mtMsg103.getText().setOrderingInstitution(orderingInstitution);
        }

        Pattern beneficiaryCustomerPattern = Pattern.compile(":59:/([A-Z]{2})(\\d{14,24})(\\s[A-Z\\s]+)", Pattern.DOTALL);
        matcher = beneficiaryCustomerPattern.matcher(mt103Message);
        if (matcher.find()) {
            MT103Message.Text.BeneficiaryCustomer beneficiaryCustomer = new MT103Message.Text.BeneficiaryCustomer();
            beneficiaryCustomer.setAccountNumber(":59:/"+matcher.group(1).trim());
            beneficiaryCustomer.setName(matcher.group(2).trim());
            beneficiaryCustomer.setAddress(matcher.group(3).trim());
            mtMsg103.getText().setBeneficiaryCustomer(beneficiaryCustomer);
        }

        Pattern remittanceInfoPattern = Pattern.compile(":70:([A-Za-z]+\\d+)", Pattern.DOTALL);
        matcher = remittanceInfoPattern.matcher(mt103Message);
        if (matcher.find()) {
            mtMsg103.getText().setRemittanceInformation(":70:"+matcher.group(1).trim());
        }

        Pattern chargesPattern = Pattern.compile(":71A:(\\w+)");
        matcher = chargesPattern.matcher(mt103Message);
        if (matcher.find()) {
            MT103Message.Text.Charges charges = new MT103Message.Text.Charges();
            charges.setChargeType(":71A:"+matcher.group(1));
            mtMsg103.getText().setCharges(charges);
        }

        Pattern senderReceiverInfoPattern = Pattern.compile(":71A:OUR-\\}", Pattern.DOTALL);
        matcher = senderReceiverInfoPattern.matcher(mt103Message);
        if (matcher.find()) {
            MT103Message.Text.SenderToReceiverInformation senderToReceiverInformation = new MT103Message.Text.SenderToReceiverInformation();
            senderToReceiverInformation.setInstruction(":71A:OUR-");
            mtMsg103.getText().setSenderToReceiverInformation(senderToReceiverInformation);
        }

        // Parse Trailer block
        Pattern trailerPattern = Pattern.compile("\\{5:\\{MAC:(\\w+)\\}\\{CHK:(\\w+)\\}\\}");
        matcher = trailerPattern.matcher(mt103Message);
        if (matcher.find()) {
            MT103Message.Trailer trailer = new MT103Message.Trailer();
            trailer.setInit("5:");
            trailer.setMac("MAC:"+matcher.group(1));
            trailer.setChecksum("CHK:"+matcher.group(2));
            mtMsg103.setTrailer(trailer);
        }

        return mtMsg103;
    }

    public String convertToJSON(MT103Message mt103) throws IOException {
        return objectMapper.writeValueAsString(mt103);
    }
}