/**
 * Copyright 2005 Jasper Systems, Inc. All rights reserved.
 *
 * This software code is the confidential and proprietary information of
 * Jasper Systems, Inc. ("Confidential Information"). Any unauthorized
 * review, use, copy, disclosure or distribution of such Confidential
 * Information is strictly prohibited.
 */
package com.hp.sms.service;

import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSProcessor;
import com.sun.xml.wss.XWSSProcessorFactory;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.callback.PasswordCallback;
import com.sun.xml.wss.impl.callback.UsernameCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.soap.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author sunjun
 * 发送短信
 */
public class SendSMSClient {
    private SOAPConnectionFactory connectionFactory;
    private MessageFactory messageFactory;
    private URL url;

    private XWSSProcessorFactory processorFactory;

    private String _namespace_uri;
    private String _prefix;
    private String _licenesekey;
    private String _username;
    private String _password;
    private String _msgid;
    private String _version;
    private String _soapheaderurl;

    /**
     * Constructor which initializes Soap Connection, messagefactory and ProcessorFactory
     *
     * @throws SOAPException
     * @throws MalformedURLException
     * @throws XWSSecurityException
     */
    public SendSMSClient(String serverurl, String soapheaderurl, String namespace_uri, String prefix, String licenesekey, String username, String password, String msgid, String version)
            throws SOAPException, MalformedURLException, XWSSecurityException {
        connectionFactory = SOAPConnectionFactory.newInstance();
        messageFactory = MessageFactory.newInstance();
        processorFactory = XWSSProcessorFactory.newInstance();
        this.url = new URL(serverurl);
        this._namespace_uri = namespace_uri;
        this._prefix = prefix;
        this._licenesekey = licenesekey;
        this._username = username;
        this._password = password;
        this._msgid = msgid;
        this._version = version;
        this._soapheaderurl = soapheaderurl;
    }

    /**
     * This method creates a Terminal Request and sends back the SOAPMessage.
     * ICCID value is passed into this method
     *
     * @return SOAPMessage
     * @throws SOAPException
     */
    private SOAPMessage createTerminalRequest(String iccid, String content) throws SOAPException {
        SOAPMessage message = messageFactory.createMessage();
        message.getMimeHeaders().addHeader("SOAPAction",
                _soapheaderurl + "/sms/SendSMS");
        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
        Name terminalRequestName = envelope.createName("SendSMSRequest", _prefix, _namespace_uri);
        SOAPBodyElement terminalRequestElement = message.getSOAPBody()
                .addBodyElement(terminalRequestName);
        Name msgId = envelope.createName("messageId", _prefix, _namespace_uri);
        SOAPElement msgElement = terminalRequestElement.addChildElement(msgId);
        msgElement.setValue(_msgid);
        Name version = envelope.createName("version", _prefix, _namespace_uri);
        SOAPElement versionElement = terminalRequestElement.addChildElement(version);
        versionElement.setValue(_version);
        Name license = envelope.createName("licenseKey", _prefix, _namespace_uri);
        SOAPElement licenseElement = terminalRequestElement.addChildElement(license);
        licenseElement.setValue(_licenesekey);

//        Name paramGroup = envelope.createName("SendSMSRequestParamGroup", _prefix, _namespace_uri);
//        SOAPElement paramGroupElement = terminalRequestElement.addChildElement(paramGroup);

        Name iccidName = envelope.createName("sentToIccid", _prefix, _namespace_uri);
        SOAPElement iccidElement = terminalRequestElement.addChildElement(iccidName);
        iccidElement.setValue(iccid);

        Name messageTextName = envelope.createName("messageText", _prefix, _namespace_uri);
        SOAPElement messageTextElement = terminalRequestElement.addChildElement(messageTextName);
        messageTextElement.setValue(content);

//        Name encoding = envelope.createName("messageTextEncoding", _prefix, _namespace_uri);
//        messageTextElement.addAttribute(encoding, "BASE64");

//        Name tpvpName = envelope.createName("tpvp", _prefix, _namespace_uri);
//        SOAPElement tpvpElement = paramGroupElement.addChildElement(tpvpName);
//        tpvpElement.setValue("");

        return message;
    }

