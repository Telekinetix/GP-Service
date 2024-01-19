import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.ingenico.responses.IngenicoTerminalReportResponse;
import com.global.api.terminals.ingenico.responses.IngenicoTerminalResponse;
import com.global.api.terminals.ingenico.responses.TerminalStateResponse;
import com.global.api.terminals.ingenico.variables.PaymentMode;
import com.global.api.terminals.ingenico.variables.ReportTypes;
import models.Config;
import models.ErrorType;

import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

public class IngenicoHandler {
  ConnectionConfig config;
  IDeviceInterface device;

  IngenicoHandler(Config config) {
    setConfig(config);
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
        // Logs error locally if the socket dies.
        ErrorHandler.error(ErrorType.socketError, e, "Socket died while sending message to EPOS");
      }
    });
  }

  IngenicoHandler connectToDevice() {
    try {
      this.device = DeviceService.create(this.config);
    } catch (ApiException e) {
      // Logs error locally if unable to connect to ingenico device
      ErrorHandler.error(ErrorType.ingenicoDeviceNotConnected, e, "Failed to connect to Ingenico device");
    }
    return this;
  }

  IngenicoTerminalResponse getStatus() throws ApiException {
    return (IngenicoTerminalResponse) device.getTerminalStatus();
  }

  IngenicoTerminalReportResponse getEodReport() throws ApiException {
    return (IngenicoTerminalReportResponse) device.getReport(ReportTypes.EOD).execute();
  }
  IngenicoTerminalResponse cancelTransaction() throws ApiException {
    return (IngenicoTerminalResponse) device.cancel();
  }

  void emergencyCancelTransaction() {
    try {
      if (this.device != null) this.cancelTransaction();
    } catch (ApiException e) {
      ErrorHandler.error(ErrorType.ingenicoGenericError, e, "Failed to cancel ongoing transaction upon socket death");
    }
  }

  IngenicoTerminalResponse doSale(BigDecimal amount, String currency, Integer ref) throws ApiException {
    return (IngenicoTerminalResponse) device.sale(amount)
        .withReferenceNumber(ref)
        .withPaymentMode(PaymentMode.APPLICATION)
        .withCurrencyCode(currency)
        .withTicket(true)
        .execute();
  }

  IngenicoTerminalResponse doRefund(BigDecimal amount, String currency, Integer ref) throws ApiException {
    return (IngenicoTerminalResponse) device.refund(amount)
        .withReferenceNumber(ref)
        .withPaymentMode(PaymentMode.APPLICATION)
        .withCurrencyCode(currency)
        .withTicket(true)
        .execute();
  }
}
