package com.itextos.beacon.gw.smppclient;
import io.prometheus.client.Counter;
import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class SmppMonitoringRoute {

    private static final Counter sentSmsCounter = Counter.build()
            .name("smpp_sent_sms_total")
            .help("Total number of sent SMS messages")
            .register();

    private static final Counter receivedDnCounter = Counter.build()
            .name("smpp_received_dn_total")
            .help("Total number of received delivery notifications")
            .register();

    private static final Counter enqirelinkCounter = Counter.build()
            .name("smpp_enquirelink_total")
            .help("Total number of received delivery notifications")
            .register();

    public static void main(String[] args) throws Exception {
    	
        CamelContext camelContext = new DefaultCamelContext();

        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:monitorSMPP")
                        .to("smpp://192.168.1.81:2775?password=password&systemType=cp&enquireLinkTimer=30000")
                        .log("SMPP session bind status: ${exchangeProperty.CamelSmppState}")
                        .log("SMPP session lifetime: ${exchangeProperty.CamelSmppSessionTime}")
                        .log("Total Sent SMS Count: ${exchangeProperty.CamelSmppSentSMSCount}")
                        .log("Total Received DN Count: ${exchangeProperty.CamelSmppReceivedDNCount}");

                from("direct:sendSMS")
                        .setHeader("CamelSmppMessageType", constant("submit_sm"))
                        .to("smpp://smppserver.com:2775?password=password&systemType=cp")
                        .log("Message sent successfully!");

                from("smpp://smppserver.com:2775?password=password&systemType=cp")
                        .process(new SmppProcessor());
            }
        });

        camelContext.start();

        camelContext.createProducerTemplate().sendBody("direct:sendSMS", null);

        Thread.sleep(5000);

        camelContext.stop();
    }

    static class SmppProcessor implements Processor {
        @Override
        public void process(org.apache.camel.Exchange exchange) throws Exception {
            // Your custom logic to monitor SMPP events
            if ("deliver_sm".equals(exchange.getIn().getHeader("CamelSMPPMessageType"))) {
                // Increment received DN counter
                receivedDnCounter.inc();
            } else if ("submit_sm".equals(exchange.getIn().getHeader("CamelSMPPMessageType"))) {
                // Increment sent SMS counter
                sentSmsCounter.inc();
            }
        }
    }
}
