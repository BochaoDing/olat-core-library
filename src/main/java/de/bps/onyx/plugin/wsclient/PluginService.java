/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.onyx.plugin.wsclient;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;

import de.bps.onyx.plugin.OnyxModule;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.6 in JDK 6
 * Generated source version: 2.1
 *
 */
@WebServiceClient(name = "PluginService", targetNamespace = "http://server.webservice.plugin.bps.de/")
public class PluginService
    extends Service
{

    private final static URL PLUGINSERVICE_WSDL_LOCATION;
    private final static Logger logger = Logger.getLogger(de.bps.onyx.plugin.wsclient.PluginService.class.getName());

    static {
        URL url = null;
        try {
            URL baseUrl;
            baseUrl = de.bps.onyx.plugin.wsclient.PluginService.class.getResource(".");
            url = new URL(baseUrl, OnyxModule.getPluginWSLocation() + "?wsdl");
        } catch (MalformedURLException e) {
            logger.warning("Failed to create URL for the wsdl Location: '"+ OnyxModule.getPluginWSLocation() + "?wsdl', retrying as a local file");
            logger.warning(e.getMessage());
        }
        PLUGINSERVICE_WSDL_LOCATION = url;
    }

    public PluginService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public PluginService() {
        super(PLUGINSERVICE_WSDL_LOCATION, new QName("http://server.webservice.plugin.bps.de/", "PluginService"));
    }

    /**
     *
     * @return
     *     returns OnyxPluginServices
     */
    @WebEndpoint(name = "OnyxPluginServicesPort")
    public OnyxPluginServices getOnyxPluginServicesPort() {
        return super.getPort(new QName("http://server.webservice.plugin.bps.de/", "OnyxPluginServicesPort"), OnyxPluginServices.class);
    }

    /**
     *
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns OnyxPluginServices
     */
    @WebEndpoint(name = "OnyxPluginServicesPort")
    public OnyxPluginServices getOnyxPluginServicesPort(WebServiceFeature... features) {
        return super.getPort(new QName("http://server.webservice.plugin.bps.de/", "OnyxPluginServicesPort"), OnyxPluginServices.class, features);
    }

}
