package org.simplePaxos.helperFiles;

import org.simplePaxos.messages.PaxosMessage;
import pt.unl.fct.di.novasys.network.data.Host;

public class TermArguments {
    public int accepted_num, promised_num, term;
    public PaxosMessage acceptedValue, decidedValue;
    public Host remoteHost;

    public void setAccepted_num(int accepted_num) {
        this.accepted_num = accepted_num;
    }

    public void setPromised_num(int promised_num) {
        this.promised_num = promised_num;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public void setAcceptedValue(PaxosMessage acceptedValue) {
        this.acceptedValue = acceptedValue;
    }

    public void setDecidedValue(PaxosMessage decidedValue) {
        this.decidedValue = decidedValue;
    }

    public void setRemoteHost(Host remoteHost) {
        this.remoteHost = remoteHost;
    }

    public TermArguments(int accepted_num, int promised_num, int term, PaxosMessage acceptedValue, PaxosMessage decidedValue, Host remoteHost) {
        this.accepted_num = accepted_num;
        this.promised_num = promised_num;
        this.term = term;
        this.acceptedValue = acceptedValue;
        this.decidedValue = decidedValue;
        this.remoteHost = remoteHost;
    }

    public TermArguments(){

    }
}
