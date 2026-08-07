package com.creatoros.enums;

import java.util.Arrays;
import java.util.Optional;

public enum GstStateCode {

    JAMMU_AND_KASHMIR("01", "Jammu & Kashmir"),
    HIMACHAL_PRADESH("02", "Himachal Pradesh"),
    PUNJAB("03", "Punjab"),
    CHANDIGARH("04", "Chandigarh"),
    UTTARAKHAND("05", "Uttarakhand"),
    HARYANA("06", "Haryana"),
    DELHI("07", "Delhi"),
    RAJASTHAN("08", "Rajasthan"),
    UTTAR_PRADESH("09", "Uttar Pradesh"),
    BIHAR("10", "Bihar"),
    SIKKIM("11", "Sikkim"),
    ARUNACHAL_PRADESH("12", "Arunachal Pradesh"),
    NAGALAND("13", "Nagaland"),
    MANIPUR("14", "Manipur"),
    MIZORAM("15", "Mizoram"),
    TRIPURA("16", "Tripura"),
    MEGHALAYA("17", "Meghalaya"),
    ASSAM("18", "Assam"),
    WEST_BENGAL("19", "West Bengal"),
    JHARKHAND("20", "Jharkhand"),
    ODISHA("21", "Odisha"),
    CHHATTISGARH("22", "Chhattisgarh"),
    MADHYA_PRADESH("23", "Madhya Pradesh"),
    GUJARAT("24", "Gujarat"),
    DADRA_NAGAR_HAVELI_DAMAN_DIU("26", "Dadra & Nagar Haveli and Daman & Diu"),
    MAHARASHTRA("27", "Maharashtra"),
    KARNATAKA("29", "Karnataka"),
    GOA("30", "Goa"),
    LAKSHADWEEP("31", "Lakshadweep"),
    KERALA("32", "Kerala"),
    TAMIL_NADU("33", "Tamil Nadu"),
    PUDUCHERRY("34", "Puducherry"),
    ANDAMAN_AND_NICOBAR("35", "Andaman & Nicobar Islands"),
    TELANGANA("36", "Telangana"),
    ANDHRA_PRADESH("37", "Andhra Pradesh"),
    LADAKH("38", "Ladakh"),
    OTHER_TERRITORY("97", "Other Territory");

    private final String code;
    private final String stateName;

    GstStateCode(String code, String stateName) {
        this.code = code;
        this.stateName = stateName;
    }

    public String getCode() {
        return code;
    }

    public String getStateName() {
        return stateName;
    }

    public static Optional<GstStateCode> ofCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String trimmed = code.trim();
        return Arrays.stream(values()).filter(s -> s.code.equals(trimmed)).findFirst();
    }

    public static Optional<GstStateCode> ofGstin(String gstin) {
        if (gstin == null || gstin.trim().length() < 2) {
            return Optional.empty();
        }
        return ofCode(gstin.trim().substring(0, 2));
    }
}
