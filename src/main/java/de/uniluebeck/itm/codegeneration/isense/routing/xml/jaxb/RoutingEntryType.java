//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.02.23 at 11:03:36 AM MEZ 
//


package de.uniluebeck.itm.codegeneration.isense.routing.xml.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RoutingEntryType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RoutingEntryType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="nextHop" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="reachableNodes" type="{http://www.itm.uni-luebeck.de/users/kleine/isense-static-routing}ReachableNodesType"/>
 *       &lt;/all>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RoutingEntryType", propOrder = {

})
public class RoutingEntryType {

    @XmlElement(required = true)
    protected String nextHop;
    @XmlElement(required = true)
    protected ReachableNodesType reachableNodes;

    /**
     * Gets the value of the nextHop property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNextHop() {
        return nextHop;
    }

    /**
     * Sets the value of the nextHop property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNextHop(String value) {
        this.nextHop = value;
    }

    /**
     * Gets the value of the reachableNodes property.
     * 
     * @return
     *     possible object is
     *     {@link ReachableNodesType }
     *     
     */
    public ReachableNodesType getReachableNodes() {
        return reachableNodes;
    }

    /**
     * Sets the value of the reachableNodes property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReachableNodesType }
     *     
     */
    public void setReachableNodes(ReachableNodesType value) {
        this.reachableNodes = value;
    }

}