    public String callWebService(String iccid, String content) throws SOAPException, IOException, XWSSecurityException {
        //组装请求msg
        SOAPMessage request = createTerminalRequest(iccid, content);
        request = secureMessage(request, _username, _password);
//        System.out.println("Request: ");
//        request.writeTo(System.out);
//        System.out.println("");
        //建立连接
        SOAPConnection connection = connectionFactory.createConnection();
        //返回结果集
        SOAPMessage response = connection.call(request, url);
//        System.out.println("Response: ");
//        response.writeTo(System.out);
//        System.out.println("");
        if (!response.getSOAPBody().hasFault()) {
            writeTerminalResponse(response);
            return "1";
        } else {
            SOAPFault fault = response.getSOAPBody().getFault();
            return fault.getFaultCode();
        }
    }

    /**
     * Gets the terminal response.
     *
     * @param message
     * @throws SOAPException
     */
    private void writeTerminalResponse(SOAPMessage message) throws SOAPException {

        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
        Name terminalResponseName = envelope.createName("SendSMSResponse", _prefix, _namespace_uri);
        SOAPBodyElement terminalResponseElement = (SOAPBodyElement) message
                .getSOAPBody().getChildElements(terminalResponseName).next();

        System.out.println(terminalResponseElement.getTextContent());

//        String terminalValue = terminalResponseElement.getTextContent();
//        Name smsMsgId = envelope.createName("smsMsgId", _prefix, _namespace_uri);
//        SOAPBodyElement smsMsgIdElement = (SOAPBodyElement) terminalResponseElement.getChildElements(smsMsgId).next();
//        String msgId = smsMsgIdElement.getTextContent();

    }

    /**
     * This method is used to add the security. This uses xwss:UsernameToken configuration and expects
     * Username and Password to be passes. SecurityPolicy.xml file should be in classpath.
     *
     * @param message
     * @param username
     * @param password
     * @return
     * @throws IOException
     * @throws XWSSecurityException
     */
    private SOAPMessage secureMessage(SOAPMessage message, final String username, final String password)
            throws IOException, XWSSecurityException {
        CallbackHandler callbackHandler = new CallbackHandler() {
            public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
                for (int i = 0; i < callbacks.length; i++) {
                    if (callbacks[i] instanceof UsernameCallback) {
                        UsernameCallback callback = (UsernameCallback) callbacks[i];
                        callback.setUsername(username);
                    } else if (callbacks[i] instanceof PasswordCallback) {
                        PasswordCallback callback = (PasswordCallback) callbacks[i];
                        callback.setPassword(password);
                    } else {
                        throw new UnsupportedCallbackException(callbacks[i]);
                    }
                }
            }
        };
        InputStream policyStream = null;
        XWSSProcessor processor = null;
        try {
            policyStream = getClass().getResourceAsStream("/securityPolicy.xml");
            processor = processorFactory.createProcessorForSecurityConfiguration(policyStream, callbackHandler);
        }
        finally {
            if (policyStream != null) {
                policyStream.close();
            }
        }
        ProcessingContext context = processor.createProcessingContext(message);
        return processor.secureOutboundMessage(context);
    }

    /**
     * Main program. Usage : TerminalClient <username> <password>
     *
     * @param args
     * @throws Exception
     */
//    public static void main(String[] args) throws Exception {
////         Apitest URL. See "Get WSDL Files" in the API documentation for Production URL.
//        String iccid = "89860116770000614263";
//        SendSMSClient terminalClient = new SendSMSClient();
////        Base64 base64 = new Base64();
////        byte[] encodedMsg = base64.encode
////                (smsMessage.getMessageText().getBytes());
//        ObjectResult obj = terminalClient.callWebService(iccid, "wakeup");
//        System.out.println(obj.getMessage());
//
//        String param = "{\"userId\":\"1\",\"deviceId\":\"12312312\", \"deviceType\":\"0\"}";
//		Json json = HttpClientUtil.doPost("http://121.40.157.200:8760/api/vehicle/newphone", param);
//        String msg = URLEncoder.encode("您好，您的验证码是654321", "GB2312");
//        String url = "http://www.zjysms.com/send/gsend.asp?name=BriAir&pwd=BriAir123&dst=18694064229&sender=&time=&txt=ccdx&msg=" + msg;
//        String json = HttpClientUtil.doGet(url);
//        System.out.println("-----" + json);
//
//    }
}

