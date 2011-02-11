
package de.bps.onyx.plugin.wsclient;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.6 in JDK 6
 * Generated source version: 2.1
 *
 */
@WebService(name = "OnyxPluginServices", targetNamespace = "http://server.webservice.plugin.bps.de/")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface OnyxPluginServices {


    /**
     *
     * @param contentPackage
     * @param instructions
     * @param language
     * @param tempalteId
     * @param uniqueId
     */
    @WebMethod
    public void run(
        @WebParam(name = "uniqueId", partName = "uniqueId")
        String uniqueId,
        @WebParam(name = "contentPackage", partName = "contentPackage")
        byte[] contentPackage,
        @WebParam(name = "language", partName = "language")
        String language,
        @WebParam(name = "instructions", partName = "instructions")
        String instructions,
        @WebParam(name = "tempalteId", partName = "tempalteId")
        String tempalteId,
        @WebParam(name = "serviceName") String serviceName,
        @WebParam(name = "allowShowSolution") boolean showSolution
    );

        

    /**
     *
     * @param instructions
     * @param language
     * @param contentPackageLocalFile
     * @param tempalteId
     * @param uniqueId
     */
    @WebMethod(operationName = "run_local")
    public void runLocal(
        @WebParam(name = "uniqueId", partName = "uniqueId")
        String uniqueId,
        @WebParam(name = "contentPackageLocalFile", partName = "contentPackageLocalFile")
        String contentPackageLocalFile,
        @WebParam(name = "language", partName = "language")
        String language,
        @WebParam(name = "instructions", partName = "instructions")
        String instructions,
        @WebParam(name = "tempalteId", partName = "tempalteId")
        String tempalteId,
        @WebParam(name = "serviceName") String serviceName,
        @WebParam(name = "allowShowSolution") boolean showSolution
 		);


    /**
     *
     * @return
     *     returns java.lang.String
     */
    @WebMethod
    @WebResult(partName = "return")
    public String getVersion();

}
