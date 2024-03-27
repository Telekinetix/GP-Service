package models;

import com.global.api.terminals.ingenico.responses.IngenicoTerminalResponse;

public class TransactionLog {
    public EPOSMessage eposMessage;
    public IngenicoTerminalResponse terminalResponse;

    public TransactionLog(EPOSMessage eposMessage, IngenicoTerminalResponse terminalResponse) {
        this.eposMessage = eposMessage;
        this.terminalResponse = terminalResponse;
    }
}
