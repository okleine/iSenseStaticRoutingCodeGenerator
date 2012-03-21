/**
 * Copyright (c) 2012, Oliver Kleine, Institute of Telematics, University of Luebeck
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 * - Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package de.uniluebeck.itm.codegeneration.isense.routing.main;

import de.uniluebeck.itm.codegeneration.isense.routing.xml.encoding.JAXBDecoder;
import de.uniluebeck.itm.codegeneration.isense.routing.xml.jaxb.NetworkRoutingConfig;
import de.uniluebeck.itm.codegeneration.isense.routing.xml.jaxb.RouterConfigType;
import de.uniluebeck.itm.codegeneration.isense.routing.xml.jaxb.RoutingEntryType;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.net.URL;
import java.util.List;

/**
 * @author Oliver Kleine
 */
public class Main {

    public static void main(String[] args){
        try {
            //Decode XML file
            String file = Main.class.getClassLoader().getResource("static-routing-config.xml").getPath();
            NetworkRoutingConfig networkRoutingConfig = decodeXML();

            //Create sourcecode
            Configuration config = new PropertiesConfiguration("static-routing.properties");
            String ipv6Prefix = config.getString("ipv6.prefix");
            StringBuffer sourceCode = createSourceCode(networkRoutingConfig, ipv6Prefix);

            //Write sourcecode to file
            FileWriter writer = new FileWriter("static_routing.cpp");
            writer.write(sourceCode.toString());

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static StringBuffer createSourceCode(NetworkRoutingConfig networkRoutingConfig, String ipv6Prefix) throws Exception {

        List<RouterConfigType> routerConfigList = networkRoutingConfig.getRouterConfig();

        StringBuffer buffer = new StringBuffer();

        buffer.append("#include <isense/protocols/ip/version6/static_routing.h>\n\n"
                    + "#if (defined ISENSE_ENABLE_IPV6) && (defined ISENSE_ENABLE_IPV6_ROUTER)\n"
                    + "#include <isense/protocols/ip/version6/ipv6.h>\n\n"
                    + "#define PTR_SIZE sizeof(IPv6Address*)\n\n"
                    + "namespace isense{\n"
                    + "namespace ip_stack{\n\n"
                    + "#define PREFIX " + ipv6Prefix + ",\n\n"
                    + "#define ADD_PREFIX(...) PREFIX __VA_ARGS__\n\n"
                    + "//Define participating hosts\n");

        for(RouterConfigType routerConfig : routerConfigList){
            String currentID = routerConfig.getRouter();
            buffer.append("const uint64 node_" + currentID + " = 0x" + currentID + "ULL;\n" );
        }

        buffer.append("\n//Define hosts IPv6 postfixes\n");

        for(RouterConfigType routerConfig : routerConfigList){
            String currentID = routerConfig.getRouter();
            buffer.append("static const IPv6Address node_" + currentID + "_ip((uint8[]){ADD_PREFIX (" + createIPv6Postfix(currentID) + ")});\n");
        }

        buffer.append("\n//Routing in 6LowPAN network (Scheme: {node_1, node_2, ..., next_hop})\n");

        for(RouterConfigType routerConfig : routerConfigList){

            String currentID = routerConfig.getRouter();
            for(int i = 1; i <= routerConfig.getRoutingEntry().size(); i++){

                RoutingEntryType routingEntry = routerConfig.getRoutingEntry().get(i-1);

                buffer.append("static const IPv6Address* node_" + currentID + "_routes_" + i + "[] = {");
                //Add nodes to be reached via this route
                for(String node : routingEntry.getReachableNodes().getNode()){
                    buffer.append("&node_" + node + "_ip, ");
                }
                //Add next hop
                buffer.append("&node_" + routingEntry.getNextHop() + "_ip");
                buffer.append("};\n");
            }

            //Add default route
            String default_route = routerConfig.getDefaultRoute();
            if(!(default_route.equals("null"))){
                buffer.append("static const IPv6Address* node_" + currentID + "_default = &node_" + routerConfig.getDefaultRoute() + "_ip;\n\n");
            }
            else{
                buffer.append("\n");
            }
        }

        buffer.append("StaticRouting::StaticRouting( IPv6& ip, uint8 zone_index ):\n" +
                "\t\tIpRouting( ip, zone_index ),\n" +
                "\t\tip6_( ip ){}\n" +
                "\n" +
                "IPv6Address* StaticRouting::hasRoute( IPv6Address& destination, const IPv6Address** const table, uint8 size ){\n" +
                "\n" +
                "\tIPv6Address* next_hop = NULL;\n" +
                "\tfor( uint8 i = 0; i<size-1; i++ ){\n" +
                "\t\tif( destination.common_prefix_length((*table[i])) == 128 ){\n" +
                "\t\t\tnext_hop = (IPv6Address*)table[size-1];\n" +
                "\t\t\tbreak;\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\tchar de[50];\n" +
                "\tchar nh[50];\n" +
                "\tif (next_hop == NULL) {\n" +
                "\t\tip6_.os_.debug(\"hasRoute: no next hop for destination %s\", destination.to_string(de, 50));\n" +
                "\t} else {\n" +
                "\t\tip6_.os_.debug(\"hasRoute: next hop for destination %s is %s\", destination.to_string(de, 50), next_hop->to_string(nh, 50));\n" +
                "\t}\n" +
                "\n" +
                "\treturn next_hop;\n" +
                "}\n" +
                "\n" +
                "bool\n" +
                "StaticRouting::create_route(IPv6Address & destination, IpRoutingCallbackHandler *handler){\n" +
                "\tchar de[50];\n" +
                "\n" +
                "\tip6_.os_.debug(\"Create route to %s\", destination.to_string(de, 50));\n" +
                "\tuint64 node_id = ip6_.os_.id();\n" +
                "\t//ip6_.os_.debug(\"node id is: %llx\", node_id);\n" +
                "\tIPv6Address* next_hop = NULL;\n\n" +
                "\t");

        for(RouterConfigType routerConfig : networkRoutingConfig.getRouterConfig()){
            List<RoutingEntryType> routingEntryList = routerConfig.getRoutingEntry();
            String currentID = routerConfig.getRouter();

            //Add first route entry
            buffer.append("if(node_id == node_" + routerConfig.getRouter() + "){\n");
            if(routingEntryList.size() > 0){
                buffer.append("\t\tnext_hop = hasRoute( destination, node_" + currentID + "_routes_1, (uint8)(sizeof( node_" + currentID + "_routes_1 ) / PTR_SIZE) );\n");

                for(int i = 1; i < routingEntryList.size(); i++){
                    buffer.append("\t\tif( !next_hop ){\n" +
                        "\t\t\tnext_hop = hasRoute( destination, node_" + currentID + "_routes_" + (i+1) + ", (uint8)(sizeof( node_" + currentID + "_routes_" + (i+1) + ") / PTR_SIZE) );\n" +
                        "\t\t}\n");
                }
            }

            if(!(routerConfig.getDefaultRoute().equals("null"))){
                buffer.append("\t\tif( !next_hop ){\n" +
                        "\t\t\tnext_hop = (IPv6Address*)node_" + currentID + "_default;\n" +
                        "\t\t}\n");
            }

            buffer.append("\t}\n\n" +
                "\telse ");
        }

        buffer.append("{\n" +
                "\t\tip6_.os_.debug(\"damn!\");\n" +
                "\t}\n" +
                "\n" +
                "\tSR_ud* userdata = (SR_ud*)isense::malloc( sizeof(SR_ud) );\n" +
                "\tif( next_hop != NULL ){\n" +
                "\n" +
                "\t\tchar nh[50];\n" +
                "\t\tip6_.os_.debug(\"create route: next hop to %s is %s\", destination.to_string(de, 50), next_hop->to_string(nh, 50));\n" +
                "\n" +
                "\t\tdestination.interface_id_ = 0;\n" +
                "\t\tnext_hop->interface_id_ = 0;\n" +
                "\t\tForwardingTableEntry* entry = ip6_.forwarding_table_.add( destination, 128, next_hop, 0, LIFETIME_INFINITY, 1 );\n" +
                "\n" +
                "\t\tif(entry == NULL){\n" +
                "\t\t\tip6_.os_.fatal(\"Adding of forwarding table entry failed!\");\n" +
                "\t\t}\n" +
                "\n" +
                "\t\tuserdata->status_ = ROUTING_SUCCESSFUL;\n" +
                "\t\tuserdata->destination_ = destination;\n" +
                "\n" +
                "\t} else {\n" +
                "\t\tuserdata->status_ = ROUTING_FAILED;\n" +
                "\t\tuserdata->destination_ = destination;\n" +
                "\t}\n" +
                "\tip6_.os_.add_task( this, (void*) userdata );\n" +
                "\treturn true;\n" +
                "}\n" +
                "\n" +
                "void\n" +
                "StaticRouting::execute( void* userdata ){\n" +
                "\tSR_ud* status = (SR_ud*)userdata;\n" +
                "\n" +
                "\tip6_.handle_ip_routing_callback( status->status_, status->destination_ , 128 );\n" +
                "\n" +
                "\tfree( status );\n" +
                "}\n" +
                "\n" +
                "}/*namespace ip_stack*/\n" +
                "}/*namespace isense*/\n" +
                "\n" +
                "\n" +
                "\n" +
                "#endif /*#if (defined ISENSE_ENABLE_IPV6) && (defined ISENSE_ENABLE_IPV6_ROUTER)*/");

        return buffer;
    }

    //Assumes senosor nodes to have 16 bit MAC identifiers for 6LowPAN interface and 48 bit for eth interface
    private static String createIPv6Postfix(String nodeID) throws Exception {
        while(nodeID.length() < 16){
            nodeID = "0" + nodeID;
        }

        String result = "";

        for(int i = 2; i < 16; i+=2){
            result = result + ", 0x" + nodeID.substring(i, i+2);
        }

        return "0x02" + result;
    }

    private static NetworkRoutingConfig decodeXML() throws Exception{
        URL fileURL = Main.class.getClassLoader().getResource("static-routing-config.xml");
        File file = new File(fileURL.getFile());
        //Read XML file
        //File file = new File(pathToXMLFile);
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);

        //Decode XML file
        JAXBDecoder<NetworkRoutingConfig> decoder =
                new JAXBDecoder<NetworkRoutingConfig>(NetworkRoutingConfig.class);

        return decoder.decode(data);
    }
}
