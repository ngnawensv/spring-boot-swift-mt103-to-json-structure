package cm.belrose.service;

import cm.belrose.model.MT103Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MT103ConverterService1 {

    // Regular expressions for each MT103 field
    private static final String BLOCK1_REGEX = "\\{1:F01(?<applicationId>\\w{3})BIC(?<senderBIC>\\w{11})\\w{9}\\}";
    private static final String BLOCK2_REGEX = "\\{2:O(?<messageType>\\d{3})(?<inputTime>\\d{4})(?<date>\\d{6})(?<receiverBIC>\\w{11})\\w{12}\\d{10}(?<messagePriority>\\w)\\}";
    private static final String BLOCK3_REGEX = "\\{3:\\{108:(?<transactionReference>\\w+)\\}\\}";
    private static final String TEXT_REGEX = "\\{4:\\s*" +
            ":20:(?<transactionReferenceNumber>\\S+)\\s*" +
            ":23B:(?<bankOperationCode>\\S+)\\s*" +
            ":32A:(?<valueDate>\\d{6})(?<currency>\\w{3})(?<amount>[\\d,\\.]+)\\s*" +
            ":50K:(?<orderingCustomerName>.+?)\\n(?<orderingCustomerAddress>.+?)\\s*" +
            ":52D:(?<orderingInstitutionName>.+?)\\n(?<orderingInstitutionAddress>.+?)\\s*" +
            ":59:(?<beneficiaryAccountNumber>/\\d{20})\\n(?<beneficiaryCustomerName>.+?)\\n(?<beneficiaryCustomerAddress>.+?)\\s*" +
            ":70:(?<remittanceInformation>.+?)\\s*" +
            ":71A:(?<charges>\\S+)\\s*" +
            ":72:(?<senderToReceiverInformation>.+?)\\s*" +
            "-\\}";
    private static final String TRAILER_REGEX = "\\{5:\\{MAC:(?<mac>\\w+)\\}\\{CHK:(?<checksum>\\w+)\\}\\}";

    private final ObjectMapper objectMapper;

    public MT103ConverterService1(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public MT103Message parseMT103(String mt103Message) {
        MT103Message message = new MT103Message();
        MT103Message.Header header = new MT103Message.Header();

        // Extract Block 1
        Matcher block1Matcher = Pattern.compile(BLOCK1_REGEX).matcher(mt103Message);
        if (block1Matcher.find()) {
            MT103Message.Block1 block1 = new MT103Message.Block1();
            //MT103Message.Header.Block1 block1 = new MT103Message.Header.Block1();
            block1.setApplicationId(block1Matcher.group("applicationId"));
            block1.setSenderBIC(block1Matcher.group("senderBIC"));
            message.getHeader().setBlock1(block1);
        }

        // Extract Block 2
        Matcher block2Matcher = Pattern.compile(BLOCK2_REGEX).matcher(mt103Message);
        if (block2Matcher.find()) {
            MT103Message.Block2 block2 = new MT103Message.Block2();
           // MT103Message.Header.Block2 block2 = new MT103Message.Header.Block2();
            block2.setOutputType(block2Matcher.group("messageType"));
            block2.setTimeOfInput(block2Matcher.group("inputTime"));
            block2.setDateOfMessage(block2Matcher.group("date"));
            block2.setReceiverBIC(block2Matcher.group("receiverBIC"));
            block2.setMessagePriority(block2Matcher.group("messagePriority"));
            message.getHeader().setBlock2(block2);
        }

        // Extract Block 3
        Matcher block3Matcher = Pattern.compile(BLOCK3_REGEX).matcher(mt103Message);
        if (block3Matcher.find()) {
            MT103Message.Block3 block3 = new MT103Message.Block3();
            //MT103Message.Header.Block3 block3 = new MT103Message.Header.Block3();
            block3.setTransactionReference(block3Matcher.group("transactionReference"));
            header.setBlock3(block3);
        }
        message.setHeader(header);
        // Extract Text
        Matcher textMatcher = Pattern.compile(TEXT_REGEX, Pattern.DOTALL).matcher(mt103Message);
        if (textMatcher.find()) {
            MT103Message.Text text = new MT103Message.Text();
            text.setTransactionReferenceNumber(textMatcher.group("transactionReferenceNumber"));
            text.setBankOperationCode(textMatcher.group("bankOperationCode"));

            // Value Date, Currency, and Amount
            MT103Message.Text.ValueDateCurrencyInterbankSettledAmount valueDateCurrency =
                    new MT103Message.Text.ValueDateCurrencyInterbankSettledAmount();
            valueDateCurrency.setDate(textMatcher.group("valueDate"));
            valueDateCurrency.setCurrency(textMatcher.group("currency"));
            valueDateCurrency.setAmount(textMatcher.group("amount"));
            text.setValueDateCurrencyInterbankSettledAmount(valueDateCurrency);

            // Ordering Customer
            MT103Message.Text.OrderingCustomer orderingCustomer = new MT103Message.Text.OrderingCustomer();
            orderingCustomer.setName(textMatcher.group("orderingCustomerName"));
            orderingCustomer.setAddress(textMatcher.group("orderingCustomerAddress"));
            text.setOrderingCustomer(orderingCustomer);

            // Ordering Institution
            MT103Message.Text.OrderingInstitution orderingInstitution = new MT103Message.Text.OrderingInstitution();
            orderingInstitution.setName(textMatcher.group("orderingInstitutionName"));
            orderingInstitution.setAddress(textMatcher.group("orderingInstitutionAddress"));
            text.setOrderingInstitution(orderingInstitution);

            // Beneficiary Customer
            MT103Message.Text.BeneficiaryCustomer beneficiaryCustomer = new MT103Message.Text.BeneficiaryCustomer();
            beneficiaryCustomer.setAccountNumber(textMatcher.group("beneficiaryAccountNumber"));
            beneficiaryCustomer.setName(textMatcher.group("beneficiaryCustomerName"));
            beneficiaryCustomer.setAddress(textMatcher.group("beneficiaryCustomerAddress"));
            text.setBeneficiaryCustomer(beneficiaryCustomer);

            // Remittance Information
            text.setRemittanceInformation(textMatcher.group("remittanceInformation").trim());

            // Charges
            MT103Message.Text.Charges charges = new MT103Message.Text.Charges();
            charges.setChargeType(textMatcher.group("charges"));
            text.setCharges(charges);

            // Sender to Receiver Information
            MT103Message.Text.SenderToReceiverInformation senderToReceiverInformation = new MT103Message.Text.SenderToReceiverInformation();
            senderToReceiverInformation.setInstruction(textMatcher.group("senderToReceiverInformation").trim());
            text.setSenderToReceiverInformation(senderToReceiverInformation);

            message.setText(text);
        }

        // Extract Trailer
        Matcher trailerMatcher = Pattern.compile(TRAILER_REGEX).matcher(mt103Message);
        if (trailerMatcher.find()) {
            MT103Message.Trailer trailer = new MT103Message.Trailer();
            trailer.setMac(trailerMatcher.group("mac"));
            trailer.setChecksum(trailerMatcher.group("checksum"));
            message.setTrailer(trailer);
        }

        return message;
    }
    public String convertToJSON(MT103Message mt103) throws IOException {
        return objectMapper.writeValueAsString(mt103);
    }
}
