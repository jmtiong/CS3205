package sg.edu.nus.cs3205.subsystem3.nfcapplet;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;

import org.nfctools.NfcAdapter;
import org.nfctools.NfcException;
import org.nfctools.api.TagListener;
import org.nfctools.api.TagScannerListener;
import org.nfctools.api.TagType;
import org.nfctools.mf.classic.MfClassicNfcTagListener;
import org.nfctools.ndef.NdefException;
import org.nfctools.ndef.NdefOperationsListener;
import org.nfctools.ndef.Record;
import org.nfctools.ndef.wkt.records.TextRecord;
import org.nfctools.scio.Terminal;
import org.nfctools.scio.TerminalHandler;
import org.nfctools.scio.TerminalMode;
import org.nfctools.scio.TerminalStatus;
import org.nfctools.spi.acs.AcsTag;
import org.nfctools.spi.acs.AcsTagUtils;
import org.nfctools.spi.acs.AcsTerminal;
import org.nfctools.spi.acs.InitiatorTerminalTagScanner;

@SuppressWarnings("restriction")
public class NFCService {
    private static Terminal terminal;
    private static AcsTerminal singleOperationAcsTerminal;

    public static void readData(final Consumer<String[]> handler) {
        startListeningOnce(ndefOperations -> {
            if (!ndefOperations.isFormatted()) {
                throw new NdefException("Tag is not formatted");
            }
            final List<Record> records = ndefOperations.readNdefMessage();
            if (records.isEmpty()) {
                throw new NdefException("Tag is empty");
            }
            handler.accept(records.stream().map(record -> {
                if (!(record instanceof TextRecord)) {
                    throw new NdefException("Tag data is not a text record");
                }
                return ((TextRecord) record).getText();
            }).toArray(String[]::new));
        });
    }

    public static void writeData(final Runnable prehandler, final Runnable handler, final String... data) {
        startListeningOnce(ndefOperations -> {
            if (ndefOperations.isWritable()) {
                prehandler.run();
                final TextRecord[] textRecords = Stream.of(data).map(text -> new TextRecord(text))
                        .toArray(TextRecord[]::new);
                if (ndefOperations.isFormatted()) {
                    ndefOperations.writeNdefMessage(textRecords);
                } else {
                    ndefOperations.format(textRecords);
                }
                handler.run();
            } else {
                System.out.println("Tag not writable");
            }
        });
    }

    private static Terminal getTerminal() {
        if (terminal == null) {
            final TerminalHandler terminalHandler = new TerminalHandler();
            if (singleOperationAcsTerminal == null) {
                singleOperationAcsTerminal = new AcsTerminal() {
                    private InitiatorTerminalTagScanner tagScanner;

                    @Override
                    public void registerTagListener(final TagListener tagListener) {
                        this.tagScanner.setTagListener(tagListener);
                    }

                    @Override
                    public void setMode(final TerminalMode terminalMode,
                            final TagScannerListener tagScannerListener) {
                        this.tagScanner = new InitiatorTerminalTagScanner(this.cardTerminal,
                                tagScannerListener) {
                            @Override
                            public void run() {
                                this.notifyStatus(TerminalStatus.WAITING);
                                try {
                                    if (this.cardTerminal.waitForCardPresent(0)) {
                                        try {
                                            Card e = null;
                                            e = this.cardTerminal.connect("*");
                                            this.notifyStatus(TerminalStatus.CONNECTED);
                                            this.handleCard(e);
                                        } finally {
                                            this.waitForCardAbsent();
                                        }
                                    }
                                } catch (final CardException e) {
                                    throw new NfcException(null, e);
                                }
                            }

                            private void handleCard(final Card card) throws CardException {
                                final byte[] historicalBytes = card.getATR().getHistoricalBytes();
                                final TagType tagType = AcsTagUtils.identifyTagType(historicalBytes);
                                final AcsTag acsTag = new AcsTag(tagType, historicalBytes, card);
                                if (tagType.equals(TagType.NFCIP)) {
                                    throw new CardException("Not a valid NFC tag");
                                } else {
                                    this.tagListener.onTag(acsTag);
                                }
                            }
                        };
                        final Thread scanningThread = new Thread(this.tagScanner);
                        scanningThread.setDaemon(true);
                        scanningThread.start();
                    }
                };
            }
            terminalHandler.addTerminal(singleOperationAcsTerminal);
            terminal = terminalHandler.getAvailableTerminal();
        }
        return terminal;
    }

    private static void startListeningOnce(final NdefOperationsListener ndefOperationListener) {
        new NfcAdapter(getTerminal(), TerminalMode.INITIATOR)
                .registerTagListener(new MfClassicNfcTagListener(ndefOperationListener));
    }
}
