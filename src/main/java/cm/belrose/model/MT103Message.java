package cm.belrose.model;

import lombok.Data;
@Data
public class MT103Message {
    private Header header;
    private Text text;
    private Trailer trailer;

    @Data
    public static class Header {
        private Block1 block1;
        private Block2 block2;
        private Block3 block3;
    }

    @Data
    public static class Block1 {
        private String applicationId;
        private String senderBIC;
        private String sessionNumber;
        private String sequenceNumber;
    }

    @Data
    public static class Block2 {
        private String outputType;
        private String timeOfInput;
        private String dateOfMessage;
        private String receiverBIC;
        private String messagePriority;
        private String deliveryNotification;
        private String dateTimeIndicator;
        private String priorityIndicator;
    }

    @Data
    public static class Block3 {
        private String transactionReference;
        private Block31 block31;
        private Block32 block32;
        private Block33 Block33;

        @Data
        public static class Block31 {
            private String code1;
            private String expression1;
        }
        @Data
        public static class Block32 {
            private String code2;
            private String expression2;
        }
        @Data
        public static class Block33 {
            private String code3;
            private String expression3;
        }
    }

    @Data
    public static class Text {
        private String transactionReferenceNumber;   // :20:
        private String bankOperationCode;            // :23B:
        private ValueDateCurrencyInterbankSettledAmount valueDateCurrencyInterbankSettledAmount; // :32A:
        private OrderingCustomer orderingCustomer;   // :50K:
        private OrderingInstitution orderingInstitution; // :52D:
        private BeneficiaryCustomer beneficiaryCustomer;  // :59:
        private String remittanceInformation;        // :70:
        private Charges charges;                     // :71A:
        private SenderToReceiverInformation senderToReceiverInformation; // :72:

        @Data
        public static class ValueDateCurrencyInterbankSettledAmount {
            private String date;
            private String currency;
            private String amount;
        }

        @Data
        public static class OrderingCustomer {
            private String name;
            private String address;
        }

        @Data
        public static class OrderingInstitution {
            private String name;
            private String address;
        }

        @Data
        public static class BeneficiaryCustomer {
            private String accountNumber;
            private String name;
            private String address;
        }

        @Data
        public static class Charges {
            private String chargeType;
        }

        @Data
        public static class SenderToReceiverInformation {
            private String instruction;
        }
    }

    @Data
    public static class Trailer {
        private String init;
        private String mac;
        private String checksum;
    }
}