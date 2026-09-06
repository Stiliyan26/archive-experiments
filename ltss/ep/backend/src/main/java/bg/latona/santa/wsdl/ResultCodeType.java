
package bg.latona.santa.wsdl;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ResultCodeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <pre>
 * &lt;simpleType name="ResultCodeType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="OK"/&gt;
 *     &lt;enumeration value="VOK"/&gt;
 *     &lt;enumeration value="NOK"/&gt;
 *     &lt;enumeration value="ERR"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ResultCodeType")
@XmlEnum
public enum ResultCodeType {

    OK,
    VOK,
    NOK,
    ERR;

    public String value() {
        return name();
    }

    public static ResultCodeType fromValue(String v) {
        return valueOf(v);
    }

}
