import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.ingenico.responses.IngenicoTerminalResponse;
import com.global.api.terminals.ingenico.responses.TerminalStateResponse;
import com.global.api.terminals.ingenico.variables.PaymentMode;
import models.Config;

import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

public class IngenicoHandler {
  ConnectionConfig config;
  IDeviceInterface device;
  ErrorHandler errorHandler;

  IngenicoHandler(Config config, ErrorHandler errorHandler) {
    setConfig(config);
    this.errorHandler = errorHandler;
    this.connectToDevice();
  }

  void setConfig(Config config) {
    this.config = new ConnectionConfig();

    this.config.setPort(config.cardPort.toString());
    this.config.setTimeout(config.cardTimeout);
    this.config.setDeviceType(DeviceType.INGENICO_L3000);
    this.config.setConnectionMode(ConnectionModes.SERIAL);
    this.config.setBaudRate(BaudRate.r19200);
    this.config.setDataBits(DataBits.Eight);
    this.config.setParity(Parity.Even);
    this.config.setStopBits(StopBits.One);
  }

  void setupCallback(DataOutputStream out) {
    this.device.setOnBroadcastMessageReceived((code, message) -> {
      String msgToSend = ("{\"code\":\"" + code + "\", \"message\":\"" + message + "\"}" + (char) 3);
      System.out.println(msgToSend);
      try {
        out.write(msgToSend.getBytes());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  void connectToDevice() {
    try {
      this.device = DeviceService.create(this.config);
    } catch (ApiException e) {
      // TODO: Handle error
      throw new RuntimeException(e);
    }
  }

  void getStatus() throws ApiException {
    TerminalStateResponse r = (TerminalStateResponse) device.getTerminalStatus();
  }

  IngenicoTerminalResponse cancelTransaction() throws ApiException {
    device.cancel();
    return (IngenicoTerminalResponse) device.cancel();
  }

  IngenicoTerminalResponse doSale(BigDecimal amount, Integer ref) throws ApiException {
    return (IngenicoTerminalResponse) device.sale(amount)
        .withReferenceNumber(ref)
        .withPaymentMode(PaymentMode.APPLICATION)
        .withCurrencyCode("826")
        .withTicket(true)
        .execute();
  }

  IngenicoTerminalResponse doRefund(BigDecimal amount, Integer ref) throws ApiException {
    return (IngenicoTerminalResponse) device.refund(amount)
        .withReferenceNumber(ref)
        .withPaymentMode(PaymentMode.APPLICATION)
        .withCurrencyCode("826")
        .withTicket(true)
        .execute();
  }
}